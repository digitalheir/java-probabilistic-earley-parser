package org.leibnizcenter.earleyparser;

import org.leibnizcenter.earleyparser.grammar.Token;

/**
 * Created by maarten on 11-6-16.
 */
public class CaseInsenstivieStringTerminal implements StringTerminal {
    private final String string;

    public CaseInsenstivieStringTerminal(String s) {
        this.string = s;
    }

    @Override
    public boolean hasCategory(Token<String> token) {
        return string.equalsIgnoreCase(token.obj);
    }
}
