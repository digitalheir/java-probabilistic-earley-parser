package org.leibnizcenter.cfg.earleyparser;

import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Document grammar
 *
 * Created by Maarten on 23-8-2016.
 */
public class DocGramTest {
    public static final NonTerminal DOCUMENT = new NonTerminal("#root");
    public static final NonTerminal HEADER = new NonTerminal("Header");
    public static final NonTerminal DOCUMENT_BODY = new NonTerminal("DocumentContent");

    /**
     * 1 or more sections in sequence
     */
    public static final NonTerminal SECTION_BLOB = new NonTerminal("Sections");

    public static final NonTerminal SECTION = new NonTerminal("Section");
    public static final NonTerminal SECTION_TITLE = new NonTerminal("SectionTitle");
    public static final NonTerminal NEWLINE_AND_TITLE_TEXT = new NonTerminal("NEWLINE_AND_TITLE_TEXT");
    public static final NonTerminal SECTION_CONTENT = new NonTerminal("SectionContent");
    public static final NonTerminal SECTION_SEQUENCE = new NonTerminal("SECTION_SEQUENCE");
    public static final NonTerminal COMPLETE_SECTION_BLOB_W_TRAILING_TEXT = new NonTerminal("COMPLETE_SECTION_BLOB_W_TRAILING_TEXT");

    public static final NonTerminal TEXT_BLOB = new NonTerminal("Text");
    public static final NonTerminal SECTION_TITLE_TEXT = new NonTerminal("SECTION_TITLE_TEXT");
    public static final NonTerminal SINGLE_TITLE_TEXT = new NonTerminal("SINGLE_TITLE_TEXT");
    public static final NonTerminal SINGLE_NUMBERING = new NonTerminal("SINGLE_NUMBERING");
    public static final NonTerminal NONTERMINAL_NEWLINE = new NonTerminal("NONTERMINAL_NEWLINE");

    public static final ExactStringTerminal TERMINAL_NUMBERING = new ExactStringTerminal("NR");
    public static final ExactStringTerminal TERMINAL_SECTION_TITLE = new ExactStringTerminal("SECTION_TITLE");
    public static final ExactStringTerminal TERMINAL_TEXT = new ExactStringTerminal("TEXT_BLOCK");
    public static final ExactStringTerminal TERMINAL_NEWLINE = new ExactStringTerminal("NEWLINE");
    public final static Grammar<String> grammar = new Grammar.Builder<String>()
            .withSemiring(LogSemiring.get())
            .addRule(1.0, DOCUMENT, /* -> */ DOCUMENT_BODY)

            .addRule(0.7, DOCUMENT_BODY, /* -> */ SECTION_SEQUENCE)
            .addRule(0.1, DOCUMENT_BODY, /* -> */ TEXT_BLOB, SECTION_SEQUENCE)
            .addRule(0.1, DOCUMENT_BODY, /* -> */ SECTION_SEQUENCE, TEXT_BLOB)
            .addRule(0.1, DOCUMENT_BODY, /* -> */ TEXT_BLOB, SECTION_SEQUENCE, TEXT_BLOB)
//
            .addRule(1.0, SECTION_SEQUENCE, SECTION_BLOB)
//
            .addRule(0.3, SECTION_BLOB, /* -> */ SECTION_BLOB, SECTION_BLOB)
            .addRule(0.3, SECTION_BLOB, /* -> */ SECTION)
            .addRule(0.2, SECTION_BLOB, /* -> */ SECTION, TEXT_BLOB)
            .addRule(0.2, SECTION_BLOB, /* -> */ TEXT_BLOB, SECTION)

            .addRule(0.99, SECTION, /* -> */ SECTION_TITLE, SECTION_CONTENT)
            .addRule(1 - 0.99, SECTION, /* -> */ SECTION_TITLE)

            .addRule(0.4, SECTION_CONTENT, /* -> */ SECTION_CONTENT, SECTION_CONTENT)
            .addRule(0.4, SECTION_CONTENT, /* -> */ TEXT_BLOB)
            .addRule(0.2, SECTION_CONTENT, /* -> */ SECTION_SEQUENCE)

            .addRule(0.5, TEXT_BLOB, /* -> */ TEXT_BLOB, TEXT_BLOB)
            .addRule(0.3, TEXT_BLOB, /* -> */ TERMINAL_TEXT)
            .addRule(0.2, TEXT_BLOB, /* -> */ TERMINAL_NEWLINE)

