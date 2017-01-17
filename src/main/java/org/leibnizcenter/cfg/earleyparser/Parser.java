package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.*;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedTokenState;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ParseTree;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.parse.callbacks.ParseCallbacks;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper function for parsing
 * <p>
 * Created by Maarten on 31-7-2016.
 */
@SuppressWarnings("WeakerAccess")
public class Parser {

    /**
     * Parses the given list of tokens and returns he parse probability
     *
     * @param goal    Goal category, typically S for Sentence
     * @param grammar Grammar to apply to tokens
     * @param tokens  list of tokens to parse
     * @return Probability that given string of tokens mathces gven non-terminal with given grammar
     */
    public static <E> double recognize(NonTerminal goal,
                                       Grammar<E> grammar,
                                       Iterable<Token<E>> tokens) {
        return recognize(goal, grammar, tokens, null);
    }

    /**
     * Parses the given list of tokens and returns he parse probability
     *
     * @param goal    Goal category, typically S for Sentence
     * @param grammar Grammar to apply to tokens
     * @param tokens  list of tokens to parse
     * @return Probability that given string of tokens mathces gven non-terminal with given grammar
     */
    public static <E> double recognize(NonTerminal goal,
                                       Grammar<E> grammar,
                                       Iterable<Token<E>> tokens,
                                       @SuppressWarnings("SameParameterValue") ParseCallbacks callbacks) {
        final ChartWithInputPosition<E> parse = parseAndCountTokens(goal, grammar, tokens, callbacks);
        final Collection<State> completedStates = parse.chart.stateSets.getCompletedStates(parse.index, Category.START);
        if (completedStates.size() > 0) {
            if (completedStates.size() > 1)
                throw new IssueRequest("Multiple final states found. This is likely an error.");
            return completedStates.stream().mapToDouble(finalState ->
                    grammar.getSemiring().toProbability(
                            parse.chart.getForwardScore(finalState)
                    )).sum();
        } else {
            return 0.0;
        }
    }


    /**
     * Performs the backward part of the forward-backward algorithm
     * <p/>
     * TODO make this an iterative algorithm instead of recursive: might be more efficient?
     */
    public static ParseTree getViterbiParse(State state, Chart chart) {
        if (state.getRuleDotPosition() <= 0)
            // Prediction state
            return new ParseTree.NonToken(state.getRule().getLeft());
        else {
            Category prefixEnd = state.getRule().getRight()[state.getRuleDotPosition() - 1];
            if (prefixEnd instanceof Terminal) {
                // Scanned terminal state
                if (!(state instanceof ScannedTokenState))
                    throw new IssueRequest("Expected state to be a scanned state. This is a bug.");

                // let \'a = \, call
                ParseTree T = getViterbiParse(
                        chart.stateSets.get(
                                state.getPosition() - 1,
                                state.getRuleStartPosition(),
                                state.getRuleDotPosition() - 1,
                                state.getRule()
                        ),
                        chart
                );
                final ScannedTokenState scannedState = (ScannedTokenState) state;
                //noinspection unchecked
                T.addRightMost(new ParseTree.Token<>(scannedState));
                return T;
            } else {
                if (!(prefixEnd instanceof NonTerminal)) throw new IssueRequest("Something went terribly wrong.");

                // Completed non-terminal state
                State.ViterbiScore viterbi = chart.getViterbiScore(state); // must exist

                // Completed state that led to the current state
                State origin = viterbi.getOrigin();

                // Recurse for predecessor state (before the completion happened)
                ParseTree T = getViterbiParse(
                        chart.stateSets.get(
                                origin.ruleStartPosition,
                                state.getRuleStartPosition(),
                                state.getRuleDotPosition() - 1,
                                state.getRule()
                        )
                        , chart);
                // Recurse for completed state
                ParseTree Tprime = getViterbiParse(origin, chart);

                T.addRightMost(Tprime);
                return T;
            }
        }
    }

    public static <E> Chart<E> parse(NonTerminal S,
                                     Grammar<E> grammar,
                                     Iterable<Token<E>> tokens) {
        return parse(S, grammar, tokens, (ScanProbability) null);
    }

