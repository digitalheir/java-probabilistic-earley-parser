/*
 * $Id: EarleyParserTest.java 1805 2010-02-03 22:37:31Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep;

import static edu.osu.ling.pep.PepFixture.*;
import static edu.osu.ling.pep.Status.ACCEPT;
import static edu.osu.ling.pep.Status.ERROR;
import static edu.osu.ling.pep.Status.REJECT;
import static edu.osu.ling.pep.earley.ParserOption.IGNORE_TERMINAL_CASE;
import static edu.osu.ling.pep.earley.ParserOption.PREDICT_FOR_PRETERMINALS;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import edu.osu.ling.pep.earley.Chart;
import edu.osu.ling.pep.earley.EarleyParser;
import edu.osu.ling.pep.earley.Edge;
import edu.osu.ling.pep.grammar.*;
import edu.osu.ling.pep.grammar.categories.Category;
import org.junit.Assert;


/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1805 $
 */
public class EarleyParserTest implements ParserListener {
    private Parse parse;

    private static EarleyParser<String> earleyParser = new EarleyParser<>(grammar);
    private static Grammar emptyGrammar = new Grammar("empty", ImmutableMultimap.of());

    public final void testGetGrammar() {
        Assert.assertEquals(grammar, earleyParser.getGrammar());
    }


    public final void testRecognize() throws PepException {
        Assert.assertEquals(ACCEPT, earleyParser.recognize(tokens, seed));
    }

