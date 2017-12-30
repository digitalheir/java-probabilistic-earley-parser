package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;

import static java.lang.Double.compare;
import static java.lang.String.valueOf;

/**
 * Atomic value
 * Created by maarten on 21/01/17.
 */
public class Atom implements Resolvable {
    public final double value;

    public Atom(final double value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o
                || !(o == null || getClass() != o.getClass())
                && compare(((Atom) o).value, value) == 0;
    }

    @Override
    public int hashCode() {
        final long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return valueOf(value);
    }

    @Override
    public double resolveFinal() {
        return value;
    }
}
