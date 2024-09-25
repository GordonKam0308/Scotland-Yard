package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.*;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;


public final class MyGameStateFactory implements Factory<GameState> {

    @Nonnull
    @Override
    public GameState build(
            GameSetup setup,
            Player mrX,
            ImmutableList<Player> detectives) {
        return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
    }

    private final class MyGameState implements GameState {
        private GameSetup setup;
        private ImmutableSet<Piece> remaining;
        private ImmutableList<LogEntry> log;
        private Player mrX;
        private List<Player> detectives;
        private ImmutableSet<Move> moves;
        private ImmutableSet<Piece> winner;

        // Game initiation
        private MyGameState(
                final GameSetup setup,
                final ImmutableSet<Piece> remaining,
                final ImmutableList<LogEntry> log,
                final Player mrX,
                final List<Player> detectives) {
            // Ensure not empty moves
            if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

            // Ensure detectives are nonnull
            if (detectives.isEmpty()) throw new IllegalArgumentException("Detectives are null!");

            // Ensure no detective has double tickets
            if (detectives.stream()
                    .map(player -> player.tickets().get(ScotlandYard.Ticket.DOUBLE))
                    .anyMatch(integer -> integer > 0))
                throw new IllegalArgumentException("Some detective has double ticket!");

            // Ensure graph is not empty
            if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("The graph is empty!");

            // Ensure the detectives have the correct properties
            List<Integer> playersLocation = new ArrayList<>();

            for (Player detective : detectives) {
                // 1. no detectives have overlap location
                int detectiveLocation = detective.location();
                if (playersLocation.contains((Integer) detectiveLocation))
                    throw new IllegalArgumentException("Detective locations overlap!");
                playersLocation.add(detectiveLocation);
                // 2. no detectives have secret tickets
                if (detective.tickets().get(ScotlandYard.Ticket.SECRET) != 0)
                    throw new IllegalArgumentException("Detective has secret tickets!");
            }

            // Set winner to empty set if the game initiates
            if (log.isEmpty()) this.winner = ImmutableSet.of();

            // Ensure mrX is nonnull
            if (mrX == null) throw new NullPointerException("MrX is null!");

            this.setup = setup;
            this.remaining = remaining;
            this.log = log;
            this.mrX = mrX;
            this.detectives = detectives;
            // check the game is not over initially
            checkWinner();
        }

        private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

