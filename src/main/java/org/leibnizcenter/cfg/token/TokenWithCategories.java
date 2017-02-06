package org.leibnizcenter.cfg.token;

import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.Iterator;
import java.util.Set;

/**
 * <p>
 * This class represents a token with all applicable categories.
 * Note that this is <em>not</em> the place to mess with which categories apply to the
 * given token; that should be a function in {@link Terminal}. This is just the place where all applicable categories
 * get computed.
 * </p>
 * Created by maarten on 11-1-17.
 */
public class TokenWithCategories<T> {
    public final Token<T> token;
    public final Set<Terminal<T>> categories;

    private TokenWithCategories(Token<T> token, Set<Terminal<T>> categories) {
        this.token = token;
        this.categories = categories;
    }

    /**
     * @param tokens  Iterable of tokens
     * @param grammar Grammar that contains {@link Terminal Terminals} that recognize tokens
     * @return the same iterable, but with additional information: what categories the given token adhere to,
     * as defined by {@link Terminal} types in the grammar.
     */
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
                return new TokenWithCategories<>(token, grammar.getCategories(token));
            }
        };
    }
}
