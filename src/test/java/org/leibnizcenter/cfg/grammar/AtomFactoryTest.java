package org.leibnizcenter.cfg.grammar;

import org.junit.Test;
import org.leibnizcenter.cfg.earleyparser.Atom;

import static org.junit.Assert.assertFalse;

/**
 * Created by maarten on 27-1-17.
 */
public class AtomFactoryTest {
    @Test
    public void getAtom() throws Exception {
        final AtomFactory am = new AtomFactory();
        final Atom got = am.getAtom(1);
        assertFalse(got == new Atom(1));
        //assertTrue(got == am.getAtom(1));
    }

}