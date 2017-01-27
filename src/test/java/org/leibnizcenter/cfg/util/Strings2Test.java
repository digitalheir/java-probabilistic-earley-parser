package org.leibnizcenter.cfg.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class Strings2Test {
    @Test
    public void isNotNullOrEmpty() throws Exception {
        assertEquals(false, Strings2.isNullOrEmpty("a"));
    }

    @Test
    public void isEmpty() throws Exception {
        assertEquals(true, Strings2.isNullOrEmpty(""));
    }

    @Test
    public void isNull() throws Exception {
        assertEquals(true, Strings2.isNullOrEmpty(null));
    }

}