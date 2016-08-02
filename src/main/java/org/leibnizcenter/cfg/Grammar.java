
package org.leibnizcenter.cfg;

import Jama.Matrix;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.rule.RuleFactory;
import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.semiring.dbl.ProbabilitySemiring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
public class Grammar {
    public final String name;
    public final ImmutableMultimap<Category, Rule> rules;

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

    private final Set<NonTerminal> nonTerminals;
    private final DblSemiring semiring;

    /**
     * Creates a grammar with the given name, and given rules.
     * <p>
     * <p>
     * TODO: Ensure that the probabilities in a SCFG are proper and consistent as defined in Booth and Thompson (1973), and that the grammar contains no useless nonterminals (ones that can never appear in a derivation).
     * TODO: check that no rules are doubled with different probabilities (in which case we either have undefined dehaviour or conflate the rules?)
     * These restrictions ensure that
     * all nonterminals define probability measures over strings; i.e., P(X ~ x) is a proper distribution over x for all
     * X. Formal definitions of these conditions are given in Appendix A of An Efficient Probabilistic .
     *
     * @param name     The mnemonic name for this grammar.
     * @param rules    Rules for the grammar
     * @param semiring Semiring
     */
    public Grammar(String name, ImmutableMultimap<Category, Rule> rules, DblSemiring semiring) {
        this.name = name;
        this.rules = rules;
        this.nonTerminals = getAllRules().stream()
                .map(Rule::getLeft)
                .distinct().collect(Collectors.toSet());
        this.semiring = semiring;
        leftCorners = new LeftCorners(semiring);
        setLeftCorners();
        leftStarCorners = getReflexiveTransitiveClosure(semiring, nonTerminals, leftCorners);
        unitStarScores = getUnitStarCorners();
    }

    /**
     * Uses a trick to compute left*Corners (R_L), the reflexive transitive closure of leftCorners:
     * <p>
     * <code>R_L = I + P_L R_L = (I - P_L)^-1</code>
     */
    private static LeftCorners getReflexiveTransitiveClosure(DblSemiring semiring, Set<NonTerminal> nonTerminals, LeftCorners P) {
        // TODO make this method robust to any semiring, instead of converting to/from common probability and risking arithm underflow
        NonTerminal[] nonterminalz = nonTerminals.toArray(new NonTerminal[nonTerminals.size()]);
        final Matrix R_L_inverse = new Matrix(nonTerminals.size(), nonTerminals.size());
        for (int row = 0; row < nonterminalz.length; row++) {
            NonTerminal X = nonterminalz[row];
            for (int col = 0; col < nonterminalz.length; col++) {
                NonTerminal Y = nonterminalz[col];
                final double prob = semiring.toProbability(P.get(X, Y));
//                if(prob != 1.0 && prob != 0.0)
//                    System.out.println(prob);
                // I - P_L
                R_L_inverse.set(row, col, (row == col ? 1 : 0) - prob);
            }
        }
        final Matrix R_L = R_L_inverse.inverse();

        LeftCorners R__L = new LeftCorners(semiring);
        /**
         * Copy all matrix values into our {@link LeftCorners} object
         */
        IntStream.range(0, R_L.getRowDimension()).forEach(row ->
                IntStream.range(0, R_L.getColumnDimension()).forEach(col ->
                        R__L.set(nonterminalz[row], nonterminalz[col], semiring.fromProbability(R_L.get(row, col)))
                )
        );
        return R__L;
    }

    public DblSemiring getSemiring() {
        return semiring;
    }

    private LeftCorners getUnitStarCorners() {
        // Sum all probabilities for unit relations
        final LeftCorners P_U = new LeftCorners(semiring);
        nonTerminals.stream()
                .forEach(X -> getRules(X).stream()
                        .filter(Rule::isUnitProduction)
//                        .map(rule -> Maps.immutableEntry(rule.getLeft(), rule.getRight()[0]))
//                        .distinct()
                        .forEach(Yrule -> {
                            System.out.println(X + " -> " + Yrule.getRight()[0] + ": " + Yrule.getProbability());
                            P_U.plus(X, Yrule.getRight()[0], Yrule.getProbability());
                        }));

        // R_U = (I - P_U)
        return getReflexiveTransitiveClosure(semiring, nonTerminals, P_U);
    }

    /**
     * Compute left corner relations
     */
    private void setLeftCorners() {
        // Sum all probabilities for left corners
        nonTerminals.stream()
                .forEach(X -> getRules(X).stream()
                        .filter(yRule -> yRule.getRight().length > 0 && yRule.getRight()[0] instanceof NonTerminal)
                        .forEach(Yrule -> leftCorners.plus(X, Yrule.getRight()[0], Yrule.getProbability())));
    }

