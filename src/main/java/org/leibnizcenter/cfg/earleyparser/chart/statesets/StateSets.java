package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.state.StateToXMap;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an index of states, indexed by many different aspects
 * <p>
 * Created by maarten on 31/10/16.
 */
@SuppressWarnings("WeakerAccess")
public class StateSets<T> {
    /**
     * The forward probability <code>α_i</code> of a state is
     * the sum of the probabilities of
     * all constrained paths of length i that end in that state, do all
     * paths from start to position i. So this includes multiple
     * instances of the same history, which may happen because of recursion.
     */
    public final ForwardScores forwardScores;
    /**
     * The inner probability <code>γ_{i}</code> of a state
     * is the sum of the probabilities of all
     * paths of length (i - k) that start at position k (the rule's start position),
     * and end at the current state and generate the input the input symbols up to k.
     * Note that this is conditional on the state happening at position k with
     * a certain non-terminal X
     */
    public final InnerScores innerScores;
    public final ViterbiScores viterbiScores = new ViterbiScores();
    public final CompletedStates completedStates = new CompletedStates();
    public final ActiveStates<T> activeStates = new ActiveStates<>();
    private final StateToXMap<State> states = new StateToXMap<>(500);
    private final TIntObjectHashMap<Set<State>> byIndex = new TIntObjectHashMap<>(500);
    private final Grammar<T> grammar;


    public StateSets(Grammar<T> grammar) {
        this.grammar = grammar;
        DblSemiring semiring = grammar.getSemiring();
        this.forwardScores = new ForwardScores(semiring);
        this.innerScores = new InnerScores(semiring);
    }

    /**
     * Runs in O(1) (expected time of map put)
     */
    static void add(TIntObjectHashMap<Set<State>> states, int position, State state) {
        if (!states.containsKey(position)) states.put(position, new HashSet<>());
        states.get(position).add(state);
    }

    public State getOrCreate(int index, int ruleStart, int dotPosition, Rule rule) {
        return getOrCreate(index, ruleStart, dotPosition, rule, null);
    }

    /**
     * Adds state if it does not exist yet
     *
     * @param position     State position
     * @param ruleStart    Rule start position
     * @param dotPosition  Rule dot position
     * @param rule         State rule
     * @param scannedToken The token that was scanned to create this state
     * @param <E>          Token type
     * @return State specified by parameter. May or may not be in the state table. If not, it is added.
     */
    public <E> State getOrCreate(int position, int ruleStart, int dotPosition, Rule rule, Token<E> scannedToken) {
        if (states.contains(rule, position, ruleStart, dotPosition)) {
            return states.get(rule, position, ruleStart, dotPosition);
        } else {
            State state = State.create(position, ruleStart, dotPosition, rule, scannedToken);
            addState(state);
            return state;
        }

    }

    /**
     * Runs in O(N) for N is the number of NonTerminals with non-zero unit-star score on active category, which is bounded by the total number of non-terminals
     *
     * @param state State to add
     */
    private void addState(final State state) {
        final int index = state.position;

        states.put(state, state);
        add(byIndex, index, state);
        completedStates.add(index, state);
        activeStates.add(index, state, grammar.getUnitStar());
    }

    public Set<State> getStates(int index) {
        return byIndex.get(index);
    }

    public void addIfNew(State state) {
        if (!contains(state.rule, state.position, state.ruleStartPosition, state.ruleDotPosition))
            addState(state);
    }

    public State get(int index, int ruleStart, int ruleDot, Rule rule) {
        return states.get(rule, index, ruleStart, ruleDot);
    }

    public int countStates() {
        //noinspection unchecked
        return Arrays.stream(byIndex.values(new Set[byIndex.size()]))
                .mapToInt(Set::size).sum();
    }

    public boolean contains(Rule rule, int position, int ruleStart, int dotPosition) {
        return states.contains(rule, position, ruleStart, dotPosition);
    }
}
