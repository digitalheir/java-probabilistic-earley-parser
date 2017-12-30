package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.ScoresAsSemiringElements;
import org.leibnizcenter.cfg.util.MyMultimap;
import org.leibnizcenter.cfg.util.StateInformationTriple;

import java.util.*;
import java.util.stream.Stream;

import static org.leibnizcenter.cfg.util.Collections2.*;

/**
 * Represents an index of active states in a chart
 * <p>
 * Created by maarten on 18-1-17.
 */
public class ActiveStates<T> {
    private final List<Set<State>> statesActiveOnNonTerminals = new ArrayList<>(500);
    private final List<MyMultimap<NonTerminal, State>> nonTerminalActiveAtIWithNonZeroUnitStarToY = new ArrayList<>(500);
    private final List<Map<Terminal<T>, Set<State>>> statesActiveOnTerminals = new ArrayList<>(500);
    private final Map<NonTerminal, List<Set<State>>> statesActiveOnNonTerminal = new HashMap<>(500);
    private final MyMultimap<Integer, State> justScannedError = new MyMultimap<>(); // todo int
    public Collection<State> activeOnNonLexicalToken = new HashSet<>();

    /**
     * Runs in O(1).
     */
    @SuppressWarnings("WeakerAccess")
    public Collection<State> getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(final int j, final NonTerminal cat) {
        return j < nonTerminalActiveAtIWithNonZeroUnitStarToY.size()
                ? nonTerminalActiveAtIWithNonZeroUnitStarToY.get(j).get(cat) : null;
    }

    public Set<State> getStatesActiveOnNonTerminal(final NonTerminal nonTerminal, final int position, final int beforeOrOnPosition) {
        // stateToAdvance.position <= beforeOrOnPosition;
        if (position <= beforeOrOnPosition) {
            final List<Set<State>> statesActiveOnPosition = statesActiveOnNonTerminal.get(nonTerminal);
            if (statesActiveOnPosition != null && statesActiveOnPosition.size() > position)
                return statesActiveOnPosition.get(position);
        }
        return null;
    }

    public Set<State> getActiveOnNonTerminals(final int index) {
        //if (!statesActiveOnNonTerminals.containsKey(index)) statesActiveOnNonTerminals.put(index, new HashSet<>());
        return getOrInitEmptySet(statesActiveOnNonTerminals, index);
    }

    /**
     * runs in O(1)
     *
     * @param position Position in input
     * @param terminal Terminal on which states should be active
     * @return States active on given position and terminal
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public Collection<State> getActiveOn(final int position, final Terminal<?> terminal) {
        final Map<Terminal<T>, Set<State>> map = getOrInitEmptyMap(statesActiveOnTerminals, position);
        final Set<State> t = map.get(terminal);
        return t != null ? t : Collections.emptySet();
    }

    /**
     * runs in O(1)
     *
     * @param position       Position in input
     * @param activeCategory Category on which state is active
     * @param state          State to add
     */
    private void addStateToActiveOnTerminal(final int position, final Terminal<T> activeCategory, final State state) {
        if (!activeCategory.equals(state.getActiveCategory()))
            throw new IssueRequest("Given category was not the same category on which the state was active. This is a bug.");
        final Map<Terminal<T>, Set<State>> terminalSetMap = getOrInitEmptyMap(statesActiveOnTerminals, position);
        if (!terminalSetMap.containsKey(activeCategory))
            terminalSetMap.put(activeCategory, new HashSet<>());
        terminalSetMap.get(activeCategory).add(state);
    }

    /**
     * runs in O(1)
     *
     * @param state State to add
     */
    private void addToStatesActiveOnNonTerminal(final State state) {
        final int position = state.position;
        final NonTerminal activeCategory = (NonTerminal) state.getActiveCategory();

        final List<Set<State>> mapForCategory = statesActiveOnNonTerminal.containsKey(activeCategory)
                ? statesActiveOnNonTerminal.get(activeCategory)
                : new ArrayList<>(50);

        final Set<State> s = getOrInitEmptySet(mapForCategory, position);
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
        add(nonTerminalActiveAtIWithNonZeroUnitStarToY, position, Y, state);
    }

    /**
     * Runs in O(N) for N is the number of NonTerminals with non-zero unit-star score on active category, which is bounded by the total number of non-terminals
     */
    void addIfActive(
            final int position,
            final State state,
            final ScoresAsSemiringElements unitStar) {
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

    public Stream<? extends StateInformationTriple> streamAllStatesToAdvance(final StateInformationTriple completedState) {
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

    public Collection<State> getJustScannedError(final int position) {
        return justScannedError.get(position);
    }

}
