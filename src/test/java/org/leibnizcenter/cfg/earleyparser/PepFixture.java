
package org.leibnizcenter.cfg.earleyparser;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
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
    public static final NonTerminal S = Category.nonTerminal("S");
    public static final NonTerminal seed = S;
    public static final NonTerminal NP = Category.nonTerminal("NP");
    public static final NonTerminal VP = Category.nonTerminal("VP");
    public static final NonTerminal Det = Category.nonTerminal("Det");
    public static final NonTerminal N = Category.nonTerminal("N");

    public final static NonTerminal A = Category.nonTerminal("A");
    public final static NonTerminal B = Category.nonTerminal("B");
    public final static NonTerminal C = Category.nonTerminal("C");
    public final static NonTerminal D = Category.nonTerminal("D");
    public final static NonTerminal E = Category.nonTerminal("E");
    public final static NonTerminal X = Category.nonTerminal("X");
    public final static NonTerminal Y = Category.nonTerminal("Y");
    public final static NonTerminal Z = Category.nonTerminal("Z");

    public final static Category a = new ExactStringTerminal("a");
    public final static Category b = new ExactStringTerminal("b");
    public final static Category c = new ExactStringTerminal("c");
    public final static Category d = new ExactStringTerminal("d");
    public final static Category e = new ExactStringTerminal("e");
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

    public final static Rule rule1 = Rule.create(1.0, A, B, C, D, E);
    public final static Rule rule2 = Rule.create(1.0, A, a);
    public final static Rule rule3 = Rule.create(1.0, X, Y, Z);
    public final static Rule rule4 = Rule.create(1.0, A, X, a);
    public final static Rule rule5 = Rule.create(1.0, X, a, Z);
    public final static Rule rule6 = Rule.create(1.0, Z, b);
    public final static Rule rule7 = Rule.create(1.0, X, a);
    public final static Rule rule8 = Rule.create(1.0, X, b);
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
            .addRule(S, NP, VP)
            .addRule(NP, Det, N)
            .addRule(VP, left)
            .addRule(Det, a)
            .addRule(Det, the)
            .addRule(N, boy)
            .addRule(N, girl)
            .build();
    public static Grammar grammarCaseInsensitive = new Grammar.Builder("test")
            .addRule(S, NP, VP)
            .addRule(NP, Det, N)
            .addRule(VP, LefT)
            .addRule(Det, aA)
            .addRule(Det, ThE)
            .addRule(N, bOy)
            .addRule(N, GirL)
            .build();

    @Test
    public void testFixture() {
    }
}
