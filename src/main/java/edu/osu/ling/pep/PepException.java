/*
 * $Id: PepException.java 268 2007-03-21 19:25:28Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep;


/**
 * An exception thrown in the process of running Pep.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 268 $
 */
public class PepException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a pep exception with the specified message.
	 */
	public PepException(String message) {
		super(message);
	}

	/**
	 * Creates a pep exception with the specified underlying cause.
	 */
	PepException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a pep exception with the specified message and cause.
	 */
	PepException(String message, Throwable cause) {
		super(message, cause);
	}

}
