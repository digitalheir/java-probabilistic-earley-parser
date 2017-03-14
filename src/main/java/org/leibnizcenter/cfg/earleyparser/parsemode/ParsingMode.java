package org.leibnizcenter.cfg.earleyparser.parsemode;

import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.scan.TokenNotInLexiconException;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.leibnizcenter.cfg.util.Collections2.nullOrEmpty;

public enum ParsingMode {
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
