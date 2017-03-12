package org.leibnizcenter.cfg.category.nonterminal;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.leibnizcenter.cfg.category.Category.START;

/**
 */
public class NonTerminalTest {


    @Test
    public void testEquals() throws Exception {
        assertNotEquals(START, NonTerminal.of(START.name));
        assertEquals(NonTerminal.of(START.name), NonTerminal.of(START.name));
    }

    @Test
    public void testHashCode() throws Exception {
        assertFalse(START.hashCode() == NonTerminal.of(START.name).hashCode());
        assertFalse(START.hashCode() == NonTerminal.of("123").hashCode());
    }
}