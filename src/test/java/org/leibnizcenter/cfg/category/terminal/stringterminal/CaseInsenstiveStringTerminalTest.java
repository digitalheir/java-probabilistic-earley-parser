package org.leibnizcenter.cfg.category.terminal.stringterminal;

import org.junit.Test;
import org.leibnizcenter.cfg.token.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by maarten on 27-1-17.
 */
public class CaseInsenstiveStringTerminalTest {
    @Test
    public void hasCategory() throws Exception {
        final CaseInsenstiveStringTerminal aaA = new CaseInsenstiveStringTerminal("aaA");
        assertTrue(aaA.hasCategory(Token.of("Aaa")));
    }

    @Test
    public void testEquals() throws Exception {
        final CaseInsenstiveStringTerminal Aaa = new CaseInsenstiveStringTerminal("Aaa");
        final CaseInsenstiveStringTerminal aaA = new CaseInsenstiveStringTerminal("aaA");

        assertEquals(Aaa, aaA);
        assertTrue(Aaa.hashCode() == aaA.hashCode());
    }
}