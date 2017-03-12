package org.leibnizcenter.cfg.category.nonterminal;

import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.token.Token;

public final class NonLexicalToken implements Terminal, KleeneClosure {
    public static final String ERROR_SYMBOL = "<error>";
    public static final NonLexicalToken INSTANCE = new NonLexicalToken();

    private NonLexicalToken() {
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return ERROR_SYMBOL.hashCode();
    }

    @Override
    public boolean hasCategory(Token token) {
        throw new IllegalStateException("Knowing whether a token is lexical only makes sense in the context of a vocabulary / grammar");
    }

    @Override
    public String toString() {
        return ERROR_SYMBOL;
    }
}