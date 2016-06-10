/*
 * $Id: EdgeTest.java 1797 2010-01-29 20:07:16Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package org.leibnizcenter.earleyparser;

import junit.framework.Assert;
import org.leibnizcenter.earleyparser.earley.Edge;
import org.leibnizcenter.earleyparser.grammar.DottedRule;
import org.leibnizcenter.earleyparser.grammar.Rule;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1797 $
 */
public class EdgeTest extends PepFixture {

    public void testEdge() {
        try {
            new Edge(new DottedRule(edge1.dottedRule, 0), -1);
            Assert.fail("able to create edge with origin -1");
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public final void testGetOrigin() {
        Assert.assertEquals(3, edge1.getOrigin());
    }

    public final void testGetDottedRule() {
        Assert.assertEquals(new DottedRule(rule1, 2), edge1.getDottedRule());
    }

    public final void testPredict() {
        Edge pe = Edge.predictFor(rule1, 1);
        Assert.assertEquals(A, pe.dottedRule.left);
        Assert.assertEquals(B, pe.dottedRule.activeCategory);
        Assert.assertFalse(pe.isPassive());
        Assert.assertTrue(1 == pe.origin);

        try {
            Edge.predictFor(null, 0);
            Assert.fail("able to predict for null rule");
        } catch (NullPointerException expected) {
        }

        try {
            Edge.predictFor(rule2, -2);
            Assert.fail("able to predict index < 0");
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    public final void testComplete() {
        Edge completer = new Edge(
                new DottedRule(new Rule(D, Z), 1), edge1.origin);

        try {
            Edge.complete(edge2, null);
            Assert.fail("able to complete with null edge");
        } catch (NullPointerException expected) {
        }

        try {
            Edge.complete(edge1, completer);
        } catch (IllegalArgumentException iae) {
            Assert.fail("complete failed: " + iae.getMessage());
        }

        try {
            Edge.complete(edge2, completer);
            Assert.fail("able to complete " + edge2 + " with unsuitable edge: "
                    + completer);
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * Test method for {@link Edge#getBases()}.
     */
    public final void testGetBases() {
        Edge edge2Completer = new Edge(
                new DottedRule(new Rule(Y, A), 1), edge2.origin),
                ce1Completer = new Edge(
                        new DottedRule(new Rule(Z, B), 1), edge2.origin);

        Edge ce1 = Edge.complete(edge2, edge2Completer);
        Edge ce2 = Edge.complete(ce1, ce1Completer);

        Set<Edge> bases = new LinkedHashSet<Edge>(ce1.bases);
        bases.add(ce1Completer);
        Assert.assertEquals(bases, ce2.getBases());
    }

    /**
     * Test method for {@link Edge#hashCode()}.
     */
    public final void testHashCode() {
        Assert.assertEquals((37 + edge1.origin) * edge1.dottedRule.hashCode()
                * (1 + edge1.bases.hashCode()), edge1.hashCode());
        Assert.assertFalse("hash codes of edge2 and edge3 should not match",
                edge2.hashCode() == edge3.hashCode());
    }

    /**
     * Test method for {@link Edge#isPassive()}.
     */
    public final void testIsPassive() {
        Assert.assertFalse("edge1 passive", edge1.isPassive());
        Assert.assertFalse("edge2 passive", edge2.isPassive());
        Assert.assertTrue("edge3 active", edge3.isPassive());
    }

    /**
     * Test method for {@link Edge#equals(java.lang.Object)}.
     */
    public void testEqualsObject() {
        Edge e = new Edge(edge1.dottedRule, edge1.origin);
        Assert.assertEquals(e, edge1);
        Assert.assertEquals(edge2, edge2);
        Assert.assertNotSame(edge2, edge3);
        Assert.assertFalse(edge2.equals(edge3));

        //TODO
//		e.bases = new LinkedHashSet<Edge>();
//		e.bases.add(edge1);
//		Edge e2 = new Edge(edge1.dottedRule, edge1.origin);
//		e2.bases = new LinkedHashSet<Edge>();
//		e2.bases.add(edge1);
//		Assert.assertEquals(e, e2);
    }

    /**
     * Test method for {@link Edge#toString()}.
     */
    public final void testToString() {
        Assert.assertEquals("3[A -> B C * D E]", edge1.toString());
        Assert.assertEquals("0[X -> * Y Z]", edge2.toString());
        Assert.assertEquals("2[A -> a *]", edge3.toString());
    }

}
