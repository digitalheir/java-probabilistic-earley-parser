package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.earleyparser.chart.state.State;

/**
 * Predict phase
 * <p>
 * Created by maarten on 31/10/16.
 */
@SuppressWarnings("WeakerAccess")
public class Predict {
    private Predict() {
    }

    public static class Delta {
        public final double Y_to_vProbability;
        public final double fw;
        public final State statePredecessor;
        public final State predicted;
        public final boolean isNew;

        public Delta(final boolean isNew, final State state,
                     final double y_to_vProbability,
                     final double fw,
                     final State statePredecessor) {
            this.isNew = isNew;
            this.predicted = state;
            this.Y_to_vProbability = y_to_vProbability;
            this.fw = fw;
            this.statePredecessor = statePredecessor;
        }
    }
}
