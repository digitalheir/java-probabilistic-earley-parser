package edu.osu.ling.pep;

import com.google.common.collect.Lists;
import edu.osu.ling.pep.earley.ParserOption;
import edu.osu.ling.pep.grammar.categories.Category;
import edu.osu.ling.pep.grammar.Grammar;
import edu.osu.ling.pep.grammar.Rule;
import edu.osu.ling.pep.grammar.Token;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by Maarten on 2016-06-06.
 */
public class TestParser {
    private static final Category S = Category.nonTerminal("S");
    private static final Category ac = Category.terminal((Function<Token<String>, Boolean>) t -> t.toString().equals("a"));
    private static final Category bc = Category.terminal((Function<Token<String>, Boolean>) t -> t.toString().equals("b"));
    private static final Token<String> a = new Token<>("a");
    private static final Token<String> b = new Token<>("b");

    public static void main(String[] args) throws PepException {
        Map<ParserOption, Boolean> options = new EnumMap<>(
                ParserOption.class);
        Pep pep = new Pep(options);
        Grammar grammar = new Grammar.Builder()
                .addRule(new Rule(S, ac, S, ac))
                .addRule(new Rule(S, bc))
                .build();
        List<Token<String>> tokens = Lists.newArrayList(a, a, a, a, b, a, a, a, a);
        pep.parse(grammar, tokens, S);
    }


}
