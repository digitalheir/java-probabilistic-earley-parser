package org.leibnizcenter.cfg.earleyparser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by maarten on 27-1-17.
 */
public class ExpressionWrapperTest {
    @Test
    public void getExpression() throws Exception {
        ExpressionWrapper w = new ExpressionWrapper(new Atom(1));
        assertEquals(new Atom(1), w.getExpression());
    }

    @Test
    public void setExpression() throws Exception {
        ExpressionWrapper w = new ExpressionWrapper(new Atom(1));
        assertEquals(new Atom(1), w.getExpression());
        w.setExpression(new Atom(3));
        assertEquals(new Atom(3), w.getExpression());
    }

    @Test(expected = IllegalStateException.class)
    public void setExpressionIllegal() throws Exception {
        ExpressionWrapper w = new ExpressionWrapper(new Atom(1));
        w.resolveFinal();
        w.setExpression(null);
    }

    @Test
    public void resolve() throws Exception {
        assertTrue(1.0 == new ExpressionWrapper(new Atom(1)).resolve());
    }

}