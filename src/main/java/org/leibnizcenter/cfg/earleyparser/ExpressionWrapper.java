package org.leibnizcenter.cfg.earleyparser;


import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ResolvableLockable;

@SuppressWarnings("WeakerAccess")
public class ExpressionWrapper extends ResolvableLockable {
    private double literal = Double.NaN;
    private Resolvable expression = null;

    public ExpressionWrapper(final Resolvable expression) {
        if (expression == null) throw new NullPointerException();
        this.expression = expression;
    }

    public ExpressionWrapper(final double literal) {
        this.literal = literal;
    }

    @SuppressWarnings("unused")
    public Resolvable getExpression() {
        if (lock) throw new IllegalStateException("Value already locked");
        if (expression == null) throw new NullPointerException();
        return expression;
    }

    @SuppressWarnings("unused")
    public void setExpression(final Resolvable expression) {
        if (lock) throw new IllegalStateException("Value already locked");
        if (expression == null) throw new NullPointerException();
        this.literal = Double.NaN;
        this.expression = expression;
    }

    public double resolveAndClean() {
        if (lock) return cached;
        if (expression == null) return literal;
        else {
            literal = expression.resolveFinal();
            expression = null;
            return literal;
        }
    }

    public boolean hasExpression() {
        return expression != null;
    }

    public double getLiteral() {
        return literal;
    }
}
