package org.leibnizcenter.cfg.category.terminal;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.token.Token;

/**
 * Terminal {@link Category}
 * <p>
 * Created by maarten on 10-6-16.
 */
public interface Terminal<T> extends Category {

    /**
     * Expected to run in O(1), or else the complexity analysis does not apply anymore
     *
     * @param token Token to test
     * @return Whether this category conforms to the given token
     */
    boolean hasCategory(Token<T> token);
}
