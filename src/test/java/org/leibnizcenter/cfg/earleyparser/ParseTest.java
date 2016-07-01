///*
// * $Id: ParseTest.java 562 2007-08-16 15:16:13Z scott $
// * Copyright (C) 2007 Scott Martin
// *
// * This library is free software; you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as published by the
// * Free Software Foundation; either version 2.1 of the License, or (at your
// * option) any later version. The GNU Lesser General Public License is
// * distributed with this software in the file COPYING.
// * Copyright (C) 2007 Scott Martin (http://www.coffeeblack.org/contact/)
// */
//package org.leibnizcenter.cfg.earleyparser;
//
//import org.junit.Test;
//import org.leibnizcenter.cfg.Grammar;
//import org.leibnizcenter.cfg.category.terminal.StringTerminal;
//import org.leibnizcenter.cfg.earleyparser.parse.Chart;
//import org.leibnizcenter.cfg.earleyparser.chart.State;
//import org.leibnizcenter.cfg.earleyparser.parse.Parse;
//import org.leibnizcenter.cfg.earleyparser.parse.Status;
//import org.leibnizcenter.cfg.rule.Rule;
//import org.leibnizcenter.cfg.token.Token;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.leibnizcenter.cfg.earleyparser.PepFixture.seed;
//
///**
// */
//public class ParseTest {
//
//    private static final Chart chart = new Chart(new Grammar.Builder("").build());
//    private static final List<Token<String>> tokens = new ArrayList<>();
//    private static final Parse<String> parse = new Parse<>(seed, chart);
//
//    static {
//        StringTerminal test = "test"::equals;
//        tokens.add(new Token<>("test"));
//
//        State startState = new State(Rule.startRule(seed), 0, 0);
//
//        State edge = startState.complete(new State(new Rule(seed, test), 1, 1));
//
//        chart.addState(1, edge);
//
//    }
//
//    static {
//        parse.tokens.addAll(tokens);
//    }
//
//
//    /**
//     * Test method for {@link Parse#hashCode()}.
//     */
//    @Test
//    public final void testHashCode() {
//        assertEquals(31 * tokens.hashCode() * chart.hashCode()
//                * seed.hashCode(), parse.hashCode());
//    }
//
//    /**
//     * Test method for {@link Parse#getTokens()}.
//     */
//    @Test
//    public final void testGetTokens() {
//        assertEquals(tokens, parse.getTokens());
//    }
//
//    /**
//     * Test method for {@link Parse#getSeed()}.
//     */
//    @Test
//    public final void testGetSeed() {
//        assertEquals(seed, parse.getSeed());
//    }
//
//    /**
//     * Test method for {@link Parse#getChart()}.
//     */
//    @Test
//    public final void testGetChart() {
//        assertEquals(chart, parse.getChart());
//    }
//
//    /**
//     * Test method for {@link Parse#getStatus()}.
//     */
//    @Test
//    public final void testGetStatus() {
//        assertEquals(Status.ACCEPT, parse.getStatus());
//    }
//
//    /**
//     * Test method for {@link Parse#equals(java.lang.Object)}.
//     */
//    @Test
//    public final void testEqualsObject() {
//        Parse<String> p = new Parse<>(seed, chart);
//        p.tokens.addAll(tokens);
//        assertEquals(p, parse);
//    }
//
//}
