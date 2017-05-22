package org.leibnizcenter.cfg.earleyparser.chart;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.earleyparser.parsemode.PanicMode;
import org.leibnizcenter.cfg.earleyparser.parsemode.ParsingMode;
import org.leibnizcenter.cfg.earleyparser.scan.ScanMode;
import org.leibnizcenter.cfg.earleyparser.scan.TokenNotInLexiconException;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.leibnizcenter.cfg.earleyparser.parsemode.ParsingMode.NORMAL;
import static org.leibnizcenter.cfg.earleyparser.parsemode.ParsingMode.PANIC_MODE;
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
    /**
     * End position for chart (may be lower than tokenIndex necause some tokens may be ignored)
     */
    public int chartIndex = 0;
    /**
     * End position for token stream (may be higher than chartIndex necause some tokens may be ignored)
     */
    public int tokenIndex = 0;
    public ParsingMode parsingMode = ParsingMode.NORMAL;

    private TIntObjectHashMap<Token<T>> tokensPassed = new TIntObjectHashMap<>(50, 0.5F, -1);

    public ChartWithInputPosition(
            Grammar<T> grammar,
            NonTerminal goal,
            ParseOptions<T> parseOptions
    ) {
        this.grammar = grammar;
        strategy = parseOptions == null || parseOptions.scanMode == null ? ScanMode.STRICT : parseOptions.scanMode;
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
            final TokenNotInLexiconException notInLexiconException = new TokenNotInLexiconException(t, tokenIndex, chartIndex);
            incidents.add(notInLexiconException);
            // if (callbacks == null) throw notInLexiconException;
        }

        switch (strategy) {
            case SYNCHRONIZE:
                final boolean justEnteringPanicMode = parsingMode != PANIC_MODE && nullOrEmpty(categories);
                if (justEnteringPanicMode) {
                    parsingMode = PANIC_MODE;
                    PanicMode.proceedAllStatesThatWereActiveOnError(chart, chartIndex, tokensPassed);
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
        tokensPassed.putIfAbsent(chartIndex, t);

        if (PANIC_MODE.equals(parsingMode) && chart.getJustCompletedErrorRulesCount(chartIndex) > 0) {
            parsingMode = NORMAL;
        }
        tokenIndex++;
    }


}
