package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.earleyparser.parse.Chart;
import org.leibnizcenter.cfg.earleyparser.parse.ParseTree;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.token.Token;

/**
 * Created by Maarten on 31-7-2016.
 */
public class Parser {

    public static <E> boolean recognize(NonTerminal S,
                                        Grammar grammar,
                                        Iterable<Token<E>> tokens) {
        final Chart parse = parse(S, grammar, tokens);
        return parse.getCompletedStates(parse.length, S).size() > 0;
    }

//    public static <E> Path getViterbiPath(NonTerminal S,
//                                          Grammar grammar,
//                                          Iterable<Token<E>> tokens) {
//        Chart chart = parse(S, grammar, tokens, null);
//        Set<State> endStates = chart.getCompletedStates(i, Category.START);
//
//        State bestEndState = null;
//        for (State endState : endStates)
//            if (bestEndState == null || chart.getForwardScore(bestEndState) < chart.getForwardScore(endState))
//                bestEndState = endState;
//
//        return getViterbiPath(bestEndState, chart, new Path(grammar.getSemiring()));
//    }

    /**
     * Performs the backward part of the forward-backward algorithm
     *
     * TODO make this an iterative algo instead of recursive
     */
    public static ParseTree getViterbiPath(State state, Chart chart) {
        //TODO index these relations
        // state { i: X_k -> \.v }
        if (state.getRuleDotPosition() <= 0) {
            return new ParseTree(state.getRule().getLeft());
        } else {
            Category prefixEnd = state.getRule().getRight()[state.getRuleDotPosition() - 1];
            if (prefixEnd instanceof Terminal) {
                // let \'a = \, call
                ParseTree T = getViterbiPath(
                        chart.stateSets.get(state.getPosition() - 1, state.getRuleStartPosition(), state.getRuleDotPosition() - 1, state.getRule()),
                        chart
                );
                T.addRightMost(new ParseTree(state.getRule().getRight()[state.getRuleDotPosition() - 1]));
                return T;
            } else {
                if (!(prefixEnd instanceof NonTerminal)) throw new Error("???");
                State.ViterbiScore viterbi = chart.getViterbiScore(state); // must exist

                State origin = viterbi.getOrigin();
                ParseTree T = getViterbiPath(chart.stateSets.get(origin.ruleStartPosition, state.getRuleStartPosition(), state.getRuleDotPosition() - 1, state.getRule()), chart);
                ParseTree Tprime = getViterbiPath(origin, chart);
                T.addRightMost(Tprime);
                return T;
            }
        }
        }

    public static <E> Chart parse(NonTerminal S,
                                  Grammar grammar,
                                  Iterable<Token<E>> tokens) {
        return parse(S, grammar, tokens, null);
    }

    public static <E> Chart parse(NonTerminal S,
                                  Grammar grammar,
                                  Iterable<Token<E>> tokens,
                                  ScanProbability scanProbability) {
        Chart chart = new Chart(grammar);
        DblSemiring sr = grammar.getSemiring();

        // Initial state
        State initialState = new State(Rule.create(sr.one(), Category.START, S), 0);
        chart.addState(0, initialState, sr.one(), sr.one());

        // Cycle through input
        int i = 0;
        for (Token<E> token : tokens) {
            chart.predict(i);

            chart.scan(i, token, scanProbability);
            chart.completeTruncated(i + 1);
            chart.completeForViterbi(i + 1);
            i++;
        }
        //Set<State> completed = chart.getCompletedStates(i, Category.START);
        //if (completed.size() > 1) throw new Error("This is a bug");
        chart.length = i;
        return chart;
    }
}
