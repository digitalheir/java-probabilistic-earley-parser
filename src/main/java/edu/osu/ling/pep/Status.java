/*
 * $Id: Status.java 305 2007-04-11 04:57:32Z scott $ 
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
import edu.osu.ling.pep.grammar.categories.Category;
import edu.osu.ling.pep.grammar.Grammar;

/**
 * Reflects the {@link Parse#getStatus() status} of a {@link Parse parse}
 * completed by an {@link EarleyParser Earley parser}.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 305 $
 * @see Parse
 * @see EarleyParser
 */
public enum Status {
	/**
	 * Signals that a string is rejected after parsing.
	 */
	REJECT,
	
	/**
	 * Means that a string is a valid string of a given {@link Grammar grammar},
	 * as determined by
	 * {@link EarleyParser#parse(Iterable, Category) parsing}.
	 */
	ACCEPT,
	
	/**
	 * Used for {@link Parse parses} where an error occurs during processing.
	 */
	ERROR;
}
