package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseCallbacks;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParserCallbacksBuilder;
import org.leibnizcenter.cfg.earleyparser.callbacks.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.ChartWithInputPosition;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedToken;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.Grammar;
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
                                       @SuppressWarnings("SameParameterValue") ParseCallbacks<E> callbacks) {
        final ChartWithInputPosition<E> parse = parseAndCountTokens(goal, grammar, tokens, callbacks);
        final Collection<State> completedStates = parse.chart.stateSets.completedStates.getCompletedStates(parse.index, Category.START);
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
     */
    public static ParseTree getViterbiParse(State state, Chart chart) {
        if (state.ruleDotPosition <= 0)
            // Prediction state
            return new ParseTree.NonToken(state.rule.getLeft());
        else {
            Category prefixEnd = state.rule.getRight()[state.ruleDotPosition - 1];
            if (prefixEnd instanceof Terminal) {
                // Scanned terminal state
                ScannedToken scannedState = chart.stateSets.getScannedToken(state);
                if ((scannedState == null))
                    throw new IssueRequest("Expected state to be a scanned state. This is a bug.");

                // let \'a = \, call
                State state1 = State.create(
                        state.position - 1,
                        state.ruleStartPosition,
                        state.ruleDotPosition - 1,
                        state.rule
                );
                ParseTree T = getViterbiParse(
                        state1,
                        chart
                );
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
                State predecessor = State.create(
                        origin.ruleStartPosition,
                        state.ruleStartPosition,
                        state.ruleDotPosition - 1,
                        state.rule
                );
                ParseTree T = getViterbiParse(
                        predecessor
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
        return parse(S, grammar, tokens, (ScanProbability<E>) null);
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
            @SuppressWarnings("SameParameterValue") ParseCallbacks<E> callbacks
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
            ParseCallbacks<E> callbacks
    ) {
        ChartWithInputPosition<E> chart = parseAndCountTokens(S, grammar, tokens, callbacks);
        final StateSets<E> stateSets = chart.chart.stateSets;
        List<ParseTreeWithScore> parses = stateSets.completedStates.getCompletedStates(chart.index, Category.START).stream()
                .map(state -> new ParseTreeWithScore(getViterbiParse(state, chart.chart), chart.chart.getViterbiScore(state), grammar.getSemiring()))
                .collect(Collectors.toList());
        if (parses.size() > 1) throw new Error("Found more than one Viterbi parses. This is a bug.");
        return parses.size() == 0 ? null : parses.get(0);
    }

    public static <E> Chart<E> parse(NonTerminal S,
                                     Grammar<E> grammar,
                                     Iterable<Token<E>> tokens,
                                     ScanProbability<E> scanProbability) {
        final ParseCallbacks<E> build = new ParserCallbacksBuilder<E>().withScanProbability(scanProbability).build();
        return parseAndCountTokens(
                S,
                grammar,
                tokens,
                build
        ).chart;
    }

    public static <E> Chart<E> parse(NonTerminal S,
                                     Grammar<E> grammar,
                                     Iterable<Token<E>> tokens,
                                     ParseCallbacks<E> callbacks) {
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
                                                                    ParseCallbacks<T> callbacks) {
        Chart<T> chart = new Chart<>(grammar);
        DblSemiring sr = grammar.getSemiring();

        // Initial state
        State initialState = new State(Rule.create(sr, 1.0, Category.START, S), 0);
        chart.addState(initialState, sr.one(), sr.one());

        // Cycle through input
        final int[] i = {0};

        TokenWithCategories.from(tokens, grammar).forEach((token) -> {
            predict(grammar, callbacks, chart, i[0], token);
            scan(grammar, callbacks, chart, i[0], token);
            complete(grammar, callbacks, chart, i[0], token);
            i[0]++;
        });
        //Set<State> completed = chart.getCompletedStates(i, Category.START);
        //if (completed.size() > 1) throw new Error("This is a bug");
        return new ChartWithInputPosition<>(chart, i[0]);
    }

    private static <T> void complete(Grammar<T> grammar, ParseCallbacks<T> callbacks, Chart<T> chart, int i, TokenWithCategories<T> token) {
        if (callbacks != null) callbacks.beforeComplete(i, token, chart);

        final Set<State> completedStates = new HashSet<>(chart.stateSets.completedStates.getCompletedStates(i + 1));
        Complete.completeNoViterbi(i + 1, grammar, chart.stateSets);
        completedStates.forEach(s -> Complete.computeViterbiScores(s, grammar.getSemiring(), chart.stateSets));

        if (callbacks != null) callbacks.onComplete(i, token, chart);
    }

    private static <T> void scan(Grammar<T> grammar, ParseCallbacks<T> callbacks, Chart<T> chart, int i, TokenWithCategories<T> token) {
        final ScanProbability<T> scanProbability = callbacks != null ? callbacks.scanProbability : null;
        if (callbacks != null) callbacks.beforeScan(i, token, chart);

        Scan.scan(
                i,
                token,
                scanProbability,
                grammar.getSemiring(),
                chart.stateSets
        );

        if (callbacks != null) callbacks.onScan(i, token, chart);
    }

    private static <T> void predict(Grammar<T> grammar, ParseCallbacks<T> callbacks, Chart<T> chart, int i, TokenWithCategories<T> token) {
        if (callbacks != null) callbacks.beforePredict(i, token, chart);

        Predict.predict(i, grammar, chart.stateSets);

        if (callbacks != null) callbacks.onPredict(i, token, chart);
    }

}
