
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ProbabilitySemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class GrammarTest {
    private static final ProbabilitySemiring sr = ProbabilitySemiring.get();

    private final static NonTerminal A = Category.nonTerminal("A");
    private final static NonTerminal B = Category.nonTerminal("B");
    private final static NonTerminal C = Category.nonTerminal("C");
    private final static NonTerminal D = Category.nonTerminal("D");
    private final static NonTerminal E = Category.nonTerminal("E");
    private final static NonTerminal X = Category.nonTerminal("X");
    private final static NonTerminal Y = Category.nonTerminal("Y");
    private final static NonTerminal Z = Category.nonTerminal("Z");
    private final static Category e = new ExactStringTerminal("e");
    private final static Category a = new ExactStringTerminal("a");

    private final static Rule rule1 = Rule.create(sr, 1.0, A, B, C, D, E);
    private final static Rule rule2 = Rule.create(sr, 1.0, A, e);
    private final static Rule rule3 = Rule.create(sr, 1.0, X, Y, Z);
    private static final Rule ruleB = Rule.create(sr, 0.5, B, C);
    private static final Rule ruleC = Rule.create(sr, 0.5, C, D);
    private static final Rule ruleD = Rule.create(sr, 0.5, D, E);
    private static final Rule ruleDa = Rule.create(sr, 0.5, D, a);
    private static final Rule ruleEE = Rule.create(sr, 0.5, E, E, E);
    private static final Rule ruleE = Rule.create(sr, 0.5, E, e);
    private static final Rule ruleEC = Rule.create(sr, 0, E, C);
    private static final Grammar<String> g = new Grammar.Builder<String>("test")
            .setSemiring(sr)
            .addRule(ruleB)
            .addRule(ruleC)
            .addRule(ruleD)
            .addRule(ruleDa)
            .addRule(ruleE)
            .addRule(ruleEE)
            .addRule(ruleEC)
            .addRule(rule1)
            .addRule(rule2)
            .addRule(rule3)
            .build();


    @Test
    public final void testContainsRules() {
        Assert.assertTrue(g.containsRules(rule1.left));
        Assert.assertTrue(g.getRules(rule2.left).contains(rule2));
        Assert.assertFalse(g.getRules(rule3.left).contains(rule2));

        Assert.assertEquals(ruleB, Rule.create(sr, 0.5, B, C));
        Assert.assertEquals(ruleC, Rule.create(sr, 0.5, C, D));
        Assert.assertEquals(ruleD, ruleD);
        Assert.assertEquals(ruleE, ruleE);
        Assert.assertEquals(rule1, rule1);
        Assert.assertEquals(rule2, rule2);
        Assert.assertEquals(rule3, rule3);

        Assert.assertNotEquals(Rule.create(sr, 1.0, X, e), Rule.create(sr, 1.0, A, e));
        Assert.assertNotEquals(Rule.create(sr, 1.0, X, e), Rule.create(sr, 0.5, X, e));
        Assert.assertEquals(Rule.create(sr, 1.0, X, e), Rule.create(sr, 1.0, X, e));
    }

    @Test
    public final void testLeftRelation() {
        Assert.assertEquals(g.getLeftScore(A, B), 1.0, 0.01);
        Assert.assertEquals(g.getLeftScore(A, D), 0.0, 0.01);
        Assert.assertEquals(g.getLeftScore(A, X), 0.0, 0.01);
        Assert.assertEquals(g.getLeftScore(B, C), 0.5, 0.01);
    }

    @Test
    public final void testLeftStarRelation() {
        Assert.assertEquals(g.getLeftStarScore(A, B), 1.0, 0.01);
        Assert.assertEquals(g.getLeftStarScore(B, C), 0.5, 0.01);
        Assert.assertEquals(g.getLeftStarScore(B, D), 0.25, 0.01);
        Assert.assertEquals(g.getLeftStarScore(A, D), 0.25, 0.01);
        Assert.assertEquals(sr.toProbability(g.getLeftStarScore(A, X)), 0.0, 0.01);
    }

//    @Test public final void testGetPreterminal() {
//		Assert.assertEquals(rule2,
//				g.getPreterminals(rule2, rule2.right[0].name, true));
//		Assert.assertEquals(null,
//				g.getPreterminals(rule2, rule2.right[0].name.toUpperCase(),
//						false));
//}

    /**
     * Test method for {@link Grammar#getRules(Category)}.
     */
    @Test
    public final void testGetRules() {
        Set<Rule> setOfrules = new HashSet<>();
        setOfrules.add(rule1);
        setOfrules.add(rule2);
        Assert.assertEquals(setOfrules, new HashSet<>(g.getRules(rule1.left)));
        Assert.assertEquals(setOfrules, new HashSet<>(g.getRules(rule2.left)));

        setOfrules.clear();
        setOfrules.add(rule3);
        Assert.assertEquals(setOfrules, new HashSet<>(g.getRules(rule3.left)));
    }

    /**
     */
    @Test
    public final void parse() throws IOException {
        //todo
        Grammar<String> grammar = Grammar.fromString("S->NP#comment\n#comment\n\n#\n   #  com\n  \n\n VP -> eat <error> clar \n");
        System.out.println(grammar);


//        System.out.println(
//                Grammar.fromString(
//                        GrammarTest.class.getResourceAsStream("/test.cfg"), Charset.forName("UTF-8")
//                )
//        );
    }

}
