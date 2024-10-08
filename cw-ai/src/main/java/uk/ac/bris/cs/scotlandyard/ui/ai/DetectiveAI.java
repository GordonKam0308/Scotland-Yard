package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class DetectiveAI implements Ai {
    @Nonnull
    @Override
    public String name() {
        return "Detective AI";
    }

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        Score score = new DetectiveScore(board);
        // find the node with maximum score
        return score.getBestMove();
    }
}
