package org.leibnizcenter.cfg.token;

import com.google.common.collect.ImmutableSet;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.terminal.Terminal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A token with all applicable categories
 *
 * Created by maarten on 11-1-17.
 */
public class TokenWithCategories<T> {
    private final Token<T> token;
    private final Set<Terminal<T>> categories;

    @SafeVarargs
    public TokenWithCategories(Token<T> token, Terminal<T>... categories) {
        this(token, Arrays.stream(categories).map(t -> {
            if(!t.hasCategory(token))
                throw new Error("Token "+token+" did not have category "+t);
            else return t;
        }).collect(Collectors.toSet()));
    }

    private TokenWithCategories(Token<T> token, Set<Terminal<T>> categories) {
        this.token = token;
        this.categories=categories;
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> ImmutableSet<Terminal<T>> getCategories(Token<T> token, Grammar<T> g) {
        return ImmutableSet.copyOf(g.getTerminals().stream().filter(category -> category.hasCategory(token)).collect(Collectors.toSet()));
    }

    public Token<T> getToken() {
        return token;
    }

    public Set<Terminal<T>> getCategories() {
        return categories;
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
}
