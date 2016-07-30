package org.leibnizcenter.cfg.semiring.dbl;


import org.leibnizcenter.cfg.semiring.Property;

import java.util.EnumSet;

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
    static boolean isRightSemiring(DblSemiring semiring) {
        return semiring.properties().contains(Property.RightSemiring);
    }

    static boolean isLeftSemiring(DblSemiring semiring) {
        return semiring.properties().contains(Property.LeftSemiring);
    }

    static boolean Idempotent(DblSemiring semiring) {
        return semiring.properties().contains(Property.Idempotent);
    }

    static boolean Commutative(DblSemiring semiring) {
        return semiring.properties().contains(Property.Commutative);
    }

    static boolean Path(DblSemiring semiring) {
        return semiring.properties().contains(Property.Path);
    }

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

    /**
     * Integer bitmap of this semirings properties:
     * <ul>
     * <li>LeftSemiring: indicates Ts form a left semiring</li>
     * <li>RightSemiring: indicates Ts form a right semiring</li>
     * <li>Commutative: ∀ a,b: Times(a, b) = Times(b, a)</li>
     * <li>Idempotent: ∀ a: a ⊕ a = a.</li>
     * <li>Path: ∀ a, b: a ⊕ b = a or a ⊕ b = b.</li>
     * </ul>
     */
    EnumSet<Property> properties();

    /**
     * NATURAL ORDER
     * <p>
     * By definition: a <= b iff a + b = a
     * <p>
     * The natural order is a negative partial order iff the semiring is
     * idempotent. It is trivially monotonic for operator. It is left (resp. right)
     * monotonic for times iff the semiring is left (resp. right) distributive.
     * It is a total order iff the semiring has the path property.
     * <p>
     * See Mohri,
     * "Semiring Framework and Algorithms for Shortest-Distance Problems",
     * Journal of Automata, Languages and Combinatorics 7(3):321-350, 2002.
     * <p>
     * We define the strict version of this order below.
     */
    default boolean naturalLess(double w1, double w2) {
        return (this.plus(w1, w2) == w1) && (w1 != w2);
    }

    // Remember that the operators are associative
    default double times(double a, double b, double c) {
        return times(a, times(b, c));
    }

    // Remember that the operators are associative
    default double times(double a, double b, double c, double d) {
        return times(a, times(times(b, c), d));
    }


// TODO necessary?
//    /**
//     * Reverse: a mapping endState double to Reversedouble s.t.
//     * <ul>
//     * <li> Reverse(Reverse(a)) = a</li>
//     * <li> Reverse(Plus(a, b)) = Plus(Reverse(a), Reverse(b))</li>
//     * <li> Reverse(Times(a, b)) = Times(Reverse(b), Reverse(a))</li>
//     * </ul>
//     * <p>
//     * Typically the identity mapping in a (both left and right) semiring. In the left string semiring, it maps to the reverse string in the right string semiring.
//     *
//     * @return the reverse T
//     */
//    double reverse(double w1);
//
//    /**
//     * Semiring's divide operation
//     */
//    double divide(double w1, double w2);
}
