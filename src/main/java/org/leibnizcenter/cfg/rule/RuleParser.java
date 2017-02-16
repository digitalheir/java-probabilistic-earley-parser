package org.leibnizcenter.cfg.rule;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ProbabilitySemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.ErrorSection;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.RegexTerminal;
import org.leibnizcenter.cfg.earleyparser.ParseTree;
import org.leibnizcenter.cfg.earleyparser.Parser;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Character.isWhitespace;

/**
 * <p>
 * For parsing a rule from a {@link String}
 * </p>
 * Created by maarten on 6-2-17.
 */
public class RuleParser {
    private static final NonTerminal RIGHT_HAND_SIDE = NonTerminal.of("S");
    private static final NonTerminal CATEGORY = NonTerminal.of("Category");
    private static final NonTerminal CATEGORY_CONTENT = NonTerminal.of("CategoryContent");
    private static final NonTerminal REGEX = NonTerminal.of("Regex");
    private static final NonTerminal REGEX_CONTENT = NonTerminal.of("RegexContent");
    private static final NonTerminal NON_REGEX_DELIMITER = NonTerminal.of("NonRegexDelimiter");
    private static final Pattern REGEX_MODIFIER = Pattern.compile("[xmsudi]+", Pattern.CASE_INSENSITIVE);

    private static final Terminal<String> REGEX_MODIFIERS = (token) -> REGEX_MODIFIER.matcher(token.obj).matches();
    private static final Terminal<String> REGEX_DELIMITER = (token) -> token instanceof RhsToken && ((RhsToken) token).isRegexDelimiter;
    private static final Terminal<String> WHITE_SPACE = (token) -> token instanceof RhsToken && ((RhsToken) token).isWhitespace;
    private static final Terminal<String> DANK_CONTENT = (token) -> token instanceof RhsToken
            && !((RhsToken) token).isWhitespace
            && !((RhsToken) token).isRegexDelimiter;

    private static final Grammar<String> grammarRHS = new Grammar.Builder<String>()
            .addRule(Rule.create(ProbabilitySemiring.get(), RIGHT_HAND_SIDE, REGEX))
            .addRule(Rule.create(ProbabilitySemiring.get(), RIGHT_HAND_SIDE, CATEGORY))
            .addRule(Rule.create(ProbabilitySemiring.get(), RIGHT_HAND_SIDE, RIGHT_HAND_SIDE, WHITE_SPACE, RIGHT_HAND_SIDE))

            .addRule(Rule.create(ProbabilitySemiring.get(), CATEGORY, CATEGORY_CONTENT))
            .addRule(Rule.create(ProbabilitySemiring.get(), CATEGORY_CONTENT, CATEGORY_CONTENT, REGEX_DELIMITER))
            .addRule(Rule.create(ProbabilitySemiring.get(), CATEGORY_CONTENT, DANK_CONTENT))
            .addRule(Rule.create(ProbabilitySemiring.get(), CATEGORY_CONTENT, CATEGORY_CONTENT, CATEGORY_CONTENT))

            .addRule(Rule.create(ProbabilitySemiring.get(), REGEX, REGEX_DELIMITER, REGEX_CONTENT, REGEX_DELIMITER))
            .addRule(Rule.create(ProbabilitySemiring.get(), REGEX, REGEX_DELIMITER, REGEX_CONTENT, REGEX_DELIMITER, REGEX_MODIFIERS))
            .addRule(Rule.create(ProbabilitySemiring.get(), REGEX_CONTENT, NON_REGEX_DELIMITER))
            .addRule(Rule.create(ProbabilitySemiring.get(), REGEX_CONTENT, REGEX_CONTENT, REGEX_CONTENT))
            .addRule(Rule.create(ProbabilitySemiring.get(), NON_REGEX_DELIMITER, DANK_CONTENT))
            .addRule(Rule.create(ProbabilitySemiring.get(), NON_REGEX_DELIMITER, WHITE_SPACE))

            .build();

    private static final Pattern RULE = Pattern.compile("\\s*([^\\s]+)\\s*(?:->|→)((?:\\s*[^\\s(]+\\s*)+)\\s*(?:\\(([0-9](?:[.,][0-9]+)?)\\))?\\s*");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final Function<String, Category> parseCategory;
    private final DblSemiring semiring;

    /**
     * @param parseCategory how to parse category string into category
     * @param semiring      semiring to use
     * @return Parsed rule
     */
    public RuleParser(Function<String, Category> parseCategory, DblSemiring semiring) {
        this.parseCategory = parseCategory;
        this.semiring = semiring;
    }

