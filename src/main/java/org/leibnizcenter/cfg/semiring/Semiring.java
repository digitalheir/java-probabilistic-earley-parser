package org.leibnizcenter.cfg.semiring;


import java.util.EnumSet;

/**
 * Semiring defined on T objects.
 * A semiring is specified by two binary operations ⊕ and ⊗ and two designated elements 0 and 1 with the following properties:
 * <ul>
 * <li>⊕: associative, commutative, and has 0 as its identity.</li>
 * <li>⊗: associative and has identity 1, distributes w.r.t. ⊕, and has 0 as an annihilator: 0 ⊗ a = a ⊗ 0 = 0.</li>
 * </ul>
 * <p>
 * Created by maarten on 19-6-16.
 */
public interface Semiring<T> {
    static boolean isRighSemiring(Semiring semiring) {
        return semiring.properties().contains(Property.RightSemiring);
    }

    static boolean isLefSemiring(Semiring semiring) {
        return semiring.properties().contains(Property.LeftSemiring);
    }

    static boolean Idempotent(Semiring semiring) {
        return semiring.properties().contains(Property.Idempotent);
    }

    static boolean Commutative(Semiring semiring) {
        return semiring.properties().contains(Property.Commutative);
    }

    static boolean Path(Semiring semiring) {
        return semiring.properties().contains(Property.Path);
    }

    /**
     * Semiring's operator operation
     */
    T plus(T w1, T w2);

    /**
     * Semiring's times operation
     */
    T times(T w1, T w2);

    /**
     * Semiring's zero element
     */
    T zero();

    /**
     * Semiring's one element
     */
    T one();

    /**
     * predicate on set membership.
     *
     * @param m candidate member
     * @return whether m belongs to the semiring's set
     */
    T member(T m);

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
     * @return Whether ⊗ left distributes w.r.t. ⊕
     */
    boolean isLefSemiring();

    /**
     * @return Whether ⊗ right distributes w.r.t. ⊕
     */
    boolean isRighSemiring();

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
    default boolean naturalLess(T w1, T w2) {
        return (this.plus(w1, w2) == w1) && (w1 != w2);
    }


// TODO necessary?
//    /**
//     * Reverse: a mapping endState T to ReverseT s.t.
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
//    T reverse(T w1);
//
//    /**
//     * Semiring's divide operation
//     */
//    T divide(T w1, T w2);
}
