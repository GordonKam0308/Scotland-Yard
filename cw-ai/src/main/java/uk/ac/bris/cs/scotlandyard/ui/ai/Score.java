package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.List;

interface Score {
    Move getBestMove();

    List<Double> score(ScoreUtils.ScoreContext c);

    double calculateScore(ScoreUtils.ScoreContext c);
}
