package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.StringTerminal;
import org.leibnizcenter.cfg.earleyparser.scan.TokenNotInLexiconException;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.Tokens;

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
    private static final Category b = new ExactStringTerminal("b");
    private static final Terminal<String> c = new CaseInsensitiveStringTerminal("c");
    private static final Terminal<String> man = new ExactStringTerminal("man");
    private static final Terminal<String> stick = new ExactStringTerminal("stick");
    private static final Terminal<String> with = new ExactStringTerminal("with");
    private static final Terminal<String> period = new ExactStringTerminal(".");


    private final static NonTerminal A = Category.nonTerminal("A");
    private final static NonTerminal B = Category.nonTerminal("B");
    private final static NonTerminal C = Category.nonTerminal("C");
    private final static NonTerminal D = Category.nonTerminal("D");


    @Test
    public void simpleGrammar2() throws Exception {
        final double p = (0.6);
        final double q = (0.4);
        // b surrounded by a's, or a single a
        final Grammar<String> grammar = new Grammar.Builder<String>()
                .addRule(p, S, a)
                .addRule(q, S, S, b, S)
                .build();
        final List<Token<String>> tokens1 = Tokens.tokenize("a");
        final ParseTreeWithScore parse1 = new Parser<>(grammar).getViterbiParseWithScore(S, tokens1);
        assertEquals(0.6, parse1.getProbability(), 0.0000000001);

        final List<Token<String>> tokens2 = Tokens.tokenize("a  b a b a");
        final ParseTreeWithScore parse2 = new Parser<>(grammar).getViterbiParseWithScore(S, tokens2);

        Assert.assertEquals(parse2.getProbability(), 0.4 * 0.4 * 0.6 * 0.6 * 0.6, 0.00001);
    }


    @Test
    public void simpleGrammar3() throws Exception {
        final double p = (0.6);
        final double q = (0.4);
        final LogSemiring sr = LogSemiring.get();
        // a surrounded by b's, or a single a
        final Grammar<String> grammar = new Grammar.Builder<String>()
                .withSemiring(sr)
                .addRule(p, S, a)
                .addRule(q, S, b, S, b)
                .build();
        final List<Token<String>> tokens = Tokens.tokenize("b b a b b");
        final ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens);

        Assert.assertEquals(parse.getProbability(), q * q * p, 0.00001);
    }

    @Test
    public void viterbi() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        final Grammar<String> grammar = new Grammar.Builder<String>()
                .withSemiring(sr)
                .addRule(0.5, S, A)
                .addRule(0.5, S, S, S)
                .addRule(1.0, A, a)
                .build();

        final List<Token<String>> tokens = Tokens.tokenize("a", "a", "a");
        final ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens);

        Assert.assertNotNull(parse);
        assertEquals(parse.getProbability(), Math.pow(0.5, 5), 0.0001);
    }

    @Test
    public void viterbi2() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        final Grammar<String> grammar = new Grammar.Builder<String>()
                .withSemiring(sr)
                .addRule(0.9, S, A)
                .addRule(0.1, S, S, S)
                .addRule(0.2, A, B)
                .addRule(0.8, A, D)
                .addRule(0.6, B, C)
                .addRule(0.4, B, a)
                .addRule(0.6, C, D)
                .addRule(0.4, C, a)
                .addRule(1.0, D, a)
                .build();

        final List<Token<String>> tokens = Tokens.tokenize("a", "a", "a");
        final ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens);

        Assert.assertEquals(parse.getProbability(), 0.00373248, 0.0001);
    }


    @Test
    public void viterbi3() throws Exception {
        final LogSemiring sr = LogSemiring.get();
        final Grammar<String> grammar = new Grammar.Builder<String>()
                .withSemiring(sr)
                .addRule(1.0, S, A, A)
                .addRule(0.9, A, B)
                .addRule(0.1, A, D)
                .addRule(0.1, B, C)
                .addRule(1.0, D, a, a)
                .addRule(0.5, C, D)
                .addRule(0.9, B, a)
                .addRule(0.5, C, a, a)
                .build();

        final List<Token<String>> tokens = Tokens.tokenize("a", "a", "a", "a");
        final ParseTreeWithScore parse = new Parser<>(grammar).getViterbiParseWithScore(S, tokens);

        Assert.assertEquals(parse.getProbability(), 0.01, 0.0001);
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

        final double pSVP = 0.9;
        final double pSNP = 1 - pSVP;
        final Grammar<String> grammar = new Grammar.Builder<String>("test")
                .withSemiring(LogSemiring.get())
                .addRule(pSVP, S, NP, VP)
                .addRule(pSNP, S, NP)
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
                .build(true);

        final Parser<String> parser = new Parser<>(grammar);
        // Parsable
        final double pDet = grammar.getRules(Det).iterator().next().probability;
        final double pN = grammar.getRules(N).iterator().next().probability;
        final double pNP = grammar.getRules(NP).iterator().next().probability;
        final double pBV = grammar.getRules(BV).iterator().next().probability;
        final double pVP = grammar.getRules(VP).iterator().next().probability;

        assertEquals(pSVP * pNP * pDet * pN * pVP, parser.recognize(S, Tokens.tokenize("the girl left")), 0.0001);
        assertEquals((pSVP * pNP * pDet * pN * pVP) + (pSNP * pNP * pDet * pN * pBV * pN), parser.recognize(S, Tokens.tokenize("the right left")), 0.0001); // ambiguous
        assertEquals(pSNP * pNP * pDet * pN * pBV * pN, parser.recognize(S, Tokens.tokenize("the wrong right")), 0.0001); // ambiguous
        assertEquals(pSNP * pDet * pN, parser.recognize(S, Tokens.tokenize("the right")), 0.0001);
        assertEquals(pSNP * pDet * pN, parser.recognize(S, Tokens.tokenize("the girl")), 0.0001);
        assertEquals(pSNP * pNP * pDet * pN * pBV * pN, parser.recognize(S, Tokens.tokenize("the right right")), 0.0001);
        assertEquals(pSNP * pNP * pDet * pN * pBV * pN, parser.recognize(S, Tokens.tokenize("the left right")), 0.0001);
        assertEquals(pN * pBV * pN, parser.recognize(N, Tokens.tokenize("left girl")), 0.0001);
        assertEquals(pN * pBV * pN, parser.recognize(N, Tokens.tokenize("left left")), 0.0001);
        assertEquals(pN * pBV * pN, parser.recognize(N, Tokens.tokenize("wrong left")), 0.0001);

        // Unparsable
        assertEquals(0.0, parser.recognize(S, Tokens.tokenize("girl left")), 0.0001);
        assertEquals(0.0, parser.recognize(S, Tokens.tokenize("the")), 0.0001);
    }

    @Test(expected = TokenNotInLexiconException.class)
    public final void unparseable() {
        final Grammar<String> grammar = new Grammar.Builder<String>("test")
                .withSemiring(LogSemiring.get()) // If not set, defaults to Log semiring which is probably what you want
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
                .addRule(0.5, N, man)
                .addRule(0.5, N, stick)
                .addRule(TV, transitiveVerb)
                .addRule(Mod, with, NP) // eg. with a stick
                .build();
        assertEquals(new Parser<>(grammar).recognize(S, Tokens.tokenize("the notinlexicon left")), 0.0, 0.0001);
    }


}