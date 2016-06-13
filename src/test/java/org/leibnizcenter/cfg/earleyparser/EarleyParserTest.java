
package org.leibnizcenter.cfg.earleyparser;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.event.EdgeEvent;
import org.leibnizcenter.cfg.earleyparser.event.ParseErrorEvent;
import org.leibnizcenter.cfg.earleyparser.event.ParseEvent;
import org.leibnizcenter.cfg.earleyparser.event.ParserListener;
import org.leibnizcenter.cfg.earleyparser.exception.PepException;
import org.leibnizcenter.cfg.earleyparser.parse.*;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.Tokens;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;
import static org.leibnizcenter.cfg.earleyparser.parse.Status.ACCEPT;
import static org.leibnizcenter.cfg.earleyparser.parse.Status.REJECT;


/**
 */
public class EarleyParserTest implements ParserListener {
    private static Grammar emptyGrammar = new Grammar("empty", ImmutableMultimap.of());
    private Parse parse;


// TODO test
//    @Test public final void testRecognize() throws PepException {
//        Assert.assertEquals(ACCEPT, earleyParser.recognize(tokens, seed));
//    }

    /**
     * Test method for {@link ParseTokens#parse()}.
     */
    @Test
    public final void testParse() throws PepException {
        ParseTokens<String> earleyParser = new ParseTokens<>(grammar, S, tokens, new ParserOptions(), this);
        parse = earleyParser.parse();
        Assert.assertEquals(ACCEPT, parse.getStatus());

        // make sure this rejects but does not cause errors
        earleyParser = new ParseTokens<>(emptyGrammar, S, tokens, new ParserOptions(), this);
        parse = earleyParser.parse();
        Assert.assertEquals(REJECT, parse.getStatus());

        // see that this makes no difference
        ParserOptions options = new ParserOptions().set(ParserOption.PREDICT_FOR_PRETERMINALS, true);
        earleyParser = new ParseTokens<>(grammar, S, tokens, options, this);
        parse = earleyParser.parse();
        Assert.assertEquals(ACCEPT, parse.getStatus());

        // change string to "the the left"
        assertParseStatus(REJECT, "the", "the", "left");

        // change string to "girl the left"
        assertParseStatus(REJECT, "left", "the", "girl");

        // change string to "the the the"
        assertParseStatus(REJECT, "the", "the", "the");

        // change string to "the boy" but seed to NP

        assertParseStatus(ACCEPT, NP, "the", "boy");

        // change string to "left" but seed to VP
        assertParseStatus(ACCEPT, VP, "left");

        // change string to "boy" but seed to N
        assertParseStatus(ACCEPT, N, "boy");

        // change string to "the girl left"
        assertParseStatus(ACCEPT, "the", "girl", "left");

        // TODO
//        // change string to "the <null> left"
//        earleyParser = new EarleyParser<>(earleyParser.grammar, this);
//        parse = earleyParser.parse(
//                Arrays.asList(new String[]{"the", null, "left"}), S);
//        Assert.assertEquals(ERROR, parse.getStatus());

        // test option for ignoring preterminal case

        assertParseStatus(REJECT, "THE", "BOY", "LEFT");

        earleyParser = new ParseTokens<>(grammarCaseInsensitive, S, Tokens.tokenize("THE", "BOY", "LEFT"), new ParserOptions(), this);
        parse = earleyParser.parse();
        Assert.assertEquals(ACCEPT, parse.getStatus());

        //TODO
//        try {
//            parse = earleyParser.parse(Arrays.asList(new String[]{}), S);
//            Assert.fail("parse with no tokens without error");
//        } catch (PepException ignored) {
//        }
//
//        // change string to "the '' left"
//        try {
//            parse = earleyParser.parse(
//                    Arrays.asList(new String[]{"the", "", "left"}), S);
//        } catch (PepException pe) {
//            Assert.fail("parsing for empty token threw exception: " + pe);
//        }
//
//        Assert.assertEquals(REJECT, parse.getStatus());
//
//        // same thing, only with listener method
//        earleyParser.listener = this;
//        parse = earleyParser.parse(
//                Arrays.asList(new String[]{"the", "", "left"}), S);
//
//        Assert.assertEquals(REJECT, parse.getStatus());
//
//        // add epsilon production
//        grammar = new Grammar.Builder()
//                .addRules(grammar.getAllRules())
//                .addRule(new Rule(N, new ExactStringTerminal("")))
//                .build();
//        earleyParser.listener = null;
//        try {
//            parse = earleyParser.parse(
//                    Arrays.asList(new String[]{"the", "", "left"}), S);
//        } catch (PepException pe) {
//            Assert.fail("parsing for empty token threw exception: " + pe);
//        }
//
//        Assert.assertEquals(ACCEPT, parse.getStatus());
//
//        // test for mixed terminal/nonterminal grammar
//        earleyParser.grammar = (mixed);
//        for (String s : new String[]{"a a", "b a", "a b a"}) {
//            try {
//                parse = earleyParser.parse(Tokens.tokenize(s, " "), A);
//            } catch (PepException pe) {
//                Assert.fail("parsing " + s + " threw exception: " + pe);
//            }
//
//            Assert.assertEquals(ACCEPT, parse.getStatus());
//        }
//
//        try {
//            parse = earleyParser.parse(Tokens.tokenize("a b", " "), A);
//        } catch (PepException pe) {
//            Assert.fail("parsing a b threw exception: " + pe);
//        }
//
//        Assert.assertEquals(REJECT, parse.getStatus());
    }

