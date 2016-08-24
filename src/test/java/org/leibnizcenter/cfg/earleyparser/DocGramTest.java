package org.leibnizcenter.cfg.earleyparser;

import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;

/**
 * Created by Maarten on 23-8-2016.
 */
public class DocGramTest {
    private static final NonTerminal DOCUMENT = new NonTerminal("D");
    private static final NonTerminal DOCUMENT_CONTENT = new NonTerminal("DC");
    private static final NonTerminal COMPLETE_SECTION_BLOB = new NonTerminal("CSB");
    private static final NonTerminal SECTION_BLOB = new NonTerminal("SB");
    private static final NonTerminal SECTION = new NonTerminal("S");
    private static final NonTerminal SECTION_TITLE_TEXT = new NonTerminal("STT");
    private static final NonTerminal COMPLETE_SECTION_CONTENT = new NonTerminal("CSC");
    private static final Category TERMINAL_NUMBERING = new ExactStringTerminal("nr");
    private static final Category TERMINAL_SECTION_TITLE = new ExactStringTerminal("t");
    private static final NonTerminal SECTION_TITLE = new NonTerminal("ST");
    private static final Category TERMINAL_NEWLINE = new ExactStringTerminal("\n");
    private static final NonTerminal TEXT_BLOB = new NonTerminal("TB");
    private static final Category TERMINAL_TEXT = new ExactStringTerminal("termtxt");
    private static final NonTerminal SECTION_CONTENT = new NonTerminal("SECCON");

    @Test
    public void tryGram() {
        final Grammar grammar = new Grammar.Builder()
                .setSemiring(new LogSemiring())
                //.addRule(1.0, DOCUMENT, /* -> */ HEADER, DOCUMENT_CONTENT) // TODO
//                .addRule(0.8, DOCUMENT, /* -> */ DOCUMENT_CONTENT)

//                .addRule(1.0, DOCUMENT_CONTENT, /* -> */ COMPLETE_SECTION_BLOB)
//            .addRule(0.8, DOCUMENT_CONTENT, /* -> */ TEXT_BLOB, COMPLETE_SECTION_BLOB)
//            .addRule(0.8, DOCUMENT_CONTENT, /* -> */ COMPLETE_SECTION_BLOB, TEXT_BLOB)
//            .addRule(0.7, DOCUMENT_CONTENT, /* -> */ TEXT_BLOB, COMPLETE_SECTION_BLOB, TEXT_BLOB)
//
//                .addRule(1.0, COMPLETE_SECTION_BLOB, SECTION_BLOB) // TODO CompletedSectionBlob
//
//            .addRule(1.0, SECTION_BLOB, /* -> */ SECTION_BLOB, SECTION_BLOB)
//                .addRule(1.0, SECTION_BLOB, /* -> */ SECTION)
//            .addRule(0.8, SECTION_BLOB, /* -> */ SECTION, TEXT_BLOB)
//            .addRule(0.8, SECTION_BLOB, /* -> */ TEXT_BLOB, SECTION)

                // TODO allow newlines in title?
//                .addRule(1.0, SECTION, /* -> */ SECTION_TITLE, COMPLETE_SECTION_CONTENT)

//                .addRule(1.0, COMPLETE_SECTION_CONTENT, /* -> */ SECTION_CONTENT)
//                .addRule(1.0, SECTION_CONTENT, /* -> */ TEXT_BLOB)
//            .addRule(1.0, SECTION_CONTENT, /* -> */ COMPLETE_SECTION_BLOB)
//            .addRule(1.0, SECTION_CONTENT, /* -> */ SECTION_CONTENT, SECTION_CONTENT)
//
                .addRule(0.5, TEXT_BLOB, /* -> */ TERMINAL_TEXT)
//                .addRule(1.0, TEXT_BLOB, /* -> */ TERMINAL_NEWLINE)
                .addRule(0.5, TEXT_BLOB, /* -> */ TEXT_BLOB, TEXT_BLOB)

                //
                // Section Title
                //
//                .addRule(1.0, SECTION_TITLE, /* -> */ TERMINAL_NUMBERING)
//                .addRule(1.0, SECTION_TITLE, /* -> */ SECTION_TITLE_TEXT)
//                .addRule(1.0, SECTION_TITLE, /* -> */ TERMINAL_NUMBERING, SECTION_TITLE_TEXT)
//
//                .addRule(1.00, SECTION_TITLE_TEXT, /* -> */ TERMINAL_SECTION_TITLE)
//                .addRule(0.99, SECTION_TITLE_TEXT, /* -> */ TERMINAL_NEWLINE, SECTION_TITLE_TEXT)

                .build();


    }
}
