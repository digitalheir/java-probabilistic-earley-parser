
package org.leibnizcenter.cfg.earleyparser.chart;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsenstiveStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.StringTerminal;
import org.leibnizcenter.cfg.earleyparser.Parser;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ParseTree;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;
import org.leibnizcenter.cfg.token.Tokens;

import java.util.List;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;


/**
 */
public class ChartTest {
    // NonTerminals are just wrappers around a string
    private static final NonTerminal S = Category.nonTerminal("S");
    private static final NonTerminal NP = Category.nonTerminal("NP");
    private static final NonTerminal VP = Category.nonTerminal("VP");
    private static final NonTerminal TV = Category.nonTerminal("TV");
    private static final NonTerminal Det = Category.nonTerminal("Det");
    private static final NonTerminal N = Category.nonTerminal("N");
    private static final NonTerminal Mod = Category.nonTerminal("Mod");

    // Token types are realized by implementing Terminal, and implementing hasCategory. This is a functional interface.
    private static final Terminal<String> transitiveVerb = (StringTerminal) token -> token.obj.matches("(hit|chased)");
    // Some utility terminal types are pre-defined:
    private static final Terminal<String> the = new CaseInsenstiveStringTerminal("the");
    private static final Terminal<String> a = new CaseInsenstiveStringTerminal("a");
    private static final Terminal<String> man = new ExactStringTerminal("man");
    @SuppressWarnings("unused")
    private static final Terminal<String> stick = new ExactStringTerminal("stick");
    private static final Terminal<String> with = new ExactStringTerminal("with");

    private static final Grammar<String> grammar = new Grammar.Builder<String>("test")
            .setSemiring(new LogSemiring()) // If not set, defaults to Log semiring which is probably what you want
            .addRule(
                    1.0,   // Probability between 0.0 and 1.0, defaults to 1.0. The builder takes care of converting it to the semiring element
                    S,     // Left hand side of the rule
                    NP, VP // Right hand side of the rule
            )
            .addRule(
                    0.5,
                    NP,
                    Det, N // eg. The man
            )
            .addRule(
                    0.5,
                    NP,
                    Det, N, Mod // eg. the man (with a stick)
            )
            .addRule(
                    0.4,
                    VP,
                    TV, NP, Mod // eg. (chased) (the man) (with a stick)
            )
            .addRule(
                    0.6,
                    VP,
                    TV, NP // eg. (chased) (the man with a stick)
            )
            .addRule(Det, the)
            .addRule(N, man)
            .addRule(TV, transitiveVerb)
            .addRule(Mod, with, NP) // eg. with a stick
            .build();

