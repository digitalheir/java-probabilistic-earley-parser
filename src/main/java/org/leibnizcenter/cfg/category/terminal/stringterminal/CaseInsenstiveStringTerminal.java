package org.leibnizcenter.cfg.category.terminal.stringterminal;

import org.leibnizcenter.cfg.token.Token;

/**
 * Terminal for which a given string token matches exactly, modulo character case
 *
 * Created by maarten on 11-6-16.
 */
public class CaseInsenstiveStringTerminal implements StringTerminal {
    public final String string;

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

        return string != null ? string.equalsIgnoreCase(that.string) : that.string == null;
    }

    @Override
    public int hashCode() {
        // NOTE this might not always work
        return string != null ?string.toLowerCase().hashCode() : 0;
    }
}
