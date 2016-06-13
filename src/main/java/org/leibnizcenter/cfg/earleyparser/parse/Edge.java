
package org.leibnizcenter.cfg.earleyparser.parse;

import com.google.common.collect.ImmutableSet;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;


/**
 * An edge in a {@link Chart chart} produced by an
 * {@link EarleyParser Earley parser}. Edges consist of a
 * {@link Rule dotted rule} paired with an
 * {@link Edge#getOrigin() origin position} within a string.
 * <p>
 * An edge is either <em>active</em> or
 * <em>{@link Edge#isPassive() passive}</em> depending on
 * {@link Edge#getPosition() how far} processing has succeeded in the
 * dotted rule. When an edge is passive, parsing has successfully completed the
 * {@link Rule#getLeft() left side category} at the edge's origin
 * position within the string being parsed.
 * <p>
 * Edges can be created by {@link Edge#predictFor(Rule, int) prediction} based
 * on a {@link Grammar#getRules(Category) grammar rule}, or by
 * {@link Edge#scan(Edge, Token) scanning} an input token that matches the
 * {@link Edge#getActiveCategory()} of some edge's dotted rule.
 * An edge can also be {@link #complete(Edge, Edge) completed} based on another
 * edge, allowing {@link ParseTree parse trees} to trace the derivation of a
 * string based on a {@link Grammar}.
 * Upon {@link Edge#complete(Edge, Edge) creation}, a completed edge advances
 * the {@link Edge#getPosition() position} of the edge's
 * {@link Rule dotted rule} by <code>1</code>,
 * but maintains the same origin position as the edge. It also maintains
 * {@link #getBases() backpointers} to the edges that were used in completing
 * the new edge.
 * <p>
 * Edges are immutable and can not be altered once they are created. In an
 * Earley parser, edges are only ever added, never removed or changed.
 *
 * @see Chart
 * @see Rule
 */
public class Edge {
    public final Rule rule;
    public final int origin;
    public final int position;
    public final ImmutableSet<Edge> bases;

    /**
     * Creates an edge containing the specified dotted rule at the origin
     * position given.
     *
     * @param rule   The dotted rule at <code>origin</code>.
     * @param origin The origin position within the string being parsed.
     * @throws IndexOutOfBoundsException If <code>origin &lt; 0</code>.
     * @see #Edge(Rule, int, int, ImmutableSet)
     */
    public Edge(Rule rule, int dotPosition, int origin) {
        this(rule, dotPosition, origin, null);
    }

    /**
     * Creates an edge for the specified dotted rule and origin position, with
     * the given set of edges as bases for its completion.
     *
     * @param dotPosition dot position
     * @param bases       The set of bases, in order, that completed this edge. If
     *                    this is <code>null</code>, {@link Collections#emptySet() the empty set}
     *                    is used.
     * @throws IndexOutOfBoundsException If <code>origin &lt; 0</code>.
     */
    public Edge(Rule rule, int dotPosition, int origin, ImmutableSet<Edge> bases) {
        if (origin < 0) throw new IndexOutOfBoundsException("origin < 0: " + origin);

        this.rule = rule;
        this.origin = origin;

        if (bases == null) this.bases = ImmutableSet.of(); // static, doesn't create new
        else this.bases = bases;

        this.position = dotPosition;
    }

    /**
     * Makes a predicted edge based on the specified rule, with the specified
     * origin position.
     *
     * @param rule   The rule to construct a predicted edge for.
     * @param origin The origin position of the newly predicted edge.
     * @return A new edge whose {@link #getRule() dotted rule} is the
     * specified rule at position <code>0</code>. The new edge's origin is the
     * specified <code>origin</code>.
     * @throws NullPointerException If <code>rule</code> is <code>null</code>.
     */
    public static Edge predictFor(Rule rule, int origin) {
        if (rule == null) throw new NullPointerException("null rule");

        return new Edge(rule, 0, origin);
    }

