package org.leibnizcenter.cfg.algebra.semiring.dbl;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests for probability semiring
 * Created by maarten on 27-1-17.
 */
public class ProbabilitySemiringTest {

    public static final ProbabilitySemiring s = ProbabilitySemiring.get();

    @Test
    public void plus() throws Exception {
        assertTrue(3 == s.plus(1, 2));
    }

    @Test
    public void times() throws Exception {
        assertTrue(6 == s.times(3, 2));
    }

    @Test
    public void zero() throws Exception {
        assertTrue(0.0 == s.zero());

    }

    @Test
    public void one() throws Exception {
        assertTrue(1.0 == s.one());

    }

    @Test
    public void fromProbability() throws Exception {
        assertTrue(0.6 == s.fromProbability(0.6));
    }

    @Test
    public void toProbability() throws Exception {
        assertTrue(0.6 == s.toProbability(0.6));

    }


}