package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.Scan;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.earleyparser.callbacks.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.scan.ScanMode;
import org.leibnizcenter.cfg.earleyparser.scan.TokenNotInLexiconException;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;

import java.util.*;
import java.util.stream.IntStream;

import static org.leibnizcenter.cfg.earleyparser.chart.ChartWithInputPosition.ParsingMode.NORMAL;
import static org.leibnizcenter.cfg.earleyparser.chart.ChartWithInputPosition.ParsingMode.PANIC_MODE;
import static org.leibnizcenter.cfg.util.Collections2.nullOrEmpty;

/**
 */
public class ChartWithInputPosition<T> {
    public final Chart<T> chart;
    /**
     * Parsing errors encountered along the way
     */
    public final List<Throwable> incidents = new ArrayList<>();
    // These two could be different because we might drop tokens
    private final Grammar<T> grammar;
    final private ScanMode strategy;
    private final ParseOptions<T> parseOptions;
    /**
     * End position for chart (may be lower than tokenIndex necause some tokens may be ignored)
     */
    public int chartIndex = 0;
    /**
     * End position for token stream (may be higher than chartIndex necause some tokens may be ignored)
     */
    public int tokenIndex = 0;
    public ParsingMode parsingMode = ParsingMode.NORMAL;

    public ChartWithInputPosition(
            Grammar<T> grammar,
            NonTerminal goal,
            ParseOptions<T> parseOptions
    ) {
        this.grammar = grammar;
        strategy = parseOptions == null || parseOptions.scanMode == null ? ScanMode.STRICT : parseOptions.scanMode;
        this.parseOptions = parseOptions;
        chart = new Chart<>(grammar, parseOptions);

        // Initial state
        chart.addInitialState(goal);

//        /**
//         * @param tokens  Iterable of tokens
//         * @param grammar Grammar that contains {@link Terminal Terminals} that recognize tokens
//         * @return the same iterable, but with additional information: what categories the given token adhere to,
//         * as defined by {@link Terminal} types in the grammar.
//         */
    }


    public void next(final Token<T> t) {
        final Set<Terminal<T>> categories = grammar.getCategories(t);

//            final Set<Terminal<T>> categories = determineCategoriesForToken(grammar, callbacks, indexForTokenList, t);

        if (nullOrEmpty(categories)) {
            final TokenNotInLexiconException notInLexiconException = new TokenNotInLexiconException(t, tokenIndex);
            incidents.add(notInLexiconException);
            // if (callbacks == null) throw notInLexiconException;
        }

        switch (strategy) {
            case SYNCHRONIZE:
                final boolean justEnteringPanicMode = parsingMode != PANIC_MODE && nullOrEmpty(categories);
                if (justEnteringPanicMode) {
                    parsingMode = PANIC_MODE;
                    proceedAllStatesThatWereActiveOnError();
                } else {
                    parsingMode = PANIC_MODE.equals(parsingMode) ? PANIC_MODE : ParsingMode.NORMAL;
                }
                break;
            case STRICT:
                parsingMode = ParsingMode.STRICT;
                break;
            case DROP:
                parsingMode = ParsingMode.DROP;
                break;
            case WILDCARD:
                parsingMode = ParsingMode.WILDCARD;
                break;
            default:
                parsingMode = ParsingMode.NORMAL;
                break;
        }
        chartIndex = parsingMode.processToken(chart, chartIndex, t, categories);
        if (PANIC_MODE.equals(parsingMode) && chart.getJustCompletedErrorRulesCount(chartIndex) > 0) {
            parsingMode = NORMAL;
        }
        tokenIndex++;
    }

    private void proceedAllStatesThatWereActiveOnError() {
        chart.stateSets.activeStates.getActiveOnNonLexicalToken().stream()
                .filter(s -> s.comesBefore(chartIndex))
                .flatMap(preScanState -> IntStream.range(preScanState.position + 1, chartIndex + 1)
                        .mapToObj(position -> {
                            final ExpressionSemiring sr = grammar.semiring;

                            double previousForward = chart.getForwardScore(preScanState);
                            double previousInner = chart.getInnerScore(preScanState);

                            int numberOfScannedTokens = position - preScanState.position;
                            double ruleProv = preScanState.rule.probabilityAsSemiringElement;

                            final double newInner = sr.times(previousInner, sr.pow(ruleProv, numberOfScannedTokens - 1));
                            double newForward = sr.times(previousForward, newInner);

                            Token<T> token = chart.stateSets.getScannedToken(position);

                            // TODO test

                            double scanProbPow = determineScanProbabilityOfMultipleTokens(preScanState.position, position, sr, token);

                            return new Scan.Delta<>(
                                    token,
                                    preScanState,
                                    Scan.calculateForwardScore(scanProbPow, sr, newForward),
                                    Scan.calculateInnerScore(scanProbPow, sr, newInner),
                                    preScanState.rule, position, preScanState.ruleStartPosition, preScanState.advanceDot()
                            );
                        })
                )
                .forEach(chart.stateSets::createStateAndSetScores);
    }

