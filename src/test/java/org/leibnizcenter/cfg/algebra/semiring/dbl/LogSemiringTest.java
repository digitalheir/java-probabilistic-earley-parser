package org.leibnizcenter.cfg.algebra.semiring.dbl;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test Log Semiring
 *
 * Created by Maarten on 31-7-2016.
 */
public class LogSemiringTest {
    private static final LogSemiring semiring = new LogSemiring();

    @Test
    public void plus() throws Exception {
        Assert.assertEquals(0.8,
                semiring.toProbability(semiring.plus(semiring.fromProbability(0.3), semiring.fromProbability(0.5))),
                0.01
        );
    }

    @Test
    public void times() throws Exception {
        Assert.assertEquals(0.09,
                semiring.toProbability(semiring.times(semiring.fromProbability(0.3), semiring.fromProbability(0.3))),
                0.01
        );
    }

    @Test
    public void zero() throws Exception {
        Assert.assertEquals(
                semiring.toProbability(semiring.zero()),
                0.0, 0.01);
    }

    @Test
    public void one() throws Exception {
        Assert.assertEquals(
                semiring.toProbability(semiring.one()),
                1.0, 0.01);
    }

    @Test
    public void member() throws Exception {
        Assert.assertTrue(semiring.member(semiring.zero()));
        Assert.assertTrue(semiring.member(semiring.one()));
        Assert.assertTrue(semiring.member(semiring.fromProbability(0.0)));
        // Note: this one is contentious, because we normally only want to deal with probs [0.0, 1.0]
        Assert.assertTrue(semiring.member(semiring.fromProbability(500.0)));

        Assert.assertFalse(semiring.member(semiring.fromProbability(-0.5)));
        Assert.assertFalse(semiring.member(semiring.fromProbability(-1)));
        Assert.assertFalse(semiring.member(semiring.fromProbability(-500.0)));
    }

    @Test
    public void fromProbability() throws Exception {

    }

    @Test
    public void toProbability() throws Exception {

    }

}