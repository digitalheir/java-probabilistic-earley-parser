/*
 * $Id: DottedRuleTest.java 1796 2010-01-28 22:52:27Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep;

import java.util.Arrays;

import edu.osu.ling.pep.grammar.categories.Category;
import edu.osu.ling.pep.grammar.DottedRule;
import edu.osu.ling.pep.grammar.Rule;
import org.junit.Assert;


/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1796 $
 */
public class DottedRuleTest extends RuleTest {

	DottedRule dot1, dot2, dot3;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		dot1 = new DottedRule(rule1, 2);
		dot2 = new DottedRule(rule2, 1);
		dot3 = new DottedRule(rule3, 0);
	}
	
	public final void testDottedRule() {
		try {
			new DottedRule(rule1, -1);
			Assert.fail(
				"able to create dotted rule with position < 0");
		}
		catch(IndexOutOfBoundsException expected) {}
		
		try {
			new DottedRule(rule1, 5);
			Assert.fail(
				"able to create dotted rule with position longer than right");
		}
		catch(IndexOutOfBoundsException expected) {}
	}
	
	public final void testAdvanceDot() {
		Rule advanced = new DottedRule(rule3, 1);
		Assert.assertEquals(advanced, DottedRule.advanceDot(edge2.dottedRule));
		
		try {
			DottedRule.advanceDot(edge3.dottedRule);
			Assert.fail("able to advance dot of passive edge");
		}
		catch(IndexOutOfBoundsException expected) {}
	}
	
	public final void testStartRule() {
		try {
			DottedRule.startRule(null);
			fail("able to create start rule with null seed");
		}
		catch(NullPointerException expected) {}
		
		try {
			DottedRule.startRule(a);
			fail("able to create start rule with terminal seed");
		}
		catch(IllegalArgumentException expected) {}
		
		DottedRule sr = DottedRule.startRule(A);
		Assert.assertTrue(Category.START == sr.left);
		Assert.assertTrue(Category.START.equals(sr.left));
		Assert.assertFalse(Category.START.equals(Category.nonTerminal(Category.START.toString())));
		Assert.assertEquals(0, sr.getPosition());
		Assert.assertEquals(A, sr.getActiveCategory());
		Assert.assertEquals(sr, DottedRule.startRule(A));
	}
	
	public final void testGetPosition() {
		Assert.assertEquals(2, dot1.getPosition());
	}
	
	/**
	 * Test method for {@link DottedRule#getActiveCategory()}
	 *
	 */
	public final void testGetActiveCategory() {
		Assert.assertEquals(D, dot1.activeCategory);
		Assert.assertEquals(null, dot2.activeCategory);
		Assert.assertFalse(D.equals(dot3.activeCategory));
	}

	/**
	 * Test method for {@link DottedRule#hashCode()}.
	 */
	@Override
	public final void testHashCode() {
		Assert.assertEquals(31 * dot1.left.hashCode()
				* Arrays.hashCode(dot1.right) * (31 + dot1.position),
				dot1.hashCode());
	}

	/**
	 * Test method for {@link DottedRule#equals(java.lang.Object)}.
	 */
	@Override
	public final void testEqualsObject() {
		DottedRule dr = new DottedRule(rule1, 2);
		Assert.assertEquals(dot1, dr);
		Assert.assertFalse(dot2.equals(dr));
	}

	/**
	 * Test method for {@link DottedRule#toString()}.
	 */
	@Override
	public final void testToString() {
		Assert.assertEquals("A -> B C * D E", dot1.toString());
		Assert.assertEquals("A -> a *", dot2.toString());
		Assert.assertEquals("X -> * Y Z", dot3.toString());
	}

}
