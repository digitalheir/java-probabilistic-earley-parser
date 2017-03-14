package org.leibnizcenter.cfg.category.terminal.stringterminal;

import org.leibnizcenter.cfg.token.Token;

import java.util.Locale;

/**
 * Terminal for which a given string token matches exactly, modulo character case
 * <p>
 * Created by maarten on 11-6-16.
 */
@SuppressWarnings("WeakerAccess")
public class CaseInsensitiveStringTerminal implements StringTerminal {
    public final String string;
    public final Locale locale;

    public CaseInsensitiveStringTerminal(String s) {
        this.locale = Locale.ROOT;
        this.string = s.toLowerCase(locale);
    }

    public CaseInsensitiveStringTerminal(String s, Locale locale) {
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

        CaseInsensitiveStringTerminal that = (CaseInsensitiveStringTerminal) o;

        return string.equals(that.string) && locale.equals(that.locale);
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public int hashCode() {
        int result = string.hashCode();
        result = 31 * result + locale.hashCode();
        return result;
    }
}
