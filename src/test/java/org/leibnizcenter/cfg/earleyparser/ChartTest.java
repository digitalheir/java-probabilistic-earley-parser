
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.earleyparser.parse.Chart;
import org.leibnizcenter.cfg.earleyparser.parse.Edge;

import java.util.Set;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;


/**
 */
public class ChartTest {
    private static final Chart chart = new Chart();

    static {
        chart.addEdge(0, edge1);
        chart.addEdge(0, edge2);
        chart.addEdge(1, edge3);
    }

    @Test
    public final void testFirstKey() {
        Assert.assertEquals(0, chart.firstIndex());
    }

    @Test
    public final void testLastKey() {
        Assert.assertEquals(1, chart.lastIndex());
    }


    @Test
    public final void testSubChart() {
        Chart subChart = chart.subChart(0, 1);
        Assert.assertTrue(subChart.contains(edge1));
        Assert.assertTrue(subChart.contains(edge2));
        Assert.assertFalse(subChart.contains(edge3));

        try {
            chart.subChart(1, 0);
            Assert.fail("able to create subchart with bad indeces");
        } catch (RuntimeException expected) {
            // empty
        }
    }

    @Test
    public final void testIndexOf() {
        Assert.assertEquals(0, chart.indexOf(edge1));
        Assert.assertEquals(0, chart.indexOf(edge2));
        Assert.assertEquals(1, chart.indexOf(edge3));
    }

    @Test
    public final void testContains() {
        Assert.assertTrue(chart.contains(edge1));
        Assert.assertTrue(chart.contains(edge2));
        Assert.assertTrue(chart.contains(edge2));
        Assert.assertFalse(chart.contains(new Edge(rule3, 0, 4)));
    }

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
    public final void testContainsEdge() {
        Assert.assertTrue(chart.containsEdges(0));
        Assert.assertTrue(chart.containsEdges(1));
        Assert.assertFalse(chart.containsEdges(2));
    }

    /**
     * Test method for {@link Chart#hashCode()}.
     */
    @Test
    public final void testHashCode() {
        Assert.assertEquals(37 * (1 + chart.edgeSets.hashCode()), chart.hashCode());
    }

    /**
     * Test method for {@link Chart#addEdge(int, Edge)}.
     */
    @Test
    public final void testAddEdge() {
        Assert.assertFalse("able to add edge multiple times",
                chart.addEdge(0, edge1));
    }

    @Test
    public final void testGetEdge() {
        Set<Edge> zeroEdges = chart.getEdges(0);
        Assert.assertTrue(zeroEdges.contains(edge1));
        Assert.assertTrue(zeroEdges.contains(edge2));
    }

    /**
     * Test method for {@link Chart#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
        Chart c = new Chart();
        c.addEdge(0, edge1);
        c.addEdge(0, edge2);
        c.addEdge(1, edge3);

        Assert.assertEquals(c, chart);
    }

}
