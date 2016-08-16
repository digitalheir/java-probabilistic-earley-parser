package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

/**
 * Created by Maarten on 16-8-2016.
 */
public class ScannedTokenState<E> extends State {
    public final Token<E> scannedToken;

    public ScannedTokenState(Token<E> scannedToken, Rule rule, int ruleStartPosition) {
        super(rule, ruleStartPosition);
        this.scannedToken = scannedToken;
    }

    public ScannedTokenState(Token<E> scannedToken, Rule rule, int ruleStartPosition, int positionInInput, int ruleDotPosition) {
        super(rule, ruleStartPosition, positionInInput, ruleDotPosition);
        this.scannedToken = scannedToken;
    }
}
