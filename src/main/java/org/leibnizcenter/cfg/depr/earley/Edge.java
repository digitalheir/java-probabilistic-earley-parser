//
//package org.leibnizcenter.cfg.earleyparser.parse;
//
//import com.google.common.collect.ImmutableSet;
//import org.leibnizcenter.cfg.Grammar;
//import org.leibnizcenter.cfg.category.Category;
//import org.leibnizcenter.cfg.category.terminal.Terminal;
//import org.leibnizcenter.cfg.rule.Rule;
//import org.leibnizcenter.cfg.token.Token;
//
//import java.util.Collections;
//import java.util.Optional;
//import java.util.Set;
//
//
///**
// * An edge in a {@link Chart chart} produced by an
// * {@link EarleyParser Earley parser}. States consist of a
// * {@link Rule dotted rule} paired with an
// * {@link State#getOrigin() origin position} within a string.
// * <p>
// * An edge is either <em>active</em> or
// * <em>{@link State#isPassive() passive}</em> depending on
// * {@link State#getPosition() how far} processing has succeeded in the
// * dotted rule. When an edge is passive, parsing has successfully completed the
// * {@link Rule#getLeft() left side category} at the edge's origin
// * position within the string being parsed.
// * <p>
// * States can be created by {@link State#predictFor(Rule, int) prediction} based
// * on a {@link Grammar#getRules(Category) grammar rule}, or by
// * {@link State#scan(State, Token) scanning} an input token that matches the
// * {@link State#getActiveCategory()} of some edge's dotted rule.
// * An edge can also be {@link #complete(State, State) completed} based on another
// * edge, allowing {@link ParseTree parse trees} to trace the derivation of a
// * string based on a {@link Grammar}.
// * Upon {@link State#complete(State, State) creation}, a completed edge advances
// * the {@link State#getPosition() position} of the edge's
// * {@link Rule dotted rule} by <code>1</code>,
// * but maintains the same origin position as the edge. It also maintains
// * {@link #getBases() backpointers} to the edges that were used in completing
// * the new edge.
// * <p>
// * States are immutable and can not be altered once they are created. In an
// * Earley parser, edges are only ever added, never removed or changed.
// *
// * @see Chart
// * @see Rule
// */
//public class State {
//    public final Rule rule;
//    public final int origin;
//    public final int position;
////    public final ImmutableSet<State> bases;
//
//    /**
//     * Creates an edge containing the specified dotted rule at the origin
//     * position given.
//     *
//     * @param rule   The dotted rule at <code>origin</code>.
//     * @param origin The origin position within the string being parsed.
//     * @throws IndexOutOfBoundsException If <code>origin &lt; 0</code>.
//     * @see #State(Rule, int, int, ImmutableSet)
//     */
//    public State(Rule rule, int dotPosition, int origin) {
//        this(rule, dotPosition, origin, null);
//    }
//
//    /**
//     * Creates an edge for the specified dotted rule and origin position, with
//     * the given set of edges as bases for its completion.
//     *
//     * @param dotPosition dot position
//     * @param bases       The set of bases, in order, that completed this edge. If
//     *                    this is <code>null</code>, {@link Collections#emptySet() the empty set}
//     *                    is used.
//     * @throws IndexOutOfBoundsException If <code>origin &lt; 0</code>.
//     */
//    public State(Rule rule, int dotPosition, int origin, ImmutableSet<State> bases) {
//        if (origin < 0) throw new IndexOutOfBoundsException("origin < 0: " + origin);
//
//        this.rule = rule;
//        this.origin = origin;
//
////        if (bases == null) this.bases = ImmutableSet.of(); // static, doesn't create new
////        else this.bases = bases;
//
//        this.position = dotPosition;
//    }
//
//


//
//    /**
//     * Gets a string representation of this dotted rule.
//     *
//     * @return &quot;<code>S -> NP * VP</code>&quot; for a dotted rule with
//     * an underlying rule <code>S -> NP VP</code> and a dot position
//     * <code>1</code>.
//     * @see Rule#toString()
//     */
//    public static String toString(Rule r, int position) {
//        StringBuilder sb = new StringBuilder(r.left.toString());
//        sb.append(" ->");
//
//        for (int i = 0; i <= r.right.length; i++) {
//            if (i == position) sb.append(" {*"); // insert dot at position
//            if (i < r.right.length) {
//                sb.append(' '); // space between category
//                sb.append(r.right[i].toString());
//            }
//            if (i == position) sb.append("}");
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * Helper for scan and complete.
//     */
//    static ImmutableSet<State> addBasisState(ImmutableSet<State> bases, State basis) {
//        if (bases.isEmpty()) {
//            return ImmutableSet.of(basis);
//        } else {
//            ImmutableSet.Builder<State> b = new ImmutableSet.Builder<>();
//            b.addAll(bases);
//            b.add(basis);
//            return b.build();
//        }
//    }
//
//    public static Optional<Category> getActiveCategory(State edge) {
//        return Rule.getActiveCategory(edge.rule, edge.position);
//    }
//
//    public int getPosition() {
//        return position;
//    }
//
//    public Optional<Category> getActiveCategory() {
//        return Rule.getActiveCategory(rule, position);
//    }
//
//    /**
//     * Gets this edge's dotted rule.
//     *
//     * @return The dotted rule specified when this edge was created.
//     */
//    public Rule getRule() {
//        return rule;
//    }
//
//    /**
//     * Gets this edge's origin position.
//     *
//     * @return The origin position given for this edge at creation.
//     */
//    public int getOrigin() {
//        return origin;
//    }
//
//    /**
//     * Gets the bases for completion of this edge, in order of insertion. A
//     * completed edge inherits its bases from the edge from which it is created.
//     *
//     * @return If this edge was completed based on other edges, those edges
//     * are returned in their order of insertion. Otherwise,
//     * {@link Collections#emptySet() empty set} is returned.
//     * @see #complete(State, State)
//     */
//    public Set<State> getBases() {
//        return bases;
//    }
//
//    /**
//     * Tests whether this edge is equal to another edge by comparing their
//     * dotted rules, origin positions, and {@link #getBases() basis edges}.
//     *
//     * @return <code>true</code> iff the given object is an instance of
//     * <code>State</code> and its dotted rule, origin, and bases are equal to
//     * this edge's dotted rule, origin, and bases.
//     * @see Rule#equals(Object)
//     */
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        State edge = (State) o;
//
//        if (origin != edge.origin) return false;
//        if (position != edge.position) return false;
//        if (rule != null ? !rule.equals(edge.rule) : edge.rule != null) return false;
//        return bases != null ? bases.equals(edge.bases) : edge.bases == null;
//
//    }
//
//
//    @Override
//    public int hashCode() {
//        int result = rule.hashCode();
//        result = 31 * result + origin;
//        result = 31 * result + position;
//        result = 31 * result + (bases != null ? bases.hashCode() : 0);
//        return result;
//    }
//
//    /**
//     * Gets a string representation of this edge.
//     *
//     * @return &quot;0[S -> NP * VP]&quot; for an edge at origin <code>0</code>
//     * and dotted rule <code>S -> NP * VP</code>.
//     * @see Rule#toString()
//     */
//    @Override
//    public String toString() {
//        return Integer.toString(origin) + '[' +
//                toString(rule, position) +
//                ']';
//    }
//}