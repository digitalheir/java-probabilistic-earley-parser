package org.leibnizcenter.cfg.category.terminal.stringterminal;

import org.junit.Test;
import org.leibnizcenter.cfg.token.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by maarten on 27-1-17.
 */
public class CaseInsensitiveStringTerminalTest {
    @Test
    public void hasCategory() throws Exception {
        final CaseInsensitiveStringTerminal aaA = new CaseInsensitiveStringTerminal("aaA");
        assertTrue(aaA.hasCategory(Token.of("Aaa")));
    }

    @Test
    public void testEquals() throws Exception {
        final CaseInsensitiveStringTerminal Aaa = new CaseInsensitiveStringTerminal("Aaa");
        final CaseInsensitiveStringTerminal aaA = new CaseInsensitiveStringTerminal("aaA");

        assertEquals(Aaa, aaA);
        assertTrue(Aaa.hashCode() == aaA.hashCode());
    }
}