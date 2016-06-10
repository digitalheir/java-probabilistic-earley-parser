/*
 * $Id: EarleyParser.java 1807 2010-02-05 22:20:02Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package org.leibnizcenter.earleyparser.earley;

import com.google.common.collect.Lists;
import org.leibnizcenter.earleyparser.*;
import org.leibnizcenter.earleyparser.grammar.*;
import org.leibnizcenter.earleyparser.grammar.categories.Category;
import org.leibnizcenter.util.HashSets;

import java.util.*;
import java.util.stream.Collectors;


/**
 * An Earley parser, named after the inventor of
 * <a href="http://en.wikipedia.org/wiki/Earley_parser#The_algorithm">the
 * algorithm it implements</a>.
 * <p>
 * Earley parsers are used to parse strings for conformance with a given
 * {@link Grammar context-free grammar}. Once instantiated with a grammar,
 * an instance of this class can be used to
 * {@link #parse(Iterable, Category) parse} (or just
 * {@link #recognize(Iterable, Category) recognize}) strings
 * (represented as {@link Iterable iterable} series of tokens).
 * <p>
 * This parser fills out a {@link Chart chart} based on the specified tokens
 * for a specified {@link Category seed category}. Because of this, it can
 * be used to recognize strings that represent any rule in the grammar. The
 * {@link #parse(Iterable, Category)} method returns a {@link Parse} object
 * that encapsulates the completed chart, the tokens given and the seed
 * category for that parse.
 * <p>
 * For example, if a grammar contains the following {@link Rule rules}:
 * <ul>
 * <li><code>S -> NP VP</code></li>
 * <li><code>NP -> Det N</code></li>
 * <li><code>Det -> the</code></li>
 * <li><code>N -> boy</code></li>
 * <li><code>VP -> left</code></li>
 * </ul>
 * parses can be requested for category <code>S</code>
 * (&quot;<code>the boy left</code>&quot;) but also for category <code>NP</code>
 * (&quot;<code>the boy</code>&quot;). For convenience, this class provides
 * the {@link #recognize(Iterable, Category)} method that just returns
 * the {@link Status status} for a given parse (but not its completed chart,
 * tokens, and seed category).
 * <p>
 * A parser instance can be configured using
 * {@link #setOption(ParserOption, Boolean)}. When no configuration is
 * done, a parser just uses the {@link ParserOption#getDefaultValue()
 * default values} of options. Note, however, that instances of this class are
 * not synchronized. If it is possible that a thread could be calling
 * both {@link #parse(Iterable, Category)} or
 * {@link #recognize(Iterable, Category)}
 * and another thread could be setting options, steps should be taken by the
 * developer to ensure that these do not happen concurrently.
 *
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1807 $
 * @see Grammar
 * @see Parse
 * @see ParserOption
 */
public class EarleyParser<T> {
    public Grammar grammar;
    public ParserListener listener;
    public Map<ParserOption, Boolean> options;

    private boolean predictPreterm;

    /**
     * Creates a new Earley parser for the specified grammar.
     *
     * @see #EarleyParser(Grammar, ParserListener)
     */
    public EarleyParser(Grammar grammar) {
        this(grammar, null);
    }

    /**
     * Creates a new Earley parser for the given set of production rules with
     * the specified listener.
     *
     * @param grammar  The grammar that this parser will consult for valid
     *                 production rules.
     * @param listener A listener that will be notified as edges are added
     *                 and tokens scanned by this Earley parser.
     */
    public EarleyParser(Grammar grammar, ParserListener listener) {
        if (grammar == null) throw new IllegalArgumentException("null grammar");
        this.grammar = grammar;
        /**
         * Sets the listener that will receive notification of {@link
         * ParserEvent events} during parsing.
         * @param listener A listener, possibly <code>null</code>. If a
         * <code>null</code> listener is specified, event notification is
         * effectively turned off for this parser.
         */
        this.listener = listener;

        options = new EnumMap<>(ParserOption.class);
    }

    /**
     * The grammar where this parser looks up its production rules.
     *
     * @return The grammar provided when this parser was created.
     */
    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Gets the listener currently receiving {@link ParserEvent
     * events} from this parser.
     *
     * @return <code>null</code> if no listener has been specified.
     * @since 0.2
     */
    public ParserListener getListener() {
        return listener;
    }

