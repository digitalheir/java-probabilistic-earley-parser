package org.leibnizcenter.cfg.category.terminal.stringterminal;

import org.leibnizcenter.cfg.token.Token;

import java.util.regex.Pattern;

/**
 * <p>
 * A terminal that matches a {@link Pattern Regex pattern}
 * </p>
 * <p>
 * Created by maarten on 7-2-17.
 * </p>
 */
public class RegexTerminal implements StringTerminal {
    public final Pattern pattern;

    @SuppressWarnings("unused")
    public RegexTerminal(final Pattern pattern) {
        this.pattern = pattern;
    }

    @SuppressWarnings("unused")
    public RegexTerminal(final String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public RegexTerminal(final String pattern, final int flags) {
        this.pattern = Pattern.compile(pattern, flags);
    }

    @Override
    public boolean hasCategory(final Token<String> token) {
        return pattern.matcher(token.obj).matches();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RegexTerminal that = (RegexTerminal) o;

        return pattern.equals(that.pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    @Override
    public String toString() {
        return "Regex{" + pattern + '}';
    }
}
