/*
 * $Id: PepTest.java 1812 2010-02-08 22:06:32Z scott $
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep;

import java.io.IOException;
import java.util.EnumMap;

import edu.osu.ling.pep.earley.ParserOption;
import junit.framework.Assert;


/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1812 $
 */
public class PepTest extends PepFixture {

	Pep pep;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		pep = new Pep(new EnumMap<>(
				ParserOption.class));
	}

	public final void testParse() throws PepException {
		pep.parse(grammar, tokens, seed);
	}

//	public final void testMain() {
//		Pep.main(new String[] {"-g", "./samples/tiny.xml", "-s", "S",
//			"\"Mary", "saw", "her", "dragon", "in", "the", "cave\"", "-v"});
//		Pep.main(new String[] {"-g", "./samples/miniscule.xml", "-s", "S",
//				"\"the", "", "left\""});
//		System.err.print("expecting ERROR: ");
//		try {
//			Pep.main(new String[] {"-g", "./samples/miniscule.xml", "-s", "S",
//				"\"the", null, "left\""});
//		}catch(NullPointerException expected) {
//			System.err.println(expected.getMessage());
//		}
//	}
//
//	public final void testInvoke() {
//		try {
//			Runtime.getRuntime().exec(
//				"./bin/pep -g ./samples/miniscule.xml -s S \"the boy left\"",
//				new String[] {"PEP_HOME=" + System.getProperty("user.dir")});
//		}
//		catch(IOException io) {
//			Assert.fail(io.getMessage());
//		}
//	}
}
