package edu.osu.ling.pep;

import edu.osu.ling.pep.grammar.Token;
import edu.osu.ling.pep.grammar.categories.Category;

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
