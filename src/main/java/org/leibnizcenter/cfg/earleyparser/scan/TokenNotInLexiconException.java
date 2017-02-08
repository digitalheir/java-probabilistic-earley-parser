package org.leibnizcenter.cfg.earleyparser.scan;

import org.leibnizcenter.cfg.token.Token;

/**
 * Created by maarten on 8-2-17.
 */
public class TokenNotInLexiconException extends ScanException {
    public final Token token;

    public TokenNotInLexiconException(Token t) {
        super("Token not found in lexicon: " + t);
        this.token = t;
    }
}
