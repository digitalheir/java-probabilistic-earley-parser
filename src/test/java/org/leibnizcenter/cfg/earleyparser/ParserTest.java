package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.StringTerminal;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.earleyparser.scan.ScanMode;
import org.leibnizcenter.cfg.earleyparser.scan.TokenNotInLexiconException;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.Tokens;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 */
public class ParserTest {
    // NonTerminals are just wrappers around a string
    private static final NonTerminal S = Category.nonTerminal("S");
    private static final NonTerminal NP = Category.nonTerminal("NP");
    private static final NonTerminal VP = Category.nonTerminal("VP");
    private static final NonTerminal TV = Category.nonTerminal("TV");
    private static final NonTerminal Det = Category.nonTerminal("Det");
    private static final NonTerminal N = Category.nonTerminal("N");
    private static final NonTerminal Mod = Category.nonTerminal("Mod");

    // Token types are realized by implementing Terminal, and implementing hasCategory. This is a functional interface.
    private static final Pattern HIT_CHASED = Pattern.compile("(hit|chased)");
    private static final Terminal<String> transitiveVerb = (StringTerminal) token -> HIT_CHASED.matcher(token.obj).matches();
    // Some utility terminal types are pre-defined:
    private static final Terminal<String> the = new CaseInsensitiveStringTerminal("the");
    private static final Terminal<String> a = new CaseInsensitiveStringTerminal("a");
    private static final Terminal<String> c = new CaseInsensitiveStringTerminal("c");
    private static final Terminal<String> man = new ExactStringTerminal("man");
    private static final Terminal<String> stick = new ExactStringTerminal("stick");
    private static final Terminal<String> with = new ExactStringTerminal("with");
    private static final Terminal<String> period = new ExactStringTerminal(".");


    private final static NonTerminal A = Category.nonTerminal("A");
    private final static NonTerminal B = Category.nonTerminal("B");
    private final static NonTerminal C = Category.nonTerminal("C");
    private final static NonTerminal D = Category.nonTerminal("D");

    private final static Category b = new ExactStringTerminal("b");

    @Test
    public void simpleGrammar2() throws Exception {
        double p = (0.6);
        double q = (0.4);
        // b surrounded by a's, or a single a
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a)
                .addRule(q, S, S, b, S)
                .build();
        List<Token<String>> tokens1 = Tokens.tokenize("a");
        ParseTreeWithScore parse1 = new Parser<>(grammar).getViterbiParseWithScore(S, tokens1);
        assertEquals(0.6, parse1.getProbability(), 0.0000000001);

        List<Token<String>> tokens2 = Tokens.tokenize("a  b a b a");
        ParseTreeWithScore parse2 = new Parser<>(grammar).getViterbiParseWithScore(S, tokens2);

