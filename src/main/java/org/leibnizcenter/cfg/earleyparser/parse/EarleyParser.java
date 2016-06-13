//
//package org.leibnizcenter.cfg.earleyparser.parse;
//
//import com.google.common.collect.Lists;
//import org.leibnizcenter.cfg.Grammar;
//import org.leibnizcenter.cfg.earleyparser.event.*;
//import org.leibnizcenter.cfg.earleyparser.exception.PepException;
//import org.leibnizcenter.cfg.category.Category;
//import org.leibnizcenter.cfg.rule.Rule;
//import org.leibnizcenter.cfg.token.Token;
//import org.leibnizcenter.cfg.token.Tokens;
//import org.leibnizcenter.cfg.util.HashSets;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//
///**
// * An Earley parser, named after the inventor of
// * <a href="http://en.wikipedia.org/wiki/Earley_parser#The_algorithm">the
// * algorithm it implements</a>.
// * <p>
// * Earley parsers are used to parse strings for conformance with a given
// * {@link Grammar context-free grammar}. Once instantiated with a grammar,
// * an instance of this class can be used to
// * {@link #parse(Iterable, Category) parse} (or just
// * {@link #recognize(Iterable, Category) recognize}) strings
// * (represented as {@link Iterable iterable} series of tokens).
// * <p>
// * This parser fills out a {@link Chart chart} based on the specified tokens
// * for a specified {@link Category seed category}. Because of this, it can
// * be used to recognize strings that represent any rule in the grammar. The
// * {@link #parse(Iterable, Category)} method returns a {@link Parse} object
// * that encapsulates the completed chart, the tokens given and the seed
// * category for that parse.
// * <p>
// * For example, if a grammar contains the following {@link Rule rules}:
// * <ul>
// * <li><code>S -> NP VP</code></li>
// * <li><code>NP -> Det N</code></li>
// * <li><code>Det -> the</code></li>
// * <li><code>N -> boy</code></li>
// * <li><code>VP -> left</code></li>
// * </ul>
// * parses can be requested for category <code>S</code>
// * (&quot;<code>the boy left</code>&quot;) but also for category <code>NP</code>
// * (&quot;<code>the boy</code>&quot;). For convenience, this class provides
// * the {@link #recognize(Iterable, Category)} method that just returns
// * the {@link Status status} for a given parse (but not its completed chart,
// * tokens, and seed category).
// * <p>
// * A parser instance can be configured using
// * {@link #setOption(ParserOption, Boolean)}. When no configuration is
// * done, a parser just uses the {@link ParserOption#getDefaultValue()
// * default values} of options. Note, however, that instances of this class are
// * not synchronized. If it is possible that a thread could be calling
// * both {@link #parse(Iterable, Category)} or
// * {@link #recognize(Iterable, Category)}
// * and another thread could be setting options, steps should be taken by the
// * developer to ensure that these do not happen concurrently.
// *
// // // * @see Grammar
// * @see Parse
// * @see ParserOption
// */
//public class EarleyParser<T> {
//    public Grammar grammar;
//    public ParserListener listener;
//    public Map<ParserOption, Boolean> options;
//
//    private boolean predictPreterm;
//
//    /**
//     * Creates a new Earley parser for the specified grammar.
//     *
//     * @see #EarleyParser(Grammar, ParserListener)
//     */
//    public EarleyParser(Grammar grammar) {
//        this(grammar, null);
//    }
//
//    /**
//     * Creates a new Earley parser for the given set of production rules with
//     * the specified listener.
//     *
//     * @param grammar  The grammar that this parser will consult for valid
//     *                 production rules.
//     * @param listener A listener that will be notified as edges are added
//     *                 and tokens scanned by this Earley parser.
//     */
//    public EarleyParser(Grammar grammar, ParserListener listener) {
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
//        options = new EnumMap<>(ParserOption.class);
//    }
//
//    /**
//     * The grammar where this parser looks up its production rules.
//     *
//     * @return The grammar provided when this parser was created.
//     */
//    public Grammar getGrammar() {
//        return grammar;
//    }
//
//    /**
//     * Gets the listener currently receiving {@link ParserEvent
//     * event} from this parser.
//     *
//     * @return <code>null</code> if no listener has been specified.
//     //     */
//    public ParserListener getListener() {
//        return listener;
//    }
//
//    /**
//     * Tests whether this parser has a defined option identified by the
//     * specified option name.
//     *
//     * @param optionName The option name to test for.
//     * @return <code>true</code> iff the corresponding option has been
//     * previously {@link #setOption(ParserOption, Boolean) set} on this
//     * parser instance. Even if this returns <code>false</code>,
//     * {@link #getOption(ParserOption)} can still be called as it will
//     * just return the {@link ParserOption#getDefaultValue() default
//     * value} of the specified option.
//     */
//    public boolean containsOption(ParserOption optionName) {
//        return (options != null && options.containsKey(optionName));
//    }
//
//
//
//    /**
//     * Sets an option on this parser instance with the specified name.
//     *
//     * @param optionName The option to set.
//     * @param value      The new value for this option.
//     * @return The former value for the specified option, or the
//     * {@link ParserOption#getDefaultValue default value} that would have
//     * been used..
//     * @throws IllegalArgumentException If <code>value</code> or
//     *                                  <code>optionName</code> is <code>null</code>.
//     * @see EnumMap#put(Enum, Object)
//     */
//    public Boolean setOption(ParserOption optionName, Boolean value) {
//        if (optionName == null) throw new IllegalArgumentException("null option name");
//        if (value == null) throw new IllegalArgumentException("null value");
//
//
//        Boolean oldValue = options.put(optionName, value);
//
//        if (oldValue == null) oldValue = optionName.defaultValue;
//        if (!value.equals(oldValue)) fireOptionSet(optionName, value);
//
//        return oldValue;
//    }
//
//
//    /**
//     * Tests whether this parser recognizes a given string (list of tokens)
//     * for the specified seed category.
//     *
//     * @param tokens The tokens to parse.
//     * @param seed   The seed category to attempt to recognize for the given
//     *               <code>tokens</code>.
//     * @return {@link Status#ACCEPT} if the string is recognized,
//     * {@link Status#REJECT} if the string is rejected, and {@link Status#ERROR}
//     * if an error occurred during parsing.
//     * @see #parse(Iterable, Category)
//     * @see Parse#getStatus()
//     */
//    public Status recognize(Iterable<Token<T>> tokens, Category seed)
//            throws PepException {
//        return parse(Lists.newArrayList(tokens), seed).getStatus();
//    }
//
//    public Parse<T> parse(List<T> ts, Category seed) throws PepException {
//        return parse(
//                ts.stream().map(Token::from).collect(Collectors.toList()),
//                seed
//        );
//    }
//
//    /**
//     * Gets a parse for the specified string ({@link Iterable iterable} series
//     * of tokens) and seed category.
//     * <p>
//     * While parsing is underway, this method will generate event to the
//     * {@link ParserListener listener} specified for
//     * {@link #EarleyParser(Grammar, ParserListener) this parser}, if any.
//     * Specifically, event are generated whenever the parser is
//     * {@link ParserListener#parserSeeded(EdgeEvent) seeded}, an
//     * {@link Edge edge} is added to the {@link Chart chart} as a result of
//     * {@link ParserListener#edgePredicted(EdgeEvent) prediction} or
//     * {@link ParserListener#edgeCompleted(EdgeEvent) completion}, or
//     * a token is {@link ParserListener#edgeScanned(EdgeEvent) scanned}
//     * from the input string.
//     * </p>
//     *
//     * @param tokens The tokens to parse.
//     * @param seed   The seed category to attempt to find for the given
//     *               <code>tokens</code>.
//     * @return A parse for the specified <code>tokens</code> and
//     * <code>seed</code>, containing a completed {@link Parse#getChart() chart}.
//     * @throws PepException If no {@link ParserListener listener} has
//     *                      been specified for this parser, or if this parser's listener decides
//     *                      to re-throw exceptions it is notified about, then this method throws
//     *                      a {@link PepException} in any of the following cases:
//     *                      <ul>
//     *                      <li><code>tokens</code> is <code>null</code> or empty</li>
//     *                      <li><code>seed</code> is <code>null</code></li>
//     *                      <li>An exception is thrown in the process of parsing, for example,
//     *                      in case the parser is unable to parse one of the input tokens</li>
//     *                      </ul>
//     */
//    public Parse<T> parse(Iterable<Token<T>> tokens, Category seed)
//            throws PepException {
//        Chart chart = new Chart();
//        int index = 0;
//
//        Parse parse = new Parse(seed, chart);
//
//        if (seed == null) {
//            fireParseError(parse, index, "invalid seed category: " + null);
//        } else if (tokens == null || !tokens.iterator().hasNext()) {
//            fireParseError(parse, index, "null or empty tokens");
//        } else {
//            // get and cache boolean values
//            predictPreterm = getOption(ParserOption.PREDICT_FOR_PRETERMINALS);
//
//            if (!predictPreterm) {
//                // check for rules that don't work if not predicting preterms
//                for (Rule r : grammar.getAllRules()) {
//                    if (r.isPreterminal() && r.right.length > 1) {
//                        predictPreterm = true;
//                        fireParseMessage(parse, "setting "
//                                + ParserOption.PREDICT_FOR_PRETERMINALS.name() + " to true;"
//                                + " grammar contains incompatible rule: " + r);
//                        break;
//                    }
//                }
//            }
//
//            Iterator<Token<T>> tokenIterator = tokens.iterator();
//
//            Edge seedEdge = new Edge(Rule.startRule(seed), 0, index);
//            chart.addEdge(index, seedEdge); // seed parser
//            fireParserSeeded(index, seedEdge); // notify listeners
//            while (tokenIterator.hasNext()) {
//                try {
//                    parseStep(chart, index, parse, tokenIterator);
//                    index ++;
//                } catch (PepException pe) {
//                    fireParseError(parse, index, pe); // may re-throw exception
//                }
//            }
//        }
//
//        fireParseComplete(parse); // notify listener
//        return parse; // return completed parse
//    }
//
//    private void parseStep(Chart chart, int index, Parse<T> parse, Iterator<Token<T>> tokenIterator) throws PepException {
//        predict(chart, index); // make predictions at this index
//
//        Token<T> token = tokenIterator.next(); // get next token
//        parse.addToken(token); // add to tokens in parse
//
//        scan(chart, index, token); // scan and increment index
//        index++;
//        complete(chart, index); // complete for next index
//
//        if (!tokenIterator.hasNext())
//            // finish filling chart by predicting for final index
//            predict(chart, index);
//    }
//
//
//
//    /**
//     * Makes completions in the specified chart based on the given edge at
//     * the given index. This method is recursively called whenever a new
//     * edge is added in order to make completions based on the newly-added
//     * edge.
//     *
//     * @param chart The chart to fill.
//     * @param edge  The edge to complete for.
//     * @param index The index to make completions at.
//     */
////    private void completeForEdge(Chart chart, Edge edge, int index) {
////
////    }
//
//    /**
//     * Gets a string representation of this Earley parser.
//     */
//    @Override
//    public String toString() {
//        return "[" + getClass().getSimpleName() + ": grammar "
//                + grammar.name + "]";
//    }
//
//    private void fireOptionSet(ParserOption option, Boolean value) {
//        if (listener != null) {
//            listener.optionSet(
//                    new ParserOptionEvent(this, option, value));
//        }
//    }
//}
