package org.leibnizcenter.cfg.algebra.semiring.dbl;

import java.text.DecimalFormat;

/**
 * Semiring for abstract expression trees
 * <p>
 * Created by Maarten on 24-8-2016.
 */
public abstract class ExpressionSemiring implements DblSemiring {
    private static final DecimalFormat df = new DecimalFormat("0.00");


//    public Resolvable times(Resolvable r1, Resolvable r2, Resolvable r3) {
//        if (isMultiplicativeIdentity(r1)) return times(r2, r3);
//        else if (isMultiplicativeIdentity(r2)) return times(r1, r3);
//        else if (isMultiplicativeIdentity(r3)) return times(r1, r2);
//         return new Times(r1, r2, r3);
//    }

    public Resolvable times(double r1, Resolvable r2, Resolvable r3) {
//        if (isMultiplicativeIdentity(r1)) return times(r2, r3);
//        else if (isMultiplicativeIdentity(r2)) return times(r1, r3);
//        else if (isMultiplicativeIdentity(r3)) return times(r1, r2);
        if (r1 == one()) return new Times(r2, r3);
        return new DblTimes(r1, r2, r3);
    }

    //    private Resolvable times(Resolvable r1, Resolvable r2) {
//        if (isMultiplicativeIdentity(r1)) return r2;
//        else if (isMultiplicativeIdentity(r2)) return r1;
//         return new Times(r1, r2);
//    }
//
//    public Resolvable times(double r1, Resolvable r2) {
////        if (isMultiplicativeIdentity(r1)) return r2;
////        else if (isMultiplicativeIdentity(r2)) return new Atom(r1);
//        if (r1 == one()) return r2;
//        return new DblTimes(r1, r2);
//    }
//
//    private boolean isMultiplicativeIdentity(Resolvable r) {
//        return r instanceof Atom && isMultiplicativeIdentity(((Atom) r).value);
//    }
//
//    private boolean isMultiplicativeIdentity(double d) {
//        return d == this.one();
//    }
//
//    private boolean isAdditiveIdentity(Resolvable x) {
//        return x instanceof Atom && ((Atom) x).value == this.zero();
//    }

    public Resolvable plus(Resolvable r1, Resolvable r2) {
//        if (isAdditiveIdentity(r1)) return r2;
//        else if (isAdditiveIdentity(r2)) return r1;
        return new Plus(r1, r2);
    }

    public Resolvable plus(Resolvable r1, double r2) {
        if (r2 == zero()) return r1;
        return new DblPlus(r1, r2);
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
            return "(" + left.toString() + " + " + right.toString() + ")" + (lock ? "=" + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Plus plus = (Plus) o;

            if (!right.equals(plus.right)) return false;
            if (!left.equals(plus.left)) return false;

            return true;
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
            return "(" + left + " + " + right + ")" + (lock ? "=" + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DblPlus dblPlus = (DblPlus) o;

            if (Double.compare(dblPlus.right, right) != 0) return false;
            if (!left.equals(dblPlus.left)) return false;

            return true;
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

            return right.equals(times.right) && left.equals(times.left) && (right2 != null ? right2.equals(times.right2) : times.right2 == null);

        }

        @Override
        public int hashCode() {
            int result = right.hashCode();
            result = 31 * result + left.hashCode();
            result = 31 * result + (right2 != null ? right2.hashCode() : 0);
            return result;
        }
    }

    private class DblTimes extends Resolvable {
        private final double left;
        private final Resolvable right;
        private final Resolvable right2;

        public DblTimes(double left, Resolvable right) {
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
            return "(" + left + " * " + right.toString() + (right2 == null ? "" : (" * " + right2.toString())) + ")" + (lock ? "=" + df.format(toProbability(cached)) : "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DblTimes dblTimes = (DblTimes) o;

            if (Double.compare(dblTimes.left, left) != 0) return false;
            if (!right.equals(dblTimes.right)) return false;
            if (right2 != null ? !right2.equals(dblTimes.right2) : dblTimes.right2 != null) return false;

            return true;
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
