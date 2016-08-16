package org.leibnizcenter.cfg.earleyparser.chart.state;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;

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
 * A state with the dot to the right of the entire RHS is called a completeTruncated state, since
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
    public final int positionInInput;

    /**
     * Makes a predicted State based on the specified rule, with the specified
     * origin position.
     * A new State whose {@link #getRule() dotted rule} is the
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

    public State(Rule rule, int ruleStartPosition, int positionInInput, int ruleDotPosition) {
        if (rule == null) throw new NullPointerException("null rule");
        this.rule = rule;
        this.ruleStartPosition = ruleStartPosition;
        this.ruleDotPosition = ruleDotPosition;
        this.positionInInput = positionInInput;
    }

    @Override
    public String toString() {
        return getPosition() + ": (" + ruleStartPosition + ") " + rule.toString(ruleDotPosition) + "";
    }


    public boolean isCompleted() {
        return rule.isPassive(ruleDotPosition);
    }

    public boolean isActive() {
        return !isCompleted();
    }

    /**
     * @return Active category for this state. May be null.
     */
    public Category getActiveCategory() {
        return rule.getActiveCategory(ruleDotPosition);
    }

    public Rule getRule() {
        return rule;
    }

    public int getPosition() {
        return positionInInput;
    }

//    /**
//     * Completes this State based on the specified basis, i.e. increments the dot positon.
//     *
//     * @param basis The basis on which this State is being completed.
//     * @return This state, except that its
//     * {@link #getRule() dotted rule}'s position is advanced by
//     * <code>1</code>.
//     * @throws NullPointerException     if <code>this</code> or
//     *                                  <code>basis</code> is <code>null</code>.
//     * @throws IllegalArgumentException If the specified basis is not a
//     *                                  suitable State for completing this State. Reasons for this errors are
//     *                                  that the basis State:
//     *                                  <ul>
//     *                                  <li>has a {@link #getRule() dotted rule} whose
//     *                                  {@link #getPosition() position} is <code>0</code>
//     *                                  (meaning that no completion has actually taken place)</li>
//     *                                  <li>has a dotted rule whose {@link Rule#getLeft() left}
//     *                                  category does not equal this State's dotted rule's
//     *                                  {@link #getActiveCategory() active category}.</li>
//     *                                  </ul>
//     * @see #advanceDot()
//     */
//    public State completeTruncated(State basis) {
//        if (this.isCompleted()) throw new IllegalArgumentException(
//                "attempt to completeTruncated passive State: " + this);
//        if (basis == null) throw new NullPointerException("null basis");
//        if (!basis.isCompleted()) throw new IllegalArgumentException("basis is not completed: " + basis);
//        if ((ruleStartPosition + ruleDotPosition) == 0 || !basis.getRule().left.equals(this.getActiveCategory()))
//            throw new IllegalArgumentException(this + " is not completed by basis " + basis);
//        advanceDot();
//        return this;
//    }
//

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
//
//
//    /**
//     * Mutates this state by reading a token.
//     *
//     * @param token The just-scanned token.
//     * @throws NullPointerException     If <code>state</code> or <code>token</code>
//     *                                  is <code>null</code>.
//     * @throws IllegalArgumentException In any of the following cases:
//     *                                  <ol>
//     *                                  <li>The specified <code>state</code> is
//     *                                  {@link #isCompleted() passive}.</li>
//     *                                  <li>The specified <code>state</code>'s
//     *                                  {@link #getRule() dotted rule}'s
//     *                                  {@link #getActiveCategory() active category} is not a
//     *                                  {@link Terminal terminal}.</li>
//     *                                  <li>The <code>state</code>'s active category
//     *                                  does not match the scanned
//     *                                  <code>token</code>.</li>
//     *                                  </ol>
//     */
//    public <T> State scan(Token<T> token) {
//        State state = this;
//        if (token == null) throw new NullPointerException("null input token");
//
//        Category activeCategory = getActiveCategory();
//        if (activeCategory == null) throw new IllegalArgumentException("passive state");
//        if (!(activeCategory instanceof Terminal)) throw new IllegalArgumentException(
//                "state's active category is nonterminal: " + state);
//
//        //noinspection unchecked
//        if (!((Terminal<T>) activeCategory).hasCategory(token)) throw new IllegalArgumentException("token " + token
//                + " incompatible with " + state);
//
//        advanceDot();
//        return this;
//    }

    public int getRuleDotPosition() {
        return ruleDotPosition;
    }

    public int getRuleStartPosition() {
        return ruleStartPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return ruleStartPosition == state.ruleStartPosition
                && ruleDotPosition == state.ruleDotPosition
                && positionInInput == state.positionInInput
                && rule.equals(state.rule);

    }

    @Override
    public int hashCode() {
        int result = rule.hashCode();
        result = 31 * result + ruleStartPosition;
        result = 31 * result + ruleDotPosition;
        result = 31 * result + positionInInput;
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

    public static class ViterbiScore implements Comparable<ViterbiScore> {
        private final State origin;
        private final double innerScore;
        private final DblSemiring sr;
        private final State resultingState;

        public ViterbiScore(double innerScore, State origin, State resultingState, DblSemiring semiring) {
            this.innerScore = innerScore;
            this.origin = origin;
            this.resultingState = resultingState;
            this.sr = semiring;
        }

        public double getScore() {
            return innerScore;
        }

        public State getOrigin() {
            return origin;
        }

        @Override
        public int compareTo(ViterbiScore other) {
            return Double.compare(sr.toProbability(innerScore), sr.toProbability(other.getScore()));
        }

        public State getResultingState() {
            return resultingState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ViterbiScore that = (ViterbiScore) o;
            return Double.compare(that.innerScore, innerScore) == 0 && (origin != null ? origin.equals(that.origin) : that.origin == null && (sr != null ? sr.equals(that.sr) : that.sr == null && !(resultingState != null ? !resultingState.equals(that.resultingState) : that.resultingState != null)));

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = origin != null ? origin.hashCode() : 0;
            temp = Double.doubleToLongBits(innerScore);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (sr != null ? sr.hashCode() : 0);
            result = 31 * result + (resultingState != null ? resultingState.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ViterbiScore{" +
                    "origin=" + origin +
                    ", score=" + sr.toProbability(innerScore) +
                    ", resultingState=" + resultingState +
                    '}';
        }
    }
}
