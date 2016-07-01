
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.earleyparser.exception.ParseException;
import org.leibnizcenter.cfg.earleyparser.parse.Chart;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.Collection;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;


/**
 */
public class ChartTest {
    private static final Chart chart = new Chart(grammar);

    static {
        chart.addState(0, edge1);
        chart.addState(0, edge2);
        chart.addState(1, edge3);
    }

    @Test
    public final void example() throws ParseException {
        Grammar grammar = new Grammar.Builder()
                .addRule(new Rule(0.6, S, a))
                .addRule(new Rule(0.4, S, S, S))
                .build();
        Chart chart = new Chart(grammar);


        // Left-corner matrix P_L has rank 1:
        // P_L = [q]
        // Its transitive closure is
        // R_L = (I - P_L)^-1 = [p]^-1 = [p^-1]

        chart.addState(0, new State(
                new Rule(Category.START, S), 0
        ));
        for (int i = 0; i < 3; i++) {
            chart.predict(i);
            chart.scan(i, new Token<>("a"));
            if (i < 3) chart.complete(i + 1);
        }

        System.out.println(chart.countStates());
    }

    @Test
    public final void predict() {
//        .addRule(new Rule(S, NP, VP))
//        .addRule(new Rule(NP, Det, N))
//        .addRule(new Rule(VP, left))
//        .addRule(new Rule(Det, a))
//        .addRule(new Rule(Det, the))
//        .addRule(new Rule(N, boy))
//        .addRule(new Rule(N, girl))
//        Chart chart = new Chart(grammar);
//        chart.predict();

//            chart.addState(0, edge1);
//            chart.addState(0, edge2);
//            chart.addState(1, edge3);

    }

    //    @Test
//    public final void testFirstKey() {
//        Assert.assertEquals(0, chart.firstIndex());
//    }
//
//    @Test
//    public final void testLastKey() {
//        Assert.assertEquals(1, chart.lastIndex());
//    }
//
//
//    @Test
//    public final void testSubChart() {
//        Chart subChart = chart.subChart(0, 1);
//        Assert.assertTrue(subChart.contains(edge1));
//        Assert.assertTrue(subChart.contains(edge2));
//        Assert.assertFalse(subChart.contains(edge3));
//
//        try {
//            chart.subChart(1, 0);
//            Assert.fail("able to create subchart with bad indeces");
//        } catch (RuntimeException expected) {
//            // empty
//        }
//    }
//
//    @Test
//    public final void testIndexOf() {
//        Assert.assertEquals(0, chart.indexOf(edge1));
//        Assert.assertEquals(0, chart.indexOf(edge2));
//        Assert.assertEquals(1, chart.indexOf(edge3));
//    }
//
//    @Test
//    public final void testContains() {
//        Assert.assertTrue(chart.contains(edge1));
//        Assert.assertTrue(chart.contains(edge2));
//        Assert.assertTrue(chart.contains(edge2));
//        Assert.assertFalse(chart.contains(new State(rule3, 0, 4)));
//    }
//
    @Test
    public final void testGetIndeces() {
        //TODO
//        Set<Integer> indeces = chart.indices();
//        Assert.assertTrue(indeces.contains(0));
//        Assert.assertTrue(indeces.contains(1));
//
//        SortedSet<Integer> expected = new TreeSet<Integer>();
//        for (int i : indeces) {
//            expected.add(i);
//        }
//
//        Assert.assertEquals(expected, indeces);
//
//        int current = null, last;
//        Iterator<Integer> it = indeces.iterator();
//        while (it.hasNext()) {
//            last = current;
//            current = it.next();
//            if (last != null) {
//                Assert.assertTrue(current > last);
//            }
//        }
    }

    @Test
    public final void testContainsState() {
        Assert.assertTrue(chart.containsStates(0));
        Assert.assertTrue(chart.containsStates(1));
        Assert.assertFalse(chart.containsStates(2));
    }

//    /**
//     * Test method for {@link Chart#hashCode()}.
//     */
//    @Test
//    public final void testHashCode() {
//        Assert.assertEquals(37 * (1 + chart.stateSets.hashCode()), chart.hashCode());
//    }
//
//    /**
//     * Test method for {@link Chart#addState(int, State)}.
//     */
//    @Test
//    public final void testAddState() {
//        Assert.assertFalse("able to add edge multiple times",
//                chart.addState(0, edge1));
//    }

    @Test
    public final void testGetState() {
        Collection<State> zeroStates = chart.getStates(0);
        Assert.assertTrue(zeroStates.contains(edge1));
        Assert.assertTrue(zeroStates.contains(edge2));
    }

    /**
     * Test method for {@link Chart#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
        Chart c = new Chart(grammar);
        c.addState(0, edge1);
        c.addState(0, edge2);
        c.addState(1, edge3);

        Assert.assertEquals(c, chart);
    }

}
