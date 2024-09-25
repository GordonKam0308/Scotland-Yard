package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;

public class DetectiveScore extends ScoreUtils implements Score {
    DetectiveScore(Board board) {
        super(board);
    }

    public Move getBestMove() {
        List<Double> scores = score(new DetectiveScoreContext(getBoard(), null));
        // find the min score from the set
        double minScore = Integer.MAX_VALUE;
        int minScoreIndex = 0;
        for (int i = 0; i < scores.size(); i++) {
            double score = scores.get(i);
            if (score < minScore) {
                minScore = scores.get(i);
                minScoreIndex = i;
            }
        }
        return getBoard().getAvailableMoves().stream().toList().get(minScoreIndex);
    }

    // scores all detectives' moves from the current board
    // return the list of scores
    public List<Double> score(ScoreContext context) {
        if (context instanceof DetectiveScoreContext) {
            List<Double> result = new ArrayList<>();
            // retrieve all available moves from the context
            var allPossibleMoves = ((DetectiveScoreContext) context).board.getAvailableMoves();
            // find the score of each move
            allPossibleMoves.forEach(move -> result.add(calculateScore(new DetectiveScoreContext(null, move))));
            return result;
        } else {
            throw new IllegalArgumentException("context need to be DetectiveScoreContext");
        }

    }

    // scoring algorithm that takes
    // distanceScore (distance to mrX)
    // into a factor
    public double calculateScore(ScoreContext context) {
        if (context instanceof DetectiveScoreContext) {
            // retrieve the latest mrX location from log
            Integer mrXLocation = getMrXLastRevealPosition();
            // if no reveal moves has made yet,
            // moves cannot be scored
            if (getMrXLastRevealPosition() == null) return 0.0;
            Move move = ((DetectiveScoreContext) context).move;
            int destination = move.accept(new Move.Visitor<Integer>() {
                @Override
                public Integer visit(Move.SingleMove move) {
                    return move.destination;
                }

                @Override
                public Integer visit(Move.DoubleMove move) {
                    return move.destination2;
                }
            });
            double distanceScore = calculateDistance(destination, mrXLocation);
            return distanceScore;
        } else {
            throw new IllegalArgumentException("context need to be DetectiveScoreContext");
        }
    }
}
