package org.leibnizcenter.cfg.algebra.semiring.dbl;

import org.leibnizcenter.cfg.earleyparser.Atom;

import java.text.DecimalFormat;

/**
 * Semiring for abstract expression trees
 * <p>
 * Created by Maarten on 24-8-2016.
 */
public abstract class ExpressionSemiring implements DblSemiring {
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public final Atom ZERO_EXPRESSION = new Atom(zero());

    public final class Plus extends Resolvable {
        private final Resolvable right;
        private final Resolvable left;


        public Plus(Resolvable left, Resolvable right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public double resolve() {
            if (lock)
                return cached;
            else {
                return plus(left.resolveFinal(), right.resolveFinal());
            }
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " + " + right.toString() + ")" + (lock ? "=" + df.format(toProbability(cached)) : "");
        }
    }


    public final class Times extends Resolvable {
        private final Resolvable right;
        private final Resolvable left;
        private final Resolvable right2;

        public Times(Resolvable left, Resolvable right, Resolvable right2) {
            this.left = left;
            this.right = right;
            this.right2 = right2;
        }

        @Override
        public double resolve() {
            return times(left.resolveFinal(), right.resolveFinal(), right2.resolveFinal());
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " * " + right.toString() + " * " + right2.toString() + ")" + (lock ? "=" + df.format(toProbability(cached)) : "");
        }
    }
}
