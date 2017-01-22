package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represented chart indexes to completed states
 *
 * Created by maarten on 18-1-17.
 */
public class CompletedStates {
    private final TIntObjectHashMap<Set<State>> completedStates = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<MyMultimap<NonTerminal, State>> completedStatesFor = new TIntObjectHashMap<>(500);
    private final TIntObjectHashMap<Set<State>> completedStatesThatAreNotUnitProductions = new TIntObjectHashMap<>(500);

    private MyMultimap<NonTerminal, State> getMapFromLeftHandSide(int position) {
        return completedStatesFor.get(position);
    }

    private Set<State> getCompletedStates(int index, boolean allowUnitProductions) {
        if (allowUnitProductions) {
            if (!completedStates.containsKey(index)) completedStates.put(index, new HashSet<>());
            return completedStates.get(index);
        } else {
            if (!completedStatesThatAreNotUnitProductions.containsKey(index))
                completedStatesThatAreNotUnitProductions.put(index, new HashSet<>());
            return completedStatesThatAreNotUnitProductions.get(index);
        }
    }

    public Set<State> getCompletedStates(int index) {
        return getCompletedStates(index, true);
    }

    public Set<State> getCompletedStatesThatAreNotUnitProductions(int index) {
        return getCompletedStates(index, false);
    }

    /**
     * Runs in O(1)
     */
    void add(int position, State state) {
        if (state.isCompleted()) {
            StateSets.add(completedStates, position, state);
            if (!state.rule.isUnitProduction()) StateSets.add(completedStatesThatAreNotUnitProductions, position, state);
            addToCompletedStatesFor(state);
        }
    }

    public Collection<State> getCompletedStates(int i, NonTerminal s) {
        MyMultimap<NonTerminal, State> m = this.getMapFromLeftHandSide(i);
        if (m != null && m.containsKey(s)) return m.get(s);
        return Collections.emptySet();
    }

    /**
     * Runs in O(1)
     *
     * @param state State to add
     */
    private void addToCompletedStatesFor(final State state) {
        final int index = state.position;

        MyMultimap<NonTerminal, State> m = completedStatesFor.get(index);
        if (m == null) m = new MyMultimap<>();

        m.put(state.rule.left, state);
        completedStatesFor.putIfAbsent(index, m);
    }
}