    /**
     * Tests whether this parser has a defined option identified by the
     * specified option name.
     *
     * @param optionName The option name to test for.
     * @return <code>true</code> iff the corresponding option has been
     * previously {@link #setOption(ParserOption, Boolean) set} on this
     * parser instance. Even if this returns <code>false</code>,
     * {@link #getOption(ParserOption)} can still be called as it will
     * just return the {@link ParserOption#getDefaultValue() default
     * value} of the specified option.
     */
    public boolean containsOption(ParserOption optionName) {
        return (options != null && options.containsKey(optionName));
    }

    /**
     * Gets the value of the option with the specified name.
     *
     * @param optionName The option to fetch a value for.
     * @return The defined value of the specified option, or its
     * {@link ParserOption#getDefaultValue() default value} if it has
     * not been set.
     */
    public Boolean getOption(ParserOption optionName) {
        if (options == null) {
            return optionName.defaultValue;
        }

        Boolean o = options.get(optionName);
        return (o == null) ? optionName.defaultValue : o;
    }

    /**
     * Sets an option on this parser instance with the specified name.
     *
     * @param optionName The option to set.
     * @param value      The new value for this option.
     * @return The former value for the specified option, or the
     * {@link ParserOption#getDefaultValue default value} that would have
     * been used..
     * @throws IllegalArgumentException If <code>value</code> or
     *                                  <code>optionName</code> is <code>null</code>.
     * @see EnumMap#put(Enum, Object)
     */
    public Boolean setOption(ParserOption optionName, Boolean value) {
        if (optionName == null) throw new IllegalArgumentException("null option name");
        if (value == null) throw new IllegalArgumentException("null value");


        Boolean oldValue = options.put(optionName, value);

        if (oldValue == null) oldValue = optionName.defaultValue;
        if (!value.equals(oldValue)) fireOptionSet(optionName, value);

        return oldValue;
    }


    /**
     * Tests whether this parser recognizes a given string (list of tokens)
     * for the specified seed category.
     *
     * @param tokens The tokens to parse.
     * @param seed   The seed category to attempt to recognize for the given
     *               <code>tokens</code>.
     * @return {@link Status#ACCEPT} if the string is recognized,
     * {@link Status#REJECT} if the string is rejected, and {@link Status#ERROR}
     * if an error occurred during parsing.
     * @see #parse(Iterable, Category)
     * @see Parse#getStatus()
     */
    public Status recognize(Iterable<Token<T>> tokens, Category seed)
            throws PepException {
        return parse(Lists.newArrayList(tokens), seed).getStatus();
    }

    public Parse<T> parse(List<T> ts, Category seed) throws PepException {
        return parse(
                ts.stream().map(Token::from).collect(Collectors.toList()),
                seed
        );
    }

