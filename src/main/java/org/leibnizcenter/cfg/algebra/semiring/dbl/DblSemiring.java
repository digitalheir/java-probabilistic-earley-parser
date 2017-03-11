package org.leibnizcenter.cfg.algebra.semiring.dbl;


import java.util.stream.IntStream;

/**
 * Semiring defined on double primitives.
 * <p>
 * A semiring is specified by two binary operations ⊕ and ⊗ and two designated elements 0 and 1 with the following properties:
 * <ul>
 * <li>⊕: associative, commutative, and has 0 as its identity.</li>
 * <li>⊗: associative and has identity 1, distributes w.r.t. ⊕, and has 0 as an annihilator: 0 ⊗ a = a ⊗ 0 = 0.</li>
 * </ul>
 * <p>
 * Created by maarten on 19-6-16.
 */
public interface DblSemiring {
//    @SuppressWarnings("unused")
//    static boolean isRightSemiring(DblSemiring semiring) {
//        return semiring.properties().contains(Property.RightSemiring);
//    }
//
//    @SuppressWarnings("unused")
//    static boolean isLeftSemiring(DblSemiring semiring) {
//        return semiring.properties().contains(Property.LeftSemiring);
//    }
//
//    @SuppressWarnings("unused")
//    static boolean isIdempotent(DblSemiring semiring) {
//        return semiring.properties().contains(Property.Idempotent);
//    }
//
//    @SuppressWarnings("unused")
//    static boolean isCommutative(DblSemiring semiring) {
//        return semiring.properties().contains(Property.Commutative);
//    }
//
//    @SuppressWarnings("unused")
//    static boolean isPath(DblSemiring semiring) {
//        return semiring.properties().contains(Property.Path);
//    }

    /**
     * Semiring's operator operation
     */
    double plus(double w1, double w2);

    /**
     * Semiring's times operation
     */
    double times(double w1, double w2);

    /**
     * Semiring's zero element
     */
    double zero();

    /**
     * Semiring's one element
     */
    double one();

    /**
     * predicate on set membership.
     *
     * @param m candidate member
     * @return whether m belongs to the semiring's set
     */
    boolean member(double m);

//    /**
//     * Integer bitmap of this semirings properties:
//     * <ul>
//     * <li>LeftSemiring: indicates Ts form a left semiring</li>
//     * <li>RightSemiring: indicates Ts form a right semiring</li>
//     * <li>Commutative: ∀ a,b: Times(a, b) = Times(b, a)</li>
//     * <li>Idempotent: ∀ a: a ⊕ a = a.</li>
//     * <li>Path: ∀ a, b: a ⊕ b = a or a ⊕ b = b.</li>
//     * </ul>
//     */
//    EnumSet<Property> properties();
//
//    /**
//     * NATURAL ORDER
//     * <p>
//     * By definition: a <= b iff a + b = a
//     * <p>
//     * The natural order is a negative partial order iff the semiring is
//     * idempotent. It is trivially monotonic for operator. It is left (resp. right)
//     * monotonic for times iff the semiring is left (resp. right) distributive.
//     * It is a total order iff the semiring has the path property.
//     * <p>
//     * See Mohri,
//     * "Semiring Framework and Algorithms for Shortest-Distance Problems",
//     * Journal of Automata, Languages and Combinatorics 7(3):321-350, 2002.
//     * <p>
//     * We define the strict version of this order below.
//     */
//    @SuppressWarnings("unused")
//    default boolean naturalLess(double w1, double w2) {
//        return (this.plus(w1, w2) == w1) && (w1 != w2);
//    }
//

    double fromProbability(double x);

    double toProbability(double x);

    /**
     * @return As in {@link Double#compare(double, double)}
     */
    int compare(double x, double y);


    default double pow(double ruleProv, int i) {
        if (i == 0) {
            return one();
        } else if (i < 0) {
            throw new IllegalArgumentException();
        } else {
            return IntStream.range(0, i - 1)
                    .mapToDouble(_i -> ruleProv)
                    .reduce(ruleProv, this::times);
        }
    }
}