    /**
     * Creates an edge based on the given edge and the token that was just
     * scanned.
     *
     * @param edge  The edge whose active category is the just-scanned token.
     * @param token The just-scanned token.
     * @return A new edge just like the specified edge (including
     * {@link #getOrigin() origin}), but with its rule's
     * {@link Edge#getPosition() dot position} advanced by one. The new
     * edge's {@link #getBases() bases} incorporates the old edge and all of
     * its bases.
     * @throws NullPointerException     If <code>edge</code> or <code>token</code>
     *                                  is <code>null</code>.
     * @throws IllegalArgumentException In any of the following cases:
     *                                  <ol>
     *                                  <li>The specified <code>edge</code> is
     *                                  {@link #isPassive() passive}.</li>
     *                                  <li>The specified <code>edge</code>'s
     *                                  {@link #getRule() dotted rule}'s
     *                                  {@link Edge#getActiveCategory(Rule, int) active category} is not a
     *                                  {@link Terminal terminal}.</li>
     *                                  <li>The <code>edge</code>'s active category
     *                                  does not match the scanned
     *                                  <code>token</code>.</li>
     *                                  </ol>
     */
    public static <T> Edge scan(Edge edge, Token<T> token) {
        if (edge == null) throw new NullPointerException("null edge");
        if (token == null) throw new NullPointerException("null input token");

        Rule rule = edge.rule;

        Optional<Category> activeCategory = getActiveCategory(edge);
        if (!activeCategory.isPresent()) throw new IllegalArgumentException("passive edge");
        if (!(activeCategory.get() instanceof Terminal)) throw new IllegalArgumentException(
                "edge's active category is nonterminal: " + edge);

        //noinspection unchecked
        if (!((Terminal<T>) activeCategory.get()).hasCategory(token)) {
            throw new IllegalArgumentException("token " + token
                    + " incompatible with " + edge);
        }

        return new Edge(
                rule,
                advanceDot(rule, edge.position),
                edge.origin,
                Edge.addBasisEdge(edge.bases, edge)
        );
    }

    /**
     * Completes the specified edge based on the specified basis.
     *
     * @param toComplete The edge to complete.
     * @param basis      The basis on which this edge is being completed. This edge
     *                   will be added to the set of {@link #getBases() bases} already in the
     *                   edge, if any are present.
     * @return A new edge exactly like this one, except that its
     * {@link #getRule() dotted rule}'s position is advanced by
     * <code>1</code> and its bases contains <code>basis</code>.
     * @throws NullPointerException     if <code>toComplete</code> or
     *                                  <code>basis</code> is <code>null</code>.
     * @throws IllegalArgumentException If the specified basis is not a
     *                                  suitable edge for completing this edge. Reasons for this exception are
     *                                  that the basis edge:
     *                                  <ul>
     *                                  <li>has a {@link #getRule() dotted rule} whose
     *                                  {@link Edge#getPosition() position} is <code>0</code>
     *                                  (meaning that no completion has actually taken place)</li>
     *                                  <li>has a dotted rule whose {@link Rule#getLeft() left}
     *                                  category does not equal this edge's dotted rule's
     *                                  {@link Edge#getActiveCategory() active category}.</li>
     *                                  </ul>
     * @see Edge#advanceDot(Rule, int)
     */
    public static Edge complete(Edge toComplete, Edge basis) {
        if (toComplete == null) {
            throw new NullPointerException("null edge to complete");
        }
        if (toComplete.isPassive()) {
            throw new IllegalArgumentException(
                    "attempt to complete passive edge: " + toComplete);
        }

        if (basis == null) {
            throw new NullPointerException("null basis");
        }
        if (!basis.isPassive()) {
            throw new IllegalArgumentException("basis is active: " + basis);
        }
        if (basis.position == 0 ||
                !basis.rule.left.equals(toComplete.getActiveCategory().get())) {
            throw new IllegalArgumentException(toComplete +
                    " is not completed by basis " + basis);
        }

        ImmutableSet<Edge> newBases = Edge.addBasisEdge(toComplete.bases, basis);

        return new Edge(toComplete.rule, advanceDot(toComplete.rule, toComplete.position),
                toComplete.origin, newBases);
    }

    /**
     * Creates and returns a new dotted rule exactly like the one provided
     * except that its dot position is advanced by
     * <code>1</code>.
     *
     * @param r The dotted rule whose dot position should be advanced.
     * @return A new dotted rule wrapping this rule with its position
     * incremented.
     * @throws IndexOutOfBoundsException If the dotted rule's dot position
     *                                   is already at the end of its right side.
     */
    public static int advanceDot(Rule r, int currentPosition) {
        currentPosition = currentPosition + 1;
        if (currentPosition < 0 || currentPosition > r.right.length) throw new IndexOutOfBoundsException(
                "illegal position: " + currentPosition);
        return currentPosition;
    }

