/*
 * $Id: ParserListener.java 1799 2010-01-30 05:40:25Z scott $ 
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep;

import edu.osu.ling.pep.earley.EarleyParser;
import edu.osu.ling.pep.grammar.Grammar;

import java.util.EventListener;


/**
 * Listens to an {@link EarleyParser Earley parser}, receiving notifications
 * as the parser adds edges to the chart or scans the input string. The parser
 * also notifies a listener whenever a parse completes or an error occurs 
 * in a parse.
 * <p>
 * A listener can be specified for an Earley parser when the parser is 
 * created.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1799 $
 * @see EarleyParser#EarleyParser(Grammar, ParserListener)
 */
public interface ParserListener extends EventListener {
	/**
	 * Lets the listener know that an option was set on an Earley parser.
	 * @param optionEvent The event containing the newly set option and its
	 * value.
	 */
	public void optionSet(ParserOptionEvent optionEvent);
	
	/**
	 * Notifies a listener that the parser was seeded with a start category.
	 * @param edgeEvent An event containing the added edge.
	 */
	public void parserSeeded(EdgeEvent edgeEvent);
	
	/**
	 * Signals to a listener that an edge was added to the chart based on a
	 * prediction from the {@link Grammar grammar}.
	 * @param edgeEvent An event containing the added edge.
	 */
	public void edgePredicted(EdgeEvent edgeEvent);
	
	/**
	 * Lets the listener know that a token was scanned from the input string.
	 * @param edgeEvent An event containing the edge created on the basis of
	 * scanning a token.
	 */
	public void edgeScanned(EdgeEvent edgeEvent);
	
	/**
	 * Notifies a listener that an edge was added to the chart based on
	 * completion.
	 * @param edgeEvent An event containing the added edge.
	 */
	public void edgeCompleted(EdgeEvent edgeEvent);
	
	/**
	 * Called when parsing completes.
	 * @param parseEvent An event containing the completed {@link Parse parse}.
	 */
	public void parseComplete(ParseEvent parseEvent);
	
	/**
	 * Notifies the listener of a message from the parser.
	 * @param parseEvent A parse event containing the parse in progress.
	 * @param message The message.
	 */
	public void parseMessage(ParseEvent parseEvent, String message);
	
	/**
	 * Notifies a listener that an error occurred during parsing.
	 * @param parseErrorEvent An error event containing the parse that was
	 * in progress and the error that occurred.
	 * @throws PepException If the listener does not wish to handle the
	 * exception, it can re-throw the cause of the parse error event.
	 */
	public void parseError(ParseErrorEvent parseErrorEvent) throws PepException;
}
