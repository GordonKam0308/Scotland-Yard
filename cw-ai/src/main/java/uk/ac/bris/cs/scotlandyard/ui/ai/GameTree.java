package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameTree {
    private final Integer mrXLocation;
    private final GameSetup setup;
    private final Board.GameState state;
    private final Iterable<ScotlandYard.Ticket> tickets;
    private final Integer levels;
    private List<GameTree> branch = new ArrayList<>();

    GameTree(GameSetup setup, Board.GameState state, Iterable<ScotlandYard.Ticket> tickets, Integer levels, Integer mrXLocation) {
        this.setup = setup;
        this.state = state;
        this.tickets = tickets;
        this.mrXLocation = mrXLocation;
        this.levels = levels;
        // predict the next move and build branch
        if (levels > 0) build(state.getAvailableMoves());
    }

    // create the current game state with board
    public static Board.GameState buildGameState(Board board) {
        Player mrX = new Player(Piece.MrX.MRX, genereateTickets(board, Piece.MrX.MRX), board.getAvailableMoves().stream().findFirst().get().source());
        ImmutableList.Builder<Player> playerList = ImmutableList.builder();
        for (Piece p : board.getPlayers().stream().filter(Piece::isDetective).toList()) {
            playerList.add(new Player(p, genereateTickets(board, p), board.getDetectiveLocation((Piece.Detective) p).get()));
        }
        return new MyGameStateFactory().build(board.getSetup(), mrX, playerList.build());
    }

    // generate tickets info of a given player
    private static ImmutableMap<ScotlandYard.Ticket, Integer> genereateTickets(Board board, Piece player) {
        ImmutableMap.Builder<ScotlandYard.Ticket, Integer> tickets = ImmutableMap.builder();
        for (ScotlandYard.Ticket t : ScotlandYard.Ticket.values()) {
            tickets.put(t, board.getPlayerTickets(player).get().getCount(t));
        }
        return tickets.build();
    }

    private void build(Set<Move> moves) {
        // add corresponding child nodes from the set of available moves of the current gameState
        moves.forEach(move -> branch.add(new GameTree(setup, state.advance(move), move.commencedBy().isMrX() ? move.tickets() : null, levels - 1, move.accept(new Move.Visitor<Integer>() {
            @Override
            public Integer visit(Move.SingleMove move) {
                // check if it is mrX round
                if (move.commencedBy().isMrX()) return move.destination;
                else return mrXLocation;
            }

            @Override
            public Integer visit(Move.DoubleMove move) {
                return move.destination2;
            }
        }))));
    }

    public boolean hasNoChild() {
        return branch.isEmpty();
    }

    public List<GameTree> getBranch() {
        return branch;
    }

    public Board.GameState getState() {
        return state;
    }
    // helper function for build the GameTree

    public Integer getMrXLocation() {
        return mrXLocation;
    }

    public Iterable<ScotlandYard.Ticket> getTickets() {
        return tickets;
    }
}
