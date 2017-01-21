package org.leibnizcenter.cfg.earleyparser.chart.state;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

/**
 * Represents the result of a scan operation
 */
public class ScannedToken<E> {
    public final Token<E> scannedToken;
    public final Category scannedCategory;


    public ScannedToken(Token<E> scannedToken, Rule rule, int ruleDotPosition) {
        this.scannedToken = scannedToken;
        this.scannedCategory = rule.getRight()[ruleDotPosition - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScannedToken<?> that = (ScannedToken<?>) o;

        return scannedToken != null ? scannedToken.equals(that.scannedToken) : that.scannedToken == null && (scannedCategory != null ? scannedCategory.equals(that.scannedCategory) : that.scannedCategory == null);

    }

    @Override
    public int hashCode() {
        int result = scannedToken != null ? scannedToken.hashCode() : 0;
        result = 31 * result + (scannedCategory != null ? scannedCategory.hashCode() : 0);
        return result;
    }
}
