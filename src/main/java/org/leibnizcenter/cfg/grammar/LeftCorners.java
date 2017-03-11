package org.leibnizcenter.cfg.grammar;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
 * to {@link Category} with some utility functions to deal with probabilities.
 */
public class LeftCorners {
    final Map<Category, TObjectDoubleMap<Category>> mapToSemiringElements = new HashMap<>();
    final MyMultimap<NonTerminal, NonTerminal> nonZeroScores = new MyMultimap<>();
    private final Map<Category, TObjectDoubleMap<Category>> mapToProb = new HashMap<>();
    private final DblSemiring semiring;

    /**
     * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
     * to {@link Category} with some utility functions to deal with probabilities.
     */
    LeftCorners(DblSemiring semiring) {
        this.semiring = semiring;
    }

    /**
     * Compute left corner relations
     */
    LeftCorners(ExpressionSemiring semiring, Set<NonTerminal> nonTerminals, MyMultimap<NonTerminal, Rule> rules) {
        this(semiring);
        // Sum all probabilities for left corners
        nonTerminals.forEach(leftHandSide -> {
            final Collection<Rule> rulesOnNonTerminal = rules.get(leftHandSide);
            if (rulesOnNonTerminal != null) rulesOnNonTerminal.stream()
                    .filter(yRule -> yRule.right.length > 0 && yRule.right[0] instanceof NonTerminal)
                    .forEach(YRule -> plusProbability(leftHandSide, (NonTerminal) YRule.right[0], YRule.probability));
        });
    }


    private void plusProbability(NonTerminal x, NonTerminal y, double probability) {
        setPlusSemiringElement(x, y, semiring.fromProbability(probability));
        setPlusProbability(x, y, probability);
    }

    private void setPlusProbability(NonTerminal x, NonTerminal y, double probability) {
        TObjectDoubleMap<Category> yToProb = getYToProbs(x);
        final double newProbability = yToProb.get(y) + probability;
        if (Double.isNaN(newProbability))
            throw new Error();


        yToProb.put(y, newProbability);
        if (probability != 0.0) {
            nonZeroScores.put(x, y);
        }
    }

    private void setPlusSemiringElement(NonTerminal x, NonTerminal y, double semiringElement) {
        TObjectDoubleMap<Category> yToSemiringElements = getYToSemiringElements(x);
        final double newElement = semiring.plus(yToSemiringElements.get(y)/*defaults to zero*/, semiringElement);
        if (Double.isNaN(newElement))
            throw new Error();

        yToSemiringElements.put(y, newElement);
        if (semiringElement != semiring.zero()) {
            nonZeroScores.put(x, y);
        }
    }

    /**
     * Adds the given number to the current value of [X, Y]
     *
     * @param x               Left hand side
     * @param y               Right hand side
     * @param semiringElement number to add
     */
    void plusSemiringElement(NonTerminal x, NonTerminal y, double semiringElement) {
        setPlusSemiringElement(x, y, semiringElement);
        setPlusProbability(x, y, semiring.toProbability(semiringElement));
    }

    /**
     * @return stored value in left-corner relationship. zero by default
     */
    double getSemiringElement(Category x, Category y) {
        return getYToSemiringElements(x).get(y)/*defaults to zero*/;
    }

    /**
     * @return stored value in left-corner relationship. zero by default
     */
    public double getProbability(Category x, Category y) {
        return getYToProbs(x).get(y)/*defaults to zero*/;
    }

    /**
     * Will instantiate empty map if it does not exist yet.
     *
     * @param x LHS
     * @return map for given LHS.
     */
    private TObjectDoubleMap<Category> getYToSemiringElements(Category x) {
        if (mapToSemiringElements.containsKey(x)) return mapToSemiringElements.get(x);
        else {
            TObjectDoubleMap<Category> yToProb = (new TObjectDoubleHashMap<>(10, 0.5F, semiring.zero()));
            mapToSemiringElements.put(x, yToProb);
            return yToProb;
        }
    }

    /**
     * Will instantiate empty map if it does not exist yet.
     *
     * @param x LHS
     * @return map for given LHS.
     */
    private TObjectDoubleMap<Category> getYToProbs(Category x) {
        if (mapToProb.containsKey(x))
            return mapToProb.get(x);
        else {
            TObjectDoubleMap<Category> yToProb = (new TObjectDoubleHashMap<>(10, 0.5F, 0.0));
            mapToProb.put(x, yToProb);
            return yToProb;
        }
    }

    /**
     * Sets table entry to a given probability. Will instantiate empty map if it does not exist yet.
     *
     * @param x   LHS
     * @param y   RHS
     * @param val Dbl to set table entry to
     */
    void setSemiringElement(NonTerminal x, NonTerminal y, final double val) {
        putSemiringElement_(x, y, val);
        putProbability_(x, y, semiring.toProbability(val));
    }

    private void putSemiringElement_(NonTerminal x, NonTerminal y, double val) {
        TObjectDoubleMap<Category> yToSemiringElements = getYToSemiringElements(x);
        yToSemiringElements.put(y, val);
        if (val != semiring.zero()) {
            nonZeroScores.put(x, y);
        }
    }

    private void putProbability_(NonTerminal x, NonTerminal y, double prob) {
        TObjectDoubleMap<Category> yToProb = getYToProbs(x);
        yToProb.put(y, prob);
        if (prob != 0.0) {
            nonZeroScores.put(x, y);
        }
    }

    /**
     * Sets table entry to a given probability. Will instantiate empty map if it does not exist yet.
     */
    public void setProbability(NonTerminal x, NonTerminal y, final double prob) {
        putSemiringElement_(x, y, semiring.fromProbability(prob));
        putProbability_(x, y, prob);
    }

    Collection<NonTerminal> getNonZeroScores(NonTerminal Y) {
        return nonZeroScores.get(Y);
    }
}
