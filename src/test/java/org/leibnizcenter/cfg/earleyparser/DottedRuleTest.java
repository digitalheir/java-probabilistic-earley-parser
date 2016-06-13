
package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.earleyparser.parse.Edge;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;


/**
 */
public class DottedRuleTest {
    private final Edge dot1 = new Edge(rule1, 2, 0);
    private final Edge dot2 = new Edge(rule2, 1, 0);
    private final Edge dot3 = new Edge(rule3, 0, 0);

//    @Test public final void testDottedRule() {
//        try {
//            new Edge(rule1, -1);
//            Assert.fail(
//                    "able to create dotted rule with position < 0");
//        } catch (IndexOutOfBoundsException expected) {
//        }
//
//        try {
//            new DottedRule(rule1, 5);
//            Assert.fail(
//                    "able to create dotted rule with position longer than right");
//        } catch (IndexOutOfBoundsException expected) {
//        }
//    }
//
//    @Test public final void testAdvanceDot() {
//        Rule advanced = new DottedRule(rule3, 1);
//        Assert.assertEquals(advanced, DottedRule.advanceDot(edge2.dottedRule));
//
//        try {
//            DottedRule.advanceDot(edge3.dottedRule);
//            Assert.fail("able to advance dot of passive edge");
//        } catch (IndexOutOfBoundsException expected) {
//        }
//    }
//
//    @Test public final void testStartRule() {
//        try {
//            Rule.startRule(null);
//            fail("able to create start rule with null seed");
//        } catch (NullPointerException expected) {
//        }
//
//        try {
//            Rule.startRule(a);
//            fail("able to create start rule with terminal seed");
//        } catch (IllegalArgumentException expected) {
//        }
//
//        DottedRule sr = Rule.startRule(A);
//        Assert.assertTrue(Category.START == sr.left);
//        Assert.assertTrue(Category.START.equals(sr.left));
//        Assert.assertFalse(Category.START.equals(Category.nonTerminal(Category.START.toString())));
//        Assert.assertEquals(0, sr.getPosition());
//        Assert.assertEquals(A, sr.getActiveCategory());
//        Assert.assertEquals(sr, Rule.startRule(A));
//    }
//
//    @Test public final void testGetPosition() {
//        Assert.assertEquals(2, dot1.getPosition());
//    }
//
//    /**
//     * Test method for {@link DottedEdge#getActiveCategory()}
//     */
//    @Test public final void testGetActiveCategory() {
//        Assert.assertEquals(D, dot1.activeCategory);
//        Assert.assertEquals(null, dot2.activeCategory);
//        Assert.assertFalse(D.equals(dot3.activeCategory));
//    }
//
//    /**
//     * Test method for {@link DottedRule#hashCode()}.
//     */
//    @Override
//    @Test public final void testHashCode() {
//        Assert.assertEquals(31 * dot1.left.hashCode()
//                        * Arrays.hashCode(dot1.right) * (31 + dot1.position),
//                dot1.hashCode());
//    }
//
//    /**
//     * Test method for {@link DottedRule#equals(java.lang.Object)}.
//     */
//    @Override
//    @Test public final void testEqualsObject() {
//        DottedRule dr = new DottedRule(rule1, 2);
//        Assert.assertEquals(dot1, dr);
//        Assert.assertFalse(dot2.equals(dr));
//    }
//
//    /**
//     * Test method for {@link DottedRule#toString()}.
//     */
//    @Override
//    @Test public final void testToString() {
//        Assert.assertEquals("A -> B C * D E", dot1.toString());
//        Assert.assertEquals("A -> a *", dot2.toString());
//        Assert.assertEquals("X -> * Y Z", dot3.toString());
//    }
//TODO
}
