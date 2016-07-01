
package org.leibnizcenter.cfg.earleyparser;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.terminal.CaseInsenstivieStringTerminal;
import org.leibnizcenter.cfg.category.terminal.ExactStringTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.List;
import java.util.stream.Collectors;


/**
 */
@SuppressWarnings({"WeakerAccess", "SuspiciousNameCombination"})
public final class PepFixture {
    public static final Category S = Category.nonTerminal("S");
    public static final Category seed = S;
    public static final Category NP = Category.nonTerminal("NP");
    public static final Category VP = Category.nonTerminal("VP");
    public static final Category Det = Category.nonTerminal("Det");
    public static final Category N = Category.nonTerminal("N");

    public final static Category A = Category.nonTerminal("A");
    public final static Category B = Category.nonTerminal("B");
    public final static Category C = Category.nonTerminal("C");
    public final static Category D = Category.nonTerminal("D");
    public final static Category E = Category.nonTerminal("E");
    public final static Category X = Category.nonTerminal("X");
    public final static Category Y = Category.nonTerminal("Y");
    public final static Category Z = Category.nonTerminal("Z");

    public final static Category a = new ExactStringTerminal("a");
    public final static Category b = new ExactStringTerminal("b");
    public final static Category the = new ExactStringTerminal("the");
    public final static Category boy = new ExactStringTerminal("boy");
    public final static Category girl = new ExactStringTerminal("girl");
    public final static Category left = new ExactStringTerminal("left");

    public final static Category aA = new CaseInsenstivieStringTerminal("a");
    public final static Category bB = new CaseInsenstivieStringTerminal("b");
    public final static Category ThE = new CaseInsenstivieStringTerminal("the");
    public final static Category bOy = new CaseInsenstivieStringTerminal("boy");
    public final static Category GirL = new CaseInsenstivieStringTerminal("girl");
    public final static Category LefT = new CaseInsenstivieStringTerminal("left");

    public final static Rule rule1 = new Rule(A, B, C, D, E);
    public final static Rule rule2 = new Rule(A, a);
    public final static Rule rule3 = new Rule(X, Y, Z);
    public final static Rule rule4 = new Rule(A, X, a);
    public final static Rule rule5 = new Rule(X, a, Z);
    public final static Rule rule6 = new Rule(Z, b);
    public final static Rule rule7 = new Rule(X, a);
    public final static Rule rule8 = new Rule(X, b);
    public static final Grammar mixed = new Grammar.Builder("mixed")
            .addRule(rule4)
            .addRule(rule5)
            .addRule(rule6)
            .addRule(rule7)
            .addRule(rule8)
            .build();
    public static final State edge1 = new State(rule1, 2, 3, 1);
    public static final State edge2 = new State(rule3, 0);
    public static final State edge3 = new State(rule2, 1, 2, 1);
    public static final List<Token<String>> tokens = Lists.newArrayList("the", "boy", "left").stream()
            .map(Token::new)
            .collect(Collectors.toList());
    public static Grammar grammar = new Grammar.Builder("test")
            .addRule(new Rule(S, NP, VP))
            .addRule(new Rule(NP, Det, N))
            .addRule(new Rule(VP, left))
            .addRule(new Rule(Det, a))
            .addRule(new Rule(Det, the))
            .addRule(new Rule(N, boy))
            .addRule(new Rule(N, girl))
            .build();
    public static Grammar grammarCaseInsensitive = new Grammar.Builder("test")
            .addRule(new Rule(S, NP, VP))
            .addRule(new Rule(NP, Det, N))
            .addRule(new Rule(VP, LefT))
            .addRule(new Rule(Det, aA))
            .addRule(new Rule(Det, ThE))
            .addRule(new Rule(N, bOy))
            .addRule(new Rule(N, GirL))
            .build();

    @Test
    public void testFixture() {
    }
}
