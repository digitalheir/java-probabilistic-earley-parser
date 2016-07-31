
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.earleyparser.exception.ParseException;
import org.leibnizcenter.cfg.earleyparser.parse.Chart;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.token.Token;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;


/**
 */
public class ChartTest {
    private static final Chart chart = new Chart(grammar);

    //    static {
//        chart.addState(0, edge1);
//        chart.addState(0, edge2);
//        chart.addState(1, edge3);
//    }
//
    @Test
    public final void example() throws ParseException {
        double p = 0.6;
        double q = 0.4;
        Grammar grammar = new Grammar.Builder()
                .addRule(new Rule(p, S, a))
                .addRule(new Rule(q, S, B))
                .addRule(new Rule(1, B, S))
                .build();
        Chart chart = new Chart(grammar);
        DblSemiring sr = grammar.getSemiring();


        State initialState = new State(new Rule(sr.one(), Category.START, S), 0);
        chart.addState(0, initialState, sr.one(), sr.one());

        chart.predict(0);

        Assert.assertTrue(chart.getStates(0).contains(initialState));
        Assert.assertTrue(chart.getStates(0).contains(new State(new Rule(p, S, a), 0)));
        Assert.assertTrue(chart.getStates(0).contains(new State(new Rule(q, S, B), 0)));
        Assert.assertTrue(chart.getStates(0).contains(new State(new Rule(1, B, S), 0)));
        Assert.assertEquals(chart.getForwardScore(new State(new Rule(1, B, S), 0)), (q / p), 0.01);
        Assert.assertEquals(chart.getInnerScore(new State(new Rule(1, B, S), 0)), 1, 0.01);
        Assert.assertEquals(chart.getForwardScore(new State(new Rule(q, S, B), 0)), (q / p), 0.01);
        Assert.assertEquals(chart.getInnerScore(new State(new Rule(q, S, B), 0)), q, 0.01);

//        for (State s : chart.getStates(0)) {
//            System.out.println((s) + "[" + chart.getForwardScore(s) + "]" + "[" + chart.getInnerScore(s) + "]");
//        }
//
//        for (int i = 0; i < 3; i++) {
//            chart.scan(i, new Token<>("a"));
//            if (i < 3) chart.complete(i + 1);
//        }

    }

    @Test
    public final void parse() {
        final LogSemiring semiring = new LogSemiring();
        final Rule ruleB = new Rule(semiring.fromProbability(0.5), B, C);
        final Rule ruleC = new Rule(semiring.fromProbability(0.5), C, D);
        final Rule ruleD = new Rule(semiring.fromProbability(0.5), D, E);
        final Rule ruleE = new Rule(semiring.fromProbability(0.5), E, e);
        final Rule rule1 = new Rule(semiring.one(), A, B, C, D, E);
        final Rule ruleAa = new Rule(semiring.one(), A, a);
        final Rule rule3 = new Rule(semiring.one(), X, Y, Z);

        Grammar grammar = new Grammar.Builder("test")
                .setSemiring(semiring)
                .addRule(ruleB)
                .addRule(ruleC)
                .addRule(ruleD)
                .addRule(ruleE)
                .addRule(rule1)
                .addRule(ruleAa)
                .addRule(rule3).build();
        DblSemiring sr = grammar.getSemiring();
        Chart chart = new Chart(grammar);

        chart.addState(0, new State(new Rule(sr.one(), Category.START, A), 0), sr.one(), sr.one());
        chart.predict(0);
        chart.scan(0, new Token<>("a"), index -> semiring.fromProbability(0.5));
        chart.complete(1);

//        for (int i = 0; i < 2; i++) {
//            for (State s : chart.getStates(i)) {
//                final double probFw = semiring.toProbability(chart.getForwardScore(s));
//                final double probInn = semiring.toProbability(chart.getInnerScore(s));
//                System.out.println((s) + "[" + probFw + "]" + "[" + probInn + "]");
//            }
//        }

        // TODO assert some stuff
        //Assert.assertEquals(0.1, semiring.toProbability(chart.getForwardScore(s)), 0.2);
        //Assert.assertEquals(0.03125,chart.getForwardScore(s),0.01);
    }
//
//    //    @Test
////    public final void testFirstKey() {
////        Assert.assertEquals(0, chart.firstIndex());
////    }
////
////    @Test
////    public final void testLastKey() {
////        Assert.assertEquals(1, chart.lastIndex());
////    }
////
////
////    @Test
////    public final void testSubChart() {
////        Chart subChart = chart.subChart(0, 1);
////        Assert.assertTrue(subChart.contains(edge1));
////        Assert.assertTrue(subChart.contains(edge2));
////        Assert.assertFalse(subChart.contains(edge3));
////
////        try {
////            chart.subChart(1, 0);
////            Assert.fail("able to create subchart with bad indeces");
////        } catch (RuntimeException expected) {
////            // empty
////        }
////    }
////
////    @Test
////    public final void testIndexOf() {
////        Assert.assertEquals(0, chart.indexOf(edge1));
////        Assert.assertEquals(0, chart.indexOf(edge2));
////        Assert.assertEquals(1, chart.indexOf(edge3));
////    }
////
////    @Test
////    public final void testContains() {
////        Assert.assertTrue(chart.contains(edge1));
////        Assert.assertTrue(chart.contains(edge2));
////        Assert.assertTrue(chart.contains(edge2));
////        Assert.assertFalse(chart.contains(new State(rule3, 0, 4)));
////    }
////
//    @Test
//    public final void testGetIndeces() {
//        //TODO
////        Set<Integer> indeces = chart.indices();
////        Assert.assertTrue(indeces.contains(0));
////        Assert.assertTrue(indeces.contains(1));
////
////        SortedSet<Integer> expected = new TreeSet<Integer>();
////        for (int i : indeces) {
////            expected.add(i);
////        }
////
////        Assert.assertEquals(expected, indeces);
////
////        int current = null, last;
////        Iterator<Integer> it = indeces.iterator();
////        while (it.hasNext()) {
////            last = current;
////            current = it.next();
////            if (last != null) {
////                Assert.assertTrue(current > last);
////            }
////        }
//    }
//
//    @Test
//    public final void testContainsState() {
//        Assert.assertTrue(chart.containsStates(0));
//        Assert.assertTrue(chart.containsStates(1));
//        Assert.assertFalse(chart.containsStates(2));
//    }
//
////    /**
////     * Test method for {@link Chart#hashCode()}.
////     */
////    @Test
////    public final void testHashCode() {
////        Assert.assertEquals(37 * (1 + chart.stateSets.hashCode()), chart.hashCode());
////    }
////
////    /**
////     * Test method for {@link Chart#addState(int, State)}.
////     */
////    @Test
////    public final void testAddState() {
////        Assert.assertFalse("able to add edge multiple times",
////                chart.addState(0, edge1));
////    }
//
//    @Test
//    public final void testGetState() {
//        Collection<State> zeroStates = chart.getStates(0);
//        Assert.assertTrue(zeroStates.contains(edge1));
//        Assert.assertTrue(zeroStates.contains(edge2));
//    }
//
//    /**
//     * Test method for {@link Chart#equals(java.lang.Object)}.
//     */
//    @Test
//    public final void testEqualsObject() {
//        Chart c = new Chart(grammar);
//        c.addState(0, edge1);
//        c.addState(0, edge2);
//        c.addState(1, edge3);
//
//        Assert.assertEquals(c, chart);
//    }

}
