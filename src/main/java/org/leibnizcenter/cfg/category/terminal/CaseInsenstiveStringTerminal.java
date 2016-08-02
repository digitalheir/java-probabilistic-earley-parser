package org.leibnizcenter.cfg.category.terminal;

import org.leibnizcenter.cfg.token.Token;

/**
 * Terminal for which a given string token matches exactly, modulo character case
 * Created by maarten on 11-6-16.
 */
public class CaseInsenstiveStringTerminal implements StringTerminal {
    private final String string;

    public CaseInsenstiveStringTerminal(String s) {
        this.string = s;
    }

    @Override
    public boolean hasCategory(Token<String> token) {
        return string.equalsIgnoreCase(token.obj);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CaseInsenstiveStringTerminal that = (CaseInsenstiveStringTerminal) o;

        return string != null ? string.equals(that.string) : that.string == null;

    }

    @Override
    public int hashCode() {
        return string != null ? string.hashCode() : 0;
    }
}
