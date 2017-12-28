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
    public static void main(final String[] ignored) {
        final List<long[]> l = run();
        //l.forEach(line -> System.out.println(line[0] + "\t" + line[1]));
    }

    public static List<long[]> run() {
        final NonTerminal A = Category.nonTerminal("A");
        final NonTerminal B = Category.nonTerminal("B");
        final NonTerminal C = Category.nonTerminal("C");
        final NonTerminal D = Category.nonTerminal("D");
        final NonTerminal S = Category.nonTerminal("S");
        final Category aa = new ExactStringTerminal("a");

        final LogSemiring sr = LogSemiring.get();
        final Grammar<String> grammar = new Grammar.Builder<String>()
                .withSemiring(sr)
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
        final List<Token<String>> tokens = Tokens.tokenize();

        final List<long[]> l = new ArrayList<>();

        for (int i = 0; i <= 1; i++)
            tokens.add(a);

        final ParseOptions<String> parseOptions = new ParseOptions.Builder<String>()
//                .parallelizeScan()
//                .parallelizeComplete()
//                .parallelizePredict()
                .build();

        final Parser<String> p = new Parser<>(grammar);
        for (int i = 0; i <= 200; i++) {
            final long timeStart = System.currentTimeMillis();
            p.getViterbiParseWithScore(S, tokens, parseOptions);
            final long timeEnd = System.currentTimeMillis();
            final long time = timeEnd - timeStart;
            //noinspection ObjectAllocationInLoop
            l.add(new long[]{i, time});
            System.out.println(i + "\t" + (i == 0 ? 0 : time));
            tokens.add(a);
        }

        return l;
    }
}

