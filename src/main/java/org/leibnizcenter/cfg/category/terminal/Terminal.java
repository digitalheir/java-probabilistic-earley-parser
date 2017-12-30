package org.leibnizcenter.cfg.category.terminal;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.token.Token;

/**
 *<p>
 * Terminal {@link Category}. Implements a function {@link #hasCategory} that returns, given a
 * {@link Token token}, whether that token a terminal of this type.
 * </p>
 * Created by maarten on 10-6-16.
 *
 * @see Token
 * @see org.leibnizcenter.cfg.category.nonterminal.NonTerminal NonTerminal
 */
public interface Terminal<T> extends Category {

    /**
     *
     * Returns whether a given token is of this terminal type.
     * </p>
     *
     * Expected to run in O(1), or else the complexity analysis does not apply anymore.
     * </p>
     *
     * Take note that
     * {@link org.leibnizcenter.cfg.grammar.Grammar#getCategories(Token) Grammar#getCategories} caches the result
     * of this function on {@link Token#equals(Object)}, so this function should be idempotent.
     * </p>
     *
     * @param token Token to test
     * @return Whether the given token has this Terminal type
     */
    boolean hasCategory(Token<T> token);
}
