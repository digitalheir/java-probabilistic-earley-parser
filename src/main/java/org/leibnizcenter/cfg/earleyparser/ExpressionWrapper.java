package org.leibnizcenter.cfg.earleyparser;


import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;

@SuppressWarnings("WeakerAccess")
public class ExpressionWrapper extends Resolvable {
    private Resolvable expression;

    public ExpressionWrapper(Resolvable expression) {
        this.expression = expression;
    }

    @SuppressWarnings("unused")
    public Resolvable getExpression() {
        if (lock) throw new IllegalStateException("Value already locked");
        return expression;
    }

    @SuppressWarnings("unused")
    public void setExpression(Resolvable expression) {
        if (lock) throw new IllegalStateException("Value already locked");
        this.expression = expression;
    }

    public double resolve() {
        if (lock) return cached;
        return expression.resolveFinal();
    }

}
