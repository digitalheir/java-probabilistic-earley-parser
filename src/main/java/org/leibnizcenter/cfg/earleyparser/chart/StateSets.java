package org.leibnizcenter.cfg.earleyparser.chart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedTokenState;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.state.StateToXMap;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.*;

/**
 * Represents an index of states, indexed by many different aspects
 * <p>
 * Created by maarten on 31/10/16.
 */
@SuppressWarnings("WeakerAccess")
public class StateSets<T> {
    private final StateToXMap<State> states = new StateToXMap<>(500);
    private final TIntObjectHashMap<Set<State>> byIndex = new TIntObjectHashMap<>(500);

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

    private final TIntObjectHashMap<Set<State>> completedStates = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<Multimap<NonTerminal, State>> completedStatesFor = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<Set<State>> completedStatesThatAreNotUnitProductions = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<Set<State>> statesActiveOnNonTerminals = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<Multimap<NonTerminal, State>> nonTerminalActiveAtIWithNonZeroUnitStarToY = new TIntObjectHashMap<>(500, 0.5F, -1);
    private final TIntObjectHashMap<Map<Terminal<T>, Set<State>>> statesActiveOnTerminals = new TIntObjectHashMap<>(500);
    private final Grammar<T> grammar;
    private final Map<NonTerminal, TIntObjectHashMap<Set<State>>> statesActiveOnNonTerminal = new HashMap<>(500);


    StateSets(Grammar<T> grammar) {
        this.grammar = grammar;
        DblSemiring semiring = grammar.getSemiring();
        this.forwardScores = new ForwardScores(semiring);
        this.innerScores = new InnerScores(semiring);
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
        if (!nonTerminalActiveAtIWithNonZeroUnitStarToY.contains(j))
            return null;
        else
            return nonTerminalActiveAtIWithNonZeroUnitStarToY.get(j).get(Y);
    }

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
            State state = create(position, ruleStart, dotPosition, rule, scannedToken);
            addState(state);
            return state;
        }

    }

    private void addState(State state) {
        states.put(state, state);

        int index = state.getPosition();
        add(byIndex, index, state);

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
                final Grammar.LeftCorners unitStar = grammar.getUnitStar();
                Collection<NonTerminal> scores = unitStar.getNonZeroNonTerminals(activeCategory);
                scores.forEach(Y -> addToNonTerminalActiveAtIWithNonZeroUnitStarToY(state, index, Y));
            } else if (activeCategory instanceof Terminal)  //noinspection unchecked
                addStateToActiveOnTerminal(index, (Terminal<T>) activeCategory, state);
            else throw new IssueRequest("Neither Terminal nor NonToken...?");
        }
    }

    private void addToNonTerminalActiveAtIWithNonZeroUnitStarToY(State state, int index, NonTerminal Y) {
        if (!nonTerminalActiveAtIWithNonZeroUnitStarToY.containsKey(index))
            nonTerminalActiveAtIWithNonZeroUnitStarToY.put(index, HashMultimap.create());
        nonTerminalActiveAtIWithNonZeroUnitStarToY.get(index).put(Y, state);
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


    public Set<State> getStates(int index) {
        return byIndex.get(index);
    }

    public void addIfNew(State state) {
        if (!contains(state.rule, state.positionInInput, state.ruleStartPosition, state.ruleDotPosition))
            addState(state);
    }

    public State get(int index, int ruleStart, int ruleDot, Rule rule) {
        return states.get(rule, index, ruleStart, ruleDot);
    }

    public static <E> State create(int index, int ruleStart, int dotPosition, Rule rule, Token<E> c) {
        if (c != null) return new ScannedTokenState<>(c, rule, ruleStart, index, dotPosition);
        else return new State(rule, index, ruleStart, dotPosition);
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
