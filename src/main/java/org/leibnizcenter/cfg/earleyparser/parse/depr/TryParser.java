//package org.leibnizcenter.cfg.earleyparser;
//
//import org.leibnizcenter.cfg.Grammar;
//import org.leibnizcenter.cfg.category.Category;
//import org.leibnizcenter.cfg.category.terminal.ExactStringTerminal;
//import org.leibnizcenter.cfg.errors.PepException;
//import org.leibnizcenter.cfg.earleyparser.parse.ParserOptions;
//import org.leibnizcenter.cfg.rule.Rule;
//import org.leibnizcenter.cfg.token.Token;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Function;
//
///**
// * Created by Maarten on 2016-06-06.
// */
//public class TryParser {
//    private static final Category Sections = Category.nonTerminal("Sections");
//    private static final Category Texts = Category.nonTerminal("Texts");
//    private static final Category Section = Category.nonTerminal("Section");
//    private static final Category Content = Category.nonTerminal("Section");
//    private static final Category Title = new ExactStringTerminal("title");
//    private static final Category Text = new ExactStringTerminal("text");
//    private static final Token<String> title = new Token<>("title");
//    private static final Token<String> text = new Token<>("text");
//
//    public static void main(String[] args) throws PepException, IOException {
//        ParserOptions options = new ParserOptions();
//        Pep pep = new Pep(options);
//        Grammar grammar = new Grammar.Builder()
//                .addRule(new Rule(Sections, Sections, Section))
//                .addRule(new Rule(Sections, Section))
//
//                .addRule(new Rule(Section, Title, Content))
//
////                .addRule(new Rule(Content, Texts, Sections, Texts))
////                .addRule(new Rule(Content, Texts, Sections))
////                .addRule(new Rule(Content, Sections, Texts))
////                .addRule(new Rule(Content, Sections))
//                .addRule(new Rule(Content, Texts))
//
//                .addRule(new Rule(Texts, Texts, Text))
//                .addRule(new Rule(Texts, Text))
//                .build();
//
//        List<Token<String>> tokens = new ArrayList<>(100000);
//        FileWriter fw = new FileWriter("results.csv");
//        for (int i = 1; i <= 800; i += 1) {
//            long in = System.nanoTime();
//                System.out.println(i);
////            for (int j = 0; j < 1; j++) {
//                tokens.add(title);
//                tokens.add(text);
//                tokens.add(text);
//                tokens.add(text);
//                tokens.add(title);
//                tokens.add(text);
//                tokens.add(text);
////            }
//            pep.parseTokens(grammar, tokens, Sections);
//            long out = System.nanoTime();
//            fw.append(Long.toString(out - in)).append(",").append(Integer.toString(i)).append("\n");
//        }
//        fw.close();
//    }
//
//
//}