    public static <E> ParseTree getViterbiParse(
            NonTerminal S,
            Grammar<E> grammar,
            Iterable<Token<E>> tokens
    ) {
        return getViterbiParse(S, grammar, tokens, null);
    }

    public static <E> ParseTree getViterbiParse(
            NonTerminal S,
            Grammar<E> grammar,
            Iterable<Token<E>> tokens,
            @SuppressWarnings("SameParameterValue") ParseCallbacks callbacks
    ) {
        final ParseTreeWithScore viterbiParseWithScore = getViterbiParseWithScore(S, grammar, tokens, callbacks);
        if (viterbiParseWithScore == null) return null;
        return viterbiParseWithScore.getParseTree();
    }

    public static <E> ParseTreeWithScore getViterbiParseWithScore(
            NonTerminal S,
            Grammar<E> grammar,
            Iterable<Token<E>> tokens
    ) {
        return getViterbiParseWithScore(S, grammar, tokens, null);
    }


    public static <E> ParseTreeWithScore getViterbiParseWithScore(
            NonTerminal S,
            Grammar<E> grammar,
            Iterable<Token<E>> tokens,
            ParseCallbacks callbacks
    ) {
        ChartWithInputPosition<E> chart = parseAndCountTokens(S, grammar, tokens, callbacks);
        final StateSets<E> stateSets = chart.chart.stateSets;
        List<ParseTreeWithScore> parses = stateSets.getCompletedStates(chart.index, Category.START).stream()
                .map(state -> new ParseTreeWithScore(getViterbiParse(state, chart.chart), chart.chart.getViterbiScore(state), grammar.getSemiring()))
                .collect(Collectors.toList());
        if (parses.size() > 1) throw new Error("Found more than one Viterbi parses. This is a bug.");
        return parses.size() == 0 ? null : parses.get(0);
    }

    public static <E> Chart<E> parse(NonTerminal S,
                                     Grammar<E> grammar,
                                     Iterable<Token<E>> tokens,
                                     ScanProbability scanProbability) {
        return parseAndCountTokens(
                S,
                grammar,
                tokens,
                new ParseCallbacks.Builder().withScanProbability(scanProbability).build()
        ).chart;
    }

    public static <E> Chart<E> parse(NonTerminal S,
                                     Grammar<E> grammar,
                                     Iterable<Token<E>> tokens,
                                     ParseCallbacks callbacks) {
        return parseAndCountTokens(
                S,
                grammar,
                tokens,
                callbacks
        ).chart;
    }

    public static <T> ChartWithInputPosition<T> parseAndCountTokens(NonTerminal S,
                                                                    Grammar<T> grammar,
                                                                    Iterable<Token<T>> tokens,
                                                                    ParseCallbacks callbacks) {
        ScanProbability scanProbability = callbacks != null ? callbacks.scanProbability : null;
        if (callbacks == null) callbacks = new ParseCallbacks.Builder().build();

        Chart<T> chart = new Chart<>(grammar);
        DblSemiring sr = grammar.getSemiring();

        // Initial state
        State initialState = new State(Rule.create(sr, 1.0, Category.START, S), 0);
        chart.addState(0, initialState, sr.one(), sr.one());

        // Cycle through input
        int i = 0;
        for (TokenWithCategories<T> token : TokenWithCategories.from(tokens, grammar)) {
            callbacks.beforePredict(i, token, chart);
            Predict.predict(i, grammar, chart.stateSets);
            callbacks.onPredict(i, token, chart);

            callbacks.beforeScan(i, token, chart);
            Scan.scan(i, token, scanProbability, grammar, chart.stateSets);
            callbacks.onScan(i, token, chart);


            callbacks.beforeComplete(i, token, chart);
            Set<State> completedStates = new HashSet<>(chart.stateSets.getCompletedStates(i + 1));
            Complete.completeNoViterbi(i + 1, grammar, chart.stateSets);
            completedStates.forEach(s -> Complete.setViterbiScores(s, grammar.getSemiring(), chart.stateSets));
            callbacks.onComplete(i, token, chart);


            i++;
        }
        //Set<State> completed = chart.getCompletedStates(i, Category.START);
        //if (completed.size() > 1) throw new Error("This is a bug");
        return new ChartWithInputPosition<>(chart, i);
    }

}
