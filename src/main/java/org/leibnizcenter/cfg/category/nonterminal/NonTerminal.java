package org.leibnizcenter.cfg.category.nonterminal;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.util.Strings2;

/**
 * <p>
 * Non-terminal {@link Category}. Just a symbol, so contains a name to distinguish it from other non-terminals.
 * </p>
 * Created by maarten on 10-6-16.
 */
public class NonTerminal implements Category {
    @SuppressWarnings("WeakerAccess")
    public final String name;

    /**
     * Creates a new category <code>name</code>.
     *
     * @param name The name for this category.
     * @throws IllegalArgumentException If <code>name</code> is
     *                                  <code>null</code> or zero-length.
     */
    public NonTerminal(final String name) {
        if (Strings2.isNullOrEmpty(name)) throw new IllegalArgumentException("empty name specified for category");
        this.name = name;
    }

    public static NonTerminal of(final String name) {
        return new NonTerminal(name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final NonTerminal that = (NonTerminal) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * Gets a string representation of this category.
     *
     * @return The value of this category's name.
     */
    @Override
    public String toString() {
        return (name.length() == 0) ? "<empty>" : name;
    }
}
