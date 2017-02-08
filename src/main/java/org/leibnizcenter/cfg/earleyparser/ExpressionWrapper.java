package org.leibnizcenter.cfg.earleyparser;


import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;

@SuppressWarnings("WeakerAccess")
public class ExpressionWrapper extends Resolvable {
    private Resolvable expression;

    public ExpressionWrapper(Resolvable expression) {
        if (expression == null) throw new NullPointerException();
        this.expression = expression;
    }

    @SuppressWarnings("unused")
    public Resolvable getExpression() {
        if (lock) throw new IllegalStateException("Value already locked");
        if (expression == null) throw new NullPointerException();
        return expression;
    }

    @SuppressWarnings("unused")
    public void setExpression(Resolvable expression) {
        if (lock) throw new IllegalStateException("Value already locked");
        if (expression == null) throw new NullPointerException();
        this.expression = expression;
    }

    public double resolve() {
        if (expression == null) throw new NullPointerException();
        if (lock) return cached;
        return expression.resolveFinal();
    }

}
