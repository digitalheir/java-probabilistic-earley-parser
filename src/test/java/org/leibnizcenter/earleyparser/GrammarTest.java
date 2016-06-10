/*
 * $Id: GrammarTest.java 1805 2010-02-03 22:37:31Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package org.leibnizcenter.earleyparser;

import org.junit.Assert;
import org.leibnizcenter.earleyparser.grammar.Grammar;
import org.leibnizcenter.earleyparser.grammar.Rule;
import org.leibnizcenter.earleyparser.grammar.categories.Category;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1805 $
 */
public class GrammarTest extends PepFixture {

    Grammar g;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        g = new Grammar.Builder("test")
                .addRule(rule1)
                .addRule(rule2)
                .addRule(rule3).build();
    }

    public final void testContainsRules() {
        Assert.assertTrue(g.containsRules(rule1.left));
        Assert.assertTrue(g.getRules(rule2.left).contains(rule2));
        Assert.assertFalse(g.getRules(rule3.left).contains(rule2));
    }
    /* TODO fix
    public final void testGetPreterminal() {
		Assert.assertEquals(rule2,
				g.getPreterminals(rule2, rule2.right[0].name, true));
		Assert.assertEquals(null,
				g.getPreterminals(rule2, rule2.right[0].name.toUpperCase(),
						false));
	}*/

    /**
     * Test method for {@link Grammar#getRules(Category)}.
     */
    public final void testGetRules() {
        Set<Rule> setOfrules = new HashSet<>();
        setOfrules.add(rule1);
        setOfrules.add(rule2);
        Assert.assertEquals(setOfrules, new HashSet<>(g.getRules(rule1.left)));
        Assert.assertEquals(setOfrules, new HashSet<>(g.getRules(rule2.left)));

        setOfrules.clear();
        setOfrules.add(rule3);
        Assert.assertEquals(setOfrules, new HashSet<>(g.getRules(rule3.left)));
    }

}
