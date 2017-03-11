package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.UnitStarScores;
import org.leibnizcenter.cfg.util.Collections2;
import org.leibnizcenter.cfg.util.MyMultimap;
import org.leibnizcenter.cfg.util.StateInformationTriple;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents an index of active states in a chart
 * <p>
 * Created by maarten on 18-1-17.
 */
public class ActiveStates<T> {
    private final TIntObjectHashMap<Set<State>> statesActiveOnNonTerminals = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<MyMultimap<NonTerminal, State>> nonTerminalActiveAtIWithNonZeroUnitStarToY = new TIntObjectHashMap<>(500, 0.5F, -1);
    private final TIntObjectHashMap<Map<Terminal<T>, Set<State>>> statesActiveOnTerminals = new TIntObjectHashMap<>(500);
    private final Map<NonTerminal, TIntObjectHashMap<Set<State>>> statesActiveOnNonTerminal = new HashMap<>(500);
    private final MyMultimap<Integer, State> justScannedError = new MyMultimap<>(); // todo int
    private Collection<State> activeOnNonLexicalToken = new HashSet<>();

    /**
     * Runs in O(1).
     */
    @SuppressWarnings("WeakerAccess")
    public Collection<State> getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(int j, NonTerminal cat) {
        if (!nonTerminalActiveAtIWithNonZeroUnitStarToY.contains(j))
            return null;
        else
            return nonTerminalActiveAtIWithNonZeroUnitStarToY.get(j).get(cat);
    }

    public Set<State> getStatesActiveOnNonTerminal(NonTerminal nonTerminal, int position, int beforeOrOnPosition) {
        // stateToAdvance.position <= beforeOrOnPosition;
        if (position <= beforeOrOnPosition) {
            TIntObjectHashMap<Set<State>> setTIntObjectHashMap = statesActiveOnNonTerminal.get(nonTerminal);
            if (setTIntObjectHashMap != null && setTIntObjectHashMap.containsKey(position))
                return setTIntObjectHashMap.get(position);
        }
        return null;
    }

    public Set<State> getActiveOnNonTerminals(int index) {
        //if (!statesActiveOnNonTerminals.containsKey(index)) statesActiveOnNonTerminals.put(index, new HashSet<>());
        return statesActiveOnNonTerminals.get(index);
    }

    /**
     * runs in O(1)
     *
     * @param position Position in input
     * @param terminal Terminal on which states should be active
     * @return States active on given position and terminal
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public Collection<State> getActiveOn(int position, Terminal<?> terminal) {
        if (statesActiveOnTerminals.containsKey(position)) {
            Set<State> states = statesActiveOnTerminals.get(position).get(terminal);
            if (Collections2.isFilled(states))
                return Collections.unmodifiableCollection(states);
        }
        return Collections.emptySet();
    }

    /**
     * runs in O(1)
     *
     * @param position       Position in input
     * @param activeCategory Category on which state is active
     * @param state          State to add
     */
    private void addStateToActiveOnTerminal(int position, Terminal<T> activeCategory, State state) {
        if (!activeCategory.equals(state.getActiveCategory()))
            throw new IssueRequest("Given category was not the same category on which the state was active. This is a bug.");
        if (!statesActiveOnTerminals.containsKey(position))
            statesActiveOnTerminals.put(position, new HashMap<>());
        final Map<Terminal<T>, Set<State>> terminalSetMap = statesActiveOnTerminals.get(position);
        if (!terminalSetMap.containsKey(activeCategory))
            terminalSetMap.put(activeCategory, new HashSet<>());
        terminalSetMap.get(activeCategory).add(state);
    }

    /**
     * runs in O(1)
     *
     * @param state State to add
     */
    private void addToStatesActiveOnNonTerminal(State state) {
        int position = state.position;
        final NonTerminal activeCategory = (NonTerminal) state.getActiveCategory();
        final TIntObjectHashMap<Set<State>> mapForCategory = statesActiveOnNonTerminal.containsKey(activeCategory) ? statesActiveOnNonTerminal.get(activeCategory) : new TIntObjectHashMap<>(50, 0.5F, -1);
        final Set<State> s = mapForCategory.containsKey(position) ? mapForCategory.get(position) : new HashSet<>();
        mapForCategory.putIfAbsent(position, s);
        s.add(state);
        statesActiveOnNonTerminal.putIfAbsent(activeCategory, mapForCategory);
    }


    /**
     * Runs in O(1)
     *
     * @param state State to add
     */
    private void addToNonTerminalActiveAtIWithNonZeroUnitStarToY(
            final State state,
            final int position,
            final NonTerminal Y) {
        if (!nonTerminalActiveAtIWithNonZeroUnitStarToY.containsKey(position))
            nonTerminalActiveAtIWithNonZeroUnitStarToY.put(position, new MyMultimap<>());
        nonTerminalActiveAtIWithNonZeroUnitStarToY.get(position).put(Y, state);
    }

    /**
     * Runs in O(N) for N is the number of NonTerminals with non-zero unit-star score on active category, which is bounded by the total number of non-terminals
     */
    void addIfActive(
            final int position,
            final State state,
            final UnitStarScores unitStar) {
        if (state.isActive()) {
            if (state.ruleDotPosition > 0 && state.rule.right.length > 0 && state.rule.right[state.ruleDotPosition - 1] instanceof NonLexicalToken) {
                justScannedError.put(position, state);
            }
            final Category activeCategory = state.getActiveCategory();
            if (activeCategory instanceof NonLexicalToken) {
                activeOnNonLexicalToken.add(state);
            }
            if (activeCategory instanceof NonTerminal) {
                addToStatesActiveOnNonTerminal(state);
                StateSets.add(statesActiveOnNonTerminals, position, state);
                final Collection<NonTerminal> scores = unitStar.getNonZeroNonTerminals((NonTerminal) activeCategory);
                scores.forEach(Y -> addToNonTerminalActiveAtIWithNonZeroUnitStarToY(state, position, Y));
            } else if (activeCategory instanceof Terminal)  //noinspection unchecked
                addStateToActiveOnTerminal(position, (Terminal<T>) activeCategory, state);
            else throw new IssueRequest("Neither Terminal nor NonToken...?");
        }
    }

    public Stream<? extends StateInformationTriple> streamAllStatesToAdvance(StateInformationTriple completedState) {
        final State state = completedState.completedState;
        final Collection<State> statesActive = getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(state.ruleStartPosition, state.rule.left);
        return statesActive == null ? Stream.empty() : statesActive
                .stream()
                .map(stateToAdvance -> new StateInformationTriple(
                                stateToAdvance,
                                state,
                                completedState.completedInner
                        )
                );
    }

    public Collection<State> getJustScannedError(int position) {
        return justScannedError.get(position);
    }

    public Collection<State> getActiveOnNonLexicalToken() {
        return activeOnNonLexicalToken;
    }
}
