package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParseSimpleRecursiveGrammarEarleySetTest {
    private static final NonTerminal S = Category.nonTerminal("S");
    private static final Terminal<String> a = new CaseInsensitiveStringTerminal("a");
    private static final double p = (0.6);
    private static final double q = (0.4);

    private static void stateSet3(final Chart<String> chart) {
        final ExpressionSemiring sr = chart.grammar.semiring;
        // scanned
        //new Token<>("a"),
        final State s23Sa1 = new State(
                Rule.create(sr, p, S, a), 3, 2, 1);
        Assert.assertTrue(chart.getStates(3).contains(s23Sa1));
        assertEquals(sr.toProbability(chart.getForwardScore(s23Sa1)), (1 + p) * q * q, 0.0001);
        assertEquals(sr.toProbability(chart.getInnerScore(s23Sa1)), p, 0.0001);
    }

    private static void stateSet2(final Chart<String> chart) {
        final ExpressionSemiring sr = chart.grammar.semiring;
        // scanned
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
        final Collection<State> states2 = chart.getStates(2);
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
    }

    private static void stateSet1(final Chart<String> chart) {
        final ExpressionSemiring sr = chart.grammar.semiring;
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
    }

    private static void stateSet0(final Chart<String> chart) {
        final ExpressionSemiring sr = chart.grammar.semiring;

        final State s00Sa = new State(Rule.create(sr, p, S, a), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00Sa));
        assertEquals(sr.toProbability(chart.getForwardScore(s00Sa)), 1.0, 0.01);
        assertEquals(sr.toProbability(chart.getInnerScore(s00Sa)), p, 0.00001);

        final State s00SSS = new State(Rule.create(sr, q, S, S, S), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00SSS));
        assertEquals(sr.toProbability(chart.getForwardScore(s00SSS)), q / p, 0.01);
        assertEquals(sr.toProbability(chart.getInnerScore(s00SSS)), q, 0.00001);
    }

    @Test
    public void simpleRecursiveGrammar() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        final Grammar<String> grammar = new Grammar.Builder<String>()
                .withSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, S, S)
                .build();

        final List<Token<String>> tokens = IntStream.range(0, 3).mapToObj(i -> new Token<>("a")).collect(Collectors.toList());

        final Chart<String> chart = new Parser<>(grammar).parse(S, tokens);

        // State set 0
        stateSet0(chart);

        // State set 1
        stateSet1(chart);

        // State set 2
        stateSet2(chart);

        // State set 3
        stateSet3(chart);

        assertEquals(
                2.0,
                chart.getStates(0).stream().mapToDouble(s -> chart.getViterbiScore(s).getProbability()).sum(),
                0.0000001
        );

        IntStream.range(0, tokens.size())
                .forEach(j -> chart.getStates(j).forEach(s -> {
                    // double probFw = sr.toProbability(chart.getForwardScore(s));
                    // double probInn = sr.toProbability(chart.getInnerScore(s));
                    // double v = 0.0;
                    final State.ViterbiScore viterbiScore = chart.getViterbiScore(s);
                    assertNotNull(viterbiScore);
                    // v = sr.toProbability(viterbiScore.getScore());
                    // System.out.println(s + "[" + probFw + "]" + "[" + probInn + "] value: " + v);
                }));

        final Collection<State> howMany = chart.stateSets.completedStates.getCompletedStates(tokens.size(), Category.START);
        assertEquals(howMany.size(), 1);
        final State finalState = howMany.iterator().next();
        // ParseTree viterbi = Parser.getViterbiParse(finalState, chart);

        // 1 of 2 options
        assertEquals(
                Math.pow(p, 3) * Math.pow(q, 2),
                chart.getViterbiScore(finalState).getProbability(),
                0.00000000001
        );
    }
}