        Assert.assertEquals(parse2.getProbability(), 0.4 * 0.4 * 0.6 * 0.6 * 0.6, 0.00001);
    }


    @Test
    public void simpleGrammar3() throws Exception {
        double p = (0.6);
        double q = (0.4);
        final LogSemiring sr = LogSemiring.get();
        // a surrounded by b's, or a single a
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, b, S, b)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("b b a b b");
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens);

        Assert.assertEquals(parse.getProbability(), q * q * p, 0.00001);
    }

    @Test
    public void viterbi() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(1.0, S, A)
                .addRule(0.5, S, S, S)
                .addRule(0.5, A, a)
                .build();

        List<Token<String>> tokens = Tokens.tokenize("a", "a", "a");
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens);

        Assert.assertNotNull(parse);
        assertEquals(parse.getProbability(), Math.pow(0.5, 5), 0.0001);
    }

    @Test
    public void viterbi2() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(1.0, S, A)
                .addRule(0.1, S, S, S)
                .addRule(1.0, A, B)
                .addRule(0.5, A, D)
                .addRule(0.5, B, C)
                .addRule(1.0, B, a)
                .addRule(0.5, C, D)
                .addRule(0.5, C, a)
                .addRule(0.5, D, a)
                .build();

        List<Token<String>> tokens = Tokens.tokenize("a", "a", "a");
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens);

        Assert.assertEquals(parse.getProbability(), 0.01, 0.0001);
    }


    @Test
    public void viterbi3() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        Grammar<String> grammar = new Grammar.Builder<String>()
                .setSemiring(sr)
                .addRule(1.0, S, A, A)
                .addRule(1.0, A, B)
                .addRule(0.1, A, D)
                .addRule(1.0, B, C)
                .addRule(0.9, D, a, a)
                .addRule(0.9, C, D)
                .addRule(0.9, B, a)
                .addRule(0.5, C, a, a)
                .build();

        List<Token<String>> tokens = Tokens.tokenize("a", "a", "a", "a");
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens);

        Assert.assertEquals(parse.getProbability(), 0.6561, 0.0001);
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
                .setSemiring(LogSemiring.get())
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

        Parser<String> parser = new Parser<>(grammar);
        // Parsable
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("the girl left")), PSVP, 0.0001);
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("the right left")), PSNP + PSVP, 0.0001); // ambiguous
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("the wrong right")), PSNP, 0.0001); // ambiguous
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("the right")), PSNP, 0.0001);
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("the girl")), PSNP, 0.0001);
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("the right right")), PSNP, 0.0001);
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("the left right")), PSNP, 0.0001);

        Assert.assertEquals(parser.recognize(N, Tokens.tokenize("left girl")), 1.0, 0.0001);
        Assert.assertEquals(parser.recognize(N, Tokens.tokenize("left left")), 1.0, 0.0001);
        Assert.assertEquals(parser.recognize(N, Tokens.tokenize("wrong left")), 1.0, 0.0001);

        // Unparsable
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("girl left")), 0.0, 0.0001);
        Assert.assertEquals(parser.recognize(S, Tokens.tokenize("the")), 0.0, 0.0001);
    }

    @Test(expected = TokenNotInLexiconException.class)
    public final void unparseable() {


        final Grammar<String> grammar = new Grammar.Builder<String>("test")
                .setSemiring(LogSemiring.get()) // If not set, defaults to Log semiring which is probably what you want
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
                .addRule(N, stick)
                .addRule(TV, transitiveVerb)
                .addRule(Mod, with, NP) // eg. with a stick
                .build();
        Assert.assertEquals(Parser.recognize(S, grammar, Tokens.tokenize("the notinlexicon left")), 0.0, 0.0001);
    }


    @Test
    public void scanModeDrop() throws Exception {
        double p = (0.6);
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("b a");

        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.DROP).build();
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);
        Assert.assertEquals(p, parse.getProbability(), 0.00001);
    }

    @Test(expected = TokenNotInLexiconException.class)
    public void scanModeStrict() throws Exception {
        double p = (0.6);
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("b a");

        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.STRICT).build();
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);
        Assert.assertEquals(p, parse.getProbability(), 0.00001);
    }

    @Test
    public void scanModeWildcard() throws Exception {
        double p = (0.6);
        final double q = 0.666;
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a, a)
                .addRule(q, S, b, a)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("z a");

        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.WILDCARD).build();
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);
        Assert.assertEquals(q, parse.getProbability(), 0.00001);

        System.out.println(parse);
    }

    @Test
    public void scanModeSynchronizeTokensPre() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(0.8, S, A)
                .addRule(0.7, S, S, A)
                .addRule(0.6, A, a, a, period)
                .addRule(0.5, A, b, b, NonLexicalToken.INSTANCE, period)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("a a . b b z a . a a .");

        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(0.7 * .7 * 0.8 * 0.6 * Math.pow(.5, 2) * .6, parse.getProbability(), 0.00001);
    }

