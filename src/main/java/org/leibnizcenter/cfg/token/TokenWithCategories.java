package org.leibnizcenter.cfg.token;

import org.leibnizcenter.cfg.category.terminal.Terminal;

import java.util.Set;

/**
 *
 * This class represents a token with all applicable categories.
 * Note that this is <em>not</em> the place to mess with which categories apply to the
 * given token; that should be a function of interface {@link Terminal}. This is just the place where all applicable categories
 * get stored.
 * </p>
 * Created by maarten on 11-1-17.
 */
public class TokenWithCategories<T> {
    public final Token<T> token;
    public final Set<Terminal<T>> categories;

    public TokenWithCategories(final Token<T> token, final Set<Terminal<T>> categories) {
        this.token = token;
        this.categories = categories;
    }
}