    static List<RhsToken> lexRhs(char[] chars) {
        List<RhsToken> l = new ArrayList<>();

        StringBuilder sb = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            final boolean isEscapedRegexDelimiter = (c == '/') && sb.length() > 0 && chars[i - 1] == '\\';
            if ((c == '/') && !(isEscapedRegexDelimiter)) {
                if (sb.length() > 0) {
                    l.add(new RhsToken(sb.toString()));
                    sb = new StringBuilder(chars.length - 1 - i);
                }
                l.add(new RhsToken(Character.toString(c)));
            } else if ((isWhitespace(c) && sb.length() > 0 && !isWhitespace(chars[i - 1]))
                    || (!isWhitespace(c) && sb.length() > 0 && isWhitespace(chars[i - 1]))) {

                l.add(new RhsToken(sb.toString()));
                sb = new StringBuilder(chars.length - 1 - i);

                if (isEscapedRegexDelimiter)
                    sb.deleteCharAt(sb.length() - 1);
                sb.append(c);
            } else {
                if (isEscapedRegexDelimiter)
                    sb.deleteCharAt(sb.length() - 1);
                sb.append(c);
            }
        }
        if (sb.length() > 0) l.add(new RhsToken(sb.toString()));
        return l;
    }

    private static RegexTerminal parseRegexTerminal(ParseTree parseTree) {
        final List<ParseTree> children = parseTree.children;

        Set<String> modifiers = new HashSet<>();
        int i = children.size() - 1;
        for (; i >= 0; i--) {
            final ParseTree child = children.get(i);
            if (child.category.equals(REGEX_DELIMITER)) break;

            final char[] chars = ((ParseTree.Token<String>) child).token.obj.toLowerCase(Locale.ROOT).toCharArray();
            for (char c : chars) modifiers.add(Character.toString(c));
        }
        int flag = 0;
        if (modifiers.contains("x")) flag = flag | Pattern.COMMENTS;
        if (modifiers.contains("m")) flag = flag | Pattern.MULTILINE;
        if (modifiers.contains("s")) flag = flag | Pattern.DOTALL;
        if (modifiers.contains("u")) flag = flag | Pattern.UNICODE_CASE;
        if (modifiers.contains("d")) flag = flag | Pattern.UNIX_LINES;
        if (modifiers.contains("i")) flag = flag | Pattern.CASE_INSENSITIVE;

        return new RegexTerminal(
                children.subList(1, i).stream()
                        .map(t -> ((ParseTree.Token<String>) t))
                        .map(t -> t.token.obj)
                        .collect(Collectors.joining()),
                flag);
    }

    private static ParseTree.FlattenOption getFlattenOption(List<ParseTree> parents, ParseTree parseTree) {
        final ParseTree parent = (!parents.isEmpty()) ? parents.get(parents.size() - 1) : null;
        if (parseTree instanceof ParseTree.Token && parent != null) {
            if (parent.category.equals(REGEX)) return ParseTree.FlattenOption.KEEP;
            else if (parent.category.equals(CATEGORY))
                return ((ParseTree.Token) parseTree).token instanceof RhsToken && ((RhsToken) ((ParseTree.Token) parseTree).token).isWhitespace
                        ? ParseTree.FlattenOption.REMOVE
                        : ParseTree.FlattenOption.KEEP;
            else
                return ParseTree.FlattenOption.REMOVE;
        } else if (Stream.of(REGEX, CATEGORY).filter(c -> parseTree.category.equals(c)).findAny().isPresent())
            return ParseTree.FlattenOption.KEEP;
        else if (parseTree instanceof ParseTree.NonToken)
            return ParseTree.FlattenOption.KEEP_ONLY_CHILDREN;
        else
            return ParseTree.FlattenOption.REMOVE;
    }

    Category[] parseRHS(String rhsStr) {
        ParseTree viterbi = new Parser(grammarRHS)
                .getViterbiParse(RIGHT_HAND_SIDE, lexRhs(rhsStr.toCharArray()));
        if (viterbi == null) throw new IllegalArgumentException("Could not parse grammar");
        viterbi = viterbi.flatten(RuleParser::getFlattenOption);
        List<Category> rhsList = viterbi.getChildren().stream()
                .map(this::getCategory)
                .collect(Collectors.toList());

        Category[] RHS = new Category[rhsList.size()];
        RHS = rhsList.toArray(RHS);
        return RHS;
    }

    private Category getCategory(ParseTree parseTree) {
        final boolean isSimpleCategory = parseTree.category.equals(CATEGORY);
        final boolean isRegex = parseTree.category.equals(REGEX);
        if (!isSimpleCategory && !isRegex) throw new IllegalStateException("Error while parsing grammar");
        return isRegex ? parseRegexTerminal(parseTree) : parseCategory.apply(parseTree.children.stream()
                .map(t -> (ParseTree.Token<String>) t)
                .map(t -> t.token.obj)
                .collect(Collectors.joining()));
    }

    public Rule fromString(String line) {
        Matcher m = RULE.matcher(line);
        if (!m.matches())
            throw new IllegalArgumentException("String was not a valid rule: " + line);
        else {
            final NonTerminal LHS = new NonTerminal(m.group(1));

            Category[] RHS = parseRHS(m.group(2).trim());

            final String prob = m.group(3);
            final double probability = semiring.fromProbability(prob == null ? 1.0 : Double.parseDouble(prob));
            boolean isErrorRule = (Stream.of(RHS).anyMatch(cat -> cat instanceof ErrorSection));

            if (isErrorRule) {
                return new SynchronizingRule(probability, LHS, RHS);
            } else {
                return new Rule(probability, LHS, RHS);
            }
        }
    }

    static class RhsToken extends Token<String> {
        final boolean isWhitespace;
        final boolean isRegexDelimiter;

        public RhsToken(String s) {
            super(s);
            this.isWhitespace = WHITESPACE.matcher(s).matches();
            this.isRegexDelimiter = s.length() == 1 && s.charAt(0) == '/';
        }

        @Override
        public String toString() {
            return super.obj;
        }
    }
}
