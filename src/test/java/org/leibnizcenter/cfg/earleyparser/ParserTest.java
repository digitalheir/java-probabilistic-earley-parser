package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedTokenState;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ParseTree;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.Tokens;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 */
public class ParserTest {
    private static final NonTerminal S = Category.nonTerminal("S");

    private final static NonTerminal A = Category.nonTerminal("A");
    private final static NonTerminal B = Category.nonTerminal("B");
    private final static NonTerminal C = Category.nonTerminal("C");
    private final static NonTerminal D = Category.nonTerminal("D");

    private final static Category a = new ExactStringTerminal("a");
    private final static Category b = new ExactStringTerminal("b");

    @Test
    public void simpleRecursiveGrammar() throws Exception {
        final LogSemiring sr = new LogSemiring();
        double p = (0.6);
        double q = (0.4);
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, S, S)
                .build();

        List<Token<String>> tokens = IntStream.range(0, 3).mapToObj(i -> new Token<>("a")).collect(Collectors.toList());

        Chart<String> chart = Parser.parse(S, grammar, tokens, null);

        // State set 0
        final State s00Sa = new State(Rule.create(sr, p, S, a), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00Sa));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s00Sa)), 1.0, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s00Sa)), p, 0.00001);

        final State s00SSS = new State(Rule.create(sr, q, S, S, S), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00SSS));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s00SSS)), q / p, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s00SSS)), q, 0.00001);

        // State set 1
        // scanned
        final State s01Sa1 = new ScannedTokenState<>(new Token<>("a"), Rule.create(sr, p, S, a), 0, 1, 1);
        Assert.assertTrue(chart.getStates(1).contains(s01Sa1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s01Sa1)), 1, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s01Sa1)), p, 0.0001);

        // completed
        final State s01SSS1 = new State(Rule.create(sr, q, S, S, S), 1, 0, 1);
        Assert.assertTrue(chart.getStates(1).contains(s01SSS1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s01SSS1)), q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s01SSS1)), p * q, 0.0001);

        // predicted
        final State s11Sa0 = new State(Rule.create(sr, p, S, a), 1, 1, 0);
        Assert.assertTrue(chart.getStates(1).contains(s11Sa0));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s11Sa0)), q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s11Sa0)), p, 0.0001);
        final State s11SSS0 = new State(Rule.create(sr, q, S, S, S), 1, 1, 0);
        Assert.assertTrue(chart.getStates(1).contains(s11SSS0));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s11SSS0)), Math.pow(q, 2) / p, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s11SSS0)), q, 0.0001);

        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, q, S, S, S), 1, 0, 1)));
        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, q, S, S, S), 1, 1, 0)));

        // State set 2
        // scanned
        final State s12Sa1 = new ScannedTokenState<>(new Token<>("a"), Rule.create(sr, p, S, a), 1, 2, 1);
        Assert.assertTrue(chart.getStates(2).contains(s12Sa1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s12Sa1)), q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s12Sa1)), p, 0.0001);
        // completed
        final State s12SSS1 = new State(Rule.create(sr, q, S, S, S), 2, 1, 1);
        Assert.assertTrue(chart.getStates(2).contains(s12SSS1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s12SSS1)), q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s12SSS1)), p * q, 0.0001);

        final State s02SSS2 = new State(Rule.create(sr, q, S, S, S), 2, 0, 2);
        Assert.assertTrue(chart.getStates(2).contains(s02SSS2));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s02SSS2)), p * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s02SSS2)), p * p * q, 0.0001);

        final State s02SSS1 = new State(Rule.create(sr, q, S, S, S), 2, 0, 1);
        Assert.assertTrue(chart.getStates(2).contains(s02SSS1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s02SSS1)), p * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s02SSS1)), p * p * q * q, 0.0001);

        final State s02S1 = new State(Rule.create(sr, Category.START, S), 2, 0, 1);
        Collection<State> states2 = chart.getStates(2);
        Assert.assertTrue(states2.contains(s02S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s02S1)), p * p * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s02S1)), p * p * q, 0.0001);
        // predicted
        final State s22S0 = new State(Rule.create(sr, p, S, a), 2, 2, 0);
        Assert.assertTrue(chart.getStates(2).contains(s22S0));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s22S0)), (1 + p) * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s22S0)), p, 0.0001);

        final State s22SS0 = new State(Rule.create(sr, q, S, S, S), 2, 2, 0);
        Assert.assertTrue(chart.getStates(2).contains(s22SS0));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s22SS0)), (1 + 1 / p) * q * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s22SS0)), q, 0.0001);

        // State set 3
        // scanned
        final State s23Sa1 = new ScannedTokenState<>(new Token<>("a"),
                Rule.create(sr, p, S, a), 2, 3, 1);
        Assert.assertTrue(chart.getStates(3).contains(s23Sa1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s23Sa1)), (1 + p) * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s23Sa1)), p, 0.0001);

        // completed
        final State s23S1 = new State(Rule.create(sr, q, S, S, S),
                3, 2, 1
        );
        Assert.assertTrue(chart.getStates(3).contains(s23S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s23S1)), (1 + p) * q * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s23S1)), p * q, 0.0001);

        final State s13S2 = new State(Rule.create(sr, q, S, S, S), 3, 1, 2);
        Assert.assertTrue(chart.getStates(3).contains(s13S2));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s13S2)), p * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s13S2)), p * p * q, 0.0001);

        final State s13S1 = new State(Rule.create(sr, q, S, S, S), 3, 1, 1);
        Assert.assertTrue(chart.getStates(3).contains(s13S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s13S1)), p * q * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s13S1)), p * p * q * q, 0.0001);

        final State s03S2 = new State(Rule.create(sr, q, S, S, S), 3, 0, 2);
        Assert.assertTrue(chart.getStates(3).contains(s03S2));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s03S2)), 2 * p * p * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s03S2)), 2 * p * p * p * q * q, 0.0001);

        final State s03S1 = new State(Rule.create(sr, q, S, S, S), 3, 0, 1);
        Assert.assertTrue(chart.getStates(3).contains(s03S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s03S1)), 2 * p * p * q * q * q, 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s03S1)), 2 * p * p * p * q * q * q, 0.0001);

        final State s33S1 = new State(Rule.create(sr, Category.START, S), 3, 0, 1);
        final Set<State> states3s = chart.getStates(3);
        Assert.assertTrue(states3s.contains(s33S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s33S1)), 2 * (Math.pow(p, 3) * Math.pow(q, 2)), 0.0001);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s33S1)), 2 * (Math.pow(p, 3) * Math.pow(q, 2)), 0.0001);


        for (int j = 0; j <= tokens.size(); j++) {
            chart.getStates(j).forEach(s -> {
                double probFw = sr.toProbability(chart.getForwardScore(s));
                double probInn = sr.toProbability(chart.getInnerScore(s));
                double v = 0.0;
                //noinspection StatementWithEmptyBody
                if (chart.getViterbiScore(s) == null) {
                    //System.out.println();
                } else
                    v = sr.toProbability(chart.getViterbiScore(s).getScore());

                //System.out.println(s + "[" + probFw + "]" + "[" + probInn + "] v: " + v);
            });
        }
        Collection<State> howMany = chart.stateSets.getCompletedStates(tokens.size(), Category.START);
        Assert.assertEquals(howMany.size(), 1);
        //if (howMany.size() > 1) throw new Error("Found more than one result sets. This is a nasty bug.");
        State finalState = howMany.iterator().next();
        ParseTree viterbi = Parser.getViterbiParse(finalState, chart);
        System.out.println(viterbi);
        System.out.println(chart.getViterbiScore(finalState));
    }

    @Test
    public void simpleGrammar2() throws Exception {
        double p = (0.6);
        double q = (0.4);
        final LogSemiring sr = new LogSemiring();
        // b surrounded by a's, or a single a
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, S, b, S)
                .build();
        List<Token<String>> tokens1 = Tokens.tokenize("a");
        ParseTreeWithScore parse1 = Parser.getViterbiParseWithScore(S, grammar, tokens1);
        List<Token<String>> tokens2 = Tokens.tokenize("a a b a a");
        ParseTreeWithScore parse2 = Parser.getViterbiParseWithScore(S, grammar, tokens2);

