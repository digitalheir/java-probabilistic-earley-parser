package org.leibnizcenter.cfg.algebra.semiring.dbl;

import org.leibnizcenter.cfg.algebra.semiring.Property;

import java.util.EnumSet;

/**
 * Log semiring implementation, used to avoid underflow in probability calculations.
 */
public class LogSemiring extends ExpressionSemiring {
    public static final double ZERO = Double.POSITIVE_INFINITY;
    public static final double ONE = 0.;
    private static final LogSemiring SINGLETON = new LogSemiring();
    private static final EnumSet<Property> properties = EnumSet.of(
            Property.LeftSemiring,
            Property.RightSemiring,
            Property.Commutative
    );

    private LogSemiring() {
    }

    public static LogSemiring get() {
        return SINGLETON;
    }

    @Override
    public double plus(double w1, double w2) {
        if (!member(w1) || !member(w2)) return Double.NEGATIVE_INFINITY;
        else if (w1 == ZERO) return w2;
        else if (w2 == ZERO) return w1;
        else return -Math.log(Math.exp(-w1) + Math.exp(-w2));
    }

    @Override
    public double times(double w1, double w2) {
        if (!member(w1) || !member(w2)) return Double.NEGATIVE_INFINITY;
        else if (w1 == ONE) return w2;
        else if (w2 == ONE) return w1;
        else return w1 + w2;
    }

    @Override
    public double zero() {
        return ZERO;
    }

    @Override
    public double one() {
        return ONE;
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

    @Override
    public int compare(double x, double y) {
        return Double.compare(y, x);
    }
}
