package org.leibnizcenter.cfg.algebra.semiring.dbl;

import org.leibnizcenter.cfg.algebra.semiring.Property;

import java.util.EnumSet;

/**
 * Log semiring implementation, used to avoid underflow in probability calculations.
 */
public class LogSemiring extends ExpressionSemiring {
    private static final EnumSet<Property> properties = EnumSet.of(
            Property.LeftSemiring,
            Property.RightSemiring,
            Property.Commutative
    );

    @Override
    public double plus(double w1, double w2) {
        if (!member(w1) || !member(w2)) {
            return Double.NEGATIVE_INFINITY;
        }
        if (w1 == Double.POSITIVE_INFINITY) {
            return w2;
        } else if (w2 == Double.POSITIVE_INFINITY) {
            return w1;
        }
        return -Math.log(Math.exp(-w1) + Math.exp(-w2));
    }

    @Override
    public double times(double w1, double w2) {
        if (!member(w1) || !member(w2)) {
            return Double.NEGATIVE_INFINITY;
        }
        return w1 + w2;
    }

    @Override
    public double zero() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double one() {
        return 0.;
    }


    @Override
    public EnumSet<Property> properties() {
        return properties;
    }


    @Override
    public boolean member(double w) {
        return (!Double.isNaN(w)) // not a NaN
                && (w != Double.NEGATIVE_INFINITY); // and different endState -inf
    }

    @Override
    public double fromProbability(double x) {
        return -Math.log(x);
    }

    @Override
    public double toProbability(double x) {
        return Math.exp(-x);
    }
}
