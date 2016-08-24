package org.leibnizcenter.cfg.algebra.semiring.dbl;

import java.text.DecimalFormat;

/**
 * Created by Maarten on 24-8-2016.
 */
public abstract class ExpressionSemiring implements DblSemiring {
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public Value dbl(double value) {
        return new Value(new Dbl(value));
    }

    public interface Resolvable {
        public double resolve();
    }

    /**
     * Deferred value. Might change internally until resolved.
     */
    public class Value implements Resolvable {
        private Expression expression;

        public Value(Expression e) {
            expression = e;
        }

        public Value plus(Expression factor) {
            return new Value(new Plus(expression, factor));
        }

        public Value plus(Value factor) {
            if (factor == null) return new Value(expression);
            return new Value(new Plus(expression, factor.getExpression()));
        }

        public Value times(Expression factor) {
            return new Value(new Times(expression, factor));
        }

        public Value times(Value factor) {
            return new Value(new Times(expression, factor.getExpression()));
        }

        @Override
        public String toString() {
            return "@" + expression.toString();
        }

        public double resolve() {
            return expression.resolve();
        }

        public Expression getExpression() {
            return expression;
        }

        public void setExpression(Expression expression) {
            this.expression = expression;
        }
    }

    public abstract class Expression implements Resolvable {
        double value = Double.NaN;

        public boolean isDefinite() {
            return !Double.isNaN(value);
        }
    }

    public class Dbl extends Expression {
        public Dbl(double num) {
            this.value = num;
        }

        @Override
        public double resolve() {
            return value;
        }


        @Override
        public String toString() {
            return df.format(toProbability(value));
        }
    }

    public class Plus extends Expression {
        private final Resolvable right;
        private final Resolvable left;

        public Plus(Resolvable left, Resolvable right) {
            this.value = Double.NaN;
            this.left = left;
            this.right = right;
        }

        @Override
        public double resolve() {
            if (isDefinite()) return value;
            else {
                value = ExpressionSemiring.this.plus(left.resolve(), right.resolve());
                return value;
            }
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " + " + right.toString() + ")" + (isDefinite() ? "=" + df.format(toProbability(value)) : "");
        }
    }


    public class Times extends Expression {
        private final Resolvable right;
        private final Resolvable left;

        public Times(Resolvable left, Resolvable right) {
            this.value = Double.NaN;
            this.left = left;
            this.right = right;
        }

        @Override
        public double resolve() {
            if (isDefinite()) return value;
            else {
                value = ExpressionSemiring.this.times(left.resolve(), right.resolve());
                return value;
            }
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " * " + right.toString() + ")" + (isDefinite() ? "=" + df.format(toProbability(value)) : "");
        }
    }
}