    //TODO
//    isProper(){
//    }
//    isConsistent(){
//    }
//    hasNoUselessNonTerminals(){
//
//    }

    /**
     * Gets the name of this grammar.
     *
     * @return The value specified when this grammar was created.
     */
    public String getName() {
        return name;
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
    public Collection<Rule> getAllRules() {
        return rules.values();
    }

    public double getLeftStarScore(Category LHS, Category RHS) {
        return leftStarCorners.get(LHS, RHS);
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
                rules.entries().stream()
                        .map(Map.Entry::getValue)
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

    public LeftCorners getLeftCorners() {
        return leftCorners;
    }

    public LeftCorners getLeftStarCorners() {
        return leftStarCorners;
    }

    public double getUnitStarScore(Category LHS, NonTerminal RHS) {
        return unitStarScores.get(LHS, RHS);
    }

    public static class Builder {
        private final ImmutableMultimap.Builder<Category, Rule> rules;
        private String name;
        private DblSemiring semiring = new ProbabilitySemiring();
        private RuleFactory rf = new RuleFactory(semiring);

        public Builder(String name) {
            this.name = name;
            this.rules = new ImmutableMultimap.Builder<>();
        }

        public Builder() {
            this.rules = new ImmutableMultimap.Builder<>();
        }

        public Builder setSemiring(DblSemiring semiring) {
            this.semiring = semiring;
            this.rf = new RuleFactory(semiring);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a production rule.
         *
         * @param rule The rule to add.
         * @throws NullPointerException If <code>rule</code> is <code>null</code>.
         */
        public Builder addRule(Rule rule) {
            if (rule == null) throw new NullPointerException("null rule");
            rules.put(rule.left, rule);
            return this;
        }

        public Builder addRule(double probability, NonTerminal left, Category... right) {
            return addRule(rf.newRule(probability, left, right));
        }

        public Builder addRule(NonTerminal left, Category... right) {
            return addRule(rf.newRule(left, right));
        }

        public Grammar build() {
            return new Grammar(name, rules.build(), semiring);
        }

        @SuppressWarnings("unused")
        public Builder addRules(Collection<Rule> rules) {
            rules.forEach(this::addRule);
            return this;
        }
    }

    /**
     * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
     * to {@link Category} with some utility functions to deal with probabilities.
     */
    public static class LeftCorners {
        private Map<Category, TObjectDoubleMap<Category>> map = new HashMap<>();
        private Multimap<Category, Category> nonZeroScores = HashMultimap.create();
        private DblSemiring semiring;

        /**
         * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
         * to {@link Category} with some utility functions to deal with probabilities.
         */
        LeftCorners(DblSemiring semiring) {
            this.semiring = semiring;
        }


        /**
         * Adds the given number to the current value of [X, Y]
         *
         * @param x           Left hand side
         * @param y           Right hand side
         * @param probability number to add
         */
        void plus(Category x, Category y, double probability) {
            TObjectDoubleMap<Category> yToProb = getYToProb(x);
            final double newProbability = semiring.plus(yToProb.get(y)/*defaults to zero*/, probability);
            if (Double.isNaN(newProbability))
                throw new Error();
            set(x, y, yToProb, newProbability);
        }

        /**
         * @return stored value in left-corner relationship. zero by default
         */
        public double get(Category x, Category y) {
            return getYToProb(x).get(y)/*defaults to zero*/;
        }

        /**
         * Will instantiate empty map if it does not exist yet.
         *
         * @param x LHS
         * @return map for given LHS.
         */
        private TObjectDoubleMap<Category> getYToProb(Category x) {
            if (map.containsKey(x)) return map.get(x);
            else {
                TObjectDoubleMap<Category> yToProb = (new TObjectDoubleHashMap<>(10, 0.5F, semiring.zero()));
                map.put(x, yToProb);
                return yToProb;
            }
        }

        /**
         * Sets table entry to a given probability. Will instantiate empty map if it does not exist yet.
         *
         * @param x   LHS
         * @param y   RHS
         * @param val Value to set table entry to
         */
        public void set(NonTerminal x, NonTerminal y, final double val) {
            TObjectDoubleMap<Category> yToProb = getYToProb(x);
            set(x, y, yToProb, val);
        }

        public Collection<Category> getNonZeroScores(Category X) {
            return nonZeroScores.get(X);
        }

        private void set(Category x, Category y, TObjectDoubleMap<Category> yToProb, double val) {
            if (val != semiring.zero()) nonZeroScores.put(x, y);
            yToProb.put(y, val);
        }
    }
}