            //
            // Section Title
            //
            .addRule(0.2, SECTION_TITLE, /* -> */ TERMINAL_NUMBERING)
            .addRule(0.3, SECTION_TITLE, /* -> */ SECTION_TITLE_TEXT)
            .addRule(0.5, SECTION_TITLE, /* -> */ TERMINAL_NUMBERING, SECTION_TITLE_TEXT)

            .addRule(0.9, SECTION_TITLE_TEXT, /* -> */ TERMINAL_SECTION_TITLE)
            .addRule(0.1, SECTION_TITLE_TEXT, /* -> */ TERMINAL_NEWLINE, SECTION_TITLE_TEXT)

            .build();
    private static final Token<String> TEXT_BLOCK = new Token<>("TEXT_BLOCK");
    private static final Token<String> NEWLINE = new Token<>("NEWLINE");
    private static final Token<String> NR = new Token<>("NR");
    private static final Token<String> SECTION_TITLE_ = new Token<>("SECTION_TITLE");
    List<Token<String>> three_doc = new ArrayList<>(200);
    List<Token<String>> four_doc = new ArrayList<>(200);
    private List<Token<String>> one_doc = new ArrayList<>(200);
    private List<Token<String>> two_doc = new ArrayList<>(200);

    {
        one_doc.add(TEXT_BLOCK);
        one_doc.add(NEWLINE);
        one_doc.add(TEXT_BLOCK);
        one_doc.add(NEWLINE);
        one_doc.add(TEXT_BLOCK);
        one_doc.add(NEWLINE);
        one_doc.add(TEXT_BLOCK);
        one_doc.add(TEXT_BLOCK);
        one_doc.add(TEXT_BLOCK);
        one_doc.add(TEXT_BLOCK);
        one_doc.add(TEXT_BLOCK);
        one_doc.add(TEXT_BLOCK);

    }

    {
        two_doc.add(NEWLINE);
        two_doc.add(NR);
        two_doc.add(SECTION_TITLE_);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(NEWLINE);
        two_doc.add(NR);
        two_doc.add(SECTION_TITLE_);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(NEWLINE);
        two_doc.add(NR);
        two_doc.add(SECTION_TITLE_);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(NEWLINE);
        two_doc.add(NR);
        two_doc.add(TEXT_BLOCK);
        two_doc.add(TEXT_BLOCK);
    }

    {
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(NEWLINE);
        three_doc.add(NR);
        three_doc.add(SECTION_TITLE_);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(TEXT_BLOCK);
        three_doc.add(NR);
    }

    {
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NEWLINE);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(SECTION_TITLE_);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NEWLINE);
        four_doc.add(NR);
        four_doc.add(SECTION_TITLE_);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NEWLINE);
        four_doc.add(NR);
        four_doc.add(SECTION_TITLE_);
        four_doc.add(NEWLINE);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NEWLINE);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NEWLINE);
        four_doc.add(TEXT_BLOCK);
        four_doc.add(NR);
        four_doc.add(NR);
        four_doc.add(NR);
        four_doc.add(SECTION_TITLE_);
    }

    @Test
    public void tryGram() {
        final List<Token<String>> listSoFar = new ArrayList<>(1000);
        int s = 0;


        //noinspection ConstantConditions
        for (int i = 0; i < 0; i++) {
            if (i < one_doc.size())
                listSoFar.add(one_doc.get(i));
            else if (i < one_doc.size() + two_doc.size())
                listSoFar.add(two_doc.get(i - one_doc.size()));
            else if (i < one_doc.size() + two_doc.size() + three_doc.size())
                listSoFar.add(three_doc.get(i - (one_doc.size() + two_doc.size())));
            else if (i < one_doc.size() + two_doc.size() + three_doc.size() + four_doc.size())
                listSoFar.add(four_doc.get(i - (one_doc.size() + two_doc.size() + three_doc.size())));
            else {
                s = ((s + 1) % 4);
                listSoFar.addAll(s == 0 ? one_doc : s == 1 ? two_doc : s == 2 ? three_doc : four_doc);
            }
            if (i > 100) {
                final long start = System.currentTimeMillis();
                //noinspection deprecation
                Parser.getViterbiParseWithScore(DOCUMENT, grammar, listSoFar);
                final long end = System.currentTimeMillis();
                System.out.println(listSoFar.size() + "\t" + (end - start));
            }
        }

    }
}
