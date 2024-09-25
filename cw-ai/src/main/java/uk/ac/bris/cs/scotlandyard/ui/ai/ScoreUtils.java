package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import javafx.util.Pair;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScoreUtils {
    private final Board board;
    private final Dijkstra D;
    private final Graph G;

    ScoreUtils(Board board) {
        this.board = board;
        //initiate Dijkstra Graph
        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = board.getSetup().graph;
        Graph G = new SingleGraph("1");
        graph.nodes().forEach(integer -> G.addNode(String.valueOf(integer)));
        graph.edges().forEach(integers -> G.addEdge(String.valueOf(integers.nodeU()) + " " + String.valueOf(integers.nodeV()), String.valueOf(integers.nodeU()), String.valueOf(integers.nodeV())).setAttribute("length", 1));
        Dijkstra D = new Dijkstra();
        D.init(G);
        this.D = D;
        this.G = G;
    }

    // https://graphstream-project.org/  GraphStream
    // use Dijkstra to calculate the min distance from source to destination
    public double calculateDistance(int source, int destination) {
        D.setSource(G.getNode(String.valueOf(source)));
        D.compute();
        return D.getPathLength(G.getNode(String.valueOf(destination)));
    }

    // ticket score, return the average of tickets score
    public double calculateTicket(Iterable<ScotlandYard.Ticket> tickets) {
        Set<Double> scores = new HashSet<>();
        for (ScotlandYard.Ticket t : tickets) {
            switch (t) {
                case TAXI -> scores.add(0.9);
                case BUS -> scores.add(0.8);
                case UNDERGROUND -> scores.add(0.7);
                case SECRET -> scores.add(0.55);
                case DOUBLE -> scores.add(0.35);
            }
        }
        return scores.stream().reduce((double) 0, Double::sum) / scores.size();
    }

    // freedom of movement score
    // return the number of adjacent node from source
    // return 0 if there is detectives nearby source
    public double calculateFreedom(int source, Board.GameState state) {
        int degree = getBoard().getSetup().graph.degree(source);
        // get number of detectives that are in the adjacent node of mrX
        int adjDetective = getBoard().getSetup().graph.adjacentNodes(source).stream().filter(integer -> getAllDetectiveLocations(state).stream().map(Pair::getValue).toList().contains(integer)).toList().size();
        // if there are detectives nearby, mrX will be in danger
        if (adjDetective > 0) return 0.0;
        else return degree;
    }

    public ImmutableList<Pair<Piece, Integer>> getAllDetectiveLocations(Board.GameState gameState) {
        ImmutableList.Builder<Pair<Piece, Integer>> detectiveLocations = ImmutableList.builder();
        for (Piece p : gameState.getPlayers().stream().filter(Piece::isDetective).toList()) {
            detectiveLocations.add(new Pair<>(p, gameState.getDetectiveLocation((Piece.Detective) p).get()));
        }
        return detectiveLocations.build();
    }

    // get mrX last reveal position
    // return null if there is no reveal logs
    public Integer getMrXLastRevealPosition() {
        List<LogEntry> allRevealLogs = getBoard().getMrXTravelLog().stream().filter(logEntry -> logEntry.location().isPresent()).toList();
        if (allRevealLogs.isEmpty()) return null;
        else return allRevealLogs.get(allRevealLogs.size() - 1).location().get();
    }

    public Board getBoard() {
        return board;
    }

    // definition for the context of different type of scores
    public interface ScoreContext {
    }

    public static class DetectiveScoreContext implements ScoreContext {
        public final Board board;
        public final Move move;

        DetectiveScoreContext(Board board, Move move) {
            this.board = board;
            this.move = move;
        }
    }

    public static class GameTreeScoreContext implements ScoreContext {
        public final GameTree gameTree;
        public final Board.GameState gameState;
        public final Integer mrXLocation;

        GameTreeScoreContext(GameTree gameTree, Board.GameState gameState, Integer mrXLocation) {
            this.gameTree = gameTree;
            this.gameState = gameState;
            this.mrXLocation = mrXLocation;
        }
    }
}