    private void assertParseStatus(Status status, String... sentence) throws PepException {
        assertParseStatus(status, S, sentence);
    }

    private void assertParseStatus(Status status, Category goal, String... sentence) throws PepException {
        ParseTokens<String> earleyParser;
        Iterable<Token<String>> tokenz = Tokens.tokenize(sentence);
        earleyParser = new ParseTokens<>(grammar, goal, tokenz, new ParserOptions(), this);
        parse = earleyParser.parse();
        Assert.assertEquals(status, parse.getStatus());
    }

//    /**
//     * Test method for {@link EarleyParser#predict(Chart, int)}.
//     */
//    @Test public final void testPredict() {
//TODO
//        Chart chart = new Chart();
//        chart.addEdge(0, new Edge(Rule.startRule(seed), 0, 0));
//
//        earleyParser.predict(chart, 0);
//
//        Set<Edge> zeroEdges = chart.getEdges(0);
//        Assert.assertTrue("rule S -> NP VP not predicted",
//                zeroEdges.contains(
//                        new Edge(new Rule(S, NP, VP), 0, 0)));
//        Assert.assertTrue("rule NP -> Det N not predicted",
//                zeroEdges.contains(
//                        new Edge(new Rule(NP, Det, N), 0, 0)));
//    }
//
//    /**
//     * Test method for {@link EarleyParser#scan(Chart, int, Token)}.
//     */
//    @Test public final void testScan() throws PepException {
//        Chart chart = new Chart();
//        chart.addEdge(0, new Edge(Rule.startRule(seed), 0, 0));
//
//        earleyParser.predict(chart, 0);
//        Token zeroToken = tokens.get(0);
//        earleyParser.scan(chart, 0, zeroToken);
//        Set<Edge> zeroEdges = chart.getEdges(1);
//        Edge scanEdge = new Edge(new Rule(Det, the), 0, 0);
//        scanEdge = Edge.scan(scanEdge, zeroToken);
//
//        Assert.assertTrue("passive edge Det -> the not scanned",
//                zeroEdges.contains(scanEdge));
//    }
//
//    /**
//     * Test method for {@link EarleyParser#complete(Chart, int)}.
//     */
//    @Test public final void testComplete() throws PepException {
//        Chart chart = new Chart();
//        chart.addEdge(0, new Edge(Rule.startRule(seed), 0, 0));
//
//        earleyParser.predict(chart, 0);
//        earleyParser.scan(chart, 0, tokens.get(0));
//        earleyParser.complete(chart, 1);
//
//        Edge expected = new Edge(new Rule(NP, Det, N), 1, 0);
//        for (Edge e : chart.getEdges(1)) {
//            if (e.getRule().equals(expected.getRule())
//                    && e.origin == expected.origin) {
//                return;
//            }
//        }
//
//        Assert.fail("rule NP -> Det * N not completed");
//    }

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

}
