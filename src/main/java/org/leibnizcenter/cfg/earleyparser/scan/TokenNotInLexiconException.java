package org.leibnizcenter.cfg.earleyparser.scan;

import org.leibnizcenter.cfg.token.Token;

/**
 * Created by maarten on 8-2-17.
 */
public class TokenNotInLexiconException extends ScanException {
    public final Token token;
    public final int index;

    public TokenNotInLexiconException(Token t, int index) {
        super("Token not found in lexicon: " + index + ", " + t);
        this.token = t;
        this.index = index;
    }
}
