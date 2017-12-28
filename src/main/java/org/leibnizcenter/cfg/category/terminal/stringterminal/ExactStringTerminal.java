package org.leibnizcenter.cfg.category.terminal.stringterminal;

import org.leibnizcenter.cfg.token.Token;

/**
 * Terminal for which a given string token matches exactly
 * Created by maarten on 10-6-16.
 */
@SuppressWarnings("WeakerAccess")
public class ExactStringTerminal implements StringTerminal {
    public final String string;

    public ExactStringTerminal(final String s) {
        if (s == null) throw new NullPointerException();
        this.string = s;
    }

    @Override
    public boolean hasCategory(final Token<String> token) {
        return string.equals(token.obj);
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ExactStringTerminal that = (ExactStringTerminal) o;

        return string.equals(that.string);

    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }
}
