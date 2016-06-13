
package org.leibnizcenter.cfg.earleyparser;

import org.apache.commons.cli.*;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.terminal.StringTerminal;
import org.leibnizcenter.cfg.earleyparser.event.EdgeEvent;
import org.leibnizcenter.cfg.earleyparser.event.ParseErrorEvent;
import org.leibnizcenter.cfg.earleyparser.event.ParseEvent;
import org.leibnizcenter.cfg.earleyparser.event.ParserListener;
import org.leibnizcenter.cfg.earleyparser.exception.PepException;
import org.leibnizcenter.cfg.earleyparser.parse.*;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Command line front end for {@link ParseTokens Earley parsers}.
 * <p>
 * In addition to tokenizing input strings and loading the {@link Grammar} that
 * a parser will use, Pep also a {@link ParserListener listens} for event
 * from the Earley parsers it invokes. An example invocation might be something
 * like:
 * <ol>
 * <li>
 * <code>pep -g samples/miniscule.xml -s S the boy left -v</code>
 * </li>
 * <li>
 * <code>echo "the boy left" | pep -g samples/miniscule.xml -v 2 -s S -
 * </code>
 * </li>
 * </ol>
 * </blockquote>
 * The above commands cause Pep to create an Earley parser using the
 * grammar specified in the file <code>samples/miniscule.xml</code> for the
 * {@link Parse#getSeed() seed category} <code>S</code> and the input
 * string &quot;<code>the boy left</code>&quot;. Example (2) shows how to
 * configure Pep to read input from the standard input stream. For either of
 * these commands, Pep prints out the result of the parse as well as all
 * {@link ParseTree parse trees} for the specified string.
 * </p>
 *
 * @see ParseTokens
 */
public class Pep implements ParserListener {
    static final int V_ALL = 0, V_RECOGNITION = 0, V_WARN = 1, V_PARSE = 1,
            V_CHART = 2, V_GRAMMAR = 3, V_STATS = 3, V_DEBUG = 3;
    private static final float VERSION = 0.4f;
    static int verbosity = 0;

    ParserOptions parserOptions;
    long lastParseStart;

    /**
     * Do not allow this class to be instantiated except by its own
     * {@link Pep#main(String[]) main} method.
     */
    Pep(ParserOptions parserOptions) {
        this.parserOptions = parserOptions;
    }

    /**
     * Prints an object to System.out.
     *
     * @param line              The object to print out.
     * @param requiredVerbosity The required verbosity level for this
     *                          message to actually be printed out.
     */
    private static void print(Object line, int requiredVerbosity) {
        if (Pep.verbosity >= requiredVerbosity) {
            System.out.println(line);
        }
    }

    /**
     * Prints an object to System.err.
     *
     * @param message           The object to print out.
     * @param requiredVerbosity The required verbosity level for this
     *                          message to actually be printed out.
     */
    static void printMessage(String message, int requiredVerbosity) {
        if (Pep.verbosity >= requiredVerbosity) {
            System.err.println(message);
        }
    }

    /**
     * Prints an error message to System.err.
     */
    private static void printError(String msg) {
        Pep.printMessage("Error: " + msg, Pep.V_ALL);
    }

    /**
     * Prints a throwable.
     *
     * @param error The throwable that was intercepted.
     * @see #printError(String)
     */
    private static void printError(Throwable error) {
        if (error instanceof SAXParseException) {
            SAXParseException spe = (SAXParseException) error;
            Pep.printError("line " + spe.getLineNumber() + ": "
                    + spe.getMessage());
        } else {
            String msg = error.getMessage();
            Throwable cause = error.getCause();
            if (cause != null && !cause.equals(error)) {
                msg += ": " + cause.getMessage();
            }

            Pep.printError(msg);
        }
    }

    /**
     * Prints a warning string.
     *
     * @see #print(Object, int)
     */
    static void printWarning(String msg) {
        Pep.printMessage("Warning: " + msg, Pep.V_WARN);
    }

    /**
     * Prints usage information for the Pep executable.
     */
    private static void printHelp(Options options) {
        String name = Pep.class.getSimpleName();
        Pep.printMessage(name + " is an Earley Parser, version " + Pep.VERSION,
                Pep.V_ALL);

        Pep.printMessage("", Pep.V_ALL);
        Pep.printMessage(name
                        + " is free software, copyright (C) 2007 Scott Martin.",
                Pep.V_ALL);
        Pep.printMessage("See the COPYING file for details.", Pep.V_ALL);

        Pep.printMessage("", Pep.V_ALL);

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("pep [options] [string] (use - for stdin)",
                options);

        Pep.printMessage("", Pep.V_ALL);
        Pep.printMessage("where the default OPTION=value pairs are:",
                Pep.V_ALL);

        for (ParserOption option : ParserOption.values()) {
            Pep.printMessage(' ' + option.toString(), Pep.V_ALL);
        }
    }

    /**
     * Auxiliary method used by event handlers to print out information about
     * what the parser is doing in a readable format.
     */
    private static void printParser(int index, String label, Object obj) {
        if (Pep.verbosity >= Pep.V_CHART) {
            StringBuilder sb = new StringBuilder(index + ": " + label + "\t");
            if (obj instanceof String) {
                sb.append('\'');
                sb.append(obj);
                sb.append('\'');
            } else if (obj instanceof Throwable) {
                sb.append(((Throwable) obj).getMessage());
            } else {
                sb.append(obj);
            }

            Pep.print(sb.toString(), Pep.V_CHART);
        }
    }

    /**
     * Invokes Pep from the command line.
     * <p>
     * The main work this method does, apart from tokenizing the arguments and
     * input tokens, is to load and parse the XML grammar file (as specified
     * by <code>-g</code> or <code>--grammar</code>). If any of the arguments
     * <code>-g</code>, <code>--grammar</code>, <code>-s</code>,
     * <code>--seed</code>, <code>-o</code>, <code>--option</code>, occur
     * with no argument following, this method prints an error notifying the
     * user.
     *
     * @param args The expected arguments are as follows, and can occur in
     *             any particular order:
     *             <ul>
     *             <li><code>-g|--grammar &lt;grammar file&gt;</code></li>
     *             <li><code>-s|--seed &lt;seed category&gt;</code></li>
     *             <li><code>-v|--verbose {verbosity level}</code></li>
     *             <li><code>-o|--option &lt;OPTION_NAME=value&gt;</code></li>
     *             <li><code>-h|--help (prints usage information)</code></li>
     *             <li><code>&lt;token1 ... token<em>n</em>&gt;</code>
     *             (or <code>-</code> for standard input)</li>
     *             </ul>
     *             <code>OPTION_NAME</code> must be the name of one of the recognized
     *             {@link ParserOption options}. If <code>-h</code> or
     *             <code>--help</code> occur anywhere in the arguments, usage information
     *             is printed and no parsing takes place.
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        try {
            Options opts = new Options();

            opts.addOption(OptionBuilder.withLongOpt("grammar")
                    .withDescription("the grammar to use")
                    .hasArg().isRequired().withArgName("grammar file")
                    .create('g'));

            opts.addOption(OptionBuilder.withLongOpt("seed")
                    .withDescription("the seed category to parse for")
                    .hasArg().isRequired().withArgName("seed category")
                    .create('s'));

            opts.addOption(OptionBuilder.withLongOpt("verbose")
                    .withDescription("0-3")
                    .hasOptionalArg().withArgName("verbosity level")
                    .create('v'));

            opts.addOption(OptionBuilder.withLongOpt("option")
                    .withDescription("sets parser options")
                    .withArgName("OPTION=value")
                    .hasArgs(2)
                    .withValueSeparator()
                    .withDescription("use value for given property")
                    .create("o"));

            opts.addOption(OptionBuilder.withLongOpt("help")
                    .withDescription("prints this message")
                    .create('h'));

            CommandLineParser parser = new GnuParser();
            try {
                CommandLine line = parser.parse(opts, args);
                if (line.hasOption('h')) {
                    Pep.printHelp(opts);
                } else {
                    int v = Integer.parseInt(line.getOptionValue('v',
                            Integer.toString(Pep.V_PARSE)));
                    if (v < 0) {
                        throw new PepException("verbosity < 0: " + v);
                    }

                    Pep.verbosity = v;
                    ParserOptions options
                            = new ParserOptions();

                    Properties props = line.getOptionProperties("o");
                    for (Object key : props.keySet()) {
                        try {
                            options.put(
                                    ParserOption.valueOf(key.toString()),
                                    Boolean.valueOf(props.get(key).toString()));
                        } catch (IllegalArgumentException iae) {
                            Pep.printError("no option named " + key.toString());
                            Pep.printHelp(opts);
                            return;
                        }
                    }

                    Pep pep = new Pep(options);
                    Grammar grammar = new GrammarParser(
                            Pep.findGrammar(line.getOptionValue('g'))).parse();

                    List<?> ts = line.getArgList();
                    List<Token<String>> tokens;
                    if (ts.isEmpty() || ts.get(0).equals("-")) {
                        tokens = Pep.readTokens(new Scanner(System.in));
                    } else tokens = ts.stream().map(Object::toString).map(Token::new).collect(Collectors.toList());

                    pep.lastParseStart = System.currentTimeMillis();
                    try {
                        pep.parse(grammar, tokens,
                                Category.nonTerminal(line.getOptionValue('s')));
                    } catch (PepException ignore) {
                        // ignore here, we're listening
                    }
                }
            } catch (ParseException pe) {
                Pep.printError("command-line syntax problem: "
                        + pe.getMessage());
                Pep.printHelp(opts);
            }
        } catch (PepException pe) {
            Throwable cause = pe.getCause();
            printError((cause == null) ? pe : cause);
        } catch (RuntimeException re) {
            Pep.printError(re);
        }
    }

    /**
     * Locates the grammar file as specified on the command line. Attempts to
     * find the file based on the current directory.
     *
     * @param grammarLoc The grammar location string specified on the command
     *                   line.
     * @return The located file.
     * @throws PepException If the file does not exist or is a directory.
     */
    private static File findGrammar(String grammarLoc)
            throws PepException {
        File g = new File(System.getProperty("user.dir"), grammarLoc);

        if (!g.exists()) {
            throw new PepException("grammar file does not exist");
        }
        if (!g.isFile()) {
            throw new PepException(
                    "specified grammar is not a file");
        }

        return g;
    }

    /**
     * Tokenizes the string input that occurs on the command line, removing
     * &quot; characters.
     *
     * @param args The strings that occurred after <code>-t</code> or
     *             <code>--tokens</code> on the command line.
     * @return A list of tokens suitable for use in parsing.
     */
    private static List<Token<String>> readTokens(Iterator<String> args) {
        List<Token<String>> tokens = new ArrayList<>();
        Pattern q = Pattern.compile("\"");

        while (args.hasNext()) {
            String t = args.next();
            if (t == null) { // let parser deal with this
                tokens.add(null);
            } else {
                if (tokens.isEmpty()) { // it's the first token
                    if (t.startsWith("\"")) {
                        // strip off leading quote
                        tokens.add(new Token<>(q.matcher(t).replaceAll("")));
                    } else {
                        tokens.add(new Token<>(t));
                    }
                } else {
                    if (t.endsWith("\"")) { // it's the last token
                        // strip off trailing quote
                        tokens.add(new Token<>(q.matcher(t).replaceAll("")));
                        break; // done parsing
                    }

                    tokens.add(new Token<>(t)); // no quotes to strip
                }
            }
        }

        return tokens;
    }


    /**
     * Convenience method for parsing a string of tokens separated by spaces.
     *
     * @param tokens The string of tokens to parse.
     * @see #parseTokens(Grammar, List, Category)
     */
    public Parse<String> parse(Grammar grammar, String tokens, Category seed)
            throws PepException {
        return parse(grammar, tokens, " ", seed);
    }

    /**
     * Convenience method for parsing a string of tokens separated by a
     * specified string.
     *
     * @param tokens    The string of tokens to parse.
     * @param separator The separator in the token string.
     * @see ParseTokens#parseAllRemainigTokens()
     * @see #parseTokens(Grammar, List, Category)
     */
    public Parse<String> parse(Grammar grammar, String tokens, String separator, Category seed)
            throws PepException {
        return parse(grammar, Arrays.asList(tokens.split(separator)), seed);
    }

    private <T> Parse<T> parse(Grammar grammar, List<T> ts, Category seed) throws PepException {
        List<Token<T>> tokens = ts.stream().map(Token::new).collect(Collectors.toList());
        return parseTokens(grammar, tokens, seed);
    }

    /**
     * Parses a string (list of tokens) using the specified grammar and
     * seed category.
     *
     * @param grammar The grammar to use in parsing.
     * @param tokens  The string to parse.
     * @param seed    The category to seed the parser with.
     */
    <T> Parse<T> parseTokens(Grammar grammar, List<Token<T>> tokens, Category seed)
            throws PepException {
        ParseTokens<T> earleyParser = new ParseTokens<>(grammar, seed, tokens, parserOptions, this);

        if (Pep.verbosity >= Pep.V_STATS) Pep.printMessage("Parsing " + tokens + " for category " + seed,
                Pep.V_STATS);

        return earleyParser.parseAllRemainigTokens();
    }

    /**
     * Consumes event generated when the parser is seeded.
     */
    public void parserSeeded(EdgeEvent edgeEvent) {
        Pep.printParser(edgeEvent.getIndex(), "seed     ", edgeEvent.edge);
    }

    /**
     * Consumes event generated when edges are added to the parser's chart
     * because of completion.
     */
    public void edgeCompleted(EdgeEvent edgeEvent) {
        Pep.printParser(edgeEvent.getIndex(), "complete", edgeEvent.edge);
    }

    /**
     * Consumes event generated when edges are added to the parser's chart
     * because of prediction.
     */
    public void edgePredicted(EdgeEvent edgeEvent) {
        Pep.printParser(edgeEvent.getIndex(), "predict ", edgeEvent.edge);
    }

    /**
     * Consumes event generated when the parser scans a token from the input
     * string.
     */
    public void edgeScanned(EdgeEvent edgeEvent) {
        Pep.printParser(edgeEvent.getIndex(), "scan    ", edgeEvent.edge);
    }

    /**
     * Consumes event generated when the parser completes a parse.
     */
    public void parseComplete(ParseEvent parseEvent) {
        long now = System.currentTimeMillis();
        Parse parse = parseEvent.parse;

        Pep.print("", Pep.V_CHART); // only if other output above
        Pep.print(parse, Pep.V_RECOGNITION);

        if (Pep.verbosity >= Pep.V_PARSE) {
            Set<ParseTree> parseTrees = parse.getParseTrees();
            if (!parseTrees.isEmpty()) {
                int count = 1;
                for (ParseTree pt : parseTrees) {
                    Pep.print(count++ + ". " + pt.toString(), Pep.V_PARSE);
                }
            }
        }

        if (Pep.verbosity >= Pep.V_STATS) {
            Pep.printMessage("", Pep.V_STATS);
            Pep.printMessage("Parse complete: " + parse.chart.countEdges()
                    + " edges added to chart in " + (now - lastParseStart)
                    + " ms", Pep.V_STATS);
        }
    }

    public void parseMessage(ParseEvent parseEvent, String message) {
        Pep.printParser(parseEvent.getIndex(), "message ", message);
    }

    @SuppressWarnings("unused")
    public void parseError(ParseErrorEvent parseErrorEvent)
            throws PepException {
        Pep.printParser(parseErrorEvent.getIndex(), "error   ",
                parseErrorEvent.cause);
    }

    /**
     * Parses XML grammar files.
     */
    static class GrammarParser implements ErrorHandler {
        File grammarFile;
        DocumentBuilder documentBuilder;

        /**
         * Create a grammar parser for the specified file.
         */
        GrammarParser(File grammarFile) throws PepException {
            this.grammarFile = grammarFile;

            try {
                documentBuilder
                        = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                documentBuilder.setErrorHandler(this);
            } catch (ParserConfigurationException pce) {
                throw new PepException("problem instantiating parser", pce);
            }
        }

        /**
         * Parse the XML file specified when this grammar parser was created,
         * returning a grammar.
         */
        Grammar parse() throws PepException {
            Grammar g = null;

            try {
                Document d = documentBuilder.parse(grammarFile);
                // validate
                SchemaFactory sf = SchemaFactory.newInstance(
                        XMLConstants.W3C_XML_SCHEMA_NS_URI);
                StreamSource src = new StreamSource(
                        getClass().getClassLoader().getResourceAsStream(
                                "META-INF/etc/grammar.xsd"));

                if (src.getInputStream() == null) {
                    Pep.printMessage("Unable to locate grammar.xsd", Pep.V_ALL);
                } else {
                    Schema schema = sf.newSchema(src);
                    Validator vlad = schema.newValidator();

                    try {
                        vlad.validate(new DOMSource(d));
                    } catch (SAXException se) {
                        throw new PepException("invalid grammar", se);
                    }
                }

                Element root = d.getDocumentElement();

                Grammar.Builder gb = new Grammar.Builder(root.getAttribute("name"));
                Pep.printMessage("Loading grammar " + g.name + " from "
                        + grammarFile.getPath(), Pep.V_GRAMMAR);

                NodeList rules = root.getElementsByTagName("rule");
                Element ruleEl;
                Category left;
                List<Category> right;
                for (int i = 0; i < rules.getLength(); i++) {
                    ruleEl = (Element) rules.item(i);
                    left = Category.nonTerminal(ruleEl.getAttribute("category"));

                    NodeList rightList = ruleEl.getChildNodes();
                    right = new ArrayList<>(rightList.getLength());

                    Node rightNode;
                    Element rightEl;
                    for (int j = 0; j < rightList.getLength(); j++) {
                        rightNode = rightList.item(j);
                        if (rightNode instanceof Element) {
                            rightEl = (Element) rightNode;
                            Attr termAttr
                                    = rightEl.getAttributeNode("terminal");

                            boolean terminal = termAttr != null && termAttr.getTextContent().equals("true");
                            String name = rightEl.getAttribute("name");
                            if (terminal) {
                                right.add((StringTerminal) name::equals);
                            } else {
                                right.add(Category.nonTerminal(name));
                            }
                        }
                    }

                    gb.addRule(new Rule(left,
                            right.toArray(new Category[right.size()])));
                }
                g = gb.build();
            } catch (IllegalArgumentException iae) {
                throw new PepException("problem loading grammar", iae);
            } catch (SAXException se) {
                throw new PepException("problem parsing", se);
            } catch (IOException io) {
                throw new PepException("problem reading grammar", io);
            }

            Pep.printMessage(g.toString(), Pep.V_GRAMMAR);
            Pep.printMessage("", Pep.V_GRAMMAR);

            return g;
        }

        /**
         * Does nothing because an exception are generated. Present for
         * binary compatibility with ErrorHandler.
         */
        @SuppressWarnings("unused")
        public void error(SAXParseException e) throws SAXException {
            // do nothing
        }

        /**
         * Does nothing because an exception are generated. Present for
         * binary compatibility with ErrorHandler.
         */
        @SuppressWarnings("unused")
        public void fatalError(SAXParseException e) throws SAXException {
            // do nothing
        }

        /**
         * Event handler for warnings that occur during parsing. Prints a
         * warning message to System.err.
         *
         * @see Pep#printWarning(String)
         */
        @SuppressWarnings("unused")
        public void warning(SAXParseException e) throws SAXException {
            Pep.printWarning("line " + e.getLineNumber() + ": "
                    + e.getMessage());
        }
    }

}