    private double determineScanProbabilityOfMultipleTokens(int startPositionInclusive, int endPositionInclusive, ExpressionSemiring sr, Token<T> token) {
        final ScanProbability<T> scanProbability = parseOptions.scanProbability;
        double scanProbPow = Double.NaN;
        if (scanProbability != null) {
            //noinspection unchecked
            final double scanProbAtPosition = Scan.getScanProb(
                    scanProbability,
                    new TokenWithCategories<>(token, Collections.singleton(NonLexicalToken.INSTANCE)),
                    endPositionInclusive
            );
            //noinspection unchecked
            scanProbPow = IntStream.range(startPositionInclusive, endPositionInclusive)
                    .mapToDouble(p -> Scan.getScanProb(scanProbability,
                            new TokenWithCategories<>(token, Collections.singleton(NonLexicalToken.INSTANCE)),
                            p
                    ))
                    .reduce(scanProbAtPosition, sr::times);
        }
        return scanProbPow;
    }

    enum ParsingMode {
        NORMAL, PANIC_MODE, WILDCARD, DROP, STRICT;

        private static <T> int processTokenNormal(Chart<T> chart, int indexForChart, Token<T> t, Set<Terminal<T>> categories) {
            if (!nullOrEmpty(categories)) {
                TokenWithCategories<T> token = new TokenWithCategories<>(t, categories);
                chart.predict(indexForChart, token);
                chart.scan(indexForChart, token);
                chart.complete(indexForChart, token);
                indexForChart++;
            }
            return indexForChart;
        }

        private static <T> int processTokenStrict(Chart<T> chart, int indexForChart, Token<T> t, Set<Terminal<T>> categories) {
            if (nullOrEmpty(categories)) {
                throw new TokenNotInLexiconException(t, indexForChart);/*TODO fix indexForChart, should be indexForTokens*/
            }

            return processTokenNormal(chart, indexForChart, t, categories);
        }

        private static <T> int processTokenDrop(Chart<T> chart, int indexForChart, Token<T> t, Set<Terminal<T>> categories) {
            return nullOrEmpty(categories) ? indexForChart : processTokenNormal(chart, indexForChart, t, categories);
        }

        private static <T> int processTokenWildcard(Chart<T> chart, int indexForChart, Token<T> t, Set<Terminal<T>> categories) {
            return nullOrEmpty(categories)
                    ? processTokenNormal(chart, indexForChart, t, chart.grammar.terminals)
                    : processTokenNormal(chart, indexForChart, t, categories);
        }

        private static <T> int processTokenPanicMode(Chart<T> chart, final int indexForChart, Token<T> t, Set<Terminal<T>> categories) {
            //if (!Collections2.nullOrEmpty(categories)) {

            if (categories.stream().noneMatch((Terminal<T> cat) -> {
                Collection<State> activeStates = chart.stateSets.activeStates.getActiveOn(indexForChart, cat);
                return activeStates != null;// && activeStates.stream().anyMatch(state -> state.rule instanceof LexicalErrorRule);
            })) {
                // TODO If there are no <error> rules active on this terminal, advance <error>
                //noinspection unchecked
                categories = Collections.singleton(NonLexicalToken.INSTANCE);
            }


            TokenWithCategories<T> token = new TokenWithCategories<>(t, categories);
            // todo make more robust

            chart.predict(indexForChart, token);
            final Collection<State> justScannedErrors = chart.stateSets.activeStates.getJustScannedError(indexForChart);
            if (justScannedErrors != null && justScannedErrors.size() > 0) {
                if (!categories.contains(NonLexicalToken.INSTANCE)) {
                    //noinspection unchecked
                    categories.add(NonLexicalToken.INSTANCE);
                }
                chart.predictError(justScannedErrors);
            }
            chart.scan(indexForChart, token);
            //chart.scanError(indexForChart, token);
            chart.complete(indexForChart, token);
            //}
            return indexForChart + 1;
        }

        public <T> int processToken(Chart<T> chart, int indexForChart, Token<T> t, Set<Terminal<T>> categories) {
            switch (this) {
                case NORMAL:
                    return processTokenNormal(chart, indexForChart, t, categories);
                case PANIC_MODE:
                    return processTokenPanicMode(chart, indexForChart, t, categories);
                case WILDCARD:
                    return processTokenWildcard(chart, indexForChart, t, categories);
                case DROP:
                    return processTokenDrop(chart, indexForChart, t, categories);
                case STRICT:
                    return processTokenStrict(chart, indexForChart, t, categories);
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
