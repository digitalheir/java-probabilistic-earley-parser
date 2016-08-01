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

        final State s00SSS = new State(Rule.create(sr, p, S, S, S), 0, 0, 0);
        Assert.assertTrue(chart.getStates(0).contains(s00SSS));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s00SSS)), q / p, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s00SSS)), q, 0.01);

        // State set 1
        final State s11Sa0 = new State(Rule.create(sr, p, S, a), 1, 1, 0);
        Assert.assertTrue(chart.getStates(1).contains(s11Sa0));
//        Assert.assertEquals(sr.toProbability(chart.getForwardScore(s11Sa0)), q, 0.01);
//        Assert.assertEquals(sr.toProbability(chart.getInnerScore(s11Sa0)), p, 0.01);

        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, p, S, S, S), 0, 1, 1)));
        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, p, S, a), 0, 1, 1)));
        Assert.assertTrue(chart.getStates(1).contains(new State(Rule.create(sr, p, S, S, S), 1, 1, 0)));

        // State set 2

        // State set 3
        final State s33S1 = new State(Rule.create(sr.one(), Category.START, S), 0, 3, 1);
        Assert.assertTrue(chart.getStates(3).contains(s33S1));


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