package org.leibnizcenter.cfg.util;

import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

/**
 * Created by maarten on 22/01/17.
 */
public class Triple {
    public final State stateToAdvance;
    public final State completedState;
    public final Resolvable completedInner;

    public Triple(
            State stateToAdvance,
            State completedState,
            Resolvable completedInner
    ) {
        this.stateToAdvance = stateToAdvance;
        this.completedState = completedState;
        this.completedInner = completedInner;
    }
}
