package org.leibnizcenter.cfg.algebra.semiring.dbl;

import gnu.trove.map.TDoubleObjectMap;

import java.text.DecimalFormat;

/**
 * Semiring for abstract expression trees
 * <p>
 * Created by Maarten on 24-8-2016.
 */
public abstract class ExpressionSemiring implements DblSemiring {
    private static final DecimalFormat df = new DecimalFormat("0.00");

    private void addConvertedToCached(double prob, TDoubleObjectMap<Dbl> cached) {
        final double v = fromProbability(prob);
        cached.put(v, new Dbl(this, v));
    }

    public Value dbl(double value) {
        return new Value(this, new Dbl(this, value));
    }

    public interface Resolvable {
        double resolve();
    }

    /**
     * Deferred value. Might change internally until resolved.
     */
    public static class Value implements Resolvable {
        private Expression expression;
        private boolean lock = false;
        private double cached = Double.NaN;
        private ExpressionSemiring semiring;

        Value(ExpressionSemiring semiring, Expression e) {
            expression = e;
            this.semiring = semiring;
        }

        public Value plus(Expression factor) {
            if (lock) throw new IllegalStateException();
            return new Value(this.semiring, new Plus(this.semiring, expression, factor));
        }

        public Value plus(Value factor) {
            if (lock) throw new IllegalStateException();
            if (factor == null) return new Value(this.semiring, expression);
            return new Value(this.semiring, new Plus(this.semiring, expression, factor.getExpression()));
        }

        public Value times(Expression factor) {
            if (lock) throw new IllegalStateException();
            return new Value(this.semiring, new Times(this.semiring, expression, factor));
        }

        public Value times(Value factor) {
            if (lock) throw new IllegalStateException();
            return new Value(this.semiring, new Times(this.semiring, expression, factor.getExpression()));
        }

        @Override
        public String toString() {
            return "@" + expression.toString();
        }

        public double resolve() {
            return resolve(false);
        }

        private double resolve(boolean _final) {
            if (!_final)
                return expression.resolve();
            else
                return expression.resolveFinal();
        }

        public Expression getExpression() {
            return expression;
        }

        public void setExpression(Expression expression) {
            if (lock) throw new IllegalStateException();
            this.expression = expression;
        }

        public double resolveFinal() {
            if (!lock) {
                lock = true;
                this.cached = resolve(true);
                return this.cached;
            } else {
                return this.cached;
            }
        }
    }

    abstract static class Expression implements Resolvable {
        double value = Double.NaN;
        boolean lock = false;
        private double cached;

        boolean isDefinite() {
            return !Double.isNaN(value);
        }

        double resolveFinal() {
            if (!lock) {
                lock = true;
                this.cached = resolve();
            }
            return cached;
        }
    }

    public static class Dbl extends Expression {
        private final ExpressionSemiring semiring;

        Dbl(ExpressionSemiring semiring, double num) {
            this.value = num;
            this.semiring = semiring;
        }

        @Override
        public double resolve() {
            return value;
        }


        @Override
        public String toString() {
            return df.format(semiring.toProbability(value));
        }
    }

    public static final class Plus extends Expression {
        private final Resolvable right;
        private final Resolvable left;
        private final ExpressionSemiring semiring;


        Plus(ExpressionSemiring semiring, Resolvable left, Resolvable right) {
            this.value = Double.NaN;
            this.left = left;
            this.right = right;
            this.semiring = semiring;
        }

        @Override
        public double resolve() {
            if (isDefinite())
                return value;
            else {
                value = semiring.plus(left.resolve(), right.resolve());
                return value;
            }
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " + " + right.toString() + ")" + (isDefinite() ? "=" + df.format(this.semiring.toProbability(value)) : "");
        }
    }


    public final static class Times extends Expression {
        private final Resolvable right;
        private final Resolvable left;
        private final ExpressionSemiring semiring;

        Times(ExpressionSemiring semiring, Resolvable left, Resolvable right) {
            this.value = Double.NaN;
            this.left = left;
            this.right = right;
            this.semiring = semiring;
        }

        @Override
        public double resolve() {
            if (isDefinite())
                return value;
            else {
                value = this.semiring.times(left.resolve(), right.resolve());
                return value;
            }
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " * " + right.toString() + ")" + (isDefinite() ? "=" + df.format(this.semiring.toProbability(value)) : "");
        }
    }
}
