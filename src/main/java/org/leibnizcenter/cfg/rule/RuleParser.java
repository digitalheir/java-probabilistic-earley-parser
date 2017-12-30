package org.leibnizcenter.cfg.rule;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
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
 *<p>
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
            .addRule(0.4, RIGHT_HAND_SIDE, REGEX)
            .addRule(0.3, RIGHT_HAND_SIDE, CATEGORY)
            .addRule(0.3, RIGHT_HAND_SIDE, RIGHT_HAND_SIDE, WHITE_SPACE, RIGHT_HAND_SIDE)

            .addRule(1.0, CATEGORY, CATEGORY_CONTENT)
            .addRule(0.4, CATEGORY_CONTENT, CATEGORY_CONTENT, REGEX_DELIMITER)
            .addRule(0.3, CATEGORY_CONTENT, DANK_CONTENT)
            .addRule(0.3, CATEGORY_CONTENT, CATEGORY_CONTENT, CATEGORY_CONTENT)

            .addRule(0.5, REGEX, REGEX_DELIMITER, REGEX_CONTENT, REGEX_DELIMITER)
            .addRule(0.5, REGEX, REGEX_DELIMITER, REGEX_CONTENT, REGEX_DELIMITER, REGEX_MODIFIERS)
            .addRule(0.5, REGEX_CONTENT, NON_REGEX_DELIMITER)
            .addRule(0.5, REGEX_CONTENT, REGEX_CONTENT, REGEX_CONTENT)
            .addRule(0.5, NON_REGEX_DELIMITER, DANK_CONTENT)
            .addRule(0.5, NON_REGEX_DELIMITER, WHITE_SPACE)

            .build();

    private static final Pattern RULE = Pattern.compile("\\s*([^\\s]+)\\s*(?:->|→)((?:\\s*[^\\s(]+\\s*)+)\\s*(?:\\(([0-9](?:[.,][0-9]+)?)\\))?\\s*");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final Function<String, Category> parseCategory;
    private final DblSemiring semiring;

    /**
     * @param parseCategory how to parse category string into category
     * @param semiring      semiring to use
     */
    public RuleParser(final Function<String, Category> parseCategory, final DblSemiring semiring) {
        this.parseCategory = parseCategory;
        this.semiring = semiring;
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    static Iterable<Token<String>> lexRhs(final char[] chars) {
        final List<Token<String>> l = new ArrayList<>();

        StringBuilder sb = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
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

    private static RegexTerminal parseRegexTerminal(final ParseTree parseTree) {
        final List<ParseTree> children = parseTree.children;

        final Set<String> modifiers = new HashSet<>();
        int i = children.size() - 1;
        for (; i >= 0; i--) {
            final ParseTree child = children.get(i);
            if (child.category.equals(REGEX_DELIMITER)) break;

            //noinspection unchecked
            final char[] chars = ((ParseTree.Leaf<String>) child).token.obj.toLowerCase(Locale.ROOT).toCharArray();
            for (final char c : chars) modifiers.add(Character.toString(c));
        }
        int flag = 0;
        if (modifiers.contains("x")) flag = flag | Pattern.COMMENTS;
        if (modifiers.contains("m")) flag = flag | Pattern.MULTILINE;
        if (modifiers.contains("s")) flag = flag | Pattern.DOTALL;
        if (modifiers.contains("u")) flag = flag | Pattern.UNICODE_CASE;
        if (modifiers.contains("d")) flag = flag | Pattern.UNIX_LINES;
        if (modifiers.contains("i")) flag = flag | Pattern.CASE_INSENSITIVE;

        //noinspection unchecked
        return new RegexTerminal(
                children.subList(1, i).stream()
                        .map(t -> ((ParseTree.Leaf<String>) t))
                        .map(t -> t.token.obj)
                        .collect(Collectors.joining()),
                flag);
    }

    private static ParseTree.FlattenOption getFlattenOption(final List<ParseTree> parents, final ParseTree parseTree) {
        final ParseTree parent = (!parents.isEmpty()) ? parents.get(parents.size() - 1) : null;
        if (parseTree instanceof ParseTree.Leaf && parent != null) {
            if (parent.category.equals(REGEX)) return ParseTree.FlattenOption.KEEP;
            else if (parent.category.equals(CATEGORY))
                return ((ParseTree.Leaf) parseTree).token instanceof RhsToken && ((RhsToken) ((ParseTree.Leaf) parseTree).token).isWhitespace
                        ? ParseTree.FlattenOption.REMOVE
                        : ParseTree.FlattenOption.KEEP;
            else
                return ParseTree.FlattenOption.REMOVE;
        } else if (Stream.of(REGEX, CATEGORY).anyMatch(parseTree.category::equals))
            return ParseTree.FlattenOption.KEEP;
        else if (parseTree instanceof ParseTree.NonLeaf)
            return ParseTree.FlattenOption.KEEP_ONLY_CHILDREN;
        else
            return ParseTree.FlattenOption.REMOVE;
    }

    Category[] parseRHS(final String rhsStr) {
        ParseTree viterbi = new Parser<>(grammarRHS)
                .getViterbiParse(RIGHT_HAND_SIDE, lexRhs(rhsStr.toCharArray()));
        if (viterbi == null) throw new IllegalArgumentException("Could not parse grammar");
        viterbi = viterbi.flatten(RuleParser::getFlattenOption);
        final List<Category> rhsList = viterbi.getChildren().stream()
                .map(this::getCategory)
                .collect(Collectors.toList());

        Category[] RHS = new Category[rhsList.size()];
        RHS = rhsList.toArray(RHS);
        return RHS;
    }

    private Category getCategory(final ParseTree parseTree) {
        final boolean isSimpleCategory = parseTree.category.equals(CATEGORY);
        final boolean isRegex = parseTree.category.equals(REGEX);
        if (!isSimpleCategory && !isRegex) throw new IllegalStateException("Error while parsing grammar");
        //noinspection unchecked
        return isRegex ? parseRegexTerminal(parseTree) : parseCategory.apply(parseTree.children.stream()
                .map(t -> (ParseTree.Leaf<String>) t)
                .map(t -> t.token.obj)
                .collect(Collectors.joining()));
    }

    public Rule fromString(final String line) {
        final Matcher m = RULE.matcher(line);
        if (!m.matches())
            throw new IllegalArgumentException("String was not a valid rule: " + line);
        else {
            final NonTerminal LHS = new NonTerminal(m.group(1));

            final Category[] RHS = parseRHS(m.group(2).trim());

            final String prob = m.group(3);
            final double probability = prob == null ? 1.0 : Double.parseDouble(prob);
            final double semiringElement = semiring.fromProbability(probability);
            final boolean isErrorRule = (Stream.of(RHS).anyMatch(cat -> cat instanceof NonLexicalToken));

            if (isErrorRule) {
                return new LexicalErrorRule(probability, semiringElement, LHS, RHS);
            } else {
                return new Rule(probability, semiringElement, LHS, RHS);
            }
        }
    }

    static class RhsToken extends Token<String> {
        final boolean isWhitespace;
        final boolean isRegexDelimiter;

        public RhsToken(final String s) {
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
