package org.leibnizcenter.cfg.category;

import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.function.Function;

/**
 * A category in a grammar, also known as a type.
 * Categories are the atomic subparts that make up
 * {@link Rule grammar rules}.
 * <p>
 * Categories can either be <em>terminal</em> or <em>non-terminal</em>. A
 * terminal category is one from which no further category can be derived,
 * while non-terminal category can yield a series of other category when
 * they occur as the {@link Rule#getLeft() left-hand side} of a rule.
 * <p>
 * Once created, category are immutable and have no <code>setXxx</code>
 * methods. This ensures that, once loaded in a grammar, a category will
 * remain as it was when created.
 *
 * @see Rule
 */
public interface Category {
    /**
     * Special start category for seeding Earley parsers.
     */
    NonTerminal START = new NonTerminal("<start>") {
        /**
         * Overrides {@link Category#equals(Object)} to compare using the
         * <code>==</code> operator (since there is only ever one start
         * category).
         */
        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    };

    /**
     * Gets the terminal status of this category.
     *
     * @return The terminal status specified for this category upon
     * construction.
     */
    static boolean isTerminal(Category c) {
        return c instanceof Terminal;
    }

    /**
     * Creates a new non-terminal category with the specified name.
     *
     * @see Category#terminal(Function)
     */
    static NonTerminal nonTerminal(String name) {
        return new NonTerminal(name);
    }

    /**
     * Creates a new terminal category with the specified name.
     *
     * @see Category#nonTerminal(String)
     */
    static <T> Terminal<T> terminal(Function<Token<T>, Boolean> categoryFunction) {
        if (categoryFunction == null)
            throw new Error("Can not instantiate category with null function. Did you mean to create a null category?");
        return categoryFunction::apply;
    }

    /**
     * Returns the given category
     *
     * @see Category#terminal(Function)
     */
    static <T> Terminal<T> terminal(Terminal<T> terminal) {
        if (terminal == null)
            throw new Error("Can not instantiate category with null function. Did you mean to create a null category?");
        return terminal;
    }
}
