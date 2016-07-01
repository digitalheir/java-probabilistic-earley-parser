
package org.leibnizcenter.cfg;

import Jama.Matrix;
import com.google.common.collect.ImmutableMultimap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Represents a context-free grammar (set of production rules).
 * <p>
 * Grammars maintain their rules indexed by
 * {@link Rule#getLeft() left side category}. The rule sets contained for
 * any given {@link Category left category} are not guaranteed to be
 * maintained in the order of insertion.
 * <p>
 * Once the Grammar is instantiated, it is immutable.
 */
public class Grammar {
    public final String name;
    public final ImmutableMultimap<Category, Rule> rules;
    private final LeftCornerRelation leftCorners = new LeftCornerRelation();

    /**
     * Reflexive, transitive closure of leftCorners, with the probabilities summed
     */
    private final LeftCornerRelation leftStarCorners = new LeftCornerRelation();

    /**
     * Creates a grammar with the given name, and given rules.
     *
     * @param name  The mnemonic name for this grammar.
     * @param rules Rules for the grammar
     */
    public Grammar(String name, ImmutableMultimap<Category, Rule> rules) {
        this.name = name;
        this.rules = rules;

        // Compute left corners
        Collection<NonTerminal> nonTerminals = getAllRules().stream()
                .map(Rule::getLeft)
                .distinct().collect(Collectors.toSet());

        nonTerminals.stream()
                .forEach(X -> getRules(X).stream()
                        .filter(yRule -> yRule.getRight().length > 0 && yRule.getRight()[0] instanceof NonTerminal)
                        .forEach(Yrule -> leftCorners.plus(X, Yrule.getRight()[0], Yrule.getProbability())));


        /**
         * R_L = I + P_L R_L = (I - P_L)^-1
         */
        NonTerminal[] nonterminalz = nonTerminals.toArray(new NonTerminal[nonTerminals.size()]);
        Matrix matrix = new Matrix(nonTerminals.size(), nonTerminals.size());
        for (int row = 0; row < nonterminalz.length; row++) {
            NonTerminal X = nonterminalz[row];
            for (int col = 0; col < nonterminalz.length; col++) {
                NonTerminal Y = nonterminalz[col];
                final double prob = leftCorners.get(X, Y);
                matrix.set(row, col, (row == col ? 1 : 0) - prob);
            }
        }
        matrix = matrix.inverse();
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            for (int col = 0; col < matrix.getColumnDimension(); row++) {
                leftStarCorners.set(nonterminalz[row], nonterminalz[col], matrix.get(row, col));
            }
        }
        System.out.println(leftCorners);
    }

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
     * @param left The {@link Rule#getLeft() left side} of the rules to find.
     * @return A set containing the rules in this grammar whose
     * {@link Rule#getLeft() left side} is
     * {@link Category#equals(Object) the same} as <code>left</code>, or
     * <code>null</code> if no such rules are contained in this grammar. The
     * rule set returned by this method is <em>not</em> guaranteed to contain
     * the rules in the order in which they were {@link Builder#addRule(Rule) added}.
     */
    public Collection<Rule> getRules(Category left) {
        return rules.get(left);
    }

    /**
     * Gets every rule in this grammar.
     */
    public Collection<Rule> getAllRules() {
        return rules.values();
    }

    public double getLeftStarScore(Category left, Category right) {
        return leftStarCorners.get(left, right);
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

    public static class Builder {
        private final ImmutableMultimap.Builder<Category, Rule> rules;
        private String name;

        public Builder(String name) {
            this.name = name;
            this.rules = new ImmutableMultimap.Builder<>();
        }

        public Builder() {
            this.rules = new ImmutableMultimap.Builder<>();
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
            return addRule(new Rule(probability, left, right));
        }

        public Builder addRule(NonTerminal left, Category... right) {
            return addRule(new Rule(left, right));
        }

        public Grammar build() {
            return new Grammar(name, rules.build());
        }

        @SuppressWarnings("unused")
        public Builder addRules(Collection<Rule> rules) {
            rules.forEach(this::addRule);
            return this;
        }
    }

    /**
     * Two nonterminals X and Y are said to be in a left-corner relation
     * X -L> Y iff there exists a production for X that has a RHS starting with Y,
     * X -> Y ... . This relation is defined as the sum of the probabilities of
     * all such rules
     */
    private static class LeftCornerRelation {
        private Map<Category, TObjectDoubleMap<Category>> map = new HashMap<>();


        public LeftCornerRelation() {
        }


        public void plus(Category x, Category y, double probability) {
            TObjectDoubleMap<Category> yToProb = getYToProb(x);

            final double newProbability = yToProb.get(y)/*defaults to 0.0*/ + probability;
            yToProb.put(y, newProbability);
        }

        /**
         * @return stored value in left-corner relationship, 0.0 by default
         */
        public double get(Category x, Category y) {
            TObjectDoubleMap<Category> yToProb = getYToProb(x);

            return yToProb.get(y)/*defaults to 0.0*/;
        }

        private TObjectDoubleMap<Category> getYToProb(Category x) {
            TObjectDoubleMap<Category> yToProb;
            if (!map.containsKey(x)) {
                yToProb = (new TObjectDoubleHashMap<>(10, 0.5F, 0.0));
                map.put(x, yToProb);
            } else yToProb = map.get(x);
            return yToProb;
        }

        public void set(NonTerminal x, NonTerminal y, final double val) {
            TObjectDoubleMap<Category> yToProb = getYToProb(x);
            yToProb.put(y, val);
        }
    }
}
