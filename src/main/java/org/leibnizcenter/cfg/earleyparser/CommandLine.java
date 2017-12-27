package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.earleyparser.scan.ScanMode;
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
 * Interface for runnable jar
 * Created by maarten on 27-1-17.
 */
public class CommandLine {

    private static final String USAGE = "First specify the location to a grammar file with -i and the goal category with -goal, and then pass input words" + '\n' +
            "\nFor example: java parser.jar -i grammar.cfg -goal S i eat" + '\n' +
            "\nA grammar file looks something like this:" + '\n' +
            "S -> NP VP" + '\n' +
            "NP -> i" + '\n' +
            "VP -> eat" + '\n';
    private static final String OPTION_GOAL = "goal";
    private static final String OPTION_SCAN_MODE = "scanmode";
    private static final String INPUT_FILE = "i";

    /**
     * -i grammar.cfg -goal S
     */
    public static void main(final String[] args) {
        final HandleArguments arguments = new HandleArguments(args).invoke();
        final ParseTreeWithScore parse = new Parser<>(arguments.getGrammar())
                .getViterbiParseWithScore(
                        arguments.getGoal(),
                        Stream.of(arguments.getTokens()).map(Token::of).collect(Collectors.toList()),
                        new ParseOptions.Builder<String>().withScanMode(arguments.scanMode).build()
                );

        System.out.println(parse.score.getProbability());
        System.out.println(parse.parseTree);
    }


    private static class HandleArguments {
        final private String[] args;
        private String[] tokens;
        private Grammar<String> grammar;
        private NonTerminal goal;
        private ScanMode scanMode;

        HandleArguments(final String... args) {
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

        private void setGoal(final Map<String, String> options) {
            goal = Category.nonTerminal(options.getOrDefault(OPTION_GOAL, "S"));
            if (!grammar.getNonTerminals().contains(goal)) {
                throw new IllegalArgumentException("Grammar does not contains non-terminal \"" + goal + "\". \n" + USAGE);
            }
        }

        HandleArguments invoke() {
            if (args.length < 2) throw new IllegalArgumentException("No arguments specified.\n\n" + USAGE);

            final Map<String, String> options = parseOptions();

            setInputFile(options);
            setGoal(options);
            setParseMode(options);

            return this;
        }

        private Map<String, String> parseOptions() {
            int lastOption = 0;
            final Map<String, String> options = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                final String word = args[i].trim();
                if (word.charAt(0) == '-' && i < args.length - 1) {
                    // This is an option
                    final String option = word.substring(1).toLowerCase();
                    switch (option) {
                        case INPUT_FILE:
                        case OPTION_GOAL:
                        case OPTION_SCAN_MODE:
                        case "o":
                            lastOption = i + 1;
                            options.put(option, args[i + 1].trim());
                        default:
                    }
                    i++;
                }
            }

            tokens = Arrays.copyOfRange(args, lastOption + 1, args.length);
            if (tokens.length <= 0) throw new IllegalArgumentException("No tokens specified. \n" + USAGE);
            return options;
        }

        private void setParseMode(final Map<String, String> options) {
            final String scanMode = options.getOrDefault(OPTION_SCAN_MODE, "strict");
            this.scanMode = ScanMode.fromString(scanMode);
        }

        private void setInputFile(final Map<String, String> options) {
            if (!options.containsKey(INPUT_FILE)) {
                throw new IllegalArgumentException("No input file specified. \n" + USAGE);
            } else {
                final Path inputFile = Paths.get(options.get(INPUT_FILE));
                try {
                    grammar = Grammar.fromString(inputFile, Charset.forName("UTF8"));
                } catch (final IOException e) {
                    throw new IllegalArgumentException("Could not parse file at " + inputFile.toAbsolutePath() + '\n' + USAGE);
                }
            }
        }
    }

}
