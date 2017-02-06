package org.leibnizcenter.cfg.rule;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ProbabilitySemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.ParseTree;
import org.leibnizcenter.cfg.earleyparser.Parser;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Character.isWhitespace;

/**
 * Created by maarten on 6-2-17.
 */
public class RuleParser {
    private static final NonTerminal RHS = NonTerminal.of("S");
    private static final NonTerminal Cate = NonTerminal.of("Category");
    private static final NonTerminal Regex = NonTerminal.of("Regex");
    private static final NonTerminal RegexContent = NonTerminal.of("RegexContent");
    private static final NonTerminal NonRegexDelimiter = NonTerminal.of("NonRegexDelimiter");
    /**
     * Any category that start alphanumerically
     */
    private static final Pattern SIMPLE_CATEGORY = Pattern.compile("^\\p{Alnum}.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern REGEX_MODIFIER = Pattern.compile("[gi]+", Pattern.CASE_INSENSITIVE);
    private static final Terminal<String> CategorySimple = (token) -> SIMPLE_CATEGORY.matcher(token.obj).matches();

    private static final Terminal<String> RegexModifiers = (token) -> REGEX_MODIFIER.matcher(token.obj).matches();
    private static final Terminal<String> RegexDelimiter = (token) -> token instanceof RhsToken && ((RhsToken) token).isRegexDelimiter;
    private static final Terminal<String> WhiteSpace = (token) -> token instanceof RhsToken && ((RhsToken) token).isWhitespace;
    private static final Terminal<String> DankContent = (token) -> token instanceof RhsToken
            && !((RhsToken) token).isWhitespace
            && !((RhsToken) token).isRegexDelimiter;

    private static final Grammar<String> grammarRHS = new Grammar.Builder<String>()
            .addRule(Rule.create(ProbabilitySemiring.get(), RHS, Regex))
            .addRule(Rule.create(ProbabilitySemiring.get(), RHS, Cate))
            .addRule(Rule.create(ProbabilitySemiring.get(), RHS, RHS, WhiteSpace, RHS))

            .addRule(Rule.create(ProbabilitySemiring.get(), Cate, DankContent))
            .addRule(Rule.create(ProbabilitySemiring.get(), Cate, Cate, Cate))

            .addRule(Rule.create(ProbabilitySemiring.get(), Regex, RegexDelimiter, RegexContent, RegexDelimiter))
            .addRule(Rule.create(ProbabilitySemiring.get(), Regex, RegexDelimiter, RegexContent, RegexDelimiter, RegexModifiers))
            .addRule(Rule.create(ProbabilitySemiring.get(), RegexContent, NonRegexDelimiter))
            .addRule(Rule.create(ProbabilitySemiring.get(), RegexContent, RegexContent, RegexContent))
            .addRule(Rule.create(ProbabilitySemiring.get(), NonRegexDelimiter, DankContent))
            .addRule(Rule.create(ProbabilitySemiring.get(), NonRegexDelimiter, WhiteSpace))

            .build();

    private static final Pattern RULE = Pattern.compile("\\s*([^\\s]+)\\s*(?:->|→)((?:\\s*[^\\s(]+\\s*)+)\\s*(?:\\(([0-9](?:[.,][0-9]+)?)\\))?\\s*");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private RuleParser() {
    }

    static List<RhsToken> lexRhs(char[] chars) {
        List<RhsToken> l = new ArrayList<>();

        StringBuilder sb = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if ((c == '/')) {
                if (sb.length() > 0) {
                    l.add(new RhsToken(sb.toString()));
                    sb = new StringBuilder(chars.length - 1 - i);
                }
                l.add(new RhsToken(Character.toString(c)));
            } else if ((isWhitespace(c) && sb.length() > 0 && !isWhitespace(chars[i - 1]))
                    || (!isWhitespace(c) && sb.length() > 0 && isWhitespace(chars[i - 1]))) {

                l.add(new RhsToken(sb.toString()));
                sb = new StringBuilder(chars.length - 1 - i);
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) l.add(new RhsToken(sb.toString()));
        return l;
    }

    static Category[] parseRHS(Function<String, Category> parseCategory, String rhsStr) {
        ParseTree viterbi = new Parser(grammarRHS).getViterbiParse(RHS, lexRhs(rhsStr.toCharArray())).flatten(Regex, Cate);
        List<Category> rhsList = Arrays.stream(
                WHITESPACE.split(rhsStr))
                .map(parseCategory)
                .collect(Collectors.toList());
        Category[] RHS = new Category[rhsList.size()];
        RHS = rhsList.toArray(RHS);
        return RHS;
    }

    public static Rule fromString(String line, Function<String, Category> parseCategory, DblSemiring semiring) {
        Matcher m = RULE.matcher(line);
        if (!m.matches())
            throw new IllegalArgumentException("String was not a valid rule: " + line);
        else {
            final NonTerminal LHS = new NonTerminal(m.group(1));

            Category[] RHS = parseRHS(parseCategory, m.group(2).trim());

            final String prob = m.group(3);
            final double probability = semiring.fromProbability(prob == null ? 1.0 : Double.parseDouble(prob));
            return new Rule(
                    probability,
                    LHS,
                    RHS
            );
        }
    }

    static class RhsToken extends Token<String> {
        private final boolean isWhitespace;
        private final boolean isRegexDelimiter;

        public RhsToken(String s) {
            super(s);
            this.isWhitespace = WHITESPACE.matcher(s).matches();
            this.isRegexDelimiter = s.charAt(0) == '/';
        }

        @Override
        public String toString() {
            return super.obj;
        }
    }
}
