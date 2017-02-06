package org.leibnizcenter.cfg.rule;

import org.junit.Test;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

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

    }

    @Test
    public void parseRhs() throws Exception {
        RuleParser.parseRHS(s -> new CaseInsensitiveStringTerminal(s), "/ [a]+A / patrone Patrone");
    }

}