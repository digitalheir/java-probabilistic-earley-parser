package org.leibnizcenter.cfg.token;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test token
 * Created by maarten on 27-1-17.
 */
public class TokenTest {
    private Token token1 = Token.of("tôken");

    @Test
    public void from() throws Exception {
        assertEquals(Token.from("tôken"), token1);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("tôken", token1.toString());
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(new Token<>("tôken").hashCode(), token1.hashCode());
    }

}