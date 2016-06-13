
package org.leibnizcenter.cfg.earleyparser.parse;


import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;

/**
 * Reflects the {@link Parse#getStatus() status} of a {@link Parse parse}
 * completed by an {@link EarleyParser Earley parser}.
 *
 * @see Parse
 * @see EarleyParser
 */
public enum Status {
    /**
     * Signals that a string is rejected after parsing.
     */
    REJECT,

    /**
     * Means that a string is a valid string of a given {@link Grammar grammar},
     * as determined by
     * {@link EarleyParser#parse(Iterable, Category) parsing}.
     */
    ACCEPT,

    /**
     * Used for {@link Parse parses} where an error occurs during processing.
     */
    ERROR
}
