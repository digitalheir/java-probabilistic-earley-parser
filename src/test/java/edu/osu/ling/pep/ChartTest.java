/*
 * $Id: ChartTest.java 1807 2010-02-05 22:20:02Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.osu.ling.pep.earley.Chart;
import edu.osu.ling.pep.earley.Edge;
import edu.osu.ling.pep.grammar.DottedRule;
import junit.framework.Assert;


/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1807 $
 */
public class ChartTest extends PepFixture {

	Chart chart;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		chart = new Chart();
		
		chart.addEdge(0, edge1);
		chart.addEdge(0, edge2);
		chart.addEdge(1, edge3);
	}
	
	public final void testFirstKey() {
		Assert.assertEquals(0, chart.firstIndex().intValue());
	}
	
	public final void testLastKey() {
		Assert.assertEquals(1, chart.lastIndex().intValue());
	}
	
	public final void testSubChart() {
		Chart subChart = chart.subChart(0, 1);
		Assert.assertTrue(subChart.contains(edge1));
		Assert.assertTrue(subChart.contains(edge2));
		Assert.assertFalse(subChart.contains(edge3));
		
		try {
			chart.subChart(1, 0);
			Assert.fail("able to create subchart with bad indeces");
		}
		catch(RuntimeException expected) {
			// empty 
		}
	}
	
	public final void testIndexOf() {
		Assert.assertEquals(0, chart.indexOf(edge1).intValue());
		Assert.assertEquals(0, chart.indexOf(edge2).intValue());
		Assert.assertEquals(1, chart.indexOf(edge3).intValue());
	}
	
	public final void testContains() {
		Assert.assertTrue(chart.contains(edge1));
		Assert.assertTrue(chart.contains(edge2));
		Assert.assertTrue(chart.contains(edge2));
		Assert.assertFalse(chart.contains(new Edge(new DottedRule(rule3), 4)));
	}
	
	public final void testGetIndeces() {
		Set<Integer> indeces = chart.indices();
		Assert.assertTrue(indeces.contains(0));
		Assert.assertTrue(indeces.contains(1));
		
		SortedSet<Integer> expected = new TreeSet<Integer>();
		for(Integer i : indeces) {
			expected.add(i);
		}
		
		Assert.assertEquals(expected, indeces);
		
		Integer current = null, last;
		Iterator<Integer> it = indeces.iterator();
		while(it.hasNext()) {
			last = current;
			current = it.next();
			if(last != null) {
				Assert.assertTrue(current > last);
			}
		}
	}
	
	public final void testContainsEdge() {
		Assert.assertTrue(chart.containsEdges(0));
		Assert.assertTrue(chart.containsEdges(1));
		Assert.assertFalse(chart.containsEdges(2));
	}

	/**
	 * Test method for {@link Chart#hashCode()}.
	 */
	public final void testHashCode() {
		Assert.assertEquals(37 * (1 + chart.edgeSets.hashCode()), chart.hashCode());
	}

	/**
	 * Test method for {@link Chart#addEdge(int, Edge)}.
	 */
	public final void testAddEdge() {
		Assert.assertFalse("able to add edge multiple times",
				chart.addEdge(0, edge1));
	}

	public final void testGetEdge() {
		Set<Edge> zeroEdges = chart.getEdges(0);
		Assert.assertTrue(zeroEdges.contains(edge1));
		Assert.assertTrue(zeroEdges.contains(edge2));
	}

	/**
	 * Test method for {@link Chart#equals(java.lang.Object)}.
	 */
	public final void testEqualsObject() {
		Chart c = new Chart();
		c.addEdge(0, edge1);
		c.addEdge(0, edge2);
		c.addEdge(1, edge3);
		
		Assert.assertEquals(c, chart);
	}

}
