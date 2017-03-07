package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.StringTerminal;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.scan.ScanMode;
import org.leibnizcenter.cfg.earleyparser.scan.TokenNotInLexiconException;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.Tokens;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 */
public class ParserTest {
    // NonTerminals are just wrappers around a string
    private static final NonTerminal S = Category.nonTerminal("S");
    private static final NonTerminal NP = Category.nonTerminal("NP");
    private static final NonTerminal VP = Category.nonTerminal("VP");
    private static final NonTerminal TV = Category.nonTerminal("TV");
    private static final NonTerminal Det = Category.nonTerminal("Det");
    private static final NonTerminal N = Category.nonTerminal("N");
    private static final NonTerminal Mod = Category.nonTerminal("Mod");

    private static final NonTerminal Err = Category.nonTerminal("Error");

    // Token types are realized by implementing Terminal, and implementing hasCategory. This is a functional interface.
    private static final Terminal<String> transitiveVerb = (StringTerminal) token -> token.obj.matches("(hit|chased)");
    // Some utility terminal types are pre-defined:
    private static final Terminal<String> the = new CaseInsensitiveStringTerminal("the");
    private static final Terminal<String> a = new CaseInsensitiveStringTerminal("a");
    private static final Terminal<String> man = new ExactStringTerminal("man");
    private static final Terminal<String> stick = new ExactStringTerminal("stick");
    private static final Terminal<String> with = new ExactStringTerminal("with");
    private static final Terminal<String> period = new ExactStringTerminal(".");


    private final static NonTerminal A = Category.nonTerminal("A");
    private final static NonTerminal B = Category.nonTerminal("B");
    private final static NonTerminal C = Category.nonTerminal("C");
    private final static NonTerminal D = Category.nonTerminal("D");

    private final static Category b = new ExactStringTerminal("b");

    @Test
    public void simpleRecursiveGrammar() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        double p = (0.6);
        double q = (0.4);
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, S, S)
                .build();

        List<Token<String>> tokens = IntStream.range(0, 3).mapToObj(i -> new Token<>("a")).collect(Collectors.toList());

        Chart<String> chart = Parser.parse(S, grammar, tokens);

        // State set 0
        final State s00Sa = new State(Rule.create(sr, p, S, a), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00Sa));
        assertEquals(sr.toProbability(chart.getForwardScore(s00Sa)), 1.0, 0.01);
        assertEquals(sr.toProbability(chart.getInnerScore(s00Sa)), p, 0.00001);

        final State s00SSS = new State(Rule.create(sr, q, S, S, S), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00SSS));
        assertEquals(sr.toProbability(chart.getForwardScore(s00SSS)), q / p, 0.01);
        assertEquals(sr.toProbability(chart.getInnerScore(s00SSS)), q, 0.00001);

        // State set 1
        // scanned
//        new Token<>("a")
        final State s01Sa1 = new State(Rule.create(sr, p, S, a), 1, 0, 1);
//        Assert.assertTrue(chart.getStates(1).contains(s01Sa1));
        assertEquals(sr.toProbability(chart.getForwardScore(s01Sa1)), 1, 0.01);
        assertEquals(sr.toProbability(chart.getInnerScore(s01Sa1)), p, 0.0001);

        // completed
        final State s01SSS1 = new State(Rule.create(sr, q, S, S, S), 1, 0, 1);
        Assert.assertTrue(chart.getStates(1).contains(s01SSS1));
        assertEquals(sr.toProbability(chart.getForwardScore(s01SSS1)), q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s01SSS1)), p * q, 0.0001);

        // predicted
        final State s11Sa0 = new State(Rule.create(sr, p, S, a), 1, 1, 0);
        Assert.assertTrue(chart.getStates(1).contains(s11Sa0));
        assertEquals(sr.toProbability(chart.getForwardScore(s11Sa0)), q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s11Sa0)), p, 0.0001);
        final State s11SSS0 = new State(Rule.create(sr, q, S, S, S), 1, 1, 0);
        Assert.assertTrue(chart.getStates(1).contains(s11SSS0));
        assertEquals(sr.toProbability(chart.getForwardScore(s11SSS0)), Math.pow(q, 2) / p, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s11SSS0)), q, 0.0001);

        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, q, S, S, S), 1, 0, 1)));
        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, q, S, S, S), 1, 1, 0)));

        // State set 2
        // scanned
