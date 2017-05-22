package org.leibnizcenter.cfg.earleyparser.scan;

import org.leibnizcenter.cfg.token.Token;

/**
 * Created by maarten on 8-2-17.
 */
public class TokenNotInLexiconException extends ScanException {
    public final Token token;
    public final int indexTokens;
    public final int indexChart;

    public TokenNotInLexiconException(Token t, int index, int chartIndex) {
        super("Token not found in lexicon: [" + index + "]: " + t);
        this.token = t;
        this.indexTokens = index;
        this.indexChart = chartIndex;
    }
}
