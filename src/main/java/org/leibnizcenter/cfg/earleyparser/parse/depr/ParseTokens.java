//package org.leibnizcenter.cfg.earleyparser.parse;
//
//import org.leibnizcenter.cfg.Grammar;
//import org.leibnizcenter.cfg.category.Category;
//import org.leibnizcenter.cfg.earleyparser.chart.State;
//import org.leibnizcenter.cfg.earleyparser.event.StateEvent;
//import org.leibnizcenter.cfg.earleyparser.event.ParseEvent;
//import org.leibnizcenter.cfg.earleyparser.event.ParserEvent;
//import org.leibnizcenter.cfg.earleyparser.event.ParserListener;
//import org.leibnizcenter.cfg.earleyparser.exception.PepException;
//import org.leibnizcenter.cfg.rule.Rule;
//import org.leibnizcenter.cfg.token.Token;
//
//import java.util.Iterator;
//
///**
// * Created by maarten on 11-6-16.
// */
//public class ParseTokens<T> {
//    private final Iterator<Token<T>> tokenIterator;
//
//    private final Chart chart;
//    private final Parse<T> parse;
//    private final boolean predictPreterm;
//    //    private final ParserOptions options;
//    private final Grammar grammar;
//    private final ParserListener listener;
//
//    private int index;
//
//    public ParseTokens(
//            Grammar grammar,
//            Category seed,
//            Iterable<Token<T>> tokens,
//            ParserOptions options,
//            ParserListener listener
//    )
//            throws PepException {
//        if (grammar == null) throw new IllegalArgumentException("null grammar");
//        this.grammar = grammar;
//        /**
//         * Sets the listener that will receive notification of {@link
//         * ParserEvent event} during parsing.
//         * @param listener A listener, possibly <code>null</code>. If a
//         * <code>null</code> listener is specified, event notification is
//         * effectively turned off for this parser.
//         */
//        this.listener = listener;
//
//        if (seed == null) throw new PepException("invalid seed category: " + null);
//        else if (tokens == null || !tokens.iterator().hasNext())
//            throw new PepException("invalid seed category: null or empty tokens");
//
//        this.tokenIterator = tokens.iterator();
//        chart = new Chart(grammar);
//        index = 0;
//        parse = new Parse<>(seed, chart);
//
////        this.options = options;
//        // get and cache boolean values
//        boolean lPredictPreterm = options.getOption(ParserOption.PREDICT_FOR_PRETERMINALS);
//
//        if (!lPredictPreterm) {
//            // check for rules that don't work if not predicting preterms
//            for (Rule r : grammar.getAllRules()) {
//                if (r.isPreterminal() && r.right.length > 1) {
//                    lPredictPreterm = true;
//                    fireParseMessage(parse, "setting "
//                            + ParserOption.PREDICT_FOR_PRETERMINALS.name() + " to true;"
//                            + " grammar contains incompatible rule: " + r);
//                    break;
//                }
//            }
//        }
//        predictPreterm = lPredictPreterm;
//
//
//        State seedState = new State(Rule.startRule(seed), 0, index);
//        chart.addState(index, seedState); // seed parser
//        if (listener != null) listener.parserSeeded(new StateEvent(index, seedState));
//    }
//
//
//    private void fireStatePredicted(int index, State edge) {
//        if (listener != null) {
//            listener.edgePredicted(new StateEvent(index, edge));
//        }
//    }
//
//
//    void fireParseMessage(Parse parse, String message) {
//        if (listener != null) {
//            listener.parseMessage(new ParseEvent(parse), message);
//        }
//    }
//
//    public boolean hasNext() {
//        return tokenIterator.hasNext();
//    }
//
//    public Parse<T> parseAllRemainigTokens() throws PepException {
//        while (hasNext()) parseNextToken();
//        return parse;
//    }
//
//    public Parse<T> parseNextToken() throws PepException {
//        chart.predict(index, predictPreterm, listener); // make predictions at this index
//
//        Token<T> token = tokenIterator.next(); // get next token
//        parse.addToken(token); // add to tokens in parse
//
//        chart.scan(index, token, predictPreterm, listener); // scan and increment index
//        index++;
//        Chart.completeTruncated(chart, index, listener); // completeTruncated for next index
//
//        if (!tokenIterator.hasNext()) {
//            // finish filling chart by predicting for final index
//            chart.predict(index, predictPreterm, listener);
//            if (listener != null) listener.parseComplete(new ParseEvent(parse));
//        }
//
//        return parse;
//    }
//
//    public Parse<T> parse() throws PepException {
//        return parseAllRemainigTokens();
//    }
//}
