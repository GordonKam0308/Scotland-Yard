package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MrXAI implements Ai {
    @Nonnull
    @Override
    public String name() {
        return "MrX AI";
    }

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        // Create a gameTree instance and return the best move
        Integer mrXLocation = board.getAvailableMoves().stream().findFirst().get().source();
        GameTree G = new GameTree(board.getSetup(), GameTree.buildGameState(board), null, 2, mrXLocation);
        Score score = new GameTreeScore(board, G);
        return score.getBestMove();
    }
}
