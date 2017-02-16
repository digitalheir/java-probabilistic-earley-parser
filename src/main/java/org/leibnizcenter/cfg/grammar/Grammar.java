
package org.leibnizcenter.cfg.grammar;

import org.leibnizcenter.cfg.algebra.matrix.Matrix;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.ErrorSection;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.CaseInsensitiveStringTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.rule.RuleFactory;
import org.leibnizcenter.cfg.rule.RuleParser;
import org.leibnizcenter.cfg.rule.SynchronizingRule;
import org.leibnizcenter.cfg.token.Token;
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
public final class Grammar<T> {
    private static final Function<String, Category> STRING_CATEGORY_FUNCTION = s -> ErrorSection.NAME.equals(s) ? new ErrorSection() : Character.isUpperCase(s.charAt(0)) ? new NonTerminal(s) : new CaseInsensitiveStringTerminal(s);
    private static final Pattern NEWLINE = Pattern.compile("\\n");
    private static final Pattern TRAILING_COMMENT = Pattern.compile("#.*$");
    @SuppressWarnings("WeakerAccess")
    public final String name;
    /**
     * Reflexive, transitive closure of unit production relations, with the probabilities summed
     */
    public final UnitStarScores unitStarScores;
    public final ExpressionSemiring semiring;
    public final AtomFactory atoms = new AtomFactory();
    public final Map<Category, Set<Rule>> nonZeroLeftStartRules = new HashMap<>();
    private final MyMultimap<NonTerminal, Rule> rules;
    private final MyMultimap<NonTerminal, Rule> synchronizingRules;
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
    private final Set<NonTerminal> nonTerminals = new HashSet<>();
    private final Set<Terminal<T>> terminals = new HashSet<>();
    private final Map<Token<T>, Set<Terminal<T>>> tokenToTerminalsCache = new HashMap<>();

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
    public Grammar(String name, MyMultimap<NonTerminal, Rule> rules, MyMultimap<NonTerminal, Rule> synchronizingRules, ExpressionSemiring semiring) {
        this.name = name;
        this.rules = rules;
        this.synchronizingRules = synchronizingRules != null ? synchronizingRules : new MyMultimap<>();
        rules.lock();
        getAllRules().forEach(rule -> {
            nonTerminals.add(rule.left);
            for (Category c : rule.right)
                if (c instanceof Terminal)//noinspection unchecked
                    terminals.add((Terminal) c);
                else if (c instanceof NonTerminal) nonTerminals.add((NonTerminal) c);
                else throw new Error("This is a bug");
        });

        this.semiring = semiring;
        leftCorners = new LeftCorners(semiring);
        setLeftCorners();
        leftStarCorners = getReflexiveTransitiveClosure(atoms, semiring, nonTerminals, leftCorners);
        unitStarScores = getUnitStarCorners();

        nonTerminals.forEach(Yy -> {
            final Collection<NonTerminal> nonZeroScores = leftStarCorners.getNonZeroScores(Yy);
            if (nonZeroScores != null) {
                final Set<Rule> ruleSet = nonZeroScores.stream().flatMap(Y -> {
                    final Collection<Rule> rulesForY = getRules(Y);
                    return rulesForY == null ? Stream.empty() : rulesForY.stream();
                }).collect(Collectors.toSet());
                nonZeroLeftStartRules.put(Yy, ruleSet);
            }
        });
    }

    /**
     * Uses a trick to compute left*Corners (R_L), the reflexive transitive closure of leftCorners:
     * <p>
     * <code>R_L = I + P_L R_L = (I - P_L)^-1</code>
     */
    private static LeftCorners getReflexiveTransitiveClosure(AtomFactory atoms, DblSemiring semiring, Set<NonTerminal> nonTerminals, LeftCorners P) {
        NonTerminal[] nonterminalsArr = nonTerminals.toArray(new NonTerminal[nonTerminals.size()]);
        final Matrix R_L_inverse = new Matrix(nonTerminals.size(), nonTerminals.size());
        for (int row = 0; row < nonterminalsArr.length; row++) {
            NonTerminal X = nonterminalsArr[row];
            for (int col = 0; col < nonterminalsArr.length; col++) {
                NonTerminal Y = nonterminalsArr[col];
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
                        R__L.set(nonterminalsArr[row], nonterminalsArr[col], semiring.fromProbability(R_L.get(row, col)))
                )
        );
        return R__L;
    }

    public static Grammar<String> fromString(String str) {
        return fromString(str, STRING_CATEGORY_FUNCTION,
                LogSemiring.get());
    }

    public static Grammar<String> fromString(String s, Function<String, Category> parseCategory, DblSemiring semiring) {
        Builder<String> b = new Builder<>();

        RuleParser parser = new RuleParser(parseCategory, semiring);
        b.addRules(Arrays.stream(NEWLINE.split(s.trim()))
                .map(line -> TRAILING_COMMENT.matcher(line).replaceAll("").trim())
                .filter(line -> !line.isEmpty())
                .map(parser::fromString).collect(Collectors.toSet())
        );
        return b.build();
    }

    public static Grammar<String> fromString(Path path, Charset charset) throws IOException {
        return fromString(
                path,
                charset,
                STRING_CATEGORY_FUNCTION,
                LogSemiring.get()
        );
    }

    public static Grammar<String> fromString(Path path, Charset charset, Function<String, Category> parseCategory, DblSemiring semiring) throws IOException {
        Builder<String> b = new Builder<>();
        RuleParser ruleParser = new RuleParser(parseCategory, semiring);
        final Collection<Rule> rules = Files.lines(path, charset).parallel()
                .map(line -> TRAILING_COMMENT.matcher(line).replaceAll("").trim())
                .filter(line -> !line.isEmpty())
                .map(ruleParser::fromString)
                .collect(Collectors.toSet());
        b.addRules(rules);
        return b.build();
    }


