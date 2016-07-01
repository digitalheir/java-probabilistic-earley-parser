//package org.leibnizcenter.cfg.earleyparser;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.leibnizcenter.cfg.category.Category;
//import org.leibnizcenter.cfg.rule.Rule;
//
//import java.util.Arrays;
//
//import static org.junit.Assert.fail;
//import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;
//
///**
// */
//public class RuleTest {
//
//    @Test
//    public final void testRule() {
//        try {
//            new Rule(null, X, Z);
//            fail("able to create rule with null left");
//        } catch (IllegalArgumentException ignored) {
//        }
//
//        try {
//            new Rule(Z, (Category[]) null);
//            fail("able to create rule with null right");
//        } catch (IllegalArgumentException ignored) {
//        }
//
//        try {
//            new Rule(Z);
//            fail("able to create rule with empty right");
//        } catch (IllegalArgumentException ignored) {
//        }
//
//        try {
//            new Rule(Z, a, A);
//        } catch (IllegalArgumentException problem) {
//            fail("unable to create rule with mix of terminals and "
//                    + "pre-terminals on right");
//        }
//    }
//
//    @Test
//    public final void testIsPreterminal() {
//        Assert.assertTrue(rule2.isPreterminal());
//        Assert.assertFalse(rule3.isPreterminal());
//    }
//
//    @Test
//    public final void testGetLeft() {
//        Assert.assertEquals(A, rule1.getLeft());
//        Assert.assertFalse(B.equals(rule2.getLeft()));
//    }
//
//    @Test
//    public final void testGetRight() {
//        Assert.assertTrue(
//                Arrays.equals(new Category[]{Y, Z}, rule3.getRight()));
//    }
//
//    /**
//     * Test method for {@link Rule#hashCode()}.
//     */
//    public void testHashCode() {
//        Assert.assertEquals(
//                31 * rule1.left.hashCode() * Arrays.hashCode(rule1.right),
//                rule1.hashCode());
//    }
//
//    /**
//     * Test method for {@link Rule#equals(java.lang.Object)}.
//     */
//    public void testEqualsObject() {
//        Assert.assertEquals(new Rule(A, B, C, D, E), rule1);
//        Assert.assertEquals(rule1, rule1);
//        Assert.assertNotSame(rule1, rule2);
//        Assert.assertNotSame(rule2, rule3);
//        Assert.assertFalse(rule1.equals(rule2));
//        Assert.assertFalse(rule2.equals(rule3));
//    }
//
//    /**
//     * Test method for {@link Rule#toString()}.
//     */
//    public void testToString() {
//        Assert.assertEquals("A -> B C D E", rule1.toString());
//        Assert.assertEquals("A -> a", rule2.toString());
//        Assert.assertEquals("X -> Y Z", rule3.toString());
//    }
//
//}
