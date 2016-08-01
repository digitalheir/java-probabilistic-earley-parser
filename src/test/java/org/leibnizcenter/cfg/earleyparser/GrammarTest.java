
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.HashSet;
import java.util.Set;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;

/**
 */
public class GrammarTest {
    private static final Rule ruleB = Rule.create(0.5, B, C);
    private static final Rule ruleC = Rule.create(0.5, C, D);
    private static final Rule ruleD = Rule.create(0.5, D, E);
    private static final Rule ruleE = Rule.create(0.5, E, e);

    private static final Grammar g = new Grammar.Builder("test")
            .addRule(ruleB)
            .addRule(ruleC)
            .addRule(ruleD)
            .addRule(ruleE)
            .addRule(rule1)
            .addRule(rule2)
            .addRule(rule3).build();

    @Test
    public final void testContainsRules() {
        Assert.assertTrue(g.containsRules(rule1.left));
        Assert.assertTrue(g.getRules(rule2.left).contains(rule2));
        Assert.assertFalse(g.getRules(rule3.left).contains(rule2));
    }

    @Test
    public final void testLeftRelation() {
        Assert.assertEquals(g.getLeftScore(A, B), 1.0, 0.01);
        Assert.assertEquals(g.getLeftScore(A, D), 0.0, 0.01);
        Assert.assertEquals(g.getLeftScore(A, X), 0.0, 0.01);
        Assert.assertEquals(g.getLeftScore(B, C), 0.5, 0.01);
    }

    @Test
    public final void testLeftStarRelation() {
        Assert.assertEquals(g.getLeftStarScore(A, B), 1.0, 0.01);
        Assert.assertEquals(g.getLeftStarScore(B, C), 0.5, 0.01);
        Assert.assertEquals(g.getLeftStarScore(B, D), 0.25, 0.01);
        Assert.assertEquals(g.getLeftStarScore(A, D), 0.25, 0.01);
        Assert.assertEquals(g.getLeftStarScore(A, X), 0.0, 0.01);
    }

//    @Test public final void testGetPreterminal() {
//		Assert.assertEquals(rule2,
//				g.getPreterminals(rule2, rule2.right[0].name, true));
//		Assert.assertEquals(null,
//				g.getPreterminals(rule2, rule2.right[0].name.toUpperCase(),
//						false));
//}

    /**
     * Test method for {@link Grammar#getRules(Category)}.
     */
    @Test
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
