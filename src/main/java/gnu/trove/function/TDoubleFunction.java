package gnu.trove.function;

/**
 * Interface for functions that accept and return one double primitive.
 */
@FunctionalInterface
public interface TDoubleFunction {
    /**
     * Execute this function with <tt>value</tt>
     *
     * @param value a <code>double</code> input
     * @return a <code>double</code> result
     */
    double execute(double value);
}
