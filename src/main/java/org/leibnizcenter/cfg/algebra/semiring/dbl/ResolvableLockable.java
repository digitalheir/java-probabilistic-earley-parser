package org.leibnizcenter.cfg.algebra.semiring.dbl;

public abstract class ResolvableLockable implements Resolvable {
    protected boolean lock = false;
    protected double cached = Double.NaN;

    @Override
    public double resolveFinal() {
        cached = resolveAndClean();
        this.lock = true;
        return cached;
    }

    protected abstract double resolveAndClean();

}
