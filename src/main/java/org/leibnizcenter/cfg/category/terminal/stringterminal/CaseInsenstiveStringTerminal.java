package org.leibnizcenter.cfg.category.terminal.stringterminal;

import org.leibnizcenter.cfg.token.Token;

import java.util.Locale;

/**
 * Terminal for which a given string token matches exactly, modulo character case
 * <p>
 * Created by maarten on 11-6-16.
 */
public class CaseInsenstiveStringTerminal implements StringTerminal {
    public final String string;
    public final Locale locale;

    public CaseInsenstiveStringTerminal(String s) {
        this.locale = Locale.ROOT;
        this.string = s.toLowerCase(locale);
    }

    public CaseInsenstiveStringTerminal(String s, Locale locale) {
        this.locale = locale;
        this.string = s.toLowerCase(locale);
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

        if (!string.equals(that.string)) return false;
        if (!locale.equals(that.locale)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = string.hashCode();
        result = 31 * result + locale.hashCode();
        return result;
    }
}
