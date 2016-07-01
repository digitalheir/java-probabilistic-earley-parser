//package org.leibnizcenter.cfg.cyk;
//
//import com.google.common.collect.Lists;
//import org.junit.Assert;
//import org.junit.Test;
//import org.leibnizcenter.cfg.Grammar;
//import org.leibnizcenter.cfg.category.Category;
//import org.leibnizcenter.cfg.category.terminal.ExactStringTerminal;
//import org.leibnizcenter.cfg.category.terminal.Terminal;
//import org.leibnizcenter.cfg.rule.Rule;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// */
//public class CYKTest {
//
//    @Test
//    public void getBestParseTree() throws Exception {
//        Category goal = Category.nonTerminal("Sentence");
//        Category NP = Category.nonTerminal("NP");
//        Category VP = Category.nonTerminal("VP");
//        final ExactStringTerminal men = new ExactStringTerminal("men");
//        final ExactStringTerminal fish = new ExactStringTerminal("fish");
//
//        List<Terminal> words = Lists.newArrayList("fish men fish".split(" ")).stream()
//                .map(Terminal::new)
//                .collect(Collectors.toList());
//
//        Collection<Rule> rules = Lists.newArrayList(
//        );
//        Grammar grammar = new Grammar.Builder("test-grammar")
//                .addRule(1.0, goal, NP, VP)
//                .addRule(0.000000000000000001, NP, NP, NP)
//                .addRule(0.0000000000001, NP, fish)
//                .addRule(0.000000000000001, NP, men)
//                .addRule(0.00000000000000001, VP, fish)
//                .build();
//
//        System.out.println(grammar);
//
//        ScoreChart.ParseTreeContainer bestParseTree = CYK.getBestParseTree(words, grammar, goal);
//        Assert.assertNotNull(bestParseTree);
//        Assert.assertTrue(Math.exp(bestParseTree.getLogProbability()) > 0.0);
//        Assert.assertTrue(Math.exp(bestParseTree.getLogProbability()) < 1.0);
//    }
//
//}