    public static Grammar<String> fromString(InputStream inputStream, Charset charset) throws IOException {
        return fromString(
                inputStream,
                charset,
                STRING_CATEGORY_FUNCTION,
                LogSemiring.get());
    }

    public static Grammar<String> fromString(InputStream inputStream, Charset charset, Function<String, Category> parseCategory, DblSemiring semiring) throws IOException {
        Builder<String> b = new Builder<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
        RuleParser ruleParser = new RuleParser(parseCategory, semiring);

        String line = reader.readLine();
        Collection<Rule> rules = new HashSet<>();
        while (line != null) {
            line = TRAILING_COMMENT.matcher(line).replaceAll("").trim();
            if (!line.isEmpty())
                rules.add(ruleParser.fromString(line));
            line = reader.readLine();
        }
        b.addRules(rules);
        return b.build();
    }

    public Collection<Rule> getSynchronizingRules(NonTerminal left) {
        return synchronizingRules.get(left);
    }

    private UnitStarScores getUnitStarCorners() {
        // Sum all probabilities for unit relations
        final LeftCorners P_U = new LeftCorners(semiring);
        nonTerminals.forEach(nonTerminal -> {
            final Collection<Rule> rulesForNonTerminal = getRules(nonTerminal);
            if (rulesForNonTerminal != null) rulesForNonTerminal.stream()
                    .filter(Rule::isUnitProduction)
                    .forEach(Yrule -> P_U.plus(nonTerminal, (NonTerminal) Yrule.right[0], Yrule.getScore()));
        });

        // R_U = (I - P_U)
        return new UnitStarScores(getReflexiveTransitiveClosure(atoms, semiring, nonTerminals, P_U), atoms, semiring);
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
        nonTerminals.forEach(leftHandSide -> {
            final Collection<Rule> rulesOnNonTerminal = getRules(leftHandSide);
            if (rulesOnNonTerminal != null) rulesOnNonTerminal.stream()
                    .filter(yRule -> yRule.right.length > 0 && yRule.right[0] instanceof NonTerminal)
                    .forEach(YRule -> leftCorners.plus(leftHandSide, (NonTerminal) YRule.right[0], YRule.getScore()));
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
    public boolean containsRules(NonTerminal left) {
        return rules.containsKey(left);
    }

    /**
     * Gets the set of rules contained by this grammar with the given left
     * side category.
     *
     * @param LHS The {@link Rule#getLeft() left side} of the rules to find.
     * @return A set containing the rules in this grammar whose
     * {@link Rule#getLeft() left side} is
     * the same as <code>left</code>, or
     * <code>null</code> if no such rules are contained in this grammar. The
     * rule set returned by this method is <em>not</em> guaranteed to contain
     * the rules in the order in which they were {@link Builder#addRule(Rule) added}.
     */
    public Collection<Rule> getRules(NonTerminal LHS) {
        return rules.get(LHS);
    }

    /**
     * Gets every rule in this grammar.
     */
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
        return name +
                ": {\n" +
                Stream.concat(
                        rules.values().stream(),
                        synchronizingRules.values().stream()
                )
                        .map(s -> "  " + s.toString())
                        .collect(Collectors.joining(",\n")) +
                "\n}";
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

    @SuppressWarnings("unused")
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


    public Stream<MapEntry<State, Rule>> streamNonZeroLeftStarRulesWithPrecedingState(final State statePredecessor) {
        final Category Z = statePredecessor.getActiveCategory();
        return nonZeroLeftStartRules.get(Z)
                .stream()
                .map(Y_to_v -> new MapEntry<>(statePredecessor, Y_to_v));
    }

    /**
     * Runs in O(N) for N is the number of terminals. Caches tokens on {@link Token#equals(Object)} to make subsequent
     * calls potentially quicker.
     *
     * @return set of all terminals that match given token, usually a singleton set.
     */
    public Set<Terminal<T>> getCategories(Token<T> token) {
        if (!tokenToTerminalsCache.containsKey(token))
            tokenToTerminalsCache.put(token,
                    terminals.stream().filter(category -> category.hasCategory(token)).collect(Collectors.toSet())
            );
        return tokenToTerminalsCache.get(token);
    }


    public static class Builder<E> {
        private final MyMultimap<NonTerminal, Rule> rules = new MyMultimap<>();
        private final MyMultimap<NonTerminal, Rule> synchronizingRules = new MyMultimap<>();
        private String name;
        private ExpressionSemiring semiring = LogSemiring.get();
        private RuleFactory rf = new RuleFactory(semiring);

        public Builder(String name) {
            this.name = name;
        }

        public Builder() {
            this.name = null;
        }

        public Builder<E> setSemiring(ExpressionSemiring semiring) {
            this.semiring = semiring;
            this.rf = new RuleFactory(semiring);
            return this;
        }

        @SuppressWarnings("unused")
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
            if (rule instanceof SynchronizingRule) {
                synchronizingRules.put(rule.left, rule);
            } else {
                rules.put(rule.left, rule);
            }
            return this;
        }

        public Builder<E> addRule(double probability, NonTerminal left, Category... right) {
            return addRule(rf.newRule(probability, left, right));
        }

        public Builder<E> addRule(NonTerminal left, Category... right) {
            return addRule(rf.newRule(left, right));
        }

        public Grammar<E> build() {
            return new Grammar<>(name, rules, synchronizingRules, semiring);
        }

        @SuppressWarnings({"unused", "WeakerAccess"})
        public Builder<E> addRules(Collection<Rule> rules) {
            rules.forEach(this::addRule);
            return this;
        }
    }

}