    /**
     * Test method for {@link EarleyParser#parse(Iterable, Category)}.
     */
    public final void testParse() throws PepException {
        parse = earleyParser.parse(tokens, S);
        Assert.assertEquals(ACCEPT, parse.getStatus());

        // make sure this rejects but does not cause errors
        earleyParser = new EarleyParser<>(emptyGrammar);
        parse = earleyParser.parse(tokens, S);
        Assert.assertEquals(REJECT, parse.getStatus());
        earleyParser = new EarleyParser<>(grammar);

        // see that this makes no difference
        earleyParser.setOption(PREDICT_FOR_PRETERMINALS, true);
        parse = earleyParser.parse(tokens, S);
        Assert.assertEquals(ACCEPT, parse.getStatus());

        // change string to "the the left"
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"the", "the", "left"}), S);
        Assert.assertEquals(REJECT, parse.getStatus());

        // change string to "girl the left"
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"girl", "the", "left"}), S);
        Assert.assertEquals(REJECT, parse.getStatus());

        // change string to "left the girl"
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"left", "the", "girl"}), S);
        Assert.assertEquals(REJECT, parse.getStatus());

        // change string to "the the the"
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"the", "the", "the"}), S);
        Assert.assertEquals(REJECT, parse.getStatus());

        // change string to "the boy" but seed to NP
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"the", "boy"}), NP);
        Assert.assertEquals(ACCEPT, parse.getStatus());

        // change string to "left" but seed to VP
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"left"}), VP);
        Assert.assertEquals(ACCEPT, parse.getStatus());

        // change string to "boy" but seed to N
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"boy"}), N);
        Assert.assertEquals(ACCEPT, parse.getStatus());

        // change string to "the girl left"
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"the", "girl", "left"}), S);
        Assert.assertEquals(ACCEPT, parse.getStatus());

        // change string to "the <null> left"
        earleyParser = new EarleyParser<>(earleyParser.grammar, this);
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"the", null, "left"}), S);
        Assert.assertEquals(ERROR, parse.getStatus());

        // test option for ignoring preterminal case

        earleyParser.setOption(IGNORE_TERMINAL_CASE, false);
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"THE", "BOY", "LEFT"}), S);
        Assert.assertEquals(REJECT, parse.getStatus());

        earleyParser = new EarleyParser<>(grammarCaseInsensitive, this);
        earleyParser.setOption(IGNORE_TERMINAL_CASE, true);
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"THE", "BOY", "LEFT"}), S);
        Assert.assertEquals(ACCEPT, parse.getStatus());

        try {
            parse = earleyParser.parse(Arrays.asList(new String[]{}), S);
            Assert.fail("parse with no tokens without error");
        } catch (PepException ignored) {
        }

        // change string to "the '' left"
        try {
            parse = earleyParser.parse(
                    Arrays.asList(new String[]{"the", "", "left"}), S);
        } catch (PepException pe) {
            Assert.fail("parsing for empty token threw exception: " + pe);
        }

        Assert.assertEquals(REJECT, parse.getStatus());

        // same thing, only with listener method
        earleyParser.listener = this;
        parse = earleyParser.parse(
                Arrays.asList(new String[]{"the", "", "left"}), S);

        Assert.assertEquals(REJECT, parse.getStatus());

        // add epsilon production
        grammar = new Grammar.Builder()
                .addRules(grammar.getAllRules())
                .addRule(new Rule(N, new ExactStringTerminal("")))
                .build();
        earleyParser.listener = null;
        try {
            parse = earleyParser.parse(
                    Arrays.asList(new String[]{"the", "", "left"}), S);
        } catch (PepException pe) {
            Assert.fail("parsing for empty token threw exception: " + pe);
        }

        Assert.assertEquals(ACCEPT, parse.getStatus());

        // test for mixed terminal/nonterminal grammar
        earleyParser.grammar = (mixed);
        for (String s : new String[]{"a a", "b a", "a b a"}) {
            try {
                parse = earleyParser.parse(Tokens.tokenize(s, " "), A);
            } catch (PepException pe) {
                Assert.fail("parsing " + s + " threw exception: " + pe);
            }

            Assert.assertEquals(ACCEPT, parse.getStatus());
        }

        try {
            parse = earleyParser.parse(Tokens.tokenize("a b", " "), A);
        } catch (PepException pe) {
            Assert.fail("parsing a b threw exception: " + pe);
        }

        Assert.assertEquals(REJECT, parse.getStatus());
    }

    /**
     * Test method for {@link EarleyParser#predict(Chart, int)}.
     */
    public final void testPredict() {
        Chart chart = new Chart();
        chart.addEdge(0, new Edge(DottedRule.startRule(seed), 0));

        earleyParser.predict(chart, 0);

        Set<Edge> zeroEdges = chart.getEdges(0);
        Assert.assertTrue("rule S -> NP VP not predicted",
                zeroEdges.contains(
                        new Edge(new DottedRule(new Rule(S, NP, VP)), 0)));
        Assert.assertTrue("rule NP -> Det N not predicted",
                zeroEdges.contains(
                        new Edge(new DottedRule(new Rule(NP, Det, N)), 0)));
    }

    /**
     * Test method for {@link EarleyParser#scan(Chart, int, Token)}.
     */
    public final void testScan() throws PepException {
        Chart chart = new Chart();
        chart.addEdge(0, new Edge(DottedRule.startRule(seed), 0));

        earleyParser.predict(chart, 0);
        Token zeroToken = tokens.get(0);
        earleyParser.scan(chart, 0, zeroToken);
        Set<Edge> zeroEdges = chart.getEdges(1);
        Edge scanEdge = new Edge(new DottedRule(new Rule(Det, the), 0), 0);
        scanEdge = Edge.scan(scanEdge, zeroToken);

        Assert.assertTrue("passive edge Det -> the not scanned",
                zeroEdges.contains(scanEdge));
    }

    /**
     * Test method for {@link EarleyParser#complete(Chart, int)}.
     */
    public final void testComplete() throws PepException {
        Chart chart = new Chart();
        chart.addEdge(0, new Edge(DottedRule.startRule(seed), 0));

        earleyParser.predict(chart, 0);
        earleyParser.scan(chart, 0, tokens.get(0));
        earleyParser.complete(chart, 1);

        Edge expected = new Edge(new DottedRule(new Rule(NP, Det, N), 1), 0);
        for (Edge e : chart.getEdges(1)) {
            if (e.dottedRule.equals(expected.dottedRule)
                    && e.origin == expected.origin) {
                return;
            }
        }

        Assert.fail("rule NP -> Det * N not completed");
    }

    @SuppressWarnings("unused")
    public void edgeCompleted(EdgeEvent edgeEvent) {
    }

    @SuppressWarnings("unused")
    public void edgePredicted(EdgeEvent edgeEvent) {
    }

    public void parseComplete(ParseEvent parseEvent) {
        parse = parseEvent.parse;
    }

    @SuppressWarnings("unused")
    public void parseMessage(ParseEvent parseEvent, String message) {
        System.err.println(message);
    }

    @SuppressWarnings("unused")
    public void parseError(ParseErrorEvent parseErrorEvent)
            throws PepException {
    }

    @SuppressWarnings("unused")
    public void parserSeeded(EdgeEvent edgeEvent) {
    }

    @SuppressWarnings("unused")
    public void edgeScanned(EdgeEvent tokenEvent) {
    }

    @SuppressWarnings("unused")
    public void optionSet(ParserOptionEvent optionEvent) {
    }
}