    @Test
    public final void readmeExample() {
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("The man     chased the man \n\t with the man")), 0.125, 0.000001);
        Assert.assertEquals(Parser.recognize(NP, grammar, Tokens.tokenize("the man with the man")), 0.25, 0.000001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the", "man", "chased", "the", "man")), 0.15, 0.000001);

        final List<Token<String>> tokens = Tokens.tokenize("The man     chased the man \n\t with the stick");
        ParseTree parseTree = Parser.getViterbiParse(S, grammar, tokens);
        System.out.println(parseTree);

    }

    @Test
    public final void ambiguous() {
        final NonTerminal BV = new NonTerminal("BV");
        final Category a = new ExactStringTerminal("a");
        final Category the = new ExactStringTerminal("the");
        final Category right = new ExactStringTerminal("right");
        final Category wrong = new ExactStringTerminal("wrong");
        final Category girl = new ExactStringTerminal("girl");
        final Category left = new ExactStringTerminal("left");
        final NonTerminal S = Category.nonTerminal("S");
        final NonTerminal NP = Category.nonTerminal("NP");
        final NonTerminal VP = Category.nonTerminal("VP");
        final NonTerminal Det = Category.nonTerminal("Det");
        final NonTerminal N = Category.nonTerminal("N");

        double PSVP = 0.9;
        double PSNP = 1 - PSVP;
        Grammar<String> grammar = new Grammar.Builder<String>("test")
                .setSemiring(new LogSemiring())
                .addRule(PSVP, S, NP, VP)
                .addRule(PSNP, S, NP)
                .addRule(NP, Det, N)
                .addRule(N, BV, N)
                .addRule(VP, left)
                .addRule(BV, left)
                .addRule(BV, wrong)
                .addRule(BV, right)
                .addRule(Det, a)
                .addRule(Det, the)
                .addRule(N, right)
                .addRule(N, left)
                .addRule(N, girl)
                .build();

        // Parsable
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the girl left")), PSVP, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the right left")), PSNP + PSVP, 0.0001); // ambiguous
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the wrong right")), PSNP, 0.0001); // ambiguous
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the right")), PSNP, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the girl")), PSNP, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the right right")), PSNP, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the left right")), PSNP, 0.0001);

        Assert.assertEquals(Parser.recognize(N, grammar, Tokens.tokenize("left girl")), 1.0, 0.0001);
        Assert.assertEquals(Parser.recognize(N, grammar, Tokens.tokenize("left left")), 1.0, 0.0001);
        Assert.assertEquals(Parser.recognize(N, grammar, Tokens.tokenize("wrong left")), 1.0, 0.0001);

        // Unparsable
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("girl left")), 0.0, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the")), 0.0, 0.0001);
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the notinlexicon left")), 0.0, 0.0001);

    }


    @Test
    public final void paper_example() {
        double p = 0.6;
        double q = 0.4;
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a)
                .addRule(q, S, B)
                .addRule(1, B, S)
                .build();
        Chart<String> chart = new Chart<>(grammar);
        DblSemiring sr = grammar.getSemiring();

        State initialState = new State(Rule.create(sr, Category.START, S), 0);
        chart.addState(0, initialState, sr.one(), sr.one());

        Predict.predict(0, chart.grammar, chart.stateSets);

        Assert.assertTrue(chart.getStates(0).contains(initialState));
        Assert.assertTrue(chart.getStates(0).contains(new State(Rule.create(sr, p, S, a), 0)));
        Assert.assertTrue(chart.getStates(0).contains(new State(Rule.create(sr, q, S, B), 0)));
        Assert.assertTrue(chart.getStates(0).contains(new State(Rule.create(sr, 1, B, S), 0)));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(new State(Rule.create(sr, 1, B, S), 0))), (q / p), 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(new State(Rule.create(sr, 1, B, S), 0))), 1, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(new State(Rule.create(sr, q, S, B), 0))), (q / p), 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(new State(Rule.create(sr, q, S, B), 0))), q, 0.01);

        System.out.println(chart.countStates());
//        for (State s : chart.getStates(0)) {
//            System.out.println((s) + "[" + chart.getForwardScore(s) + "]" + "[" + chart.getExpression(s) + "]");
//        }
//
//        for (int i = 0; i < 3; i++) {
//            chart.scan(i, new Token<>("a"));
//            if (i < 3) chart.completeNoViterbi(i + 1);
//        }

    }

    @Test
    public final void parse() {
        final LogSemiring semiring = new LogSemiring();
        final Rule ruleB = Rule.create(semiring, 0.5, B, C);
        final Rule ruleC = Rule.create(semiring, 0.5, C, D);
        final Rule ruleD = Rule.create(semiring, 0.5, D, E);
        final Rule ruleE = Rule.create(semiring, 0.5, E, e);
        final Rule rule1 = Rule.create(semiring, 1.0, A, B, C, D, E);
        final Rule ruleAa = Rule.create(semiring, 1.0, A, a);
        final Rule rule3 = Rule.create(semiring, 1.0, X, Y, Z);

        Grammar<String> grammar = new Grammar.Builder<String>("test")
                .setSemiring(semiring)
                .addRule(ruleB)
                .addRule(ruleC)
                .addRule(ruleD)
                .addRule(ruleE)
                .addRule(rule1)
                .addRule(ruleAa)
                .addRule(rule3)
                .build();
        DblSemiring sr = grammar.getSemiring();
        Chart<String> chart = new Chart<>(grammar);

        chart.addState(0, new State(Rule.create(sr, 1, Category.START, A), 0), sr.one(), sr.one());
        Predict.predict(0, grammar, chart.stateSets);
        Scan.scan(0, new TokenWithCategories<>(new Token<>("a"), a), (index, token) -> semiring.fromProbability(0.5), grammar, chart.stateSets);
        Complete.completeNoViterbi(1, grammar, chart.stateSets);

//        for (int i = 0; i < 2; i++) {
//            for (State s : chart.getStates(i)) {
//                final double probFw = semiring.toProbability(chart.getForwardScore(s));
//                final double probInn = semiring.toProbability(chart.getExpression(s));
//                System.out.println((s) + "[" + probFw + "]" + "[" + probInn + "]");
//            }
//        }

        // TODO assert some stuff
        //Assert.assertEquals(0.1, semiring.toProbability(chart.getForwardScore(s)), 0.2);
        //Assert.assertEquals(0.03125,chart.getForwardScore(s),0.01);
    }
