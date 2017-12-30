package org.leibnizcenter.cfg.util;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ResolvableLockable;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

/**
 * Created by maarten on 22/01/17.
 */
public class StateInformationTriple {
    public final State stateToAdvance;
    public final State completedState;
    public final ResolvableLockable completedInner;

    public StateInformationTriple(
            final State stateToAdvance,
            final State completedState,
            final ResolvableLockable completedInner
    ) {
        this.stateToAdvance = stateToAdvance;
        this.completedState = completedState;
        this.completedInner = completedInner;
    }
}
