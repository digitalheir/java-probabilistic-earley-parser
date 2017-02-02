package org.leibnizcenter.cfg.rule;

import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsenstiveStringTerminal;

import static org.junit.Assert.assertEquals;

/**
 * TODO implement tests
 * <p>
 * Created by maarten on 18-1-17.
 */
public class RuleTest {
    @Test
    public void create() throws Exception {

    }

    @Test
    public void create1() throws Exception {

    }

    @Test
    public void create2() throws Exception {

    }

    @Test
    public void getActiveCategory() throws Exception {

    }

    @Test
    public void isPassive() throws Exception {

    }

    @Test
    public void getLeft() throws Exception {

    }

    @Test
    public void getRight() throws Exception {

    }

    @Test
    public void equals() throws Exception {

    }

    @Test
    public void hashCodeTest() throws Exception {

    }

    @Test
    public void toStringTest() throws Exception {

    }

    @Test
    public void toString1() throws Exception {

    }

    @Test
    public void getScore() throws Exception {

    }

    @Test
    public void isUnitProduction() throws Exception {

    }

    @Test
    public void parse() throws Exception {
        assertEquals(
                Rule.create(LogSemiring.get(), new NonTerminal("S"), new NonTerminal("NP"), new NonTerminal("VP")),
                Rule.fromString("S -> NP VP",
                        s -> Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsenstiveStringTerminal(s),
                        LogSemiring.get()
                )
        );
        assertEquals(
                Rule.create(LogSemiring.get(), new NonTerminal("S"), new CaseInsenstiveStringTerminal("Np"), new CaseInsenstiveStringTerminal("Vp")),
                Rule.fromString("S -> nP vP",
                        s -> Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsenstiveStringTerminal(s),
                        LogSemiring.get()
                )
        );

        assertEquals(
                Rule.create(LogSemiring.get(), 0.5, new NonTerminal("S"), new CaseInsenstiveStringTerminal("Np"), new CaseInsenstiveStringTerminal("Vp")),
                Rule.fromString("S -> nP vP(0.5)",
                        s -> Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsenstiveStringTerminal(s),
                        LogSemiring.get()
                )
        );
    }

}