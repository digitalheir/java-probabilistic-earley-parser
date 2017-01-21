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
        super(rule, positionInInput, ruleStartPosition, ruleDotPosition);
        this.scannedToken = scannedToken;
        this.scannedCategory = rule.getRight()[ruleDotPosition - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ScannedTokenState<?> that = (ScannedTokenState<?>) o;

        return scannedToken.equals(that.scannedToken) && scannedCategory.equals(that.scannedCategory);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + scannedToken.hashCode();
        result = 31 * result + scannedCategory.hashCode();
        return result;
    }
}
