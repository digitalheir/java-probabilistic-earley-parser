package org.leibnizcenter.cfg.earleyparser;

import com.google.common.collect.Lists;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.exception.PepException;
import org.leibnizcenter.cfg.earleyparser.parse.ParserOptions;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Maarten on 2016-06-06.
 */
public class TryParser {
    private static final Category S = Category.nonTerminal("S");
    private static final Category ac = Category.terminal((Function<Token<String>, Boolean>) t -> t.toString().equals("a"));
    private static final Category bc = Category.terminal((Function<Token<String>, Boolean>) t -> t.toString().equals("b"));
    private static final Token<String> a = new Token<>("a");
    private static final Token<String> b = new Token<>("b");

    public static void main(String[] args) throws PepException {
        ParserOptions options = new ParserOptions();
        Pep pep = new Pep(options);
        Grammar grammar = new Grammar.Builder()
                .addRule(new Rule(S, ac, S, ac))
                .addRule(new Rule(S, bc))
                .build();
        List<Token<String>> tokens = Lists.newArrayList(a, a, a, a, b, a, a, a, a);
        pep.parseTokens(grammar, tokens, S);
    }


}
