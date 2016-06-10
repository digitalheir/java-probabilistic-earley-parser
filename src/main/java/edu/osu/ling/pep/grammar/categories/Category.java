/*
 * $Id: Category.java 556 2007-08-15 19:33:53Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep.grammar.categories;


import edu.osu.ling.pep.grammar.Rule;
import edu.osu.ling.pep.grammar.Token;

import java.util.function.Function;

/**
 * A category in a grammar, also known as a type.
 * Categories are the atomic subparts that make up
 * {@link Rule grammar rules}.
 * <p>
 * Categories can either be <em>terminal</em> or <em>non-terminal</em>. A
 * terminal category is one from which no further categories can be derived,
 * while non-terminal categories can yield a series of other categories when
 * they occur as the {@link Rule#getLeft() left-hand side} of a rule.
 * <p>
 * Once created, categories are immutable and have no <code>setXxx</code>
 * methods. This ensures that, once loaded in a grammar, a category will
 * remain as it was when created.
 *
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 556 $
 * @see Rule
 */
public interface Category {
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
     * Special start category for seeding Earley parsers.
     */
    Category START = new NonTerminal("<start>") {
        /**
         * Overrides {@link Category#equals(Object)} to compare using the
         * <code>==</code> operator (since there is only ever one start
         * category).
         */
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Category && this == obj);
        }
    };

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
        if(categoryFunction == null) throw new Error("Can not instantiate category with null function. Did you mean to create a null category?");
        return categoryFunction::apply;
    }

    /**
     * Returns the given category
     *
     * @see Category#terminal(Function)
     */
    static <T> Terminal<T> terminal(Terminal<T> terminal) {
        if(terminal == null) throw new Error("Can not instantiate category with null function. Did you mean to create a null category?");
        return terminal;
    }
}
