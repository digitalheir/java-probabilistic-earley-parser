package org.leibnizcenter.earleyparser.grammar.categories;

import org.leibnizcenter.earleyparser.grammar.Token;

/**
 * Terminal {@link Category}
 * <p>
 * Created by maarten on 10-6-16.
 */
public interface Terminal<T> extends Category {

    /**
     * Expected to run in O(1)
     *
     * @param token Token to test
     * @return Whether this category conforms to the given token
     */
    abstract public boolean hasCategory(Token<T> token);
}
