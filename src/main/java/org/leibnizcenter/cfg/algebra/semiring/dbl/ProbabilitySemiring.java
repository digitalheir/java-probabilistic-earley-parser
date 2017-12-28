package org.leibnizcenter.cfg.algebra.semiring.dbl;


import org.leibnizcenter.cfg.algebra.semiring.Property;

import java.util.EnumSet;

/**
 * Probability semiring implementation.
 */
public class ProbabilitySemiring extends ExpressionSemiring {
    private static final EnumSet<Property> properties = EnumSet.of(
            Property.LeftSemiring,
            Property.RightSemiring,
            Property.Commutative
    );
    private static final ProbabilitySemiring SINGLETON = new ProbabilitySemiring();

    private ProbabilitySemiring() {
    }

    public static ProbabilitySemiring get() {
        return SINGLETON;
    }

    @Override
    public double plus(final double w1, final double w2) {
        if (!member(w1) || !member(w2)) return Double.NEGATIVE_INFINITY;
        return w1 + w2;
    }

    @Override
    public double times(final double w1, final double w2) {
        if (!member(w1) || !member(w2)) return Double.NEGATIVE_INFINITY;
        return w1 * w2;
    }

    @Override
    public double zero() {
        return 0.;
    }

    @Override
    public double one() {
        return 1.;
    }

    @Override
    public boolean member(final double candidate) {
        return !Double.isNaN(candidate) // not a NaN,
                && (candidate >= 0.0); // and positive
    }


    @Override
    public double fromProbability(final double x) {
        return x;
    }

    @Override
    public double toProbability(final double x) {
        return x;
    }

    @Override
    public int compare(final double x, final double y) {
        return Double.compare(x, y);
    }
}
