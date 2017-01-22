package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;

/**
 * Atomic value
 * Created by maarten on 21/01/17.
 */
public class Atom extends Resolvable {
    private final double v;

    public Atom(double v) {
        this.v = v;
    }

    @Override
    public double resolve() {
        return v;
    }
}
