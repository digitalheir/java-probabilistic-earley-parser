package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;

/**
 * Atomic value
 * Created by maarten on 21/01/17.
 */
public class Atom extends Resolvable {
    public final double value;

    public Atom(double value) {
        this.value = value;
    }

    @Override
    public double resolve() {
        return value;
    }


    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && Double.compare(((Atom) o).value, value) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
