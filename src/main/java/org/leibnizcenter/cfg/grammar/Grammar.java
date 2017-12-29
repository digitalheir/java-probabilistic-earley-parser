
package org.leibnizcenter.cfg.grammar;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.rule.RuleFactory;
import org.leibnizcenter.cfg.rule.RuleParser;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.leibnizcenter.cfg.grammar.GrammarAnalysis.computeUnitStarCorners;
import static org.leibnizcenter.cfg.grammar.GrammarAnalysis.findNonZeroLeftStartRules;
import static org.leibnizcenter.cfg.grammar.GrammarAnalysis.computeReflexiveTransitiveClosure;
import static org.leibnizcenter.cfg.grammar.GrammarParser.STRING_CATEGORY_FUNCTION;
import static org.leibnizcenter.cfg.grammar.GrammarParser.TRAILING_COMMENT;


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
    @SuppressWarnings("WeakerAccess")
    public final String name;
    /**
     * Reflexive, transitive closure of unit production relations, with the probabilities summed
     */
    public final ScoresAsSemiringElements unitStarScores;
    public final ExpressionSemiring semiring;
    public final AtomFactory atoms = new AtomFactory();
    public final Map<Category, Set<Rule>> nonZeroLeftStartRules;
    public final Set<Terminal<T>> terminals = new HashSet<>();
    private final MyMultimap<NonTerminal, Rule> rules;
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
    private final ScoresAsSemiringElements leftStarCornersAsSemiringElements;

    private final Set<NonTerminal> nonTerminals = new HashSet<>();
    private final Map<Token<T>, Set<Terminal<T>>> tokenToTerminalsCache = new WeakHashMap<>();

    /**
     * Creates a grammar with the given name, and given rules.
     * These restrictions ensure that
     * all nonterminals define probability measures over strings; i.e., P(X ~ x) is a proper distribution over x for all
     * X. Formal definitions of these conditions are given in Appendix A of An Efficient Probabilistic .
     *
     * @param name     The mnemonic name for this grammar.
     * @param rules_   Rules for the grammar
     * @param semiring Semiring
     */
    public Grammar(final String name, final MyMultimap<NonTerminal, Rule> rules_, final ExpressionSemiring semiring) {
        this.name = name;
        this.rules = rules_;
        this.semiring = semiring;

        rules.lock();

        collectTerminalsAndNonTerminals(rules.values());
        final NonTerminal[] nonTerminalsArr = nonTerminals.toArray(new NonTerminal[nonTerminals.size()]);


        leftCorners = new LeftCorners(nonTerminals, rules);
        final LeftCorners leftStarCorners = computeReflexiveTransitiveClosure(leftCorners, nonTerminalsArr);

        leftStarCornersAsSemiringElements = new ScoresAsSemiringElements(leftStarCorners, semiring);
        unitStarScores = new ScoresAsSemiringElements(computeUnitStarCorners(this.rules, nonTerminalsArr), this.semiring);
        nonZeroLeftStartRules = findNonZeroLeftStartRules(leftStarCorners, nonTerminals, rules);

    }

    @SuppressWarnings("unchecked")
    private void collectTerminalsAndNonTerminals(final Collection<Rule> rules) {
        rules.forEach(rule -> {
            nonTerminals.add(rule.left);
            for (final Category c : rule.right)
                if (c instanceof Terminal) terminals.add((Terminal) c);
                else if (c instanceof NonTerminal) nonTerminals.add((NonTerminal) c);
                else throw new Error("This is a bug");
        });
    }

    @SuppressWarnings("SameParameterValue")
    public static Grammar<String> fromString(final String str) {
        return fromString(str, STRING_CATEGORY_FUNCTION, LogSemiring.get());
    }

    @SuppressWarnings("WeakerAccess")
    public static Grammar<String> fromString(final String s, final Function<String, Category> parseCategory, final DblSemiring semiring) {
        final RuleParser parser = new RuleParser(parseCategory, semiring);
        return new Builder<String>().addRules(Arrays.stream(GrammarParser.NEWLINE.split(s.trim()))
                .map(line -> TRAILING_COMMENT.matcher(line).replaceAll("").trim())
                .filter(line -> !line.isEmpty())
                .map(parser::fromString)
                .collect(Collectors.toSet())).build();
    }

    public static Grammar<String> fromString(final Path path, final Charset charset) throws IOException {
        return fromString(path, charset, STRING_CATEGORY_FUNCTION, LogSemiring.get());
    }

    @SuppressWarnings("WeakerAccess")
    public static Grammar<String> fromString(final Path path, final Charset charset, final Function<String, Category> parseCategory, final DblSemiring semiring) throws IOException {
        final RuleParser ruleParser = new RuleParser(parseCategory, semiring);
        return new Builder<String>().addRules(Files.lines(path, charset).parallel()
                .map(line -> TRAILING_COMMENT.matcher(line).replaceAll("").trim())
                .filter(line -> !line.isEmpty())
                .map(ruleParser::fromString)
                .collect(Collectors.toSet())).build();
    }


    @SuppressWarnings("unused")
    public static Grammar<String> fromString(final InputStream inputStream, final Charset charset) throws IOException {
        return fromString(inputStream, charset, STRING_CATEGORY_FUNCTION, LogSemiring.get());
    }

    @SuppressWarnings("WeakerAccess")
    public static Grammar<String> fromString(final InputStream inputStream, final Charset charset, final Function<String, Category> parseCategory, final DblSemiring semiring) throws IOException {
        return GrammarParser.fromString(inputStream, charset, parseCategory, semiring);
    }

    //    public Collection<Rule> getSynchronizingRules(NonTerminal left) {