//        System.out.println(parse);
//        Assert.assertEquals(parse.getProbability(), Math.pow(0.5, 5), 0.0.0001);
        // TODO assert some stuff
    }


    @Test
    public void simpleGrammar3() throws Exception {
        double p = (0.6);
        double q = (0.4);
        final LogSemiring sr = new LogSemiring();
        // a surrounded by b's, or a single a
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, b, S, b)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("b b a b b");
        ParseTreeWithScore parse = Parser.getViterbiParseWithScore(S, grammar, tokens);

//        System.out.println(parse);
//        Assert.assertEquals(parse.getProbability(), Math.pow(0.5, 5), 0.0.0001);
        // TODO assert some stuff
    }

    @Test
    public void viterbi() throws Exception {
        final LogSemiring sr = new LogSemiring();
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(1.0, S, A)
                .addRule(0.5, S, S, S)
                .addRule(0.5, A, a)
                .build();

        List<Token<String>> tokens = Tokens.tokenize("a", "a", "a");
        ParseTreeWithScore parse = Parser.getViterbiParseWithScore(S, grammar, tokens);

        System.out.println(parse);
        Assert.assertNotNull(parse);
        Assert.assertEquals(parse.getProbability(), Math.pow(0.5, 5), 0.0001);
        // TODO assert some stuff
    }

    @Test
    public void viterbi2() throws Exception {
        final LogSemiring sr = new LogSemiring();
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

//        System.out.println(parse);
//        Assert.assertEquals(parse.getProbability(), 0.01, 0.0001);
        // TODO assert some stuff
    }


    @Test
    public void viterbi3() throws Exception {
        final LogSemiring sr = new LogSemiring();
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

//        System.out.println(parse);
//        Assert.assertEquals(parse.getProbability(), 0.6561, 0.0001);
        // TODO assert some stuff
    }
}