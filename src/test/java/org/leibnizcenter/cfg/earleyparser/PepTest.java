
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Test;
import org.leibnizcenter.cfg.earleyparser.exception.PepException;
import org.leibnizcenter.cfg.earleyparser.parse.ParserOptions;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;


/**
 */
public class PepTest {

    Pep pep = new Pep(new ParserOptions());


    @Test
    public final void testParse() throws PepException {
        pep.parseTokens(grammar, tokens, seed);
    }
//todo
//	@Test public final void testMain() {
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
//	@Test public final void testInvoke() {
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
