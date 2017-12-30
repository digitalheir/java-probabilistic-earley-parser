package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.*;

import static org.leibnizcenter.cfg.util.Collections2.*;

/**
 * Represented chart indexes to completed states
 *
 * Created by maarten on 18-1-17.
 */
public class CompletedStates {
    private final List<Set<State>> completedStates = new ArrayList<>(500);
    private final List<MyMultimap<NonTerminal, State>> completedStatesFor = new ArrayList<>(500);
    private final List<Set<State>> completedStatesThatAreNotUnitProductions = new ArrayList<>(500);
    private final List<List<Rule>> justCompletedErrorRulesCount = new ArrayList<>();

    private MyMultimap<NonTerminal, State> getMapFromLeftHandSide(final int position) {
        return getOrInitEmptyMultimap(completedStatesFor, position);
    }

    private Set<State> getCompletedStates(final int index, final boolean allowUnitProductions) {
        return getOrInitEmptySet(allowUnitProductions ? completedStates : completedStatesThatAreNotUnitProductions, index);
    }

    public Set<State> getCompletedStates(final int index) {
        return getCompletedStates(index, true);
    }

    public Set<State> getCompletedStatesThatAreNotUnitProductions(final int index) {
        return getCompletedStates(index, false);
    }

    /**
     * Runs in O(1)
     */
    void addIfCompleted(final State state) {
        if (state.isCompleted()) {
            StateSets.add(completedStates, state.position, state);
            if (!state.rule.isUnitProduction())
                StateSets.add(completedStatesThatAreNotUnitProductions, state.position, state);
            addToCompletedStatesFor(state);
            if (state.rule.isErrorRule) {
                addCompletedErrorRule(state.position, state.rule);
            }
        }
    }

    public Collection<State> getCompletedStates(final int i, final NonTerminal s) {

        final MyMultimap<NonTerminal, State> m = this.getMapFromLeftHandSide(i);
        if (m.containsKey(s)) return m.get(s);
        return Collections.emptySet();
    }

    /**
     * Runs in O(1)
     *
     * @param state State to add
     */
    private void addToCompletedStatesFor(final State state) {
        final int index = state.position;
        add(completedStatesFor, index, state.rule.left, state);
    }

    private static <T> T getSafe(final List<T> list, final int index) {
        return list.size() > index ? list.get(index) : null;
    }

    public int getCompletedErrorRulesCount(final int index) {
        if (index >= justCompletedErrorRulesCount.size()) return 0;
        final List<Rule> rules = justCompletedErrorRulesCount.get(index);
        if (rules == null) return 0;
        return rules.size();
    }

    private int addCompletedErrorRule(final int index, final Rule rule) {
        final int prev = getCompletedErrorRulesCount(index);
        addSafe(getOrInitEmptyList(justCompletedErrorRulesCount, index), index, rule);
        return prev;
    }
}
