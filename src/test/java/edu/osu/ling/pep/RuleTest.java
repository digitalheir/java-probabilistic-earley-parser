/*
 * $Id: RuleTest.java 1782 2010-01-19 16:21:11Z scott $
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
import edu.osu.ling.pep.grammar.Rule;
import junit.framework.Assert;

/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1782 $
 */
public class RuleTest extends PepFixture {

	public final void testRule() {
		try {
			new Rule(null, X, Z);
			fail("able to create rule with null left");
		}
		catch(IllegalArgumentException expected) {}
		
		try {
			new Rule(Z, (Category[])null);
			fail("able to create rule with null right");
		}
		catch(IllegalArgumentException expected) {}
		
		try {
			new Rule(Z);
			fail("able to create rule with empty right");
		}
		catch(IllegalArgumentException expected) {}
		
		try {
			new Rule(Z, a, A);
		}
		catch(IllegalArgumentException problem) {
			fail("unable to create rule with mix of terminals and "
					+ "pre-terminals on right");
		}
	}
	
	public final void testIsPreterminal() {
		Assert.assertTrue(rule2.isPreterminal());
		Assert.assertFalse(rule3.isPreterminal());
	}
	
	public final void testGetLeft() {
		Assert.assertEquals(A, rule1.getLeft());
		Assert.assertFalse(B.equals(rule2.getLeft()));
	}
	
	public final void testGetRight() {
		Assert.assertTrue(
				Arrays.equals(new Category[] {Y, Z}, rule3.getRight()));
	}
	
	/**
	 * Test method for {@link Rule#hashCode()}.
	 */
	public void testHashCode() {
		Assert.assertEquals(
				31 * rule1.left.hashCode() * Arrays.hashCode(rule1.right),
				rule1.hashCode());
	}

	/**
	 * Test method for {@link Rule#equals(java.lang.Object)}.
	 */
	public void testEqualsObject() {
		Assert.assertEquals(new Rule(A, B, C, D, E), rule1);
		Assert.assertEquals(rule1, rule1);
		Assert.assertNotSame(rule1, rule2);
		Assert.assertNotSame(rule2, rule3);
		Assert.assertFalse(rule1.equals(rule2));
		Assert.assertFalse(rule2.equals(rule3));
	}

	/**
	 * Test method for {@link Rule#toString()}.
	 */
	public void testToString() {
		Assert.assertEquals("A -> B C D E", rule1.toString());
		Assert.assertEquals("A -> a", rule2.toString());
		Assert.assertEquals("X -> Y Z", rule3.toString());
	}

}