//        return lexicalErrorRules.get(left);
//    }

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
     * Tests whether this grammar contains rules for the specified left side
     * category.
     *
     * @param left The left category of the rules to test for.
     * @return <code>true</code> iff this grammar contains rules with the
     * specified category as their {@link Rule#getLeft() left side}.
     */
    public boolean containsRules(final NonTerminal left) {
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
    public Collection<Rule> getRules(final NonTerminal LHS) {
        return rules.get(LHS);
    }

    /**
     * Gets every rule in this grammar.
     */
    @SuppressWarnings("WeakerAccess")
    public Collection<Rule> getAllRules() {
        return rules.values();
    }

    public double getLeftStarScore(final Category LHS, final Category RHS) {
        return leftStarCornersAsSemiringElements.get(LHS, RHS);
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
//                Stream.concat(
                rules.values().stream()
                        //,lexicalErrorRules.values().stream()
//                )
                        .map(s -> "  " + s.toString())
                        .collect(Collectors.joining(",\n")) +
                "\n}";
    }

    public int size() {
        return getAllRules().size();
    }

    public double getLeftScore(final NonTerminal LHS, final NonTerminal RHS) {
        return leftCorners.getProbability(LHS, RHS);
    }

    public double getUnitStarScore(final Category LHS, final NonTerminal RHS) {
        return unitStarScores.get(LHS, RHS);
    }

    @SuppressWarnings("unused")
    public Set<NonTerminal> getNonTerminals() {
        return nonTerminals;
    }


//    public Stream<MapEntry<State, Rule>> streamNonZeroLeftStarRulesWithPrecedingState(final State statePredecessor) {
//        final Category Z = statePredecessor.getActiveCategory();
//        return nonZeroLeftStartRules.get(Z)
//                .stream()
//                .map(Y_to_v -> new MapEntry<>(statePredecessor, Y_to_v));
//    }

    /**
     * Runs in O(N) for N is the number of terminals. Weakly caches tokens on {@link Token#equals(Object)} to make subsequent
     * calls potentially quicker.
     *
     * @return set of all terminals that match given token, usually a singleton set.
     */
    public Set<Terminal<T>> getCategories(final Token<T> token) {
        return tokenToTerminalsCache.computeIfAbsent(
                token,
                ignored -> this.terminals.stream()
                        .filter(category -> !(category instanceof NonLexicalToken))
                        .filter(category -> category.hasCategory(token))
                        .collect(Collectors.toSet())
        );
    }


    public static class Builder<E> {
        private final MyMultimap<NonTerminal, Rule> rules = new MyMultimap<>();
        private String name;
        private ExpressionSemiring semiring = LogSemiring.get();
        private RuleFactory rf = new RuleFactory(semiring);

        public Builder(final String name) {
            this.name = name;
        }

        public Builder() {
            this.name = null;
        }

        public Builder<E> withSemiring(final ExpressionSemiring semiring) {
            this.semiring = semiring;
            this.rf = new RuleFactory(semiring);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<E> setName(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a production rule.
         *
         * @param rule The rule to add.
         * @throws NullPointerException If <code>rule</code> is <code>null</code>.
         */
        public Builder<E> addRule(final Rule rule) {
            if (rule == null) throw new NullPointerException("null rule");
//            if (rule instanceof LexicalErrorRule) {
//                lexicalErrorRules.put(rule.left, rule);
//            } else {
            rules.put(rule.left, rule);
//            }
            return this;
        }

        public Builder<E> addRule(final double probability, final NonTerminal left, final Category... right) {
            return addRule(rf.newRule(probability, left, right));
        }

        public Builder<E> addRule(final NonTerminal left, final Category... right) {
            return addRule(rf.newRule(left, right));
        }

        /**
         * Ensures grammar is proper, i.e. the sum of production probabilities should equal 1.0 for each producible category
         */
        private MyMultimap<NonTerminal, Rule> normalizeRuleWeights(final MyMultimap<NonTerminal, Rule> rules, final DblSemiring semiring) {
            final MyMultimap<NonTerminal, Rule> newRuleMap = new MyMultimap<>();
            for (final Map.Entry<NonTerminal, Set<Rule>> entry : rules.entries()) {
                final Set<Rule> rulesForCategory = entry.getValue();
                final double probabilitySum = rulesForCategory.stream().mapToDouble(r -> r.probability).reduce(Double::sum).orElseThrow(IllegalStateException::new);
                if (probabilitySum != 1.0) {
                    final Collection<Rule> newRules = new ArrayList<>(rulesForCategory.size());
                    rulesForCategory.forEach(r -> newRules.add(Rule.create(semiring, r.probability / probabilitySum, r.left, r.right)));
                    newRuleMap.putAll(entry.getKey(), newRules);
                } else newRuleMap.putAll(entry.getKey(), rulesForCategory);
            }
            return newRuleMap;
        }

        public Grammar<E> build() {
            return build(true);
        }

        public Grammar<E> build(final boolean makeProper) {
            return new Grammar<>(name, makeProper ? normalizeRuleWeights(rules, semiring) : rules, semiring);
        }

        @SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
        public Builder<E> addRules(final Collection<Rule> rules) {
            rules.forEach(this::addRule);
            return this;
        }
    }
}
