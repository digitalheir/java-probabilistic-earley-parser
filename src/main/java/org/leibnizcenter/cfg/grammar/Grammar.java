
package org.leibnizcenter.cfg.grammar;

import org.leibnizcenter.cfg.algebra.matrix.Matrix;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsenstiveStringTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.rule.RuleFactory;
import org.leibnizcenter.cfg.util.MapEntry;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Represents a stochastic context-free grammar (set of production rules with probabilities).
 * <p>
 * Grammars maintain their rules indexed by
 * {@link Rule#getLeft() left side category}. The rule sets contained for
 * any given {@link Category left category} are not guaranteed to be
 * maintained in the order of insertion.
 * <p>
 * This class pre-computes all the left-relations for the non-terminals that occur
 * <p>
 * Once the Grammar is instantiated, it is immutable.
 */
public class Grammar<T> {
    private static final Pattern NEWLINE = Pattern.compile("\\n");
    private static final Pattern TRAILING_COMMENT = Pattern.compile("#.*$");
    @SuppressWarnings("WeakerAccess")
    public final String name;
    private final MyMultimap<Category, Rule> rules;
    /**
     * Two non-terminals X and Y are said to be in a left-corner relation
     * <code>X -L> Y</code> iff there exists a production for X that has a RHS starting with Y,
     * <code>X -> Y ...</code> . This relation is defined as the sum of the probabilities of
     * all such rules
     */
    private final LeftCorners leftCorners;
    /**
     * Reflexive, transitive closure of leftCorners, with the probabilities summed
     */
    private final LeftCorners leftStarCorners;
    /**
     * Reflexive, transitive closure of unit production relations, with the probabilities summed
     */
    private final LeftCorners unitStarScores;
    private final Set<NonTerminal> nonTerminals = new HashSet<>();
    private final Set<Terminal<T>> terminals = new HashSet<>();
    private final ExpressionSemiring semiring;
    private Map<Category, Set<Rule>> nonZeroLeftStartRules = new HashMap<>();

    /**
     * Creates a grammar with the given name, and given rules.
     * These restrictions ensure that
     * all nonterminals define probability measures over strings; i.e., P(X ~ x) is a proper distribution over x for all
     * X. Formal definitions of these conditions are given in Appendix A of An Efficient Probabilistic .
     *
     * @param name     The mnemonic name for this grammar.
     * @param rules    Rules for the grammar
     * @param semiring Semiring
     */
    public Grammar(String name, MyMultimap<Category, Rule> rules, ExpressionSemiring semiring) {
        this.name = name;
        this.rules = rules;

        getAllRules().forEach(rule -> {
            nonTerminals.add(rule.getLeft());
            for (Category c : rule.getRight())
                if (c instanceof Terminal)//noinspection unchecked
                    terminals.add((Terminal) c);
                else if (c instanceof NonTerminal) nonTerminals.add((NonTerminal) c);
                else throw new Error("This is a bug");
        });

        this.semiring = semiring;
        leftCorners = new LeftCorners(semiring);
        setLeftCorners();
        leftStarCorners = getReflexiveTransitiveClosure(semiring, nonTerminals, leftCorners);
        unitStarScores = getUnitStarCorners();

        nonTerminals.forEach(X -> {
            final Collection<Category> nonZeroScores = leftStarCorners.getNonZeroScores(X);
            if (nonZeroScores != null) {
                final Set<Rule> rulez = nonZeroScores.stream().flatMap(Y -> {
                    final Collection<Rule> rulesForY = getRules(Y);
                    return rulesForY == null ? Stream.empty() : rulesForY.stream();
                }).collect(Collectors.toSet());
                nonZeroLeftStartRules.put(X, rulez);
            }
        });
    }

    /**
     * Uses a trick to compute left*Corners (R_L), the reflexive transitive closure of leftCorners:
     * <p>
     * <code>R_L = I + P_L R_L = (I - P_L)^-1</code>
     */
    private static LeftCorners getReflexiveTransitiveClosure(DblSemiring semiring, Set<NonTerminal> nonTerminals, LeftCorners P) {
        NonTerminal[] nonterminalz = nonTerminals.toArray(new NonTerminal[nonTerminals.size()]);
        final Matrix R_L_inverse = new Matrix(nonTerminals.size(), nonTerminals.size());
        for (int row = 0; row < nonterminalz.length; row++) {
            NonTerminal X = nonterminalz[row];
            for (int col = 0; col < nonterminalz.length; col++) {
                NonTerminal Y = nonterminalz[col];
                final double prob = semiring.toProbability(P.get(X, Y));
                // I - P_L
                R_L_inverse.set(row, col, (row == col ? 1 : 0) - prob);
            }
        }
        final Matrix R_L = R_L_inverse.inverse();

        LeftCorners R__L = new LeftCorners(semiring);
        /*
         * Copy all matrix values into our {@link LeftCorners} object
         */
        IntStream.range(0, R_L.getRowDimension()).forEach(row ->
                IntStream.range(0, R_L.getColumnDimension()).forEach(col ->
                        R__L.set(nonterminalz[row], nonterminalz[col], semiring.fromProbability(R_L.get(row, col)))
                )
        );
        return R__L;
    }

    public static Grammar<String> parse(String str) {
        return parse(str, s -> Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsenstiveStringTerminal(s),
                LogSemiring.get());
    }

    public static Grammar<String> parse(String s, Function<String, Category> parseCategory, DblSemiring semiring) {
        Builder<String> b = new Builder<>();
        b.addRules(Arrays.stream(NEWLINE.split(s.trim()))
                .map(line -> TRAILING_COMMENT.matcher(line).replaceAll("").trim())
                .filter(line -> !line.isEmpty())
                .map(line -> Rule.parse(line, parseCategory, semiring)).collect(Collectors.toSet())
        );
        return b.build();
    }

    public static Grammar<String> parse(Path path, Charset charset) throws IOException {
        return parse(
                path,
                charset,
                s -> Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsenstiveStringTerminal(s),
                LogSemiring.get()
        );
    }

    public static Grammar<String> parse(Path path, Charset charset, Function<String, Category> parseCategory, DblSemiring semiring) throws IOException {
        Builder<String> b = new Builder<>();
        final Collection<Rule> rules = Files.lines(path, charset)
                .map(line -> TRAILING_COMMENT.matcher(line).replaceAll("").trim())
                .filter(line -> !line.isEmpty())
                .map(line -> Rule.parse(line, parseCategory, semiring)).collect(Collectors.toSet());
        b.addRules(rules);
        return b.build();
    }


    public static Grammar<String> parse(InputStream inputStream, Charset charset) throws IOException {
        return parse(inputStream, charset,
                s -> Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsenstiveStringTerminal(s),
                LogSemiring.get());
    }

    public static Grammar<String> parse(InputStream inputStream, Charset charset, Function<String, Category> parseCategory, DblSemiring semiring) throws IOException {
        Builder<String> b = new Builder<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        System.out.println("Reading File line by line using BufferedReader");

        String line = reader.readLine();
        Collection<Rule> rules = new HashSet<>();
        while (line != null) {
            line = TRAILING_COMMENT.matcher(line).replaceAll("").trim();
            if (!line.isEmpty())
                rules.add(Rule.parse(line, parseCategory, semiring));
            line = reader.readLine();
        }
        b.addRules(rules);
        return b.build();
    }

    public ExpressionSemiring getSemiring() {
        return semiring;
    }

    private LeftCorners getUnitStarCorners() {
        // Sum all probabilities for unit relations
        final LeftCorners P_U = new LeftCorners(semiring);
        nonTerminals.forEach(X -> {
            final Collection<Rule> rules = getRules(X);
            if (rules != null) rules.stream()
                    .filter(Rule::isUnitProduction)
//                        .map(rule -> Maps.immutableEntry(rule.getLeft(), rule.getRight()[0]))
//                        .distinct()
                    .forEach(Yrule -> P_U.plus(X, Yrule.getRight()[0], Yrule.getScore()));
        });

        // R_U = (I - P_U)
        return getReflexiveTransitiveClosure(semiring, nonTerminals, P_U);
    }

    //    /**
//     * Gets a singleton preterminal rule with the specified left category,
//     * producing the given string token.
//     *
//     * @param left  The left side of the preterminal rule.
//     * @param token The right side of the preterminal rule.
//     * @return A preterminal rule of the form <code>left -> token</code> if
//     * any exists within this grammar, or <code>null</code> if none exists.
//     * @see Rule#isSingletonPreterminal()
//     */
//    public Rule getSingletonPreterminal(Category left, Token token) {
//        if (rules.containsKey(left))
//            for (Rule r : rules.get(left))
//                if (r.isSingletonPreterminal() && Tokens.hasCategory(token, r.right[0]))
//                    return r;
//        return null;
//    }

    /**
     * Compute left corner relations
     */
    private void setLeftCorners() {
        // Sum all probabilities for left corners
        nonTerminals.forEach(X -> {
            final Collection<Rule> rules = getRules(X);
            if (rules != null) rules.stream()
                    .filter(yRule -> yRule.getRight().length > 0 && yRule.getRight()[0] instanceof NonTerminal)
                    .forEach(Yrule -> leftCorners.plus(X, Yrule.getRight()[0], Yrule.getScore()));
        });
    }

    /**
     * Tests whether this grammar contains rules for the specified left side
     * category.
     *
     * @param left The left category of the rules to test for.
     * @return <code>true</code> iff this grammar contains rules with the
     * specified category as their {@link Rule#getLeft() left side}.
     */
    public boolean containsRules(Category left) {
        return rules.containsKey(left);
    }

    /**
     * Gets the set of rules contained by this grammar with the given left
     * side category.
     *
     * @param LHS The {@link Rule#getLeft() left side} of the rules to find.
     * @return A set containing the rules in this grammar whose
     * {@link Rule#getLeft() left side} is
     * {@link Category#equals(Object) the same} as <code>left</code>, or
     * <code>null</code> if no such rules are contained in this grammar. The
     * rule set returned by this method is <em>not</em> guaranteed to contain
     * the rules in the order in which they were {@link Builder#addRule(Rule) added}.
     */
    public Collection<Rule> getRules(Category LHS) {
        return rules.get(LHS);
    }

    /**
     * Gets every rule in this grammar.
     */
    @SuppressWarnings("WeakerAccess")
    public Collection<Rule> getAllRules() {
        return rules.values();
    }

    public double getLeftStarScore(Category LHS, Category RHS) {
        return leftStarCorners.get(LHS, RHS);
    }

    /**
     * Gets a string representation of this grammar.
     *
     * @return A string listing all of the rules contained by this grammar.
     * @see Rule#toString()
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() +
                ' ' +
                name +
                ": {" +
                rules.values().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")) +
                "}]";
    }

    public int size() {
        return getAllRules().size();
    }

    public double getLeftScore(NonTerminal LHS, NonTerminal RHS) {
        return leftCorners.get(LHS, RHS);
    }

    @SuppressWarnings("unused")
    public LeftCorners getLeftCorners() {
        return leftCorners;
    }

    public LeftCorners getLeftStarCorners() {
        return leftStarCorners;
    }

    public double getUnitStarScore(Category LHS, NonTerminal RHS) {
        return unitStarScores.get(LHS, RHS);
    }

    @SuppressWarnings("unused")
    public Set<NonTerminal> getNonTerminals() {
        return nonTerminals;
    }

    public Set<Terminal<T>> getTerminals() {
        return terminals;
    }

    public LeftCorners getUnitStar() {
        return unitStarScores;
    }

    public Stream<MapEntry<State, Rule>> streamNonZeroLeftStarRulesWithPrecedingState(final State statePredecessor) {
        final Category Z = statePredecessor.getActiveCategory();
        return nonZeroLeftStartRules.get(Z)
                .stream()
                .map(Y_to_v -> new MapEntry<>(statePredecessor, Y_to_v));
    }


    public static class Builder<E> {
        private final MyMultimap<Category, Rule> rules;
        private String name;
        private ExpressionSemiring semiring = new LogSemiring();
        private RuleFactory rf = new RuleFactory(semiring);

        public Builder(String name) {
            this.name = name;
            this.rules = new MyMultimap<>();
        }

        public Builder() {
            this.rules = new MyMultimap<>();
        }

        public Builder<E> setSemiring(ExpressionSemiring semiring) {
            this.semiring = semiring;
            this.rf = new RuleFactory(semiring);
            return this;
        }

        public Builder<E> setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a production rule.
         *
         * @param rule The rule to add.
         * @throws NullPointerException If <code>rule</code> is <code>null</code>.
         */
        public Builder<E> addRule(Rule rule) {
            if (rule == null) throw new NullPointerException("null rule");
            rules.put(rule.left, rule);
            return this;
        }

        public Builder<E> addRule(double probability, NonTerminal left, Category... right) {
            return addRule(rf.newRule(probability, left, right));
        }

        public Builder<E> addRule(NonTerminal left, Category... right) {
            return addRule(rf.newRule(left, right));
        }

        public Grammar<E> build() {
            return new Grammar<>(name, rules, semiring);
        }

        @SuppressWarnings({"unused", "WeakerAccess"})
        public Builder<E> addRules(Collection<Rule> rules) {
            rules.forEach(this::addRule);
            return this;
        }
    }

}
