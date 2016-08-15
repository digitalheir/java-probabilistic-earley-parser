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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Maarten on 31-7-2016.
 */
public class Parser {

    public static <E> boolean recognize(NonTerminal S,
                                        Grammar grammar,
                                        Iterable<Token<E>> tokens) {
        final ChartWithInputPosition parse = parseAndCountTokens(S, grammar, tokens, null);
        return parse.chart.getCompletedStates(parse.index, S).size() > 0;
    }


    /**
     * Performs the backward part of the forward-backward algorithm
     * <p/>
     * TODO make this an iterative algo instead of recursive
     */
    public static ParseTree getViterbiParse(State state, Chart chart) {
        //TODO index these relations
        // state { i: X_k -> \.v }
        if (state.getRuleDotPosition() <= 0) {
            return new ParseTree(state.getRule().getLeft());
        } else {
            Category prefixEnd = state.getRule().getRight()[state.getRuleDotPosition() - 1];
            if (prefixEnd instanceof Terminal) {
                // let \'a = \, call
                ParseTree T = getViterbiParse(
                        chart.stateSets.get(state.getPosition() - 1, state.getRuleStartPosition(), state.getRuleDotPosition() - 1, state.getRule()),
                        chart
                );
                T.addRightMost(new ParseTree(state.getRule().getRight()[state.getRuleDotPosition() - 1]));
                return T;
            } else {
                if (!(prefixEnd instanceof NonTerminal)) throw new Error("???");
                State.ViterbiScore viterbi = chart.getViterbiScore(state); // must exist

                State origin = viterbi.getOrigin();
                ParseTree T = getViterbiParse(chart.stateSets.get(origin.ruleStartPosition, state.getRuleStartPosition(), state.getRuleDotPosition() - 1, state.getRule()), chart);
                ParseTree Tprime = getViterbiParse(origin, chart);
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

    public static <E> ParseTree getViterbiParse(NonTerminal S, Grammar grammar, Iterable<Token<E>> tokens) {
        return getViterbiParseWithScore(S, grammar, tokens).getParseTree();
    }

    public static <E> ParseTreeWithScore getViterbiParseWithScore(NonTerminal S, Grammar grammar, Iterable<Token<E>> tokens) {
        ChartWithInputPosition chart = parseAndCountTokens(S, grammar, tokens, null);

        List<ParseTreeWithScore> parses = chart.chart.getCompletedStates(chart.index, Category.START).stream()
                .map(state -> new ParseTreeWithScore(getViterbiParse(state, chart.chart), chart.chart.getViterbiScore(state), grammar.getSemiring()))
        .collect(Collectors.toList());
        if (parses.size() > 1) throw new Error("Found more than one Viterbi parses. This is a bug.");
        return parses.size() == 0 ? null : parses.get(0);
    }

    public static <E> ChartWithInputPosition parseAndCountTokens(NonTerminal S,
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

            Set<State> completedStates = new HashSet<>(chart.stateSets.getCompletedStates(i + 1));
            completedStates.forEach(s -> chart.viterbi(s, new HashSet<>(), grammar.getSemiring()));

            chart.completeTruncated(i + 1);
//            chart.computeViterbi(i + 1);
            i++;
        }

        //Set<State> completed = chart.getCompletedStates(i, Category.START);
        //if (completed.size() > 1) throw new Error("This is a bug");
        return new ChartWithInputPosition(chart, i);
    }

    public static <E> Chart parse(NonTerminal S,
                                  Grammar grammar,
                                  Iterable<Token<E>> tokens,
                                  ScanProbability scanProbability) {
        return parseAndCountTokens(S, grammar, tokens, scanProbability).chart;
    }

    static class ChartWithInputPosition {
        public final Chart chart;
        public final int index;

        public ChartWithInputPosition(Chart chart, int index) {
            this.chart = chart;
            this.index = index;
        }
    }
}