// TODO
//    @Test
//    public void scanModeSynchronizeTokens() throws Exception {
//        Grammar<String> grammar = new Grammar.Builder<String>()
//                .addRule(0.8, S, A)
//                .addRule(0.7, S, S, A)
//                .addRule(0.6, A, a, a, period)
//                .addRule(0.5, A, NonLexicalToken.get(), period)
//                .build();
//        List<Token<String>> tokens = Tokens.tokenize("a a . a . a a .");
//
//        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.SYNCHRONIZE).build();
//        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);
//
//        System.out.println(parse);
//        Assert.assertEquals(0.7 * .7 * 0.8 * 0.6 * Math.pow(.5, 2) * .6, parse.getProbability(), 0.00001);
//    }
//
// todo
//    @Test
//    public void scanModeSynchronizeTokens() throws Exception {
//        Grammar<String> grammar = new Grammar.Builder<String>()
//                .addRule(0.5, S, A)
//                .addRule(0.5, S, S, A)
//                .addRule(0.1, A, a, a, period)
//                .addRule(0.9, A, b, b, period)
//                .addRule(0.9, A, NonLexicalToken.get(), period)
//                .build();
//
//        final List<Token<String>> s0 = Tokens.tokenize("b b . ");
//        final List<Token<String>> s1 = Tokens.tokenize("a a . ");
//        final List<Token<String>> s2 = Tokens.tokenize("a a b a a . ");
//        final List<Token<String>> s3 = Tokens.tokenize("a a .");
//        final List<Token<String>> tokens = new ArrayList<>(s1.size() + s2.size() + s3.size());
//        tokens.addAll(s1);
//        tokens.addAll(s2);
//        tokens.addAll(s3);
//
//        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.SYNCHRONIZE).build();
//        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);
//
//        System.out.println(parse);
//        Assert.assertEquals(.5 * .5 * Math.pow(.9, s1.size() + s2.size() - 1) * .1, parse.getProbability(), 0.00001);
//        assertTrue(.5 * .5 * Math.pow(.9, s1.size() + s2.size() - 1) * .1 < .5 * Math.pow(.9, tokens.size() - 1));
//    }

    @Test
    public void scanModeSynchronizeTokens() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(1.0, S, A)
                .addRule(0.9, S, S, A)
                .addRule(0.1, A, a, a, period)
                .addRule(1, A, c, c, period)
                .addRule(0.9, A, NonLexicalToken.INSTANCE, period)
                .build();

        final List<Token<String>> s0 = Tokens.tokenize("c c . ");
        final List<Token<String>> s1 = Tokens.tokenize("a a . ");
        final List<Token<String>> s2 = Tokens.tokenize("a a b a a . ");
        final List<Token<String>> s3 = Tokens.tokenize("a a .");
        final List<Token<String>> tokens = new ArrayList<>(s0.size() + s1.size() + s2.size() + s3.size());
        tokens.addAll(s0);
        tokens.addAll(s1);
        tokens.addAll(s2);
        tokens.addAll(s3);

        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(0.9 * 0.9 * 1.0 * Math.pow(.9, s1.size() + s2.size() - 1) * .1, parse.getProbability(), 0.00001);
        //assertTrue(.5 * .5 * Math.pow(.9, s1.size() + s2.size() - 1) * .1 < .5 * Math.pow(.9, tokens.size() - 1));
    }

    @Test
    public void scanModeSynchronize() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(0.8, S, A)
                .addRule(0.7, S, S, A)
                .addRule(0.6, A, a, a, period)
                .addRule(0.5, A, NonLexicalToken.INSTANCE, period)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("a a . a b c d e f g h i j k l m n o p q r s t u v w x y z a . a a .");

        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(
                .7 * .7 * .8 * .6 * Math.pow(.5, 27) * .6,
                parse.getProbability(), 0.00001);
    }

    @Test
    public void scanModeSynchronizeSimple() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(1.0, S, A)
                .addRule(0.2, A, NonLexicalToken.INSTANCE, period)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("z a .");

        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(0.2 * 0.2, parse.getProbability(), 0.00001);
    }

    @Test
    public void scanModeSynchronizeSimple2() throws Exception {
        Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(0.2, S, A, A)
                .addRule(0.2, A, NonLexicalToken.INSTANCE, period)
                .build();
        List<Token<String>> tokens = Tokens.tokenize("z a . a .");

        ParseOptions<String> cb = new ParseOptions.Builder<String>().withScanMode(ScanMode.SYNCHRONIZE).build();
        ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens, cb);

        System.out.println(parse);
        Assert.assertEquals(0.2 * 0.2 * 0.2 * 0.2, parse.getProbability(), 0.00001);
    }
}