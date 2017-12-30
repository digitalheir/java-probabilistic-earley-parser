package org.leibnizcenter.cfg.grammar;

import org.leibnizcenter.cfg.algebra.matrix.Matrix;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.HashMap;

/**
 * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
 * to {@link Category} with some utility functions to deal with probabilities.
 */
public class LeftCorners {
    final MyMultimap<NonTerminal, NonTerminal> nonZeroScores = new MyMultimap<>();

    //private final Map<Category, TObjectDoubleMap<Category>> mapToElements = new HashMap<>();
    final double[][] mapToProb;
    final HashMap<Category, Integer> mapToIndex = new HashMap<>();
    private final Category[] categories;

    /**
     * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
     * to {@link Category} with some utility functions to deal with probabilities.
     */
    LeftCorners(final Category[] categories) {
        mapToProb = new double[categories.length][categories.length];
        this.categories = categories;
        for (int i = 0, categoriesLength = categories.length; i < categoriesLength; i++)
            mapToIndex.put(categories[i], i);
    }

    /**
     * Compute left corner relations
     */
    LeftCorners(final MyMultimap<NonTerminal, Rule> rules, final NonTerminal[] categories) {
        mapToProb = new double[categories.length][categories.length];
        this.categories = categories;
        for (int i = 0, categoriesLength = categories.length; i < categoriesLength; i++)
            mapToIndex.put(categories[i], i);

        sumLeftCornerProbabilities(rules, categories);
    }

    /**
     * Sum all probabilities for left corners
     */
    private void sumLeftCornerProbabilities(final MyMultimap<NonTerminal, Rule> rules, final NonTerminal[] categories) {
        for (int i = 0, categoriesLength = categories.length; i < categoriesLength; i++) {
            final NonTerminal leftHandSide = categories[i];
            final Collection<Rule> rulesOnNonTerminal = rules.get(leftHandSide);
            if (rulesOnNonTerminal != null) {
                for (final Rule yRule : rulesOnNonTerminal) {
                    final boolean startsWithNonTerminal = yRule.right.length > 0 && yRule.right[0] instanceof NonTerminal;
                    if (startsWithNonTerminal) {
                        plusRawProbability(i, (NonTerminal) yRule.right[0], yRule.probability);
                    }
                }
            }
        }
    }

    /**
     * Copy all matrix values into a new {@link LeftCorners} object
     *
     * @param r_L          matrix to copy values from
     * @param nonTerminals indexes of matrix
     */
    LeftCorners(final Matrix r_L, final NonTerminal[] nonTerminals) {
        final int n = nonTerminals.length;
        this.categories = nonTerminals;
        mapToProb = new double[n][n];
        for (int i = 0; i < n; i++) mapToIndex.put(nonTerminals[i], i);
        final int bound = r_L.getRowDimension();
        for (int i = 0; i < bound; i++)
            for (int col = 0; col < r_L.getColumnDimension(); col++)
                setRawProbability(i, col, r_L.get(i, col));
    }

    /**
     * Will instantiate empty map if it does not exist yet.
     *
     * @param element LHS
     * @return map for given LHS.
     */
    private static double[] getYToProbs(final double[][] mapToProb, final int element) {
        final double[] doubles = mapToProb[element];
        if (doubles != null) return doubles;
        else {
            final double[] yToProb = new double[mapToProb.length];
            mapToProb[element] = yToProb;
            return yToProb;
        }
    }