//
//    //    @Test
////    public final void testFirstKey() {
////        Assert.assertEquals(0, chart.firstIndex());
////    }
////
////    @Test
////    public final void testLastKey() {
////        Assert.assertEquals(1, chart.lastIndex());
////    }
////
////
////    @Test
////    public final void testSubChart() {
////        Chart subChart = chart.subChart(0, 1);
////        Assert.assertTrue(subChart.contains(edge1));
////        Assert.assertTrue(subChart.contains(edge2));
////        Assert.assertFalse(subChart.contains(edge3));
////
////        try {
////            chart.subChart(1, 0);
////            Assert.fail("able to create subchart with bad indeces");
////        } catch (RuntimeException expected) {
////            // empty
////        }
////    }
////
////    @Test
////    public final void testIndexOf() {
////        Assert.assertEquals(0, chart.indexOf(edge1));
////        Assert.assertEquals(0, chart.indexOf(edge2));
////        Assert.assertEquals(1, chart.indexOf(edge3));
////    }
////
////    @Test
////    public final void testContains() {
////        Assert.assertTrue(chart.contains(edge1));
////        Assert.assertTrue(chart.contains(edge2));
////        Assert.assertTrue(chart.contains(edge2));
////        Assert.assertFalse(chart.contains(new State(rule3, 0, 4)));
////    }
////
//    @Test
//    public final void testGetIndeces() {
//        //TODO
////        Set<Integer> indeces = chart.indices();
////        Assert.assertTrue(indeces.contains(0));
////        Assert.assertTrue(indeces.contains(1));
////
////        SortedSet<Integer> expected = new TreeSet<Integer>();
////        for (int i : indeces) {
////            expected.add(i);
////        }
////
////        Assert.assertEquals(expected, indeces);
////
////        int current = null, last;
////        Iterator<Integer> it = indeces.iterator();
////        while (it.hasNext()) {
////            last = current;
////            current = it.next();
////            if (last != null) {
////                Assert.assertTrue(current > last);
////            }
////        }
//    }
//
//    @Test
//    public final void testContainsState() {
//        Assert.assertTrue(chart.containsStates(0));
//        Assert.assertTrue(chart.containsStates(1));
//        Assert.assertFalse(chart.containsStates(2));
//    }
//
////    /**
////     * Test method for {@link Chart#hashCode()}.
////     */
////    @Test
////    public final void testHashCode() {
////        Assert.assertEquals(37 * (1 + chart.stateSets.hashCode()), chart.hashCode());
////    }
////
////    /**
////     * Test method for {@link Chart#addState(int, State)}.
////     */
////    @Test
////    public final void testAddState() {
////        Assert.assertFalse("able to add edge multiple times",
////                chart.addState(0, edge1));
////    }
//
//    @Test
//    public final void testGetState() {
//        Collection<State> zeroStates = chart.getStates(0);
//        Assert.assertTrue(zeroStates.contains(edge1));
//        Assert.assertTrue(zeroStates.contains(edge2));
//    }
//
//    /**
//     * Test method for {@link Chart#equals(java.lang.Object)}.
//     */
//    @Test
//    public final void testEqualsObject() {
//        Chart c = new Chart(grammar);
//        c.addState(0, edge1);
//        c.addState(0, edge2);
//        c.addState(1, edge3);
//
//        Assert.assertEquals(c, chart);
//    }

}
