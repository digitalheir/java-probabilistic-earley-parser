package org.leibnizcenter.cfg.grammar;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.rule.RuleParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.regex.Pattern;

public class GrammarParser {
    static final Function<String, Category> STRING_CATEGORY_FUNCTION = s -> NonLexicalToken.ERROR_SYMBOL.equals(s) ? (Terminal) NonLexicalToken.INSTANCE : Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsensitiveStringTerminal(s);
    static final Pattern NEWLINE = Pattern.compile("\\n");
    static final Pattern TRAILING_COMMENT = Pattern.compile("#.*$");

    static Grammar<String> fromString(final InputStream inputStream, final Charset charset, final Function<String, Category> parseCategory, final DblSemiring semiring) throws IOException {
        final Grammar.Builder<String> b = new Grammar.Builder<>();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
        final RuleParser ruleParser = new RuleParser(parseCategory, semiring);

        String line = reader.readLine();
        final Collection<Rule> rules = new HashSet<>();
        while (line != null) {
            line = TRAILING_COMMENT.matcher(line).replaceAll("").trim();
            if (!line.isEmpty())
                rules.add(ruleParser.fromString(line));
            line = reader.readLine();
        }
        b.addRules(rules);
        return b.build();
    }
}
