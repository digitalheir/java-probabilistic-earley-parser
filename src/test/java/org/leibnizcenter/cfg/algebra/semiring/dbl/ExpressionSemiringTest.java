package org.leibnizcenter.cfg.algebra.semiring.dbl;

import org.junit.Test;
import org.leibnizcenter.cfg.earleyparser.Atom;

import static org.junit.Assert.*;

/**
 * Created by maarten on 27-1-17.
 */
public class ExpressionSemiringTest {
    @Test
    public void times() throws Exception {
        assertEquals(new Atom(2), ProbabilitySemiring.get().times(new Atom(1), new Atom(2), new Atom(1)));
        assertNotEquals(new Atom(6), ProbabilitySemiring.get().times(new Atom(1), new Atom(2), new Atom(3)));
        assertEquals(ProbabilitySemiring.get().times(new Atom(2), new Atom(3), new Atom(1)), ProbabilitySemiring.get().times(new Atom(1), new Atom(2), new Atom(3)));
        assertTrue(6.0 == ProbabilitySemiring.get().times(new Atom(2), new Atom(3), new Atom(1)).resolveFinal());
    }

    @Test
    public void plus() throws Exception {
        assertEquals(new Atom(2), ProbabilitySemiring.get().plus(new Atom(0), new Atom(2)));
        assertNotEquals(new Atom(4), ProbabilitySemiring.get().plus(new Atom(1), new Atom(2)));
        assertEquals(ProbabilitySemiring.get().plus(new Atom(2), new Atom(0)), ProbabilitySemiring.get().plus(new Atom(0), new Atom(2)));
        assertTrue(5.0 == ProbabilitySemiring.get().plus(new Atom(2), new Atom(3)).resolveFinal());
    }

}