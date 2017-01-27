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

    public Resolvable times(Resolvable x, Resolvable y, Resolvable z) {
        if (isMultiplicativeIdentity(x)) return times(y, z);
        else if (isMultiplicativeIdentity(y)) return times(x, z);
        else if (isMultiplicativeIdentity(z)) return times(x, y);
        return new Times(x, y, z);
    }

    private Resolvable times(Resolvable x, Resolvable y) {
        if (isMultiplicativeIdentity(x)) return y;
        else if (isMultiplicativeIdentity(y)) return x;
        return new Times(x, y);
    }

    private boolean isMultiplicativeIdentity(Resolvable r) {
        return r instanceof Atom && ((Atom) r).value == this.one();
    }

    private boolean isAdditiveIdentity(Resolvable x) {
        return x instanceof Atom && ((Atom) x).value == this.zero();
    }

    public Resolvable plus(Resolvable x, Resolvable y) {
        if (isAdditiveIdentity(x)) return y;
        else if (isAdditiveIdentity(y)) return x;
        else return new Plus(x, y);
    }


    public final class Plus extends Resolvable {
        private final Resolvable right;
        private final Resolvable left;

        private Plus(Resolvable left, Resolvable right) {
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

        private Times(Resolvable left, Resolvable right, Resolvable right2) {
            this.left = left;
            this.right = right;
            this.right2 = right2;
        }

        public Times(Resolvable left, Resolvable right) {
            this.left = left;
            this.right = right;
            right2 = null;
        }

        @Override
        public double resolve() {
            final double firstPart = times(left.resolveFinal(), right.resolveFinal());
            return (right2 == null) ? firstPart : times(firstPart, right2.resolveFinal());
        }

        @Override
        public String toString() {
            return "(" + left.toString() + " * " + right.toString() + (right2 == null ? "" : (" * " + right2.toString())) + ")" + (lock ? "=" + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Times times = (Times) o;

            if (!right.equals(times.right)) return false;
            if (!left.equals(times.left)) return false;
            if (right2 != null ? !right2.equals(times.right2) : times.right2 != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = right.hashCode();
            result = 31 * result + left.hashCode();
            result = 31 * result + (right2 != null ? right2.hashCode() : 0);
            return result;
        }
    }
}
