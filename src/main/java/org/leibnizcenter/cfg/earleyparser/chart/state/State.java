package org.leibnizcenter.cfg.earleyparser.chart.state;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.Atom;
import org.leibnizcenter.cfg.rule.Rule;

import java.text.DecimalFormat;

/**
 * A chart state, describing a pending derivation.
 * <p/>
 * A state is of the form <code>i: X<sub>k</sub> → λ·μ</code>
 * where X is a nonterminal of the grammar, λ and μ are strings of nonterminals and/or
 * terminals, and i and k are indices into the input string. States are derived from productions
 * in the grammar. The above state is derived from a corresponding production
 * X → λμ
 * with the following semantics:
 * <ul>
 * <li>The current position in the input is <code>i</code>, i.e., <code>x<sub>0</sub>...x<sub>i-1</sub></code>
 * have been processed
 * so far. The states describing the parser state at position <code>i</code> are collectively
 * called state set <code>i</code>. Note that there is one more state set than input
 * symbols: set <code>0</code> describes the parser state before any input is processed,
 * while set <code>|x|</code> contains the states after all input symbols have been
 * processed.</li>
 * <li>Nonterminal <code>X</code> was expanded starting at position <code>k</code> in
 * the input, i.e., <code>X</code>
 * generates some substring starting at position <code>k</code>.</li>
 * <li>The expansion of X proceeded using the production <code>X → λμ</code>, and has
 * expanded the right-hand side (RHS) <code>λμ</code> up to the position indicated by
 * the dot. The dot thus refers to the current position <code>i</code>.</li>
 * </ul>
 * <p/>
 * A state with the dot to the right of the entire RHS is called a completeNoViterbi state, since
 * it indicates that the left-hand side (LHS) nonterminal has been fully expanded.
 * <p/>
 * States are mutable
 * <p/>
 * Created by maarten on 24-6-16.
 */
public class State {
    public final Rule rule;
    public final int ruleStartPosition;
    @SuppressWarnings("WeakerAccess")
    public final int ruleDotPosition;
    @SuppressWarnings("WeakerAccess")
    public final int position;
    private final int hashCode;

    public double forwardScore = Double.NaN;
    public Atom forwardScoreAtom = null;

    /**
     * Makes a predicted State based on the specified rule, with the specified
     * origin position.
     * A new State whose {@link #rule dotted rule} is the
     * specified rule at position <code>0</code>. The new State's origin is the
     * specified <code>origin</code>.
     *
     * @param rule              The rule to construct a predicted State for.
     * @param ruleStartPosition The origin position of the newly predicted State.
     * @throws NullPointerException If <code>rule</code> is <code>null</code>.
     */
    public State(Rule rule, int ruleStartPosition) {
        this(rule, ruleStartPosition, ruleStartPosition, 0);
    }

    public State(Rule rule, int position, int ruleStartPosition, int ruleDotPosition) {
        if (rule == null) throw new NullPointerException("null rule");
        this.rule = rule;
        this.ruleStartPosition = ruleStartPosition;
        this.ruleDotPosition = ruleDotPosition;
        this.position = position;
        this.hashCode = computeHashCode();
    }

    public static State create(int index, int ruleStart, int dotPosition, Rule rule) {
        return new State(rule, index, ruleStart, dotPosition);
    }

    @Override
    public String toString() {
        return position + ": (" + ruleStartPosition + ") " + rule.toString(ruleDotPosition) + "";
    }


    public boolean isCompleted() {
        return rule.isPassive(ruleDotPosition);
    }

    public boolean isActive() {
        return !isCompleted();
    }

    /**
     * Runs in O(1)
     *
     * @return Active category for this state. May be null.
     */
    public Category getActiveCategory() {
        return rule.getActiveCategory(ruleDotPosition);
    }

    /**
     * Return dot position advanced by <code>1</code>, or errors if out of bounds.
     *
     * @throws IndexOutOfBoundsException If the dotted rule's dot position
     *                                   is already at the end of its right side.
     */
    public int advanceDot() {
        int position = (ruleDotPosition);
        if (position < 0 || position > rule.right.length) throw new IndexOutOfBoundsException(
                "illegal position: " + position);
        return position + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        else {
            State state = (State) o;
            return ruleStartPosition == state.ruleStartPosition
                    && ruleDotPosition == state.ruleDotPosition
                    && position == state.position
                    && rule.equals(state.rule);
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int computeHashCode() {
        int result = rule.hashCode();
        result = 31 * result + ruleStartPosition;
        result = 31 * result + ruleDotPosition;
        result = 31 * result + position;
        return result;
    }

    public static class StateWithScore {
        private final double forwardScore;
        private final double innerScore;
        private final State state;
        private final State origin;

        public StateWithScore(State state, double forwardScore, double innerScore, State origin) {
            this.forwardScore = forwardScore;
            this.state = state;
            this.innerScore = innerScore;
            this.origin = origin;
        }

        public double getInnerScore() {
            return innerScore;
        }

        public double getForwardScore() {
            return forwardScore;
        }

        public State getState() {
            return state;
        }

        @Override
        public String toString() {
            DecimalFormat df = new DecimalFormat("#.00");

            return "StateWithScore{" +
                    "forwardScore=" + df.format(forwardScore) +
                    ", innerScore=" + df.format(innerScore) +
                    ", state=" + state +
                    ", origin=" + origin +
                    '}';
        }
    }

    /**
     * Immutable class representing a Viterbi score coming from a certain state, transition to a result state computing
     * using a certain semiring
     */
    public static final class ViterbiScore {
        @SuppressWarnings("WeakerAccess")
        public final State origin;
        @SuppressWarnings("WeakerAccess")
        public final double innerScore;
        @SuppressWarnings("WeakerAccess")
        public final DblSemiring semiring;
        @SuppressWarnings("WeakerAccess")
        public final State resultingState;

        private final int hashCode;

        public ViterbiScore(double innerScore, State origin, State resultingState, DblSemiring semiring) {
            this.innerScore = innerScore;
            this.origin = origin;
            this.resultingState = resultingState;
            this.semiring = semiring;
            this.hashCode = computeHashCode();
        }

        public final double getRawScore() {
            return innerScore;
        }

        public final double getProbability() {
            return semiring.toProbability(innerScore);
        }

        public final State getOrigin() {
            return origin;
        }

        public final State getResultingState() {
            return resultingState;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            else if (o == null || getClass() != o.getClass()) return false;
            else {
                ViterbiScore that = (ViterbiScore) o;
                return Double.compare(that.innerScore, innerScore) == 0
                        && (origin != null ? origin.equals(that.origin) : that.origin == null
                        && semiring.equals(that.semiring)
                        && (resultingState != null ? resultingState.equals(that.resultingState) : that.resultingState == null));
            }
        }

        @Override
        public final int hashCode() {
            return hashCode;
        }

        private int computeHashCode() {
            int result;
            long temp;
            result = origin != null ? origin.hashCode() : 0;
            temp = Double.doubleToLongBits(innerScore);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + semiring.hashCode();
            result = 31 * result + (resultingState != null ? resultingState.hashCode() : 0);
            return result;
        }

        @Override
        public final String toString() {
            return "ViterbiScore{" +
                    "origin=" + origin +
                    ", score=" + getProbability() +
                    ", resultingState=" + resultingState +
                    '}';
        }
    }
}