    /**
     * @param from        From category
     * @param to          To category
     * @param probability Between 0.0 and 1.0
     */
    void plusRawProbability(final int from, final NonTerminal to, final double probability) {
        final double[] yToProb = getYToProbs(mapToProb, from);
        final int yIndex = mapToIndex.get(to);
        final double newProbability = yToProb[yIndex] + probability;
        if (Double.isNaN(newProbability)) throw new Error();


        yToProb[yIndex] = newProbability;
        if (probability != 0.0) {
            nonZeroScores.put((NonTerminal) categories[from], to);
        }
    }

//    private void setPlusElement(NonTerminal x, NonTerminal y, double Element, DblSemiring semiring) {
//        TObjectDoubleMap<Category> yToElements = getYToProbs(mapToElements, x);
//        final double newElement = semiring.plus(yToElements.get(y)/*defaults to zero*/, Element);
//        if (Double.isNaN(newElement)) throw new Error();
//
//        yToElements.put(y, newElement);
//        if (Element != semiring.zero()) {
//            nonZeroScores.put(x, y);
//        }
//    }

//    /**
//     * Adds the given number to the current value of [X, Y]
//     *
//     * @param x       Left hand side
//     * @param y       Right hand side
//     * @param Element number to add
//     */
//    void plusElement(NonTerminal x, NonTerminal y, double Element) {
//        setPlusElement(x, y, Element);
//        setPlusProbability(x, y, semiring.toProbability(Element));
//    }
//
//
//    /**
//     * @return stored value in left-corner relationship. zero by default
//     */
//    double getSemiringElement(Category x, Category y) {
//        return getYToProbs(mapToElements, x).get(y)/*defaults to zero*/;
//    }
//    /**
//     * Sets table entry to a given probability. Will instantiate empty map if it does not exist yet.
//     *
//     * @param x   LHS
//     * @param y   RHS
//     * @param val Dbl to set table entry to
//     */
//    void setElement(NonTerminal x, NonTerminal y, final double val) {
//        putElement_(x, y, val);
//        putProbability_(x, y, semiring.toProbability(val));
//    }

    private void setPlusProbability(final NonTerminal x, final NonTerminal y, final double probability) {
        final double[] yToProb = getYToProbs(mapToProb, mapToIndex.get(x));
        final int yIndex = mapToIndex.get(y);
        final double newProbability = yToProb[yIndex] + probability;
        if (Double.isNaN(newProbability)) throw new Error();


        yToProb[yIndex] = newProbability;
        if (probability != 0.0) {
            nonZeroScores.put(x, y);
        }
    }

    /**
     * @return stored value in left-corner relationship. zero by default
     */
    public double getProbability(final Category x, final Category y) {
        return getYToProbs(mapToProb, mapToIndex.get(x))[mapToIndex.get(y)]/*defaults to zero*/;
    }

    private void putProb_(final double[][] mapToProb,
                          final NonTerminal x,
                          final NonTerminal y,
                          final double val,
                          final double zero) {
        final double[] yToElements = getYToProbs(mapToProb, mapToIndex.get(x));
        yToElements[mapToIndex.get(y)] = val;
        if (val != zero) nonZeroScores.put(x, y);
    }

    private void putProb_(final double[][] mapToProb,
                          final int x,
                          final int y,
                          final double val,
                          @SuppressWarnings("SameParameterValue") final double zero) {
        final double[] yToElements = getYToProbs(mapToProb, x);
        yToElements[y] = val;
        if (val != zero) nonZeroScores.put((NonTerminal) this.categories[x], (NonTerminal) this.categories[y]);
    }

    /**
     * Sets table entry to a given probability. Will instantiate empty map if it does not exist yet.
     */
    @SuppressWarnings("unused")
    void setProbability(final NonTerminal x, final NonTerminal y, final double prob, final DblSemiring semiring) {
        putProb_(mapToProb, x, y, semiring.fromProbability(prob), semiring.zero());
    }

    /**
     * Sets table entry to a given raw probability (default 0.0). Will instantiate empty map if it does not exist yet.
     */
    void setRawProbability(final NonTerminal x, final NonTerminal y, final double prob) {
        //putProb_(mapToElements, x, y, semiring.fromProbability(prob), semiring.zero());
        putProb_(mapToProb, x, y, prob, 0.0);
    }

    /**
     * Sets table entry to a given raw probability (default 0.0). Will instantiate empty map if it does not exist yet.
     */
    private void setRawProbability(final int x, final int y, final double prob) {
        putProb_(mapToProb, x, y, prob, 0.0);
    }

    Collection<NonTerminal> getNonZeroScores(final NonTerminal Y) {
        return nonZeroScores.get(Y);
    }
}
