package org.leibnizcenter.cfg.perf;

import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.earleyparser.Parser;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.Tokens;

import java.util.ArrayList;
import java.util.List;

/**
 * Test performance todo linear / exponential regression
 * <p>
 * Created by Maarten on 23-8-2016.
 */
public class Perf {
    public static void main(String[] ignored) {
        List<long[]> l = run();
        //l.forEach(line -> System.out.println(line[0] + "\t" + line[1]));
    }

    public static List<long[]> run() {
        NonTerminal A = Category.nonTerminal("A");
        NonTerminal B = Category.nonTerminal("B");
        NonTerminal C = Category.nonTerminal("C");
        NonTerminal D = Category.nonTerminal("D");
        NonTerminal S = Category.nonTerminal("S");
        Category aa = new ExactStringTerminal("a");

        final LogSemiring sr = LogSemiring.get();
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

        List<long[]> l = new ArrayList<>();

        for (int i = 0; i <= 1; i++)
            tokens.add(a);

        final ParseOptions<String> parseOptions = new ParseOptions.Builder<String>()
//                .parallelizeScan()
//                .parallelizeComplete()
//                .parallelizePredict()
                .build();

        Parser<String> p = new Parser<>(grammar);
        for (int i = 0; i <= 200; i++) {
            long timeStart = System.currentTimeMillis();
            p.getViterbiParseWithScore(S, tokens, parseOptions);
            long timeEnd = System.currentTimeMillis();
            final long time = timeEnd - timeStart;
            l.add(new long[]{i, time});
            System.out.println(i + "\t" + (i == 0 ? 0 : time));
            tokens.add(a);
        }

        return l;
    }
}

