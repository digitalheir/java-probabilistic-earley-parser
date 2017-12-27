package org.leibnizcenter.cfg.rule;

import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.RegexTerminal;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by maarten on 6-2-17.
 */
public class RuleParserTest {
    @Test
    public void lexRhs() throws Exception {
        assertEquals(
                Stream.of(
                        new RuleParser.RhsToken("AA"),
                        new RuleParser.RhsToken("/"),
                        new RuleParser.RhsToken("  "),
                        new RuleParser.RhsToken("/"),
                        new RuleParser.RhsToken("A"),
                        new RuleParser.RhsToken("\t"),
                        new RuleParser.RhsToken("A")
                ).collect(Collectors.toList()),
                RuleParser.lexRhs("AA/  /A\tA".toCharArray())
        );
    }

    @Test
    public void fromString() throws Exception {
        final RuleParser ruleParser = new RuleParser(s -> Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsensitiveStringTerminal(s), LogSemiring.get());

        assertEquals(
                Rule.create(LogSemiring.get(), new NonTerminal("S"), new NonTerminal("NP"), new NonTerminal("VP")),
                ruleParser.fromString("S -> NP VP")
        );
        assertEquals(
                Rule.create(LogSemiring.get(), new NonTerminal("S"), new CaseInsensitiveStringTerminal("Np"), new CaseInsensitiveStringTerminal("Vp")),
                ruleParser.fromString("S -> nP vP")
        );

        assertEquals(
                Rule.create(LogSemiring.get(), 0.5, new NonTerminal("S"), new CaseInsensitiveStringTerminal("Np"), new CaseInsensitiveStringTerminal("Vp")),
                ruleParser.fromString("S -> nP vP(0.5)")
        );
        final Rule rhs = ruleParser.fromString("S\t->/ [a]+A /ii \\/O/nn//n/e Thr/e\\e//");

        assertEquals(
                Rule.create(LogSemiring.get(),
                        new NonTerminal("S"),
                        rhs.getRight()[0],
                        new CaseInsensitiveStringTerminal("/O/nn//n/e"),
                        new NonTerminal("Thr/e\\e//")
                ), rhs);
    }

    @Test
    public void parseRhs() throws Exception {
        final Category[] rhs = new RuleParser(CaseInsensitiveStringTerminal::new, LogSemiring.get()).parseRHS("/ [a]+A /ii \\/O/nn//n/e Thr/e\\e//");

        assertTrue(rhs[0] instanceof RegexTerminal);
        final RegexTerminal r1 = (RegexTerminal) rhs[0];
        final RegexTerminal r2 = new RegexTerminal(" [a]+A ", Pattern.CASE_INSENSITIVE);
        assertEquals(r2.pattern.flags(), r1.pattern.flags());
        assertEquals(r2.pattern.pattern(), r1.pattern.pattern());
        assertEquals(new CaseInsensitiveStringTerminal("/O/nn//n/e"), rhs[1]);
        assertEquals(new CaseInsensitiveStringTerminal("Thr/e\\e//"), rhs[2]);
    }

    @Test
    public void parseErrorRule() throws Exception {
        final Rule rule = new RuleParser(s -> (NonLexicalToken.ERROR_SYMBOL.equals(s) ? NonLexicalToken.INSTANCE : new CaseInsensitiveStringTerminal(s)), LogSemiring.get()).fromString("S -> A " + NonLexicalToken.ERROR_SYMBOL + " B");
        assertTrue(rule instanceof LexicalErrorRule);
    }

    @Test
    public void parseNonErrorRule() throws Exception {
        final Rule rule = new RuleParser(s -> (NonLexicalToken.ERROR_SYMBOL.equals(s) ? NonLexicalToken.INSTANCE : new CaseInsensitiveStringTerminal(s)), LogSemiring.get()).fromString("S -> A B");
        assertFalse(rule instanceof LexicalErrorRule);
    }
}