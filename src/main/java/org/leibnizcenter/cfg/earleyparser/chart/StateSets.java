package org.leibnizcenter.cfg.earleyparser.chart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedTokenState;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents an index of states, indexed by many different aspects
 * <p>
 * Created by maarten on 31/10/16.
 */
@SuppressWarnings("WeakerAccess")
public class StateSets<T> {
    private final StateIndex states = new StateIndex(500);

    private final TIntObjectHashMap<Set<State>> byIndex = new TIntObjectHashMap<>(500);

    /**
     * The forward probability <code>α_i</code> of a state is
     * the sum of the probabilities of
     * all constrained paths of length i that end in that state, do all
     * paths from start to position i. So this includes multiple
     * instances of the same history, which may happen because of recursion.
     */
    private final TObjectDoubleHashMap<State> forwardScores;

    /**
     * The inner probability <code>γ_{i}</code> of a state
     * is the sum of the probabilities of all
     * paths of length (i - k) that start at position k (the rule's start position),
     * and end at the current state and generate the input the input symbols up to k.
     * Note that this is conditional on the state happening at position k with
     * a certain non-terminal X
     */
    private final TObjectDoubleHashMap<State> innerScores;
    private final ConcurrentMap<State, State.ViterbiScore> viterbiScores = new ConcurrentHashMap<>(500);

    private final TIntObjectHashMap<Set<State>> completedStates = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<Multimap<NonTerminal, State>> completedStatesFor = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<Set<State>> completedStatesThatAreNotUnitProductions = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<Set<State>> statesActiveOnNonTerminals = new TIntObjectHashMap<>(500);
    //        private final TIntObjectHashMap<Multimap<NonTerminal, State>> statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY = new TIntObjectHashMap<>(500);


    private final TIntObjectHashMap<Multimap<NonTerminal, State>> nonTerminalActiveAtIWithNonZeroUnitStarToY = new TIntObjectHashMap<>(500, 0.5F, -1);

    private final TIntObjectHashMap<Map<Terminal<T>, Set<State>>> statesActiveOnTerminals = new TIntObjectHashMap<>(500);
    private final DblSemiring semiring;
    private final Grammar<T> grammar;
    private Map<NonTerminal, TIntObjectHashMap<Set<State>>> statesActiveOnNonTerminal = new HashMap<>(500);


    StateSets(Grammar<T> grammar) {
        this.grammar = grammar;
        this.semiring = grammar.getSemiring();
        this.forwardScores = new TObjectDoubleHashMap<>(500, 0.5F, semiring.zero());
        this.innerScores = new TObjectDoubleHashMap<>(500, 0.5F, semiring.zero());
    }

    public Collection<State> getCompletedStates(int i, NonTerminal s) {
        Multimap<NonTerminal, State> m = this.completedStatesFor.get(i);
        if (m != null && m.containsKey(s)) return m.get(s);
        return Collections.emptySet();
    }

    public Set<State> getCompletedStates(int index) {
        return getCompletedStates(index, true);
    }

    public Set<State> getCompletedStatesThatAreNotUnitProductions(int index) {
        return getCompletedStates(index, false);
    }

    public Set<State> getCompletedStates(int index, boolean allowUnitProductions) {
        if (allowUnitProductions) {
            if (!completedStates.containsKey(index)) completedStates.put(index, new HashSet<>());
            return completedStates.get(index);
        } else {
            if (!completedStatesThatAreNotUnitProductions.containsKey(index))
                completedStatesThatAreNotUnitProductions.put(index, new HashSet<>());
            return completedStatesThatAreNotUnitProductions.get(index);
        }
    }

    public Collection<State> getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(int j, NonTerminal Y) {
        if (!nonTerminalActiveAtIWithNonZeroUnitStarToY.contains(j)) return Collections.emptyList();
        else return nonTerminalActiveAtIWithNonZeroUnitStarToY.get(j).get(Y);
    }

//        public Collection<State> getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(int index, NonTerminal Y) {
//            if (!statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.containsKey(index))
//                statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.put(index, HashMultimap.create());
//            return statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.get(index).get(Y);
//        }


    public Set<State> getStatesActiveOnNonTerminal(NonTerminal y, int position, int beforeOrOnPosition) {
        // stateToAdvance.getPosition() <= beforeOrOnPosition;
        if (position <= beforeOrOnPosition) {
            TIntObjectHashMap<Set<State>> setTIntObjectHashMap = statesActiveOnNonTerminal.get(y);
            if (setTIntObjectHashMap != null && setTIntObjectHashMap.containsKey(position))
                return setTIntObjectHashMap.get(position);
        }
        return Collections.emptySet();
    }

    public Set<State> getStatesActiveOnNonTerminals(int index) {
        if (!statesActiveOnNonTerminals.containsKey(index)) statesActiveOnNonTerminals.put(index, new HashSet<>());
        return statesActiveOnNonTerminals.get(index);
    }


    public Set<State> getStatesActiveOnTerminals(int index, Terminal<T> terminal) {
        if (!statesActiveOnTerminals.containsKey(index)) return null;
        else {
            Map<Terminal<T>, Set<State>> map = statesActiveOnTerminals.get(index);
            if (map.containsKey(terminal)) return map.get(terminal);
            else return null;
        }
    }


    /**
     * Default zero
     *
     * @param s state
     * @return forward score so far
     */
    public double getForwardScore(State s) {
        return forwardScores.get(s);
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
        TIntObjectMap<State> dotToState = states.getDotToState(rule, position, ruleStart);
        if (!dotToState.containsKey(dotPosition)) {
            // Add state if it does not exist yet
            State state = create(position, ruleStart, dotPosition, rule, scannedToken);
            addState(dotToState, state);
        }
        return dotToState.get(dotPosition);
    }

    private void addState(TIntObjectMap<State> dotPositionToState, State state) {
        int index = state.getPosition();
        int dotPosition = state.getRuleDotPosition();
        add(byIndex, index, state);
        dotPositionToState.put(dotPosition, state);

        if (state.isCompleted()) {
            add(completedStates, index, state);
            if (!state.getRule().isUnitProduction()) add(completedStatesThatAreNotUnitProductions, index, state);
            addToCompletedStatesFor(state);
        }
        if (state.isActive()) {
            Category activeCategory = state.getActiveCategory();
            if (activeCategory instanceof NonTerminal) {
                addToStatesActiveOnNonTerminal(state);
                add(statesActiveOnNonTerminals, index, state);

//                    Multimap<NonTerminal, State> mapp = statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.get(index);
//                    final Multimap<NonTerminal, State> map = mapp == null ? HashMultimap.create() //We expect this
//                            : mapp;
                final Grammar.LeftCorners unitStar = grammar.getUnitStar();
                Collection<NonTerminal> scores = unitStar.getNonZeroNonTerminals(activeCategory);
                scores.forEach(Y -> {
//                        map.put(Y, state);
                    if (!nonTerminalActiveAtIWithNonZeroUnitStarToY.containsKey(index))
                        nonTerminalActiveAtIWithNonZeroUnitStarToY.put(index, HashMultimap.create());
                    nonTerminalActiveAtIWithNonZeroUnitStarToY.get(index).put(Y, state);
                });
//                    statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.put(index, map);
            } else if (activeCategory instanceof Terminal)  //noinspection unchecked
                addStateToActiveOnTerminal(index, (Terminal<T>) activeCategory, state);
            else throw new IssueRequest("Neither Terminal nor NonToken...?");
        }
    }

    private void addStateToActiveOnTerminal(int index, Terminal<T> activeCategory, State state) {
        if (!statesActiveOnTerminals.containsKey(index)) statesActiveOnTerminals.put(index, new HashMap<>());
        Map<Terminal<T>, Set<State>> terminalSetMap = statesActiveOnTerminals.get(index);
        if (!terminalSetMap.containsKey(activeCategory)) terminalSetMap.put(activeCategory, new HashSet<>());
        terminalSetMap.get(activeCategory).add(state);
    }

    private void addToCompletedStatesFor(State state) {
        int index = state.getPosition();
        Multimap<NonTerminal, State> m = completedStatesFor.get(index);
        if (m == null) m = HashMultimap.create();
        m.put(state.getRule().getLeft(), state);
        completedStatesFor.putIfAbsent(index, m);
    }

    private void addToStatesActiveOnNonTerminal(State state) {
        int index = state.getPosition();
        final NonTerminal activeCategory = (NonTerminal) state.getActiveCategory();
        TIntObjectHashMap<Set<State>> mapForCategory = statesActiveOnNonTerminal.get(activeCategory);
        if (mapForCategory == null) mapForCategory = new TIntObjectHashMap<>(50, 0.5F, -1);
        Set<State> s = mapForCategory.get(index);
        if (s == null) {
            s = new HashSet<>();
            mapForCategory.put(index, s);
        }
        s.add(state);
        statesActiveOnNonTerminal.putIfAbsent(activeCategory, mapForCategory);
    }

    private void add(TIntObjectHashMap<Set<State>> states, int index, State state) {
        if (!states.containsKey(index)) states.put(index, new HashSet<>());
        states.get(index).add(state);
    }

    public void addForwardScore(State state, double increment) {
        forwardScores.put(state, semiring.plus(getForwardScore(state)/*default zero*/, increment));
    }

//    public void addInnerScore(State state, double increment) {
//        innerScores.put(state, semiring.plus(getInnerScore(state)/*default zero*/, increment));
//    }

    public void setForwardScore(State s, double probability) {
        forwardScores.put(s, probability);
    }

    public void setInnerScore(State s, double probability) {
        innerScores.put(s, probability);
    }

    /**
     * Threadsafe because viterbiScores is a ConcurrentHashMap
     *
     * @param v viterbi score
     */

    public void setViterbiScore(State.ViterbiScore v) {
        setViterbiScore(v.getResultingState(), v);
    }

    /**
     * Threadsafe because viterbiScores is a ConcurrentHashMap
     *
     * @param resultingState The resulting state from the transition
     * @param v              viterbi score
     */
    public void setViterbiScore(State resultingState, State.ViterbiScore v) {
        viterbiScores.put(resultingState, v);
    }

    /**
     * Default zero
     *
     * @param state State for which to get inner score
     * @return inner score so far
     */
    public double getInnerScore(State state) {
        return innerScores.get(state);
    }

    public Set<State> getStates(int index) {
        return byIndex.get(index);
    }


    public State.ViterbiScore getViterbiScore(State s) {
        return viterbiScores.get(s);
    }

    public void add(State state) {
        Rule rule = state.getRule();
        int ruleStart = state.getRuleStartPosition();
        int index = state.getPosition();

        TIntObjectMap<TIntObjectMap<State>> forRuleStart = states.getRuleStartToDotToState(rule, index);
        if (!forRuleStart.containsKey(ruleStart)) forRuleStart.put(ruleStart, new TIntObjectHashMap<>(50));
        TIntObjectMap<State> dotToState = forRuleStart.get(ruleStart);

        addState(dotToState, state);
    }

//    public synchronized State getSynchronized(int index, int ruleStart, int ruleDot, Rule rule) {
//        return states.getState(rule, index, ruleStart, ruleDot);
//    }

    public State get(int index, int ruleStart, int ruleDot, Rule rule) {
        return states.getState(rule, index, ruleStart, ruleDot);
    }

    public static <E> State create(int index, int ruleStart, int dotPosition, Rule rule, Token<E> c) {
        if (c != null) return new ScannedTokenState<>(c, rule, ruleStart, index, dotPosition);
        else return new State(rule, ruleStart, index, dotPosition);
    }

    public int countStates() {
        //noinspection unchecked
        return Arrays.stream(byIndex.values(new Set[byIndex.size()]))
                .mapToInt(Set::size).sum();
    }

}
