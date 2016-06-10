/*
 * $Id: Edge.java 1806 2010-02-04 22:36:16Z scott $ 
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package org.leibnizcenter.earleyparser.earley;

import com.google.common.collect.ImmutableSet;
import org.leibnizcenter.earleyparser.ParseTree;
import org.leibnizcenter.earleyparser.grammar.DottedRule;
import org.leibnizcenter.earleyparser.grammar.Grammar;
import org.leibnizcenter.earleyparser.grammar.Rule;
import org.leibnizcenter.earleyparser.grammar.Token;
import org.leibnizcenter.earleyparser.grammar.categories.Category;
import org.leibnizcenter.earleyparser.grammar.categories.Terminal;

import java.util.Collections;
import java.util.Set;


/**
 * An edge in a {@link Chart chart} produced by an
 * {@link EarleyParser Earley parser}. Edges consist of a
 * {@link DottedRule dotted rule} paired with an
 * {@link Edge#getOrigin() origin position} within a string.
 * <p>
 * An edge is either <em>active</em> or
 * <em>{@link Edge#isPassive() passive}</em> depending on
 * {@link DottedRule#getPosition() how far} processing has succeeded in the
 * dotted rule. When an edge is passive, parsing has successfully completed the
 * {@link DottedRule#getLeft() left side category} at the edge's origin
 * position within the string being parsed.
 * <p>
 * Edges can be created by {@link Edge#predictFor(Rule, int) prediction} based
 * on a {@link Grammar#getRules(Category) grammar rule}, or by
 * {@link Edge#scan(Edge, Token) scanning} an input token that matches the
 * {@link DottedRule#getActiveCategory()} of some edge's dotted rule.
 * An edge can also be {@link #complete(Edge, Edge) completed} based on another
 * edge, allowing {@link ParseTree parse trees} to trace the derivation of a
 * string based on a {@link Grammar}.
 * Upon {@link Edge#complete(Edge, Edge) creation}, a completed edge advances
 * the {@link DottedRule#getPosition() position} of the edge's
 * {@link DottedRule dotted rule} by <code>1</code>,
 * but maintains the same origin position as the edge. It also maintains
 * {@link #getBases() backpointers} to the edges that were used in completing
 * the new edge.
 * <p>
 * Edges are immutable and can not be altered once they are created. In an
 * Earley parser, edges are only ever added, never removed or changed.
 *
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1806 $
 * @see Chart
 * @see DottedRule
 */
public class Edge {
    public final DottedRule dottedRule;
    public final int origin;
    public ImmutableSet<Edge> bases;

    /**
     * Creates an edge containing the specified dotted rule at the origin
     * position given.
     *
     * @param dottedRule The dotted rule at <code>origin</code>.
     * @param origin     The origin position within the string being parsed.
     * @throws IndexOutOfBoundsException If <code>origin &lt; 0</code>.
     * @see #Edge(DottedRule, int, ImmutableSet)
     */
    public Edge(DottedRule dottedRule, int origin) {
        this(dottedRule, origin, null);
    }

    /**
     * Creates an edge for the specified dotted rule and origin position, with
     * the given set of edges as bases for its completion.
     *
     * @param bases The set of bases, in order, that completed this edge. If
     *              this is <code>null</code>, {@link Collections#emptySet() the empty set}
     *              is used.
     * @throws IndexOutOfBoundsException If <code>origin &lt; 0</code>.
     * @since 0.2
     */
    public Edge(DottedRule dottedRule, int origin, ImmutableSet<Edge> bases) {
        if (origin < 0) {
            throw new IndexOutOfBoundsException("origin < 0: " + origin);
        }

        this.dottedRule = dottedRule;
        this.origin = origin;

        if (bases == null) {
            this.bases = ImmutableSet.of(); // static, doesn't create new
        } else {
            this.bases = bases;
        }
    }

    /**
     * Makes a predicted edge based on the specified rule, with the specified
     * origin position.
     *
     * @param rule   The rule to construct a predicted edge for.
     * @param origin The origin position of the newly predicted edge.
     * @return A new edge whose {@link #getDottedRule() dotted rule} is the
     * specified rule at position <code>0</code>. The new edge's origin is the
     * specified <code>origin</code>.
     * @throws NullPointerException If <code>rule</code> is <code>null</code>.
     */
    public static Edge predictFor(Rule rule, int origin) {
        if (rule == null) {
            throw new NullPointerException("null rule");
        }

        return new Edge(new DottedRule(rule), origin);
    }

