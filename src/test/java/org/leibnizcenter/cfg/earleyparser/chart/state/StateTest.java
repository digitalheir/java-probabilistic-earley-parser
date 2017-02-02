package org.leibnizcenter.cfg.earleyparser.chart.state;

import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ProbabilitySemiring;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.rule.Rule;

import static org.junit.Assert.*;

/**
 * Created by maarten on 27-1-17.
 */
public class StateTest {

    public static final CaseInsensitiveStringTerminal TERMINAL = new CaseInsensitiveStringTerminal("a");
    public static final CaseInsensitiveStringTerminal TERMINAL_2 = new CaseInsensitiveStringTerminal("b");
    public static final NonTerminal NON_TERMINAL = NonTerminal.of("S");
    public static final ProbabilitySemiring SEMIRING = ProbabilitySemiring.get();

    @Test
    public void create() throws Exception {

    }

    @Test
    public void isCompleted() throws Exception {
        State s1 = State.create(1, 1, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL));
        assertTrue(s1.isCompleted());

        State s2 = State.create(1, 1, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL));
        assertFalse(s2.isCompleted());
    }

    @Test
    public void isActive() throws Exception {
        State s1 = State.create(1, 1, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL));
        assertFalse(s1.isActive());

        State s2 = State.create(1, 1, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL));
        assertTrue(s2.isActive());
    }

    @Test
    public void getActiveCategory() throws Exception {
        State s1 = State.create(1, 1, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2));
        assertEquals(TERMINAL_2, s1.getActiveCategory());

        State s2 = State.create(1, 1, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL));
        assertNull(s2.getActiveCategory());
    }

    @Test
    public void advanceDot() throws Exception {
        State s1 = State.create(0, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2));
        assertEquals(2, s1.advanceDot());
    }

    @Test
    public void testequals() throws Exception {
        assertEquals(
                State.create(0, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2)),
                State.create(0, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2))
        );
        assertNotEquals(
                State.create(1, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2)),
                State.create(0, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2))
        );
    }

    @Test
    public void testhashCode() throws Exception {
        assertTrue(
                State.create(0, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2)).hashCode() ==
                        State.create(0, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2)).hashCode()
        );
        assertFalse(
                State.create(1, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2)).hashCode() ==
                        State.create(0, 0, 1, Rule.create(SEMIRING, NON_TERMINAL, TERMINAL, TERMINAL_2)).hashCode()
        );
    }

}