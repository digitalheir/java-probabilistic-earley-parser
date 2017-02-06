package org.leibnizcenter.cfg.token;

import org.leibnizcenter.cfg.category.terminal.Terminal;

/**
 * <p>
 * Represents an abstract word. A list of tokens makes a sentence. A token can be of a category / type,
 * which must be a {@link Terminal}.  {@link Terminal}
 * implements a function that returns, given a token, whether given token is of that {@link org.leibnizcenter.cfg.category.Category Category}.
 * </p>
 * Created by Maarten on 2016-06-06.
 *
 * @see org.leibnizcenter.cfg.category.nonterminal.NonTerminal
 */
public class Token<T> {
    public final T obj;

    public Token(T source) {
        if (source == null)
            throw new Error("Source object can't be null for an instantiated token. Did you mean to create a null token?");
        this.obj = source;
    }

    public static <T> Token<T> from(T t) {
        if (t == null) return null;
        else return new Token<>(t);
    }

    public static <T> Token<T> of(T source) {
        return new Token<>(source);
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token<?> token = (Token<?>) o;

        return obj.equals(token.obj);

    }

    @Override
    public int hashCode() {
        return obj.hashCode();
    }


}
