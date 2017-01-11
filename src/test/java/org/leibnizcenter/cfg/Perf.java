package org.leibnizcenter.cfg;

import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.earleyparser.ParseTreeWithScore;
import org.leibnizcenter.cfg.earleyparser.Parser;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.Tokens;

import java.util.List;

/**
 * Test performance todo linear / exponential regression
 *
 * Created by Maarten on 23-8-2016.
 */
public class Perf {
    public static void main(String[] args) {
        NonTerminal A = Category.nonTerminal("A");
        NonTerminal B = Category.nonTerminal("B");
        NonTerminal C = Category.nonTerminal("C");
        NonTerminal D = Category.nonTerminal("D");
        NonTerminal S = Category.nonTerminal("S");
        Category aa = new ExactStringTerminal("a");

        final LogSemiring sr = new LogSemiring();
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(1.0, S, A)
                .addRule(0.1, S, S, S)
                .addRule(1.0, A, B)
                .addRule(0.5, A, D)
                .addRule(0.5, B, C)
                .addRule(1.0, B, aa)
                .addRule(0.5, C, aa)
                .addRule(0.5, D, aa)
                .build();

        final Token<String> a = new Token<>("a");
        List<Token<String>> tokens = Tokens.tokenize();
        for (int i = 0; i <= 10000; i++) {
            long timeStart = System.currentTimeMillis();
            Parser.getViterbiParseWithScore(S, grammar, tokens);
            long timeEnd = System.currentTimeMillis();
            System.out.println(i + "\t" + (timeEnd - timeStart));
            tokens.add(a);
        }
    }
}

