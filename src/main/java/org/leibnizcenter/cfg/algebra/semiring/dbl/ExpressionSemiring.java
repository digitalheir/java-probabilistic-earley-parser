package org.leibnizcenter.cfg.algebra.semiring.dbl;

import org.leibnizcenter.cfg.earleyparser.Atom;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Semiring for abstract expression trees
 * <p>
 * Created by Maarten on 24-8-2016.
 */
public abstract class ExpressionSemiring implements DblSemiring {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private final double ONE;
    private final double ZERO;

    ExpressionSemiring() {
        ONE = one();
        ZERO = zero();
    }

    /*     NOTE:
     *   Checking for multiplicative and additive identities doesn't seem to add any performance in practice!
     */
    public Resolvable times(final double r1, final Resolvable r2, final Resolvable r3) {
        if (r3 instanceof Atom && r2 instanceof Atom)
            return new Atom(times(r1, times(((Atom) r3).value, ((Atom) r2).value)));

        if (r1 == ONE) return times(r2, r3);
        else if (isMultiplicativeIdentity(r2)) return times(r1, r3);
        else if (isMultiplicativeIdentity(r3)) return times(r1, r2);
        else return new DblTimes(r1, r2, r3);
    }

    public double times(final double r1, final double r2, final double r3) {
        if (r1 == ONE) return times(r2, r3);
        else if (r2 == ONE) return times(r1, r3);
        else if (r3 == ONE) return times(r1, r2);
        else return times(times(r1, r2), r3);
    }

    private Resolvable times(final Resolvable r1, final Resolvable r2) {
        if (isMultiplicativeIdentity(r1)) return r2;
        else if (isMultiplicativeIdentity(r2)) return r1;

        else if (r1 instanceof Atom && r2 instanceof Atom)
            return new Atom(times(((Atom) r1).value, ((Atom) r2).value));

        return new Times(r1, r2);
    }

    private Resolvable times(final double r1, final Resolvable r2) {
        if (r1 == ONE) return r2;
        else if (isMultiplicativeIdentity(r2)) return new Atom(r1);

        else if (r2 instanceof Atom)
            return new Atom(times(r1, ((Atom) r2).value));

        else return new DblTimes(r1, r2);
    }

    public Resolvable plus(final Resolvable r1, final Resolvable r2) {
        if (isAdditiveIdentity(r1)) return r2;
        else if (isAdditiveIdentity(r2)) return r1;

        else if (r1 instanceof Atom && r2 instanceof Atom) {
            return new Atom(plus(((Atom) r1).value, ((Atom) r2).value));
        } else
            return new Plus(r1, r2);
    }

    public Resolvable plus(final Resolvable r1, final double r2) {
        if (r2 == ZERO) return r1;
        else if (isAdditiveIdentity(r1)) return new Atom(r2);

        else if (r1 instanceof Atom)
            return new Atom(plus(((Atom) r1).value, r2));

        else return new DblPlus(r1, r2);
    }

    private boolean isMultiplicativeIdentity(final Resolvable r) {
        return r instanceof Atom && ((Atom) r).value == ONE;
    }

    private boolean isAdditiveIdentity(final Resolvable x) {
        return x instanceof Atom && ((Atom) x).value == this.zero();
    }


    private final class Plus extends ResolvableLockable {
        private Resolvable right;
        private Resolvable left;

        private Plus(final Resolvable left, final Resolvable right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public double resolveAndClean() {
            if (lock) return cached;

            final double v = plus(left.resolveFinal(), right.resolveFinal());
            left = null;
            right = null;
            return v;
        }

        @Override
        public String toString() {
            return '(' + left.toString() + " + " + right.toString() + ')' + (lock ? '=' + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Plus plus = (Plus) o;

            return right.equals(plus.right) && left.equals(plus.left);
        }

        @Override
        public int hashCode() {
            int result = right.hashCode();
            result = 31 * result + left.hashCode();
            return result;
        }
    }

    private final class DblPlus extends ResolvableLockable {
        private final double right;
        private Resolvable left;

        private DblPlus(final Resolvable left, final double right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public double resolveAndClean() {
            if (lock) return cached;
            else {
                final double finalVal = plus(left.resolveFinal(), right);
                left = null;
                return finalVal;
            }
        }

        @Override
        public String toString() {
            return "(" + left + " + " + right + ')' + (lock ? '=' + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DblPlus dblPlus = (DblPlus) o;

            return Double.compare(dblPlus.right, right) == 0
                    && ((dblPlus.left == null && left == null)
                    || (left != null && left.equals(dblPlus.left)));
        }

        @Override
        public int hashCode() {
            int result;
            final long temp;
            temp = Double.doubleToLongBits(right);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + (left == null ? 0 : left.hashCode());
            return result;
        }
    }


    private final class Times extends ResolvableLockable {
        private Resolvable right;
        private Resolvable left;

        Times(final Resolvable left, final Resolvable right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public double resolveAndClean() {
            final double v = times(left.resolveFinal(), right.resolveFinal());
            left = null;
            right = null;
            return v;
        }

        @Override
        public String toString() {
            return "(" + left + " * " + right + ')' + (lock ? '=' + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Times times = (Times) o;

            return right.equals(times.right) && left.equals(times.left);

        }

        @Override
        public int hashCode() {
            int result = right.hashCode();
            result = 31 * result + left.hashCode();
            return result;
        }
    }

    private final class DblTimes extends ResolvableLockable {
        private final double left;
        private Resolvable right;
        private Resolvable right2;

        DblTimes(final double left, final Resolvable right) {
            this.left = left;
            this.right = right;
            this.right2 = null;
        }

        private DblTimes(final double left, final Resolvable right, final Resolvable right2) {
            this.left = left;
            this.right = right;
            this.right2 = right2;
        }

        @Override
        public double resolveAndClean() {
            final double firstPart = times(left, right.resolveFinal());
            right = null;
            if (right2 == null) return firstPart;
            final double v = times(firstPart, right2.resolveFinal());
            right2 = null;
            return v;
        }

        @Override
        public String toString() {
            return "(" + left + " * " + right + (right2 == null ? "" : (" * " + right2)) + ')' + (lock ? '=' + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DblTimes dblTimes = (DblTimes) o;

            return Double.compare(dblTimes.left, left) == 0
                    && Objects.equals(right, dblTimes.right)
                    && (right2 != null ? right2.equals(dblTimes.right2) : dblTimes.right2 == null);
        }

        @Override
        public int hashCode() {
            int result;
            final long temp;
            temp = Double.doubleToLongBits(left);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + right.hashCode();
            result = 31 * result + (right2 != null ? right2.hashCode() : 0);
            return result;
        }
    }
}
