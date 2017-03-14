
package org.leibnizcenter.cfg.earleyparser.chart;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.StringTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;

import static org.leibnizcenter.cfg.earleyparser.Fixture.*;


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
    private static final Terminal<String> the = new CaseInsensitiveStringTerminal("the");
    private static final Terminal<String> a = new CaseInsensitiveStringTerminal("a");
    private static final Terminal<String> man = new ExactStringTerminal("man");
    @SuppressWarnings("unused")
    private static final Terminal<String> stick = new ExactStringTerminal("stick");
    private static final Terminal<String> with = new ExactStringTerminal("with");



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
        DblSemiring sr = grammar.semiring;

        State initialState = new State(Rule.create(sr, Category.START, S), 0);
        chart.addState(initialState, sr.one(), sr.one());

        chart.predict(0);

        Assert.assertTrue(chart.getStates(0).contains(initialState));
        Assert.assertTrue(chart.getStates(0).contains(new State(Rule.create(sr, p, S, a), 0)));
        Assert.assertTrue(chart.getStates(0).contains(new State(Rule.create(sr, q, S, B), 0)));
        Assert.assertTrue(chart.getStates(0).contains(new State(Rule.create(sr, 1, B, S), 0)));
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(new State(Rule.create(sr, 1, B, S), 0))), (q / p), 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(new State(Rule.create(sr, 1, B, S), 0))), 1, 0.01);
        Assert.assertEquals(sr.toProbability(chart.getForwardScore(new State(Rule.create(sr, q, S, B), 0))), (q / p), 0.01);
        Assert.assertEquals(sr.toProbability(chart.getInnerScore(new State(Rule.create(sr, q, S, B), 0))), q, 0.01);

        System.out.println(chart.countStates());

    }

    @Test
    public final void parse() {
        final LogSemiring semiring = LogSemiring.get();
        final Rule ruleB = Rule.create(semiring, 0.5, B, C);
        final Rule ruleC = Rule.create(semiring, 0.5, C, D);
        final Rule ruleD = Rule.create(semiring, 0.5, D, E);
        final Rule ruleE = Rule.create(semiring, 0.5, E, (Category) new ExactStringTerminal("e"));
        final Rule rule1 = Rule.create(semiring, 1.0, A, B, C, D, E);
        final Rule ruleAa = Rule.create(semiring, 1.0, A, a);
        final Rule rule3 = Rule.create(semiring, 1.0, X, Y, Z);

        Grammar<String> grammar = new Grammar.Builder<String>("test")
                .withSemiring(semiring)
                .addRule(ruleB)
                .addRule(ruleC)
                .addRule(ruleD)
                .addRule(ruleE)
                .addRule(rule1)
                .addRule(ruleAa)
                .addRule(rule3)
                .build();
        DblSemiring sr = grammar.semiring;
        Chart<String> chart = new Chart<>(grammar);

        chart.addState(new State(Rule.create(sr, 1, Category.START, A), 0), sr.one(), sr.one());
//        Predict.predict(0, grammar, chart.stateSets);
//        Scan.scan(0, new TokenWithCategories<>(new Token<>("a"), a), (index, token) -> semiring.fromProbability(0.5), grammar.semiring, chart.stateSets);

//        Complete<String> complete=new Complete<>(chart.stateSets);
//        complete.completeNoViterbi(1, grammar);

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

}