    /**
     * Gets a parse for the specified string ({@link Iterable iterable} series
     * of tokens) and seed category.
     * <p>
     * While parsing is underway, this method will generate events to the
     * {@link ParserListener listener} specified for
     * {@link #EarleyParser(Grammar, ParserListener) this parser}, if any.
     * Specifically, events are generated whenever the parser is
     * {@link ParserListener#parserSeeded(EdgeEvent) seeded}, an
     * {@link Edge edge} is added to the {@link Chart chart} as a result of
     * {@link ParserListener#edgePredicted(EdgeEvent) prediction} or
     * {@link ParserListener#edgeCompleted(EdgeEvent) completion}, or
     * a token is {@link ParserListener#edgeScanned(EdgeEvent) scanned}
     * from the input string.
     * </p>
     *
     * @param tokens The tokens to parse.
     * @param seed   The seed category to attempt to find for the given
     *               <code>tokens</code>.
     * @return A parse for the specified <code>tokens</code> and
     * <code>seed</code>, containing a completed {@link Parse#getChart() chart}.
     * @throws PepException If no {@link ParserListener listener} has
     *                      been specified for this parser, or if this parser's listener decides
     *                      to re-throw exceptions it is notified about, then this method throws
     *                      a {@link PepException} in any of the following cases:
     *                      <ul>
     *                      <li><code>tokens</code> is <code>null</code> or empty</li>
     *                      <li><code>seed</code> is <code>null</code></li>
     *                      <li>An exception is thrown in the process of parsing, for example,
     *                      in case the parser is unable to parse one of the input tokens</li>
     *                      </ul>
     */
    public Parse<T> parse(Iterable<Token<T>> tokens, Category seed)
            throws PepException {
        Chart chart = new Chart();
        Integer index = 0;

        Parse parse = new Parse(seed, chart);

        if (seed == null) {
            fireParseError(parse, index, "invalid seed category: " + null);
        } else if (tokens == null || !tokens.iterator().hasNext()) {
            fireParseError(parse, index, "null or empty tokens");
        } else {
            // get and cache boolean values
            predictPreterm = getOption(ParserOption.PREDICT_FOR_PRETERMINALS);

            if (!predictPreterm) {
                // check for rules that don't work if not predicting preterms
                for (Rule r : grammar.getAllRules()) {
                    if (r.isPreterminal() && r.right.length > 1) {
                        predictPreterm = true;
                        fireParseMessage(parse, "setting "
                                + ParserOption.PREDICT_FOR_PRETERMINALS.name() + " to true;"
                                + " grammar contains incompatible rule: " + r);
                        break;
                    }
                }
            }

            Iterator<Token<T>> tokenIterator = tokens.iterator();

            Edge seedEdge = new Edge(DottedRule.startRule(seed), index);
            chart.addEdge(index, seedEdge); // seed parser
            fireParserSeeded(index, seedEdge); // notify listeners
            while (tokenIterator.hasNext()) {
                try {
                    predict(chart, index); // make predictions at this index

                    Token token = tokenIterator.next(); // get next token
                    parse.addToken(token); // add to tokens in parse

                    scan(chart, index, token); // scan and increment index
                    index++;
                    complete(chart, index); // complete for next index

                    if (!tokenIterator.hasNext())
                        // finish filling chart by predicting for final index
                        predict(chart, index);
                } catch (PepException pe) {
                    fireParseError(parse, index, pe); // may re-throw exception
                }
            }
        }

        fireParseComplete(parse); // notify listener
        return parse; // return completed parse
    }

    /**
     * Makes predictions in the specified chart at the given index.
     *
     * @param chart The chart to fill with predictions at <code>index</code>.
     * @param index The string index to make predictions at.
     */
    public void predict(Chart chart, int index) {
        if (chart.containsEdges(index)) { // any edges at this index?
            // avoid concurrently modifying chart by getting array
            Set<Edge> edges = chart.getEdges(index);
            for (Edge edge : edges.toArray(new Edge[edges.size()])) {
                predictForEdge(chart, edge, index); // predict for each edge
            }
        }
    }

    /**
     * Makes predictions (adds edges) in the specified chart for a given edge
     * at a given index. This method is recursively called whenever an edge is
     * added to also make predictions for the newly added edge.
     *
     * @param chart The chart to fill.
     * @param edge  The edge to make predictions for.
     * @param index The index in the string under consideration.
     */
    void predictForEdge(Chart chart, Edge edge, int index) {
        Category active = edge.dottedRule.activeCategory; // null if passive

        if (active != null && grammar.containsRules(active)) {
            // get all rules with the active category on the left
            for (Rule rule : grammar.getRules(active)) {
                if (!predictPreterm && rule.isPreterminal()) {
                    // only predict for rules that aren't preterminals to avoid
                    // filling up the chart with entries for every terminal
                    continue;
                }

                // make new edge at index with dotted rule at position 0
                Edge newEdge = Edge.predictFor(rule, index);
                // only predict for edges the chart did not already contain
                if (chart.addEdge(index, newEdge)) {
                    fireEdgePredicted(index, newEdge); // notify listener
                    // recursively predict for the new edge
                    predictForEdge(chart, newEdge, index);
                }
            }
        }
    }

