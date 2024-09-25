package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import javafx.util.Pair;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class GameTreeScore extends ScoreUtils implements Score {
    private GameTree gameTree;

    GameTreeScore(Board board, GameTree gameTree) {
        super(board);
        this.gameTree = gameTree;
    }

    // minimax algorithm
    // finding the max score of all min scores
    public Move getBestMove() {
        List<Double> scores = score(new GameTreeScoreContext(gameTree, null, null));
        // find the maximum score from all branches
        double maxScore = Double.MIN_VALUE;
        int maxScoreIndex = 0;
        for (int i = 0; i < scores.size(); i++) {
            double score = scores.get(i);
            if (score > maxScore) {
                maxScore = scores.get(i);
                maxScoreIndex = i;
            }
        }
        return getBoard().getAvailableMoves().stream().toList().get(maxScoreIndex);
    }

    // scores all child of a gameTree
    // return list of scores of the children
    public List<Double> score(ScoreContext context) {
        if (context instanceof GameTreeScoreContext) {
            // retrieve gameTree from the context
            GameTree gameTree = ((GameTreeScoreContext) context).gameTree;
            List<Double> result = new ArrayList<>();
            // score each move
            for (GameTree branch : gameTree.getBranch()) {
                double minScore = scoreBranch(branch);
                double ticketScore = calculateTicket(branch.getTickets());
                double totalScore = minScore + ticketScore;
                // not allow moving back to the original location
                if (Objects.equals(gameTree.getMrXLocation(), branch.getMrXLocation())) result.add(0.0);
                else result.add(totalScore);
            }
            return result;
        } else {
            throw new IllegalArgumentException("context need to be GameTreeScoreContext");
        }
    }

    // scoring a gameTree branch
    // return the min score of the branch
    private double scoreBranch(GameTree branch) {
        if (branch.hasNoChild()) {
            return calculateScore(new GameTreeScoreContext(null, branch.getState(), branch.getMrXLocation()));
        } else {
            // find the minimum score of all child of this branch
            double minScore = Double.MAX_VALUE;
            for (GameTree node : branch.getBranch()) {
                minScore = Math.min(minScore, scoreBranch(node));
            }
            return minScore;
        }
    }

    // scoring algorithm to score the current gameState relative to mrX
    // distanceScore (min distance to the detectives)
    // freedomScore (degree of node)
    // distanceRevealPositionScore(distance to the last reveal move)
    // as 3 factors of the finalScore
    public double calculateScore(ScoreContext context) {
        if (context instanceof GameTreeScoreContext) {
            int mrXLocation = ((GameTreeScoreContext) context).mrXLocation;
            Board.GameState state = ((GameTreeScoreContext) context).gameState;
            ImmutableList<Pair<Piece, Integer>> detectiveLocations = getAllDetectiveLocations(state);
            // find the min distance(score) from all detectives
            double minScore = Double.MAX_VALUE;
            for (int location : detectiveLocations.stream().map(Pair::getValue).toList()) {
                double distanceScore = calculateDistance(location, mrXLocation);
                double freedomScore = calculateFreedom(mrXLocation, state);
                // distance to the last reveal position
                // scores -1 if there is no reveal logs yet
                double distanceRevealPositionScore = getMrXLastRevealPosition() == null ? -1.0 : calculateDistance(mrXLocation, getMrXLastRevealPosition());
                double totalScore;
                // see if there is detectives nearby
                // or moving back to the last reveal position
                if (freedomScore == 0.0 || distanceRevealPositionScore == 0.0) totalScore = Double.MIN_VALUE;
                    // check if no reveal logs yet
                else if (distanceRevealPositionScore == -1.0) totalScore = distanceScore + freedomScore*2;
                else totalScore = distanceScore + freedomScore + distanceRevealPositionScore;
                if (totalScore < minScore) minScore = totalScore;
            }
            // return the minimum score
            return minScore;
        } else {
            throw new IllegalArgumentException("context need to be GameTreeScoreContext");
        }

    }
}