    /**
     * Gets the active category in the underlying rule, if any.
     *
     * @return The category at this dotted rule's
     * dot position in the underlying rule's
     * {@link Rule#getRight() right side category sequence}. If this rule's
     * dot position is already at the end of the right side category sequence,
     * returns <code>null</code>.
     */
    public static Optional<Category> getActiveCategory(Rule r, int position) {
        return (position < r.right.length) ? Optional.of(r.right[position]) : Optional.empty();
    }

    /**
     * Gets a string representation of this dotted rule.
     *
     * @return &quot;<code>S -> NP * VP</code>&quot; for a dotted rule with
     * an underlying rule <code>S -> NP VP</code> and a dot position
     * <code>1</code>.
     * @see Rule#toString()
     */
    public static String toString(Rule r, int position) {
        StringBuilder sb = new StringBuilder(r.left.toString());
        sb.append(" ->");

        for (int i = 0; i <= r.right.length; i++) {
            if (i == position) sb.append(" {*"); // insert dot at position
            if (i < r.right.length) {
                sb.append(' '); // space between category
                sb.append(r.right[i].toString());
            }
            if (i == position) sb.append("}");
        }

        return sb.toString();
    }

    /**
     * Helper for scan and complete.
     */
    static ImmutableSet<Edge> addBasisEdge(ImmutableSet<Edge> bases, Edge basis) {
        if (bases.isEmpty()) {
            return ImmutableSet.of(basis);
        } else {
            ImmutableSet.Builder<Edge> b = new ImmutableSet.Builder<>();
            b.addAll(bases);
            b.add(basis);
            return b.build();
        }
    }

    public static Optional<Category> getActiveCategory(Edge edge) {
        return getActiveCategory(edge.rule, edge.position);
    }

    public int getPosition() {
        return position;
    }

    public Optional<Category> getActiveCategory() {
        return getActiveCategory(rule, position);
    }

    /**
     * Gets this edge's dotted rule.
     *
     * @return The dotted rule specified when this edge was created.
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Gets this edge's origin position.
     *
     * @return The origin position given for this edge at creation.
     */
    public int getOrigin() {
        return origin;
    }

    /**
     * Gets the bases for completion of this edge, in order of insertion. A
     * completed edge inherits its bases from the edge from which it is created.
     *
     * @return If this edge was completed based on other edges, those edges
     * are returned in their order of insertion. Otherwise,
     * {@link Collections#emptySet() empty set} is returned.
     * @see #complete(Edge, Edge)
     */
    public Set<Edge> getBases() {
        return bases;
    }

    /**
     * Tests whether this is a passive edge or not. An edge is passive when
     * its dotted rule contains no
     * {@link Edge#getActiveCategory() active category}.
     *
     * @return <code>true</code> iff the active category of this edge's dotted
     * rule is <code>null</code>.
     */
    public boolean isPassive() {
        return !getActiveCategory().isPresent();
    }

    /**
     * Tests whether this edge is equal to another edge by comparing their
     * dotted rules, origin positions, and {@link #getBases() basis edges}.
     *
     * @return <code>true</code> iff the given object is an instance of
     * <code>Edge</code> and its dotted rule, origin, and bases are equal to
     * this edge's dotted rule, origin, and bases.
     * @see Rule#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Edge) {
            Edge oe = (Edge) obj;
            return (origin == oe.origin
                    && rule.equals(oe.rule)
                    && bases.equals(oe.bases));
        }

        return false;
    }

    /**
     * Computes a hash code for this edge based on its dotted rule,
     * origin position, and bases.
     *
     * @see Rule#hashCode()
     */
    @Override
    public int hashCode() {
        return ((37 + origin) * rule.hashCode()
                * (1 + bases.hashCode()));
    }

    /**
     * Gets a string representation of this edge.
     *
     * @return &quot;0[S -> NP * VP]&quot; for an edge at origin <code>0</code>
     * and dotted rule <code>S -> NP * VP</code>.
     * @see Rule#toString()
     */
    @Override
    public String toString() {
        return Integer.toString(origin) + '[' +
                toString(rule, position) +
                ']';
    }
}