    /**
     * Handles a token scanned from the input string, making completions (and
     * adding edges to the chart) as needed.
     *
     * @param chart The chart to fill.
     * @param index The start index of the scan.
     * @param token The token that was scanned.
     * @throws PepException If <code>token</code> is </code>null</code>.
     */
    public void scan(Chart chart, int index, Token token) throws PepException {
        if (token == null) throw new PepException("null token at index " + index);

        if (chart.containsEdges(index)) { // any predictions at this index?
            Set<Edge> edges = chart.getEdges(index);

            // just-in-time prediction
            if (!predictPreterm) { // using array avoids comodification problems
                for (Edge edge : edges.toArray(new Edge[edges.size()])) {
                    if (!edge.isPassive()) {
                        Rule r = grammar.getSingletonPreterminal(
                                edge.dottedRule.activeCategory,
                                token
                        );
                        if (r != null) {
                            Edge pt = Edge.predictFor(r, index);
                            if (chart.addEdge(index, pt)) {
                                fireEdgePredicted(index, pt);
                            }
                        }
                    }
                }
            }

            for (Edge edge : edges.toArray(new Edge[edges.size()])) {
                // completions for active edges only
                if (!edge.isPassive()) {
                    DottedRule dr = edge.dottedRule;

                    if (Tokens.hasCategory(token, dr.activeCategory)) {
                        Edge newEdge = Edge.scan(edge, token);
                        int successor // save next index
                                = index + 1;
                        if (chart.addEdge(successor, newEdge)) {
                            fireEdgeScanned(successor, newEdge);
                        }
                    }
                }
            }
        }
    }

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param chart The chart to fill.
     * @param index The index to make completions at.
     */
    public void complete(Chart chart, int index) {
        if (chart.containsEdges(index)) {
            Set<Edge> passiveEdgesToCompleteOn = chart.getPassiveEdges(index);

            while (passiveEdgesToCompleteOn != null && passiveEdgesToCompleteOn.size() > 0) {

                Set<Edge> newEdges = null;
                for (Edge edge : passiveEdgesToCompleteOn) {
                    int origin = edge.origin;
                    if (!edge.isPassive()) throw new Error("can only make completions based on passive edges");
                    for (Edge edgeToAdvance : chart.getEdgesWithActiveCategory(origin, edge.dottedRule.left)) {
                        // get all edges at this edge's origin

                        // add new edge with dot advanced by one if same
                        Edge newEdge = Edge.complete(edgeToAdvance, edge);
                        newEdges = HashSets.add(newEdges, newEdge);
                    }
                }

                passiveEdgesToCompleteOn = null;
                if (newEdges != null) {
                    passiveEdgesToCompleteOn = newEdges.stream()
                            // only notify and recursively complete if the chart did not already contain this edge
                            .filter(newEdge -> {
                                boolean added = chart.addEdge(index, newEdge);
                                if (added) fireEdgeCompleted(index, newEdge);

                                return added && newEdge.isPassive();
                            })
                            .collect(Collectors.toSet());
                }
            }
        }
    }

    /**
     * Makes completions in the specified chart based on the given edge at
     * the given index. This method is recursively called whenever a new
     * edge is added in order to make completions based on the newly-added
     * edge.
     *
     * @param chart The chart to fill.
     * @param edge  The edge to complete for.
     * @param index The index to make completions at.
     */
//    private void completeForEdge(Chart chart, Edge edge, int index) {
//
//    }

    /**
     * Gets a string representation of this Earley parser.
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + ": grammar "
                + grammar.name + "]";
    }

    private void fireOptionSet(ParserOption option, Boolean value) {
        if (listener != null) {
            listener.optionSet(
                    new ParserOptionEvent(this, option, value));
        }
    }

    private void fireParserSeeded(Integer index, Edge edge) {
        if (listener != null) {
            listener.parserSeeded(new EdgeEvent(this, index, edge));
        }
    }

    private void fireEdgePredicted(Integer index, Edge edge) {
        if (listener != null) {
            listener.edgePredicted(new EdgeEvent(this, index, edge));
        }
    }

    private void fireEdgeScanned(Integer index, Edge edge) {
        if (listener != null) {
            listener.edgeScanned(new EdgeEvent(this, index, edge));
        }
    }

    private void fireEdgeCompleted(Integer index, Edge edge) {
        if (listener != null) {
            listener.edgeCompleted(new EdgeEvent(this, index, edge));
        }
    }

    private void fireParseComplete(Parse parse) {
        if (listener != null) {
            listener.parseComplete(new ParseEvent(this, parse));
        }
    }

    private void fireParseMessage(Parse parse, String message) {
        if (listener != null) {
            listener.parseMessage(new ParseEvent(this, parse), message);
        }
    }

    private void fireParseError(Parse parse, Integer index, String message)
            throws PepException {
        fireParseError(parse, index, new PepException(message));
    }

    private void fireParseError(Parse parse, Integer index, PepException cause)
            throws PepException {
        parse.error = true;
        if (listener == null) {
            throw cause; // re-throw if no listener
        }

        listener.parseError(new ParseErrorEvent(this, index, parse, cause));
    }
}
