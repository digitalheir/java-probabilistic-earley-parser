package org.leibnizcenter.cfg.category.nonterminal;

import com.google.common.base.Strings;
import org.leibnizcenter.cfg.category.Category;

/**
 * Non-terminal {@link Category}
 * <p>
 * Created by maarten on 10-6-16.
 */
public class NonTerminal implements Category {
    public final String name;

    /**
     * Creates a new category <code>name</code>.
     *
     * @param name The name for this category.
     * @throws IllegalArgumentException If <code>name</code> is
     *                                  <code>null</code> or zero-length.
     */
    public NonTerminal(String name) {
        if (Strings.isNullOrEmpty(name)) throw new IllegalArgumentException("empty name specified for category");
        this.name = name;
    }

    /**
     * Gets the name of this category.
     *
     * @return The value specified for this category's name when it was
     * constructed.
     */
    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NonTerminal that = (NonTerminal) o;

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
