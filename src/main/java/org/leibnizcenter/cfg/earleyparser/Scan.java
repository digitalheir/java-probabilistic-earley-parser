package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

/**
 * Scan phase of Earley
 * <p>
 * Created by maarten on 31/10/16.
 */
public final class Scan {
    public static class Delta<T> {
        public final State preScanState;
        public final double postScanForward;
        public final double postScanInner;

        public final State nextState;
        public final Token<T> token;

        public Delta(
                Token<T> token, State preScanState,
                double postScanForward,
                double postScanInner,
                Rule nextRule,
                int nextPosition,
                int nextRuleStart,
                int nextDot
        ) {
            this.preScanState = preScanState;
            this.postScanForward = postScanForward;
            this.postScanInner = postScanInner;
            this.token = token;
            this.nextState = State.create(nextPosition, nextRuleStart, nextDot, nextRule);
        }
    }
}
