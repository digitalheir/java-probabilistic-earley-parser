package org.leibnizcenter.cfg.token;

/**
 * @see org.leibnizcenter.cfg.earleyparser.scan.TokenNotInLexiconException
 * Created by maarten on 11/02/17.
 */
public class WildcardToken<T> extends Token<T> {
    public WildcardToken(final Token<T> token) {
        super(token.obj);
        // this.token = token;
    }

    @Override
    public String toString() {
        return "!" + super.toString();
    }
}
