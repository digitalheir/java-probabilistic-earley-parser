
package org.leibnizcenter.cfg.earleyparser.parse;


import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.Parser;

/**
 * Reflects the {@link Parser#getStatus() status} of a {@link Parser parse}
 * completed by an {@link Parser Earley parser}.
 *
 * @see org.leibnizcenter.cfg.earleyparser.Parser
 */
public enum Status {
    /**
     * Signals that a string is rejected after parsing.
     */
    REJECT,

    /**
     * Means that a string is a valid string of a given {@link Grammar grammar},
     * as determined by
     * {@link Parser#parse(Iterable, Category) parsing}.
     */
    ACCEPT,

    /**
     * Used for {@link Parser parses} where an error occurs during processing.
     */
    ERROR
}
