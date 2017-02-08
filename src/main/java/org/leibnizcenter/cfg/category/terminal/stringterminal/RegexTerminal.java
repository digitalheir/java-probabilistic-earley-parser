package org.leibnizcenter.cfg.category.terminal.stringterminal;

import org.leibnizcenter.cfg.token.Token;

import java.util.regex.Pattern;

/**
 * Created by maarten on 7-2-17.
 */
public class RegexTerminal implements StringTerminal {
    public final Pattern pattern;

    public RegexTerminal(Pattern pattern) {
        this.pattern = pattern;
    }

    public RegexTerminal(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public RegexTerminal(String pattern, int flags) {
        this.pattern = Pattern.compile(pattern, flags);
    }

    @Override
    public boolean hasCategory(Token<String> token) {
        return pattern.matcher(token.obj).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegexTerminal that = (RegexTerminal) o;

        if (!pattern.equals(that.pattern)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    @Override
    public String toString() {
        return "Regex{" + pattern +
                '}';
    }
}
