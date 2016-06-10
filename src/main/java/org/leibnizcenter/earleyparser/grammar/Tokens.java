package org.leibnizcenter.earleyparser.grammar;

import org.leibnizcenter.earleyparser.grammar.categories.Category;
import org.leibnizcenter.earleyparser.grammar.categories.Terminal;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility classes for {@link Token}
 * <p>
 * Created by maarten on 10-6-16.
 */
public final class Tokens {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private Tokens() {
        throw new Error();
    }

    /**
     * Convenience method for tokenizing a string on whitespace.
     **/
    public static List<Token<String>> tokenize(String tokens) {
        return tokenize(tokens, WHITESPACE, Token::new);
    }

    /**
     * Convenience method for tokenizing a string.
     **/
    public static List<Token<String>> tokenize(String tokens, String splitOn) {
        return tokenize(tokens, splitOn, Token::new);
    }

    /**
     * Convenience method for tokenizing a string.
     **/
    public static <T> List<Token<T>> tokenize(String tokens, String splitOn, Function<String, Token<T>> mapper) {
        return Arrays.stream(tokens.split(splitOn))
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Convenience method for tokenizing a string.
     **/
    public static <T> List<Token<T>> tokenize(String tokens, Pattern splitOn, Function<String, Token<T>> mapper) {
        return Arrays.stream(splitOn.split(tokens))
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static boolean hasCategory(Token token, Category category) {
        return category instanceof Terminal && ((Terminal) category).hasCategory(token);
    }
}
