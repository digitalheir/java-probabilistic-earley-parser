package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.earleyparser.callbacks.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.ChartWithInputPosition;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedToken;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;

import java.util.Collection;


/**
 * Helper function for parsing
 * <p>
 * Created by Maarten on 31-7-2016.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Parser<T> {
    private final Grammar<T> grammar;


    public Parser(Grammar<T> grammar) {
        this.grammar = grammar;
    }

    /**
     * Parses the given list of tokens and returns the parse probability
     *
     * @param goal    Goal category, typically S for Sentence
     * @param grammar Grammar to apply to tokens
     * @param tokens  list of tokens to parse
     * @return Probability that given string of tokens mathces given non-terminal with given grammar
     */
    @Deprecated
    public static <T> double recognize(NonTerminal goal,
                                       Grammar<T> grammar,
                                       Iterable<Token<T>> tokens) {
        return new Parser<>(grammar).recognize(goal, tokens, null);
    }

    /**
     * Parses the given list of tokens and returns the parse probability
     *
     * @param goal    Goal category, typically S for Sentence
     * @param grammar Grammar to apply to tokens
     * @param tokens  list of tokens to parse
     * @return Probability that given string of tokens mathces given non-terminal with given grammar
     */
    @Deprecated
    public static <T> double recognize(NonTerminal goal,
                                       Grammar<T> grammar,
                                       Iterable<Token<T>> tokens,
                                       @SuppressWarnings("SameParameterValue") ParseOptions<T> callbacks) {
        return new Parser<>(grammar).recognize(goal, tokens, callbacks);
    }

    @Deprecated
    public static <T> Chart<T> parse(NonTerminal S,
                                     Grammar<T> grammar,
                                     Iterable<Token<T>> tokens) {
        return new Parser<>(grammar).parse(S, tokens, (ScanProbability<T>) null);
    }

    @Deprecated
    public static <T> ParseTree getViterbiParse(
            NonTerminal S,
            Grammar<T> grammar,
            Iterable<Token<T>> tokens
    ) {
        return new Parser<>(grammar).getViterbiParse(S, tokens, null);
    }

    @Deprecated
    public static <T> ParseTree getViterbiParse(
            NonTerminal S,
            Grammar<T> grammar,
            Iterable<Token<T>> tokens,
            @SuppressWarnings("SameParameterValue") ParseOptions<T> callbacks
    ) {
        final ParseTreeWithScore viterbiParseWithScore = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, callbacks);
        if (viterbiParseWithScore == null) return null;
        return viterbiParseWithScore.getParseTree();
    }

    @Deprecated
    public static <T> ParseTreeWithScore getViterbiParseWithScore(
            NonTerminal S,
            Grammar<T> grammar,
            Iterable<Token<T>> tokens
    ) {
        return new Parser<>(grammar).getViterbiParseWithScore(S, tokens, null);
    }

    @Deprecated
    public static <T> ParseTreeWithScore getViterbiParseWithScore(
            NonTerminal S,
            Grammar<T> grammar,
            Iterable<Token<T>> tokens,
            ParseOptions<T> callbacks
    ) {
        return new Parser<>(grammar).getViterbiParseWithScore(S, tokens, callbacks);
    }

    @Deprecated
    public static <T> Chart<T> parse(NonTerminal S,
                                     Grammar<T> grammar,
                                     Iterable<Token<T>> tokens,
                                     ScanProbability<T> scanProbability) {
        final ParseOptions<T> build = new ParseOptions.Builder<T>().withScanProbability(scanProbability).build();
        return new Parser<>(grammar).parseAndCountTokens(
                S,
                tokens,
                build
        ).chart;
    }

    @Deprecated
    public static <T> Chart<T> parse(NonTerminal S,
                                     Grammar<T> grammar,
                                     Iterable<Token<T>> tokens,
                                     ParseOptions<T> callbacks) {
        return new Parser<>(grammar).parseAndCountTokens(
                S,
                tokens,
                callbacks
        ).chart;
    }

    @Deprecated
    public static <T> ChartWithInputPosition<T> parseAndCountTokens(NonTerminal S,
                                                                    Grammar<T> grammar,
                                                                    Iterable<Token<T>> tokens,
                                                                    ParseOptions<T> callbacks) {
        return new Parser<>(grammar).parseAndCountTokens(S, tokens, callbacks);
    }

    /**
     * Performs the backward part of the forward-backward algorithm
     */
    public static ParseTree getViterbiParse(State state, Chart chart) {
        if (state.ruleDotPosition <= 0)
            // Prediction state
            return new ParseTree.NonLeaf(state.rule.left);
        else {
            Category prefixEnd = state.rule.getRight()[state.ruleDotPosition - 1];


            if (prefixEnd instanceof NonLexicalToken) {
                // Scanned terminal state
                ScannedToken scannedToken = chart.stateSets.getScannedToken(state);
                if (scannedToken.scannedCategory instanceof NonLexicalToken)
                    System.out.println(scannedToken.scannedToken);
                if ((scannedToken == null))
                    throw new IssueRequest("Expected state to be a scanned state. This is a bug.");
                // let \'a = \, call
                final int position = state.position;

                final int previousDotPosition = state.ruleDotPosition + state.ruleStartPosition == state.position ? state.ruleDotPosition - 1 : state.ruleDotPosition;
                State state1 = State.create(
                        state.position - 1,
                        state.ruleStartPosition,
                        previousDotPosition,
                        state.rule
                );
                ParseTree T = getViterbiParse(
                        state1,
                        chart
                );
                //noinspection unchecked
                T.addRightMost(new ParseTree.Leaf<>(scannedToken));
                return T;
            } else if (prefixEnd instanceof Terminal) {
                // Scanned terminal state
                ScannedToken scannedState = chart.stateSets.getScannedToken(state);
                if (scannedState.scannedCategory instanceof NonLexicalToken)
                    System.out.println(scannedState.scannedToken);
                if ((scannedState == null))
                    throw new IssueRequest("Expected state to be a scanned state. This is a bug.");
                // let \'a = \, call
                final int position = state.position;

                State preScanState = State.create(
                        state.position - 1,
                        state.ruleStartPosition,
                        state.ruleDotPosition - 1,
                        state.rule
                );
                ParseTree T = getViterbiParse(
                        preScanState,
                        chart
                );
                //noinspection unchecked
                T.addRightMost(new ParseTree.Leaf<>(scannedState));
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

    /**
     * Parses the given list of tokens and returns he parse probability
     *
     * @param goal   Goal category, typically S for Sentence
     * @param tokens list of tokens to parse
     * @return Probability that given string of tokens mathces given non-terminal with given grammar
     */
    public double recognize(NonTerminal goal, Iterable<Token<T>> tokens) {
        return recognize(goal, tokens, null);
    }

    /**
     * Parses the given list of tokens and returns he parse probability
     *
     * @param goal   Goal category, typically S for Sentence
     * @param tokens list of tokens to parse
     * @return Probability that given string of tokens mathces given non-terminal with given grammar
     */
    public double recognize(NonTerminal goal,
                            Iterable<Token<T>> tokens,
                            @SuppressWarnings("SameParameterValue") ParseOptions<T> callbacks) {
        final ChartWithInputPosition<T> parse = parseAndCountTokens(goal, tokens, callbacks);
        final Collection<State> completedStates = parse.chart.stateSets.completedStates.getCompletedStates(parse.chartIndex, Category.START);
        if (completedStates.size() > 0) {
            if (completedStates.size() > 1)
                throw new IssueRequest("Multiple final states found. This is likely an error.");
            return completedStates.stream().mapToDouble(finalState ->
                    grammar.semiring.toProbability(
                            parse.chart.getForwardScore(finalState)
                    )).sum();
        } else {
            return 0.0;
        }
    }

    public Chart<T> parse(NonTerminal S,
                          Iterable<Token<T>> tokens) {
        return parse(S, tokens, (ScanProbability<T>) null);
    }

    public ParseTree getViterbiParse(
            NonTerminal S,
            Iterable<Token<T>> tokens
    ) {
        return getViterbiParse(S, tokens, null);
    }

    public ParseTree getViterbiParse(
            NonTerminal S,
            Iterable<Token<T>> tokens,
            @SuppressWarnings("SameParameterValue") ParseOptions<T> callbacks
    ) {
        final ParseTreeWithScore viterbiParseWithScore = getViterbiParseWithScore(S, tokens, callbacks);
        if (viterbiParseWithScore == null) return null;
        return viterbiParseWithScore.getParseTree();
    }

    public ParseTreeWithScore getViterbiParseWithScore(
            NonTerminal S,
            Iterable<Token<T>> tokens
    ) {
        return getViterbiParseWithScore(S, tokens, null);
    }

    public ParseTreeWithScore getViterbiParseWithScore(
            NonTerminal S,
            Iterable<Token<T>> tokens,
            ParseOptions<T> callbacks
    ) {
        ChartWithInputPosition<T> chart = parseAndCountTokens(S, tokens, callbacks);
        final StateSets<T> stateSets = chart.chart.stateSets;
        final Collection<State> completedStates = stateSets.completedStates.getCompletedStates(chart.chartIndex, Category.START);

        if (completedStates.size() > 1)
            throw new Error("Found more than one Viterbi parse. This is a bug.");
        else if (completedStates.size() < 1)
            throw new RuntimeException("Could not parse sentence with goal " + S);
        else return completedStates.stream().findAny()
                    .map(state -> new ParseTreeWithScore(getViterbiParse(state, chart.chart), chart.chart.getViterbiScore(state), grammar.semiring))
                    .get();
    }

    public Chart<T> parse(NonTerminal S,
                          Iterable<Token<T>> tokens,
                          ScanProbability<T> scanProbability) {
        final ParseOptions<T> build = new ParseOptions.Builder<T>().withScanProbability(scanProbability).build();
        return parseAndCountTokens(
                S,
                tokens,
                build
        ).chart;
    }

    public Chart<T> parse(NonTerminal S,
                          Iterable<Token<T>> tokens,
                          ParseOptions<T> callbacks) {
        return parseAndCountTokens(
                S,
                tokens,
                callbacks
        ).chart;
    }

    public ChartWithInputPosition<T> parseAndCountTokens(NonTerminal S,
                                                         Iterable<Token<T>> tokens,
                                                         ParseOptions<T> parseOptions) {
        ChartWithInputPosition<T> completeChart = new ChartWithInputPosition<>(grammar, S, parseOptions);

        for (Token<T> t : tokens) {
            completeChart.next(t);
        }

        //Set<State> completed = chart.getCompletedStates(i, Category.START);
        //if (completed.size() > 1) throw new Error("This is a bug");

        return completeChart;
    }

}