    /**
     * Creates an edge based on the given edge and the token that was just
     * scanned.
     *
     * @param edge  The edge whose active category is the just-scanned token.
     * @param token The just-scanned token.
     * @return A new edge just like the specified edge (including
     * {@link #getOrigin() origin}), but with its rule's
     * {@link DottedRule#getPosition() dot position} advanced by one. The new
     * edge's {@link #getBases() bases} incorporates the old edge and all of
     * its bases.
     * @throws NullPointerException     If <code>edge</code> or <code>token</code>
     *                                  is <code>null</code>.
     * @throws IllegalArgumentException In any of the following cases:
     *                                  <ol>
     *                                  <li>The specified <code>edge</code> is
     *                                  {@link #isPassive() passive}.</li>
     *                                  <li>The specified <code>edge</code>'s
     *                                  {@link #getDottedRule() dotted rule}'s
     *                                  {@link DottedRule#getActiveCategory() active category} is not a
     *                                  {@link Terminal terminal}.</li>
     *                                  <li>The <code>edge</code>'s rule's active category's
     *                                  {@link Category#getName() name} is not equal to the scanned
     *                                  <code>token</code>.</li>
     *                                  </ol>
     * @since 0.4
     */
    public static <T> Edge scan(Edge edge, Token<T> token) {
        if (edge == null) throw new NullPointerException("null edge");
        if (token == null) throw new NullPointerException("null input token");
        if (edge.isPassive()) throw new IllegalArgumentException("passive edge");

        DottedRule dr = edge.dottedRule;

        if (!(dr.activeCategory instanceof Terminal)) throw new IllegalArgumentException(
                "edge's active category is nonterminal: " + edge);

        if (!((Terminal<T>) dr.activeCategory).hasCategory(token)) {
            throw new IllegalArgumentException("token " + token
                    + " incompatiable with " + edge);
        }

        Edge scanEdge = new Edge(DottedRule.advanceDot(dr), edge.origin);
        scanEdge.bases = Edge.addBasisEdge(edge.bases, edge);

        return scanEdge;
    }

    /**
     * Completes the specified edge based on the specified basis.
     *
     * @param toComplete The edge to complete.
     * @param basis      The basis on which this edge is being completed. This edge
     *                   will be added to the set of {@link #getBases() bases} already in the
     *                   edge, if any are present.
     * @return A new edge exactly like this one, except that its
     * {@link #getDottedRule() dotted rule}'s position is advanced by
     * <code>1</code> and its bases contains <code>basis</code>.
     * @throws NullPointerException     if <code>toComplete</code> or
     *                                  <code>basis</code> is <code>null</code>.
     * @throws IllegalArgumentException If the specified basis is not a
     *                                  suitable edge for completing this edge. Reasons for this exception are
     *                                  that the basis edge:
     *                                  <ul>
     *                                  <li>has a {@link #getDottedRule() dotted rule} whose
     *                                  {@link DottedRule#getPosition() position} is <code>0</code>
     *                                  (meaning that no completion has actually taken place)</li>
     *                                  <li>has a dotted rule whose {@link DottedRule#getLeft() left}
     *                                  category does not equal this edge's dotted rule's
     *                                  {@link DottedRule#getActiveCategory() active category}.</li>
     *                                  </ul>
     * @see DottedRule#advanceDot(DottedRule)
     * @since 0.2
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
        if (basis.dottedRule.position == 0 ||
                !basis.dottedRule.left.equals(
                        toComplete.dottedRule.activeCategory)) {
            throw new IllegalArgumentException(toComplete +
                    " is not completed by basis " + basis);
        }

        ImmutableSet<Edge> newBases = Edge.addBasisEdge(toComplete.bases, basis);

        return new Edge(DottedRule.advanceDot(toComplete.dottedRule),
                toComplete.origin, newBases);
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

    /**
     * Gets this edge's dotted rule.
     *
     * @return The dotted rule specified when this edge was created.
     */
    public DottedRule getDottedRule() {
        return dottedRule;
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
     * @since 0.2
     */
    public Set<Edge> getBases() {
        return bases;
    }

    /**
     * Tests whether this is a passive edge or not. An edge is passive when
     * its dotted rule contains no
     * {@link DottedRule#getActiveCategory() active category}.
     *
     * @return <code>true</code> iff the active category of this edge's dotted
     * rule is <code>null</code>.
     */
    public boolean isPassive() {
        return (dottedRule.activeCategory == null);
    }

    /**
     * Tests whether this edge is equal to another edge by comparing their
     * dotted rules, origin positions, and {@link #getBases() basis edges}.
     *
     * @return <code>true</code> iff the given object is an instance of
     * <code>Edge</code> and its dotted rule, origin, and bases are equal to
     * this edge's dotted rule, origin, and bases.
     * @see DottedRule#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Edge) {
            Edge oe = (Edge) obj;
            return (origin == oe.origin
                    && dottedRule.equals(oe.dottedRule)
                    && bases.equals(oe.bases));
        }

        return false;
    }

    /**
     * Computes a hash code for this edge based on its dotted rule,
     * origin position, and bases.
     *
     * @see DottedRule#hashCode()
     */
    @Override
    public int hashCode() {
        return ((37 + origin) * dottedRule.hashCode()
                * (1 + bases.hashCode()));
    }

    /**
     * Gets a string representation of this edge.
     *
     * @return &quot;0[S -> NP * VP]&quot; for an edge at origin <code>0</code>
     * and dotted rule <code>S -> NP * VP</code>.
     * @see DottedRule#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Integer.toString(origin));
        sb.append('[');
        sb.append(dottedRule.toString());
        sb.append(']');
        return sb.toString();
    }
}