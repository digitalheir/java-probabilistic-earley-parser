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
    private final double ONE;
    private final double ZERO;

    ExpressionSemiring() {
        ONE = one();
        ZERO = zero();
    }

    /*     NOTE:
     *   Checking for multiplicative and additive identities doesn't seem to add any performance in practice!
     */
    public Resolvable times(double r1, Resolvable r2, Resolvable r3) {
        if (r1 == ONE) return times(r2, r3);
        else if (isMultiplicativeIdentity(r2)) return times(r1, r3);
        else if (isMultiplicativeIdentity(r3)) return times(r1, r2);
        else
            return new DblTimes(r1, r2, r3);
    }

    private Resolvable times(Resolvable r1, Resolvable r2) {
        if (isMultiplicativeIdentity(r1)) return r2;
        else if (isMultiplicativeIdentity(r2)) return r1;
        return new Times(r1, r2);
    }

    public Resolvable times(double r1, Resolvable r2) {
        if (r1 == ONE) return r2;
        else if (isMultiplicativeIdentity(r2)) return new Atom(r1);
        else
            return new DblTimes(r1, r2);
    }

    public Resolvable plus(Resolvable r1, Resolvable r2) {
        if (isAdditiveIdentity(r1)) return r2;
        else if (isAdditiveIdentity(r2)) return r1;
        else
            return new Plus(r1, r2);
    }

    public Resolvable plus(Resolvable r1, double r2) {
        if (r2 == ZERO)
            return r1;
        else if (isAdditiveIdentity(r1))
            return new Atom(r2);
        else
            return new DblPlus(r1, r2);
    }

    private boolean isMultiplicativeIdentity(Resolvable r) {
        return r instanceof Atom && ((Atom) r).value == ONE;
    }

    private boolean isAdditiveIdentity(Resolvable x) {
        return x instanceof Atom && ((Atom) x).value == this.zero();
    }


    private final class Plus extends Resolvable {
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
            return '(' + left.toString() + " + " + right.toString() + ')' + (lock ? '=' + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Plus plus = (Plus) o;

            return right.equals(plus.right) && left.equals(plus.left);
        }

        @Override
        public int hashCode() {
            int result = right.hashCode();
            result = 31 * result + left.hashCode();
            return result;
        }
    }

    private final class DblPlus extends Resolvable {
        private final double right;
        private final Resolvable left;

        private DblPlus(Resolvable left, double right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public double resolve() {
            if (lock)
                return cached;
            else {
                return plus(left.resolveFinal(), right);
            }
        }

        @Override
        public String toString() {
            return "(" + left + " + " + right + ')' + (lock ? '=' + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DblPlus dblPlus = (DblPlus) o;

            return Double.compare(dblPlus.right, right) == 0 && left.equals(dblPlus.left);
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(right);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + left.hashCode();
            return result;
        }
    }


    private final class Times extends Resolvable {
        private final Resolvable right;
        private final Resolvable left;

        Times(Resolvable left, Resolvable right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public double resolve() {
            return times(left.resolveFinal(), right.resolveFinal());
        }

        @Override
        public String toString() {
            return '(' + left.toString() + " * " + right.toString() + ')' + (lock ? '=' + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Times times = (Times) o;

            return right.equals(times.right) && left.equals(times.left);

        }

        @Override
        public int hashCode() {
            int result = right.hashCode();
            result = 31 * result + left.hashCode();
            return result;
        }
    }

    private final class DblTimes extends Resolvable {
        private final double left;
        private final Resolvable right;
        private final Resolvable right2;

        DblTimes(double left, Resolvable right) {
            this.left = left;
            this.right = right;
            this.right2 = null;
        }

        private DblTimes(double left, Resolvable right, Resolvable right2) {
            this.left = left;
            this.right = right;
            this.right2 = right2;
        }

        @Override
        public double resolve() {
            final double firstPart = times(left, right.resolveFinal());
            return (right2 == null) ? firstPart : times(firstPart, right2.resolveFinal());
        }

        @Override
        public String toString() {
            return "(" + left + " * " + right.toString() + (right2 == null ? "" : (" * " + right2.toString())) + ')' + (lock ? '=' + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DblTimes dblTimes = (DblTimes) o;

            return Double.compare(dblTimes.left, left) == 0
                    && right.equals(dblTimes.right)
                    && (right2 != null ? right2.equals(dblTimes.right2) : dblTimes.right2 == null);
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(left);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + right.hashCode();
            result = 31 * result + (right2 != null ? right2.hashCode() : 0);
            return result;
        }
    }
}