            // create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
            Set<Move.SingleMove> singleMoves = new HashSet<>();
            for (int destination : setup.graph.adjacentNodes(source)) {
                // find out if destination is occupied by a detective
                //  if the location is occupied, don't add to the collection of moves to return
                if (detectives.stream().anyMatch(detective -> detective.location() == destination)) continue;
                for (ScotlandYard.Transport t : Objects.requireNonNull(
                        setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {

                    // find out if the player has the required tickets
                    //  if it does, construct a SingleMove and add it the collection of moves to return
                    if (player.has(t.requiredTicket())) {
                        singleMoves.add(new Move.SingleMove
                                (player.piece(), source, t.requiredTicket(), destination));
                    }
                }

                // Replace the ticket with a secret ticket if the player (mrX) has it
                if (player.has(ScotlandYard.Ticket.SECRET)) {
                    // add singleMoves via a secret ticket
                    singleMoves.add(new Move.SingleMove
                            (player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
                }
            }

            // return the collection of moves
            return singleMoves;
        }

        private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player mrX, int source) {

            // create an empty collection of some sort, say, HashSet, to store all the DoubleMoves we generate
            Set<Move.DoubleMove> doubleMoves = new HashSet<>();

            // check if mrX has double move or have enough ticket
            if (!mrX.has(ScotlandYard.Ticket.DOUBLE) || mrX.tickets().size() < 2) return doubleMoves;

            // check if the GameSetup reveals the moves
            if (setup.moves.equals(ImmutableList.of(true))) return doubleMoves;

            Set<Move.SingleMove> firstMoves = makeSingleMoves(setup, detectives, mrX, source);
            // loop through each first moves
            for (Move.SingleMove firstMove : firstMoves) {
                // generate all moves from the first move destination
                Set<Move.SingleMove> secondMoves = makeSingleMoves(setup, detectives, mrX, firstMove.destination);
                // loop through each second moves and add to double moves
                for (Move.SingleMove secondMove : secondMoves) {
                    boolean enoughSameTicket = firstMove.ticket == secondMove.ticket
                            && mrX.hasAtLeast(firstMove.ticket, 2);
                    if(enoughSameTicket)
                        doubleMoves.add(new Move.DoubleMove(mrX.piece(), mrX.location(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
                    else if (firstMove.ticket != secondMove.ticket)
                        doubleMoves.add(new Move.DoubleMove(mrX.piece(), mrX.location(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
                }
            }

            // return the collection of moves
            return doubleMoves;
        }

        @Nonnull
        @Override
        public GameSetup getSetup() {
            return setup;
        }

        @Nonnull
        @Override
        public ImmutableSet<Piece> getPlayers() {
            Set<Piece> players = new HashSet<>();
            players.add(mrX.piece());
            players.addAll(detectives.stream().map(Player::piece).toList());
            return ImmutableSet.copyOf(players);
        }

        @Nonnull
        @Override
        public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
            // Check if the piece joined the game
            if (detectives.stream().noneMatch(player -> player.piece() == detective)) return Optional.empty();
                // if it joined, filter the player instance and return the location
            else return detectives.stream()
                    .filter(player -> player.piece() == detective)
                    .map(Player::location).findFirst();
        }

        @Nonnull
        @Override
        public Optional<TicketBoard> getPlayerTickets(Piece piece) {
            // check if the piece is mrX
            if (piece.isMrX()) return Optional.of(ticket -> mrX.tickets().get(ticket));
                // check if the piece joined the game
            else if (detectives.stream().noneMatch(player -> player.piece() == piece)) return Optional.empty();
                // return the corresponding detective's ticketBoard
            else return Optional.of(ticket -> detectives.stream()
                        .filter(player -> player.piece() == piece).findFirst().get().tickets().get(ticket));
        }

        @Nonnull
        @Override
        public ImmutableList<LogEntry> getMrXTravelLog() {
            return log;
        }

        @Nonnull
        @Override
        public ImmutableSet<Piece> getWinner() {
            return winner;
        }

        @Nonnull
        @Override
        // Generate available moves of all pieces in the set of remaining pieces
        public ImmutableSet<Move> getAvailableMoves() {
            Set<Move> allPossibleMoves = new HashSet<>();
            // check if the winner set is empty
            if (!winner.isEmpty()) return ImmutableSet.copyOf(allPossibleMoves);
            // mrX's turn
            if (remaining.equals(ImmutableSet.of(Piece.MrX.MRX))) {
                allPossibleMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
                allPossibleMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
            } else
            // Detectives' turn
            {
                remaining.forEach(piece -> allPossibleMoves
                        .addAll(makeSingleMoves(setup, detectives, getPlayer(piece), getPlayer(piece).location())));

            }
            return ImmutableSet.copyOf(allPossibleMoves);
        }

        @Nonnull
        @Override
        public GameState advance(Move move) {

            if (!getAvailableMoves().contains(move)) throw new IllegalArgumentException("Illegal move: " + move);

            // Visitor Pattern
            // 1. Update detectives list and mrX
            // 2. Add log if the move is done by mrX
            // 3. Update available moves and remaining
            // 4. Check if anyone win the game
            move.accept(new Move.Visitor<Void>() {
                @Override
                public Void visit(Move.SingleMove singleMove) {

                    if (singleMove.commencedBy().isDetective()) {
                        List<Player> newPlayers = new ArrayList<>();
                        for (Player player : detectives) {
                            // find the corresponding player object
                            if (player.piece() == singleMove.commencedBy()) {
                                // update player location and use tickets
                                player = player.at(singleMove.destination).use(singleMove.tickets());
                                mrX = mrX.give(singleMove.ticket);
                            }
                            newPlayers.add(player);
                        }
                        detectives = newPlayers;
                    } else if (singleMove.commencedBy().isMrX()) {
                        // update player location and use tickets
                        mrX = mrX.at(singleMove.destination).use(singleMove.tickets());
                        // update log
                        addMrXLog(singleMove.ticket, mrX.location());
                    }
                    return null;
                }

                @Override
                public Void visit(Move.DoubleMove doubleMove) {
                    // update player location and use tickets
                    mrX = mrX.at(doubleMove.destination2).use(doubleMove.tickets());
                    // update log
                    addMrXLog(doubleMove.ticket1, mrX.location());
                    addMrXLog(doubleMove.ticket2, mrX.location());
                    return null;
                }
            });
            // update remaining piece to play
            // and check if there is a winner
            updateMovesAndRemaining(move.commencedBy());
            checkWinner();
            return this;
        }

        private void updateMovesAndRemaining(Piece playedPiece) {
            // swap to detectives' turns if mrX played
            if (playedPiece == Piece.MrX.MRX) {
                remaining = ImmutableSet.copyOf(detectives.stream().map(Player::piece).toList());
            } else {
                // remove the played detective from the remaining set
                remaining = ImmutableSet.copyOf(remaining.stream().filter(piece -> piece != playedPiece).toList());
            }
            // swap to mrX's turn if all detectives played
            if (remaining.isEmpty()) {
                remaining = ImmutableSet.of(Piece.MrX.MRX);
            }
            moves = getAvailableMoves();
            // remove piece that has no possible moves from the remaining set
            remaining = ImmutableSet.copyOf(moves.stream().map(Move::commencedBy).distinct().toList());
            // if all detectives are stuck, swap to mrX's turn
            if (remaining.isEmpty()) {
                remaining = ImmutableSet.of(Piece.MrX.MRX);
                moves = getAvailableMoves();
            }
        }

        // Helper function to update mrX log
        private void addMrXLog(ScotlandYard.Ticket ticket, int location) {
            List<LogEntry> newLog = new ArrayList<>(log);
            newLog.add(revealMovesThisRound() ? LogEntry.reveal(ticket, location) : LogEntry.hidden(ticket));
            log = ImmutableList.copyOf(newLog);
        }

        // check if detectives or mrX win the game
        // if someone wins, update winner set
        // and clear available moves set
        private void checkWinner() {
            // check if detectives caught mrX
            boolean mrXGetCaught = detectives.stream().map(Player::location).toList().contains(mrX.location());
            // or mrX are stuck
            boolean mrXStuck = remaining.equals(ImmutableSet.of(Piece.MrX.MRX)) &&
                    getAvailableMoves().stream().noneMatch(move -> move.commencedBy() == Piece.MrX.MRX);
            if (mrXGetCaught || mrXStuck)
                // detectives won the game
                winner = ImmutableSet.copyOf(detectives.stream().map(Player::piece).iterator());

            // Check all detectives are out of ticket
            boolean detectiveNoTickets = detectives.stream().allMatch(player ->
                    // to see if all tickets count is 0
                    player.tickets().values().stream().allMatch(integer -> integer == 0));
            // or if last round is reached
            boolean allMovesUsed = remaining.equals(ImmutableSet.of(Piece.MrX.MRX)) && log.size() == setup.moves.size();
            if (detectiveNoTickets || allMovesUsed)
                // mrX won the game
                winner = ImmutableSet.of(Piece.MrX.MRX);
            // clear moves set if someone wins
            if (!winner.isEmpty()) moves = ImmutableSet.of();
        }

        // find the corresponding player instance with the given piece
        private Player getPlayer(Piece piece) {
            if (piece.isMrX()) return mrX;
            // check if the piece is in the game
            if (detectives.stream().noneMatch(player -> player.piece() == piece))
                throw new IllegalArgumentException("This piece is not in the game!");
            else return detectives.stream()
                    .filter(player -> player.piece() == piece)
                    .findFirst().get();
        }

        private boolean revealMovesThisRound() {
            return setup.moves.get(log.size());
        }
    }

}
