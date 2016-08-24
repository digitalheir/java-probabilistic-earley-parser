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

    @Override
    public double plus(double w1, double w2) {
        if (!member(w1) || !member(w2)) return Double.NEGATIVE_INFINITY;
        return w1 + w2;
    }

    @Override
    public double times(double w1, double w2) {
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
    public boolean member(double candidate) {
        return !Double.isNaN(candidate) // not a NaN,
                && (candidate >= 0.0); // and positive
    }

    @Override
    public EnumSet<Property> properties() {
        return properties;
    }

    @Override
    public double fromProbability(double x) {
        return x;
    }

    @Override
    public double toProbability(double x) {
        return x;
    }
}
