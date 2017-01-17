package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.TokenWithCategories;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>
 * A chart produced by an  Earley parser.
 * <p/>
 * <p>
 * Charts contain sets of {@link State states} mapped to the string indices where
 * they originate. Since the state sets are {@link Set sets}, an state can only
 * be added at a given index once (as sets do not permit duplicate members).
 * State sets are not guaranteed to maintain states in their order of insertion.
 * </p>
 */
public class Chart<T> {
    public final StateSets<T> stateSets;
    public final Grammar<T> grammar;

    /**
     * Creates a new chart, initializing its internal data structure.
     */
    public Chart(Grammar<T> grammar) {
        this(grammar, new StateSets<>(grammar));
    }

    /**
     * Creates a new chart from the specified sorted map of indices mapped to
     * state sets.
     *
     * @param stateSets The map of integer-mapped state sets to use as this
     *                  chart's backing data structure.
     */
    private Chart(Grammar<T> grammar, StateSets<T> stateSets) {
        this.stateSets = stateSets;
        this.grammar = grammar;
    }


    /**
     * Handles a token scanned from the input string.
     *
     * @param tokenPosition   The start index of the scan.
     * @param token           The token that was scanned.
     * @param scanProbability Function that provides the probability of scanning the given token at this position. Might be null for a probability of 1.0.
     */
    public void scan(final int tokenPosition, final TokenWithCategories<T> token, final ScanProbability scanProbability) {
        Scan.scan(tokenPosition, token, scanProbability, grammar, stateSets);
    }

    /**
     * For finding the Viterbi path, we can't conflate production recursions (ie can't use the left star corner),
     * exactly because we need it to find the unique Viterbi path.
     * Luckily, we can avoid looping over unit productions because it only ever lowers probability
     * (assuming p = [0,1] and Occam's razor). ~This method does not guarantee a left most parse.~
     *
     * @param completedState Completed state to calculate Viterbi score for
     * @param originPathTo   Path to state
     * @param sr             Semiring to use for calculating
     *///TODO write tests
    public static <T> void setViterbiScores(final State completedState, final Set<State> originPathTo, final DblSemiring sr, StateSets<T> stateSets) {
        Collection<State> newStates = null; // init as null to avoid arraylist creation
        Collection<State> newCompletedStates = null; // init as null to avoid arraylist creation

        if (stateSets.getViterbiScore(completedState) == null)
            throw new IssueRequest("Expected Viterbi score to be set on completed state. This is a bug.");

        final double completedViterbi = stateSets.getViterbiScore(completedState).getScore();
        final NonTerminal Y = completedState.getRule().getLeft();
        //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Yμ</code>
        int completedPos = completedState.getPosition();
        for (State stateToAdvance : stateSets.getStatesActiveOnNonTerminal(Y, completedState.getRuleStartPosition(), completedPos)) {
            if (stateToAdvance.getPosition() > completedPos || stateToAdvance.getPosition() != completedState.getRuleStartPosition())
                throw new IssueRequest("Index failed. This is a bug.");
            int ruleStart = stateToAdvance.getRuleStartPosition();
            int nextDot = stateToAdvance.advanceDot();
            Rule rule = stateToAdvance.getRule();
            State resultingState = stateSets.get(completedPos, ruleStart, nextDot, rule);
            if (resultingState == null) {
                resultingState = State.create(completedPos, ruleStart, nextDot, rule);
                if (newStates == null) newStates = new HashSet<>(20);
                newStates.add(resultingState);
            }
            if (originPathTo.contains(resultingState)) {
                System.err.println("Already went past " + resultingState);
                return;
            }
            final State.ViterbiScore viterbiScore = stateSets.getViterbiScore(resultingState);
            final State.ViterbiScore prevViterbi = stateSets.getViterbiScore(stateToAdvance);
            if (prevViterbi == null) throw new Error("Expected viterbi to be set for " + stateToAdvance);
            final double prev = prevViterbi.getScore();
            final State.ViterbiScore newViterbiScore = new State.ViterbiScore(sr.times(completedViterbi, prev), completedState, resultingState, sr);

            if (viterbiScore == null || viterbiScore.compareTo(newViterbiScore) < 0) {
                stateSets.setViterbiScore(newViterbiScore);
                if (resultingState.isCompleted()) {
                    if (newCompletedStates == null) newCompletedStates = new HashSet<>(20);
                    newCompletedStates.add(resultingState);
                }
            }

        }

        // Add new states to chart
        if (newStates != null) newStates.forEach(stateSets::add);

        // Recurse with new states that are completed
        if (newCompletedStates != null) newCompletedStates.forEach(resultingState -> {
            final Set<State> path = new HashSet<>(originPathTo);
            path.add(resultingState);
            setViterbiScores(resultingState, path, sr, stateSets);
        });
    }

    /**
     * Counts the total number of states contained in this chart, at any
     * index.
     *
     * @return The total number of states contained.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public int countStates() {
        return stateSets.countStates();
    }

    /**
     * Gets a string representation of this chart.
     */
    @Override
    public String toString() {
        return stateSets.toString();
    }

    @SuppressWarnings("WeakerAccess")
    public void addState(@SuppressWarnings("SameParameterValue") int index, State state, double forward, double inner) {
        stateSets.getOrCreate(index, state.getRuleStartPosition(), state.getRuleDotPosition(), state.getRule());
        stateSets.setInnerScore(state, inner);
        stateSets.setForwardScore(state, forward);
        if (stateSets.getViterbiScore(state) == null)
            stateSets.setViterbiScore(new State.ViterbiScore(grammar.getSemiring().one(), null, state, grammar.getSemiring()));
    }

    @SuppressWarnings("unused")
    public Set<State> getStates(int index) {
        return stateSets.getStates(index);
    }

    public double getForwardScore(State s) {
        return stateSets.getForwardScore(s);
    }

    @SuppressWarnings("unused")
    public double getInnerScore(State s) {
        return stateSets.getInnerScore(s);
    }

    public State.ViterbiScore getViterbiScore(State s) {
        return stateSets.getViterbiScore(s);
    }
}
