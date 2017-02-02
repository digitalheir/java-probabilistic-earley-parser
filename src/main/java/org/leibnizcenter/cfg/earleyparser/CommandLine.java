package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by maarten on 27-1-17.
 */
public class CommandLine {

    private static final String USAGE = "First specify the location to a grammar file with -i and the goal category with -goal, and then pass input words" + "\n" +
            "\nFor example: java parser.jar -i grammar.cfg -goal S i eat" + "\n" +
            "\nA grammar file looks something like this:" + "\n" +
            "S -> NP VP" + "\n" +
            "NP -> i" + "\n" +
            "VP -> eat" + "\n";
    private static final String OPTION_GOAL = "goal";
    private static final String INPUT_FILE = "i";

    /**
     * -i grammar.cfg -goal S
     */
    public static void main(String[] args) {
        HandleArguments handleArguments = new HandleArguments(args).invoke();
        ParseTreeWithScore parse = Parser.getViterbiParseWithScore(
                handleArguments.getGoal(),
                handleArguments.getGrammar(),
                Stream.of(handleArguments.getTokens()).map(Token::of).collect(Collectors.toList())
        );

        System.out.println(parse.score.semiring.toProbability(parse.score.getScore()));
        System.out.println(parse.parseTree);
    }


    private static class HandleArguments {
        private String[] args;
        private String[] tokens;
        private Grammar<String> grammar;
        private NonTerminal goal;

        HandleArguments(String... args) {
            this.args = args;
        }

        String[] getTokens() {
            return tokens;
        }

        Grammar<String> getGrammar() {
            return grammar;
        }

        NonTerminal getGoal() {
            return goal;
        }

        HandleArguments invoke() {
            if (args.length < 2) {
                throw new IllegalArgumentException("No arguments specified.\n\n" + USAGE);
            }
            int lastOption = 0;
            final Map<String, String> options = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                String word = args[i].trim();
                if (word.charAt(0) == '-' && i < args.length - 1) {
                    // This is an option
                    final String option = word.substring(1);
                    switch (option) {
                        case INPUT_FILE:
                        case OPTION_GOAL:
                        case "o":
                            lastOption = i + 1;
                            options.put(option, args[i + 1].trim());
                        default:
                    }
                    i++;
                }
            }
            tokens = Arrays.copyOfRange(args, lastOption + 1, args.length);

            if (!options.containsKey(INPUT_FILE)) {
                throw new IllegalArgumentException("No input file specified. \n" + USAGE);
            } else {
                Path inputFile = Paths.get(options.get(INPUT_FILE));
                try {
                    grammar = Grammar.parse(inputFile, Charset.defaultCharset());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Could not parse file at " + inputFile.toAbsolutePath() + "\n" + USAGE);
                }
            }

            if (!options.containsKey(OPTION_GOAL)) {
                throw new IllegalArgumentException("No goal category specified. \n" + USAGE);
            }

            goal = Category.nonTerminal(options.get(OPTION_GOAL));
            if (!grammar.getNonTerminals().contains(goal)) {
                throw new IllegalArgumentException("Grammar does not contains non-terminal \"" + goal + "\". \n" + USAGE);
            }

            if (tokens.length <= 0) {
                throw new IllegalArgumentException("No tokens specified. \n" + USAGE);
            }
            return this;
        }
    }

}