//        new Token<>("a"),
        final State s12Sa1 = new State(Rule.create(sr, p, S, a), 2, 1, 1);
        Assert.assertTrue(chart.getStates(2).contains(s12Sa1));
        assertEquals(sr.toProbability(chart.getForwardScore(s12Sa1)), q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s12Sa1)), p, 0.0001);
        // completed
        final State s12SSS1 = new State(Rule.create(sr, q, S, S, S), 2, 1, 1);
        Assert.assertTrue(chart.getStates(2).contains(s12SSS1));
        assertEquals(sr.toProbability(chart.getForwardScore(s12SSS1)), q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s12SSS1)), p * q, 0.0001);

        final State s02SSS2 = new State(Rule.create(sr, q, S, S, S), 2, 0, 2);
        Assert.assertTrue(chart.getStates(2).contains(s02SSS2));
        assertEquals(sr.toProbability(chart.getForwardScore(s02SSS2)), p * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s02SSS2)), p * p * q, 0.0001);

        final State s02SSS1 = new State(Rule.create(sr, q, S, S, S), 2, 0, 1);
        Assert.assertTrue(chart.getStates(2).contains(s02SSS1));
        assertEquals(sr.toProbability(chart.getForwardScore(s02SSS1)), p * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s02SSS1)), p * p * q * q, 0.0001);

        final State s02S1 = new State(Rule.create(sr, Category.START, S), 2, 0, 1);
        Collection<State> states2 = chart.getStates(2);
        Assert.assertTrue(states2.contains(s02S1));
        assertEquals(sr.toProbability(chart.getForwardScore(s02S1)), p * p * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s02S1)), p * p * q, 0.0001);
        // predicted
        final State s22S0 = new State(Rule.create(sr, p, S, a), 2, 2, 0);
        Assert.assertTrue(chart.getStates(2).contains(s22S0));
        assertEquals(sr.toProbability(chart.getForwardScore(s22S0)), (1 + p) * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s22S0)), p, 0.0001);

        final State s22SS0 = new State(Rule.create(sr, q, S, S, S), 2, 2, 0);
        Assert.assertTrue(chart.getStates(2).contains(s22SS0));
        assertEquals(sr.toProbability(chart.getForwardScore(s22SS0)), (1 + 1 / p) * q * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s22SS0)), q, 0.0001);

        // State set 3
        // scanned
        //new Token<>("a"),
        final State s23Sa1 = new State(
                Rule.create(sr, p, S, a), 3, 2, 1);
        Assert.assertTrue(chart.getStates(3).contains(s23Sa1));
        assertEquals(sr.toProbability(chart.getForwardScore(s23Sa1)), (1 + p) * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s23Sa1)), p, 0.0001);

        // completed
        final State s23S1 = new State(Rule.create(sr, q, S, S, S),
                3, 2, 1
        );
        Assert.assertTrue(chart.getStates(3).contains(s23S1));
        assertEquals(sr.toProbability(chart.getForwardScore(s23S1)), (1 + p) * q * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s23S1)), p * q, 0.0001);

        final State s13S2 = new State(Rule.create(sr, q, S, S, S), 3, 1, 2);
        Assert.assertTrue(chart.getStates(3).contains(s13S2));
        assertEquals(sr.toProbability(chart.getForwardScore(s13S2)), p * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s13S2)), p * p * q, 0.0001);

        final State s13S1 = new State(Rule.create(sr, q, S, S, S), 3, 1, 1);
        Assert.assertTrue(chart.getStates(3).contains(s13S1));
        assertEquals(sr.toProbability(chart.getForwardScore(s13S1)), p * q * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s13S1)), p * p * q * q, 0.0001);

        final State s03S2 = new State(Rule.create(sr, q, S, S, S), 3, 0, 2);
        Assert.assertTrue(chart.getStates(3).contains(s03S2));
        assertEquals(sr.toProbability(chart.getForwardScore(s03S2)), 2 * p * p * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s03S2)), 2 * p * p * p * q * q, 0.0001);

        final State s03S1 = new State(Rule.create(sr, q, S, S, S), 3, 0, 1);
        Assert.assertTrue(chart.getStates(3).contains(s03S1));
        assertEquals(sr.toProbability(chart.getForwardScore(s03S1)), 2 * p * p * q * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s03S1)), 2 * p * p * p * q * q * q, 0.0001);

        final State s33S1 = new State(Rule.create(sr, Category.START, S), 3, 0, 1);
        final Set<State> states3s = chart.getStates(3);
        Assert.assertTrue(states3s.contains(s33S1));
        assertEquals(sr.toProbability(chart.getForwardScore(s33S1)), 2 * (Math.pow(p, 3) * Math.pow(q, 2)), 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s33S1)), 2 * (Math.pow(p, 3) * Math.pow(q, 2)), 0.0001);


        assertEquals(
                2.0,
                chart.getStates(0).stream().mapToDouble(s -> chart.getViterbiScore(s).getProbability()).sum(),
                0.0000001
        );

        for (int j = 0; j <= tokens.size(); j++) {
            chart.getStates(j).forEach(s -> {
                // double probFw = sr.toProbability(chart.getForwardScore(s));
                // double probInn = sr.toProbability(chart.getInnerScore(s));
                // double v = 0.0;
                final State.ViterbiScore viterbiScore = chart.getViterbiScore(s);
                assertNotNull(viterbiScore);
                // v = sr.toProbability(viterbiScore.getScore());
                // System.out.println(s + "[" + probFw + "]" + "[" + probInn + "] value: " + v);
            });
        }

        Collection<State> howMany = chart.stateSets.completedStates.getCompletedStates(tokens.size(), Category.START);
        assertEquals(howMany.size(), 1);
        State finalState = howMany.iterator().next();
        ParseTree viterbi = Parser.getViterbiParse(finalState, chart);

        // 1 of 2 options
        assertEquals(
                Math.pow(p, 3) * Math.pow(q, 2),
                chart.getViterbiScore(finalState).getProbability(),
                0.00000000001
        );
    }

    @Test
    public void simpleGrammar2() throws Exception {
        double p = (0.6);
        double q = (0.4);
        // b surrounded by a's, or a single a
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a)
                .addRule(q, S, S, b, S)
                .build();
        List<Token<String>> tokens1 = Tokens.tokenize("a");
        ParseTreeWithScore parse1 = Parser.getViterbiParseWithScore(S, grammar, tokens1);
        assertEquals(0.6, parse1.getProbability(), 0.0000000001);

        List<Token<String>> tokens2 = Tokens.tokenize("a  b a b a");
        ParseTreeWithScore parse2 = Parser.getViterbiParseWithScore(S, grammar, tokens2);

        Assert.assertEquals(parse2.getProbability(), 0.4 * 0.4 * 0.6 * 0.6 * 0.6, 0.00001);
    }


    @Test
    public void simpleGrammar3() throws Exception {
        double p = (0.6);
        double q = (0.4);
        final LogSemiring sr = LogSemiring.get();
        // a surrounded by b's, or a single a
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, b, S, b)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("b b a b b");
        ParseTreeWithScore parse = Parser.getViterbiParseWithScore(S, grammar, tokens);

        Assert.assertEquals(parse.getProbability(), q * q * p, 0.00001);
    }

    @Test
    public void viterbi() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(1.0, S, A)
                .addRule(0.5, S, S, S)
                .addRule(0.5, A, a)
                .build();

        List<Token<String>> tokens = Tokens.tokenize("a", "a", "a");
        ParseTreeWithScore parse = Parser.getViterbiParseWithScore(S, grammar, tokens);

        Assert.assertNotNull(parse);
        assertEquals(parse.getProbability(), Math.pow(0.5, 5), 0.0001);
    }

    @Test
    public void viterbi2() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(1.0, S, A)
                .addRule(0.1, S, S, S)
                .addRule(1.0, A, B)
                .addRule(0.5, A, D)
                .addRule(0.5, B, C)
                .addRule(1.0, B, a)
                .addRule(0.5, C, D)
                .addRule(0.5, C, a)
                .addRule(0.5, D, a)
                .build();

        List<Token<String>> tokens = Tokens.tokenize("a", "a", "a");
        ParseTreeWithScore parse = Parser.getViterbiParseWithScore(S, grammar, tokens);

        Assert.assertEquals(parse.getProbability(), 0.01, 0.0001);
    }


    @Test
    public void viterbi3() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(1.0, S, A, A)
                .addRule(1.0, A, B)
                .addRule(0.1, A, D)
                .addRule(1.0, B, C)
                .addRule(0.9, D, a, a)
                .addRule(0.9, C, D)
                .addRule(0.9, B, a)
                .addRule(0.5, C, a, a)
                .build();

        List<Token<String>> tokens = Tokens.tokenize("a", "a", "a", "a");
        ParseTreeWithScore parse = Parser.getViterbiParseWithScore(S, grammar, tokens);

        Assert.assertEquals(parse.getProbability(), 0.6561, 0.0001);
    }


    @Test
    public final void ambiguous() {
        final NonTerminal BV = new NonTerminal("BV");
        final Category a = new ExactStringTerminal("a");
        final Category the = new ExactStringTerminal("the");
        final Category right = new ExactStringTerminal("right");
        final Category wrong = new ExactStringTerminal("wrong");
        final Category girl = new ExactStringTerminal("girl");
        final Category left = new ExactStringTerminal("left");
        final NonTerminal S = Category.nonTerminal("S");
        final NonTerminal NP = Category.nonTerminal("NP");
        final NonTerminal VP = Category.nonTerminal("VP");
        final NonTerminal Det = Category.nonTerminal("Det");
        final NonTerminal N = Category.nonTerminal("N");

        double PSVP = 0.9;
        double PSNP = 1 - PSVP;
        Grammar<String> grammar = new Grammar.Builder<String>("test")
                .setSemiring(LogSemiring.get())
                .addRule(PSVP, S, NP, VP)
                .addRule(PSNP, S, NP)
                .addRule(NP, Det, N)
                .addRule(N, BV, N)
                .addRule(VP, left)
                .addRule(BV, left)
                .addRule(BV, wrong)
                .addRule(BV, right)
                .addRule(Det, a)
                .addRule(Det, the)
                .addRule(N, right)
                .addRule(N, left)
                .addRule(N, girl)
                .build();

        // Parsable
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the girl left")), PSVP, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the right left")), PSNP + PSVP, 0.0001); // ambiguous
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the wrong right")), PSNP, 0.0001); // ambiguous
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the right")), PSNP, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the girl")), PSNP, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the right right")), PSNP, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the left right")), PSNP, 0.0001);

        Assert.assertEquals(Parser.recognize(N, grammar, Tokens.tokenize("left girl")), 1.0, 0.0001);
        Assert.assertEquals(Parser.recognize(N, grammar, Tokens.tokenize("left left")), 1.0, 0.0001);
        Assert.assertEquals(Parser.recognize(N, grammar, Tokens.tokenize("wrong left")), 1.0, 0.0001);

        // Unparsable
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("girl left")), 0.0, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the")), 0.0, 0.0001);
    }

    @Test(expected = TokenNotInLexiconException.class)
    public final void unparseable() {


        final Grammar<String> grammar = new Grammar.Builder<String>("test")
                .setSemiring(LogSemiring.get()) // If not set, defaults to Log semiring which is probably what you want
                .addRule(
                        1.0,   // Probability between 0.0 and 1.0, defaults to 1.0. The builder takes care of converting it to the semiring element
                        S,     // Left hand side of the rule
                        NP, VP // Right hand side of the rule
                )
                .addRule(
                        0.5,
                        NP,
                        Det, N // eg. The man
                )
                .addRule(
                        0.5,
                        NP,
                        Det, N, Mod // eg. the man (with a stick)
                )
                .addRule(
                        0.4,
                        VP,
                        TV, NP, Mod // eg. (chased) (the man) (with a stick)
                )
                .addRule(
                        0.6,
                        VP,
                        TV, NP // eg. (chased) (the man with a stick)
                )
                .addRule(Det, the)
                .addRule(N, man)
                .addRule(N, stick)
                .addRule(TV, transitiveVerb)
                .addRule(Mod, with, NP) // eg. with a stick
                .build();
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the notinlexicon left")), 0.0, 0.0001);
    }


    @Test
    public void scanModeDrop() throws Exception {
        double p = (0.6);
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("b a");

        ParseOptions cb = new ParseOptions.Builder<>().withScanMode(ScanMode.DROP).build();
        ParseTreeWithScore parse = new Parser(grammar).getViterbiParseWithScore(S, tokens, cb);
        Assert.assertEquals(p, parse.getProbability(), 0.00001);
    }

    @Test(expected = TokenNotInLexiconException.class)
    public void scanModeStrict() throws Exception {
        double p = (0.6);
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("b a");

        ParseOptions cb = new ParseOptions.Builder<>().withScanMode(ScanMode.STRICT).build();
        ParseTreeWithScore parse = new Parser(grammar).getViterbiParseWithScore(S, tokens, cb);
        Assert.assertEquals(p, parse.getProbability(), 0.00001);
    }

    @Test
    public void scanModeWildcard() throws Exception {
        double p = (0.6);
        final double q = 0.666;
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a, a)
                .addRule(q, S, b, a)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("z a");

        ParseOptions cb = new ParseOptions.Builder<>().withScanMode(ScanMode.WILDCARD).build();
        ParseTreeWithScore parse = new Parser(grammar).getViterbiParseWithScore(S, tokens, cb);
        Assert.assertEquals(q, parse.getProbability(), 0.00001);

        System.out.println(parse);
    }

    @Test
    public void scanModeSynchronizeTokensPre() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(0.8, S, A)
                .addRule(0.7, S, S, A)
                .addRule(0.6, A, a, a, period)
                .addRule(0.5, A, b, b, NonLexicalToken.get(), period)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("a a . b b z a . a a .");

        ParseOptions cb = new ParseOptions.Builder<>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(0.7 * .7 * 0.8 * 0.6 * .5 * .6, parse.getProbability(), 0.00001);
    }

    @Test
    public void scanModeSynchronize() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(0.8, S, A)
                .addRule(0.7, S, S, A)
                .addRule(0.6, A, a, a, period)
                .addRule(0.5, A, NonLexicalToken.get(), period)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("a a . z a . a a .");

        ParseOptions cb = new ParseOptions.Builder<>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(0.7 * .7 * 0.8 * 0.6 * .5 * .6, parse.getProbability(), 0.00001);
    }

    @Test
    public void scanModeSynchronizeSimple() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(1.0, S, A)
                .addRule(0.2, A, NonLexicalToken.get(), period)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("z a .");

        ParseOptions cb = new ParseOptions.Builder<>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(0.2, parse.getProbability(), 0.00001);
    }

    @Test
    public void scanModeSynchronizeSimple2() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(0.2, S, A, A)
                .addRule(0.2, A, NonLexicalToken.get(), period)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("z a . a .");

        ParseOptions cb = new ParseOptions.Builder<>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(0.008, parse.getProbability(), 0.00001);
    }
}