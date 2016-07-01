package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.rule.Rule;

/**
 * A chart state, describing a pending derivation.
 * <p>
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
 * <p>
 * A state with the dot to the right of the entire RHS is called a complete state, since
 * it indicates that the left-hand side (LHS) nonterminal has been fully expanded.
 * <p>
 * States are mutable
 * <p>
 * Created by maarten on 24-6-16.
 */
public class State {
    public final Rule rule;
    public final int ruleStartPosition;
    public final int ruleDotPosition;
    public final int positionInInput;

    /**
     * Makes a predicted State based on the specified rule, with the specified
     * origin position.
     *
     * @param rule              The rule to construct a predicted State for.
     * @param ruleStartPosition The origin position of the newly predicted State.
     * @return A new State whose {@link #getRule() dotted rule} is the
     * specified rule at position <code>0</code>. The new State's origin is the
     * specified <code>origin</code>.
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
//     *                                  suitable State for completing this State. Reasons for this exception are
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
//    public State complete(State basis) {
//        if (this.isCompleted()) throw new IllegalArgumentException(
//                "attempt to complete passive State: " + this);
//        if (basis == null) throw new NullPointerException("null basis");
//        if (!basis.isCompleted()) throw new IllegalArgumentException("basis is not completed: " + basis);
//        if ((ruleStartPosition + ruleDotPosition) == 0 || !basis.getRule().left.equals(this.getActiveCategory()))
//            throw new IllegalArgumentException(this + " is not completed by basis " + basis);
//        advanceDot();
//        return this;
//    }
//
//    /**
//     * Creates and returns a new dotted rule exactly like the one provided
//     * except that its dot position is advanced by
//     * <code>1</code>.
//     *
//     * @throws IndexOutOfBoundsException If the dotted rule's dot position
//     *                                   is already at the end of its right side.
//     */
//    @SuppressWarnings("WeakerAccess")
//    State advanceDot() {
//        int position = (ruleDotPosition);
//        if (position < 0 || position > rule.right.length) throw new IndexOutOfBoundsException(
//                "illegal position: " + position);
//        ruleDotPosition++;
//        return this;
//    }
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

        if (ruleStartPosition != state.ruleStartPosition) return false;
        if (ruleDotPosition != state.ruleDotPosition) return false;
        if (positionInInput != state.positionInInput) return false;
        return rule.equals(state.rule);

    }

    @Override
    public int hashCode() {
        int result = rule.hashCode();
        result = 31 * result + ruleStartPosition;
        result = 31 * result + ruleDotPosition;
        result = 31 * result + positionInInput;
        return result;
    }

    public static class Score {
        /**
         * The forward probability <code>α_i</code> of a state is
         * the sum of the probabilities of
         * all constrained paths of length i that end in that state, do all
         * paths from start to position i. So this includes multiple
         * instances of the same history, which may happen because of recursion.
         */
        private double forwardScore;
        /**
         * The inner probability <code>γ_{i}</code> of a state
         * is the sum of the probabilities of all
         * paths of length (i - k) that start at position k (the rule's start position),
         * and end at the current state and generate the input the input symbols up to k.
         * Note that this is conditional on the state happening at position k with
         * a certain non-terminal X
         */
        private double innerScore;

        public Score(double forwardScore, double innerScore) {
            this.forwardScore = forwardScore;
            this.innerScore = innerScore;
        }

        public void incrementForwardScore(double by) {
            forwardScore += by;
        }

        public void incrementInnerScore(double by) {
            innerScore += by;
        }

        public double getInnerScore() {
            return innerScore;
        }

        public double getForwardScore() {
            return forwardScore;
        }
    }
}
