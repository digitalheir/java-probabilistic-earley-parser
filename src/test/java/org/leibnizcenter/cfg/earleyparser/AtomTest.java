package org.leibnizcenter.cfg.earleyparser;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by maarten on 27-1-17.
 */
public class AtomTest {
    @Test
    public void testResolve() throws Exception {
        assertTrue(0.356 == new Atom(0.356).resolveFinal());
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(new Atom(0.3560), new Atom(0.356));
        assertNotEquals(new Atom(0.357), new Atom(0.356));

    }

    @Test
    public void testHashCode() throws Exception {
        assertTrue(new Atom(0.3560).hashCode() == new Atom(0.356).hashCode());
        assertFalse(new Atom(0.357).hashCode() == new Atom(0.356).hashCode());
    }

}