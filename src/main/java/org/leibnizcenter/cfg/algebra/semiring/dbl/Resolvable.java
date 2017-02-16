package org.leibnizcenter.cfg.algebra.semiring.dbl;

/**
 */
public abstract class Resolvable {
    protected boolean lock = false;
    protected double cached = Double.NaN;

    public double resolveFinal() {
        cached = resolve();
        this.lock = true;
        return cached;
    }

    public abstract double resolve();

}
