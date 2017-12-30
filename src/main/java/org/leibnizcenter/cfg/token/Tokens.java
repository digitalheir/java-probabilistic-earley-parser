package org.leibnizcenter.cfg.token;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility functions for {@link Token}
 *
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
    public static List<Token<String>> tokenize(final String tokens) {
        return tokenize(tokens.trim(), WHITESPACE, Token::new);
    }

    /**
     * Convenience method for tokenizing a string.
     **/
    @SuppressWarnings("unused")
    public static List<Token<String>> tokenize(final String tokens, final String splitOn) {
        return tokenize(tokens, splitOn, Token::new);
    }

    /**
     * Convenience method for tokenizing a string.
     **/
    @SuppressWarnings("WeakerAccess")
    public static <T> List<Token<T>> tokenize(final String tokens, final String splitOn, final Function<String, Token<T>> mapper) {
        return Arrays.stream(tokens.split(splitOn))
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Convenience method for tokenizing a string.
     **/
    @SuppressWarnings("WeakerAccess")
    public static <T> List<Token<T>> tokenize(final String tokens, final Pattern splitOn, final Function<String, Token<T>> mapper) {
        return Arrays.stream(splitOn.split(tokens))
                .map(mapper)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<Token<T>> tokenize(final T... objs) {
        return Arrays.stream(objs).map(Token::new).collect(Collectors.toList());
    }


    @SuppressWarnings("unused")
    public static <T> List<Token<T>> tokenize(final Collection<T> objs) {
        return objs.stream().map(Token::new).collect(Collectors.toList());
    }
}
