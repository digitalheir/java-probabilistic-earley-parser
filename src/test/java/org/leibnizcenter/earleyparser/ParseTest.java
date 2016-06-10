/*
 * $Id: ParseTest.java 562 2007-08-16 15:16:13Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 * Copyright (C) 2007 Scott Martin (http://www.coffeeblack.org/contact/)
 */
package org.leibnizcenter.earleyparser;

import org.junit.Assert;
import org.leibnizcenter.earleyparser.earley.Chart;
import org.leibnizcenter.earleyparser.earley.Edge;
import org.leibnizcenter.earleyparser.grammar.DottedRule;
import org.leibnizcenter.earleyparser.grammar.Rule;
import org.leibnizcenter.earleyparser.grammar.Token;


/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 562 $
 */
public class ParseTest extends PepFixture {

    Parse parse;
    Chart chart;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        tokens.clear();
        StringTerminal test = "test"::equals;
        tokens.add(new Token<>("test"));

        chart = new Chart();

        Edge startEdge = new Edge(DottedRule.startRule(seed), 0);

        Edge edge = Edge.complete(startEdge,
                new Edge(new DottedRule(new Rule(seed, test), 1), 1));

        chart.addEdge(1, edge);

        parse = new Parse(seed, chart);
        parse.tokens = tokens;
    }

    /**
     * Test method for {@link Parse#hashCode()}.
     */
    public final void testHashCode() {
        assertEquals(31 * tokens.hashCode() * chart.hashCode()
                * seed.hashCode(), parse.hashCode());
    }

    /**
     * Test method for {@link Parse#getTokens()}.
     */
    public final void testGetTokens() {
        Assert.assertEquals(tokens, parse.getTokens());
    }

    /**
     * Test method for {@link Parse#getSeed()}.
     */
    public final void testGetSeed() {
        Assert.assertEquals(seed, parse.getSeed());
    }

    /**
     * Test method for {@link Parse#getChart()}.
     */
    public final void testGetChart() {
        Assert.assertEquals(chart, parse.getChart());
    }

    /**
     * Test method for {@link Parse#getStatus()}.
     */
    public final void testGetStatus() {
        Assert.assertEquals(Status.ACCEPT, parse.getStatus());
    }

    /**
     * Test method for {@link Parse#equals(java.lang.Object)}.
     */
    public final void testEqualsObject() {
        Parse p = new Parse(seed, chart);
        p.tokens = tokens;
        Assert.assertEquals(p, parse);
    }

}
