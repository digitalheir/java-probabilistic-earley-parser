package org.leibnizcenter.cfg.category.nonterminal;

/**
 */
public final class ErrorSection extends NonTerminal {

    public static final String NAME = "<error>";

    /**
     * Creates a new error category.
     */
    public ErrorSection() {
        super(NAME);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(((ErrorSection) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
