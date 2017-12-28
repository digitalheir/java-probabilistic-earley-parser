
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;

import static org.leibnizcenter.cfg.earleyparser.Fixture.A;


/**
 */
public class CategoryTest {

    /**
     * Test method for {@link Category#nonTerminal(String)} and {@link Category#terminal}.
     */
    @Test
    public final void testCategoryString() {
        try {
            Category.nonTerminal("");
            Assert.fail("able to create non-terminal category with empty name");
        } catch (final IllegalArgumentException ignored) {
        }

        try {
            Category.terminal(new ExactStringTerminal(""));
        } catch (final IllegalArgumentException iae) {
            Assert.fail("creating terminal category with empty name threw: "
                    + iae);
        }

        try {
            Category.nonTerminal(null);
            Assert.fail("able to create category with null name");
        } catch (final IllegalArgumentException ignored) {
        }
    }


    /**
     * Test method for {@link Category#isTerminal(Category)}.
     */
    @Test
    public final void testIsTerminal() {
        Assert.assertEquals(false, Category.isTerminal(A));

        Assert.assertEquals(true, Category.isTerminal(new ExactStringTerminal("a")));
    }

    /**
     * Test method for {@link Category#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
        Assert.assertEquals(A, Category.nonTerminal("A"));
        Assert.assertFalse(A.equals(Category.nonTerminal("B")));
    }

    /**
     * Test method for {@link Category#toString()}.
     */
    @Test
    public final void testToString() {
        Assert.assertEquals("A", A.toString());
    }

}
