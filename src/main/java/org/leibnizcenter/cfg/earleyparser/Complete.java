package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

/**
 * Complete stage
 *
 * Created by maarten on 31/10/16.
 */
public class Complete {


    public static class Delta {
        public final boolean newCompletedStateNoUnitProduction;

        public Delta(final State state, final boolean newCompletedStateNoUnitProduction) {
            this.newCompletedStateNoUnitProduction = newCompletedStateNoUnitProduction;
        }
    }

    public static class ViterbiDelta {
        public final State resultingState;
        public final State.ViterbiScore newViterbiScore;
        public final boolean isNewState;
        private final boolean isNewCompletedState;

        public ViterbiDelta(final State resultingState, final boolean isNewCompletedState, final State.ViterbiScore newViterbiScore, final boolean isNewState) {
            this.isNewCompletedState = isNewCompletedState;
            this.resultingState = resultingState;
            this.newViterbiScore = newViterbiScore;
            this.isNewState = isNewState;
        }

        public boolean isNewCompletedState() {
            return isNewCompletedState;
        }

    }
}
