package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.earleyparser.parse.Chart;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.rule.RuleFactory;
import org.leibnizcenter.cfg.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.token.Tokens;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.S;
import static org.leibnizcenter.cfg.earleyparser.PepFixture.a;

/**
 * Created by Maarten on 31-7-2016.
 */
public class ParserTest {
    @Test
    public void simpleRecursiveGrammar() throws Exception {
        final LogSemiring sr = new LogSemiring();
        RuleFactory rf = new RuleFactory(sr);
        double p = (0.6);
        double q = (0.4);
        Grammar grammar = new Grammar.Builder()
                .setSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, S, S)
                .build();

        Chart chart = Parser.parse(S, grammar, Tokens.tokenize("a", "a", "a"), null);

        // State set 0
        final State s00Sa = new State(Rule.create(sr, p, S, a), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00Sa));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s00Sa)), 1.0, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s00Sa)), p, 0.01);

        final State s00SSS = new State(Rule.create(sr, q, S, S, S), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00SSS));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s00SSS)), q / p, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s00SSS)), q, 0.01);

        // State set 1
        // scanned
        final State s01Sa1 = new State(Rule.create(sr, p, S, a), 0, 1, 1);
        Assert.assertTrue(chart.getStates(1).contains(s01Sa1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s01Sa1)), 1, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s01Sa1)), p, 0.01);

        // completed
        final State s01SSS1 = new State(Rule.create(sr, q, S, S, S), 0, 1, 1);
        Assert.assertTrue(chart.getStates(1).contains(s01SSS1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s01SSS1)), q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s01SSS1)), p * q, 0.01);

        // predicted
        final State s11Sa0 = new State(Rule.create(sr, p, S, a), 1, 1, 0);
        Assert.assertTrue(chart.getStates(1).contains(s11Sa0));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s11Sa0)), q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s11Sa0)), p, 0.01);
        final State s11SSS0 = new State(Rule.create(sr, q, S, S, S), 1, 1, 0);
        Assert.assertTrue(chart.getStates(1).contains(s11SSS0));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s11SSS0)), Math.pow(q, 2) / p, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s11SSS0)), q, 0.01);

        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, q, S, S, S), 0, 1, 1)));
        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, p, S, a), 0, 1, 1)));
        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, q, S, S, S), 1, 1, 0)));

        // State set 2
        // scanned
        final State s12Sa1 = new State(Rule.create(sr, p, S, a), 1, 2, 1);
        Assert.assertTrue(chart.getStates(2).contains(s12Sa1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s12Sa1)), q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s12Sa1)), p, 0.01);
        // completed
        final State s12SSS1 = new State(Rule.create(sr, q, S, S, S), 1, 2, 1);
        Assert.assertTrue(chart.getStates(2).contains(s12SSS1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s12SSS1)), q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s12SSS1)), p * q, 0.01);

        final State s02SSS2 = new State(Rule.create(sr, q, S, S, S), 0, 2, 2);
        Assert.assertTrue(chart.getStates(2).contains(s02SSS2));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s02SSS2)), p * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s02SSS2)), p * p * q, 0.01);

        final State s02SSS1 = new State(Rule.create(sr, q, S, S, S), 0, 2, 1);
        Assert.assertTrue(chart.getStates(2).contains(s02SSS1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s02SSS1)), p * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s02SSS1)), p * p * q * q, 0.01);

        final State s02S1 = new State(Rule.create(sr.one(), Category.START, S), 0, 2, 1);
        Assert.assertTrue(chart.getStates(2).contains(s02S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s02S1)), p * p * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s02S1)), p * p * q, 0.01);
        // predicted
        final State s22S0 = new State(Rule.create(sr, p, S, a), 2, 2, 0);
        Assert.assertTrue(chart.getStates(2).contains(s22S0));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s22S0)), (1 + p) * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s22S0)), p, 0.01);

        final State s22SS0 = new State(Rule.create(sr, q, S, S, S), 2, 2, 0);
        Assert.assertTrue(chart.getStates(2).contains(s22SS0));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s22SS0)), (1 + 1 / p) * q * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s22SS0)), q, 0.01);

        // State set 3
        // scanned
        final State s23Sa1 = new State(Rule.create(sr, p, S, a), 2, 3, 1);
        Assert.assertTrue(chart.getStates(3).contains(s23Sa1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s23Sa1)), (1 + p) * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s23Sa1)), p, 0.01);

        // completed
        final State s23S1 = new State(Rule.create(sr, q, S, S, S), 2, 3, 1);
        Assert.assertTrue(chart.getStates(3).contains(s23S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s23S1)), (1 + p) * q * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s23S1)), p * q, 0.01);

        final State s13S2 = new State(Rule.create(sr, q, S, S, S), 1, 3, 2);
        Assert.assertTrue(chart.getStates(3).contains(s13S2));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s13S2)), p * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s13S2)), p * p * q, 0.01);

        final State s13S1 = new State(Rule.create(sr, q, S, S, S), 1, 3, 1);
        Assert.assertTrue(chart.getStates(3).contains(s13S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s13S1)), p * q * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s13S1)), p * p * q * q, 0.01);

        final State s03S2 = new State(Rule.create(sr, q, S, S, S), 0, 3, 2);
        Assert.assertTrue(chart.getStates(3).contains(s03S2));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s03S2)), 2 * p * p * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s03S2)), 2 * p * p * p * q * q, 0.01);

        final State s03S1 = new State(Rule.create(sr, q, S, S, S), 0, 3, 1);
        Assert.assertTrue(chart.getStates(3).contains(s03S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s03S1)), 2 * p * p * q * q * q, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s03S1)), 2 * p * p * p * q * q * q, 0.01);

        final State s33S1 = new State(Rule.create(sr.one(), Category.START, S), 0, 3, 1);
        Assert.assertTrue(chart.getStates(3).contains(s33S1));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s33S1)), 2 * (Math.pow(p, 3) * Math.pow(q, 2)), 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s33S1)), 2 * (Math.pow(p, 3) * Math.pow(q, 2)), 0.01);


        for (int j = 0; j <= chart.length; j++) {
            chart.getStates(j).forEach(s -> {
                double probFw = sr.toProbability(chart.getForwardScore(s));
                double probInn = sr.toProbability(chart.getInnerScore(s));
                double v = sr.toProbability(chart.getViterbiScore(s));
                System.out.println(s + "[" + probFw + "]" + "[" + probInn + "] v: " + v);
            });
        }
    }
}