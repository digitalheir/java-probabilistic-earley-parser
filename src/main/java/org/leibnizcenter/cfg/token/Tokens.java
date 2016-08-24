package org.leibnizcenter.cfg.token;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.terminal.Terminal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    @SuppressWarnings("unchecked")
    public static <T> boolean hasCategory(Token<T> token, Category category) {
        return Category.isTerminal(category) && ((Terminal) category).hasCategory(token);
    }

    @SafeVarargs
    public static <T> List<Token<T>> tokenize(T... objs) {
        return Arrays.stream(objs).map(Token::new).collect(Collectors.toList());
    }

    public static <T> List<Token<T>> tokenize(Spliterator<T> objs, boolean parallel) {
        return StreamSupport.stream(objs, parallel).map(Token::new).collect(Collectors.toList());
    }

    public static <T> List<Token<T>> tokenize(Spliterator<T> objs) {
        return StreamSupport.stream(objs, false).map(Token::new).collect(Collectors.toList());
    }

    public static <T> List<Token<T>> tokenize(Collection<T> objs) {
        return objs.stream().map(Token::new).collect(Collectors.toList());
    }
}
