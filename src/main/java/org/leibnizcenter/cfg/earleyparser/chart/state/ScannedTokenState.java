package org.leibnizcenter.cfg.earleyparser.chart.state;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

/**
 * Represents a state that is the result of a scan operation
 * <p>
 * Created by Maarten on 16-8-2016.
 */
public class ScannedTokenState<E> extends State {
    public final Token<E> scannedToken;
    public final Category scannedCategory;


    public ScannedTokenState(Token<E> scannedToken, Rule rule, int ruleStartPosition, int positionInInput, int ruleDotPosition) {
        super(rule, ruleStartPosition, positionInInput, ruleDotPosition);
        this.scannedToken = scannedToken;
        this.scannedCategory = rule.getRight()[ruleDotPosition - 1];
    }
}
