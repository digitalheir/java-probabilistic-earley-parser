package org.leibnizcenter.cfg.token;

import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A token with all applicable categories
 * <p>
 * Created by maarten on 11-1-17.
 */
public class TokenWithCategories<T> {
    private final Token<T> token;
    private final Set<Terminal<T>> categories;

    @SafeVarargs
    public TokenWithCategories(Token<T> token, Terminal<T>... categories) {
        this(token, Arrays.stream(categories).map(t -> {
            if (!t.hasCategory(token))
                throw new Error("Token " + token + " did not have category " + t);
            else return t;
        }).collect(Collectors.toSet()));
    }

    private TokenWithCategories(Token<T> token, Set<Terminal<T>> categories) {
        this.token = token;
        this.categories = categories;
    }

    /**
     * Runs in O(N) for N is the number of terminals
     *
     * @return set of all terminals that match given token, usually a singleton set.
     */
    private static <T> Set<Terminal<T>> getCategories(Token<T> token, Grammar<T> g) {
        return g.getTerminals().stream().filter(category -> category.hasCategory(token)).collect(Collectors.toSet());
    }

    public static <E> Iterable<TokenWithCategories<E>> from(Iterable<Token<E>> tokens, Grammar<E> grammar) {
        final Iterator<Token<E>> iterator = tokens.iterator();
        return () -> new Iterator<TokenWithCategories<E>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public TokenWithCategories<E> next() {
                final Token<E> token = iterator.next();
                return new TokenWithCategories<>(token, TokenWithCategories.getCategories(token, grammar));
            }
        };
    }

    public Token<T> getToken() {
        return token;
    }

    public Set<Terminal<T>> getCategories() {
        return categories;
    }
}
