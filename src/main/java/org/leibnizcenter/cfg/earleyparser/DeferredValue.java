package org.leibnizcenter.cfg.earleyparser;


import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;

public class DeferredValue implements Resolvable {
    Resolvable expression;

    public DeferredValue(Resolvable expression) {
        this.expression = expression;
    }

    public Resolvable getExpression() {
        return expression;
    }

    public void setExpression(Resolvable expression) {
        this.expression = expression;
    }

    @Override
    public double resolve() {
        return expression.resolve();
    }
}
