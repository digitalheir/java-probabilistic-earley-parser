/*
 * $Id: CategoryTest.java 562 2007-08-16 15:16:13Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep;

import edu.osu.ling.pep.grammar.Token;
import edu.osu.ling.pep.grammar.categories.Category;
import org.junit.Assert;

import java.util.function.Function;


/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 562 $
 */
public class CategoryTest extends PepFixture {

    /**
     * Test method for {@link Category#nonTerminal(String)} and {@link Category#terminal}.
     */
    public final void testCategoryString() {
        try {
            Category.nonTerminal("");
            Assert.fail("able to create non-terminal category with empty name");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            Category.terminal(new ExactStringTerminal(""));
        } catch (IllegalArgumentException iae) {
            Assert.fail("creating terminal category with empty name threw: "
                    + iae);
        }

        try {
            Category.nonTerminal(null);
            Assert.fail("able to create category with null name");
        } catch (IllegalArgumentException ignored) {
        }
    }


    /**
     * Test method for {@link Category#isTerminal(Category)}.
     */
    public final void testIsTerminal() {
        Assert.assertEquals(false, Category.isTerminal(A));
        Assert.assertEquals(true, Category.isTerminal(a));
    }

    /**
     * Test method for {@link Category#equals(java.lang.Object)}.
     */
    public final void testEqualsObject() {
        Assert.assertEquals(A, Category.nonTerminal("A"));
        Assert.assertFalse(A.equals(Category.nonTerminal("B")));
    }

    /**
     * Test method for {@link Category#toString()}.
     */
    public final void testToString() {
        Assert.assertEquals("A", A.toString());
    }

}
