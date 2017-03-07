package org.leibnizcenter.cfg.category.nonterminal;

import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.token.Token;

public final class NonLexicalToken implements Terminal {
    public static final String WILDCARD_SYMBOL = "<NonLexical>";
    private static final NonLexicalToken SINGLETON = new NonLexicalToken();

    private NonLexicalToken() {
    }

    public static NonLexicalToken get() {
        return SINGLETON;
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return WILDCARD_SYMBOL.hashCode();
    }

    @Override
    public boolean hasCategory(Token token) {
        throw new IllegalStateException("Knowing whether a token is lexical only makes sense in the context of a vocabulary / grammar");
    }

    @Override
    public String toString() {
        return WILDCARD_SYMBOL;
    }
}