package org.leibnizcenter.cfg.grammar;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.matrix.Matrix;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
 * to {@link Category} with some utility functions to deal with probabilities.
 */
public class LeftCorners {
    final MyMultimap<NonTerminal, NonTerminal> nonZeroScores = new MyMultimap<>();

    //private final Map<Category, TObjectDoubleMap<Category>> mapToElements = new HashMap<>();
    final Map<Category, TObjectDoubleMap<Category>> mapToProb = new HashMap<>();

    /**
     * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
     * to {@link Category} with some utility functions to deal with probabilities.
     */
    LeftCorners() {
    }

    /**
     * Compute left corner relations
     */
    LeftCorners(final Set<NonTerminal> nonTerminals, final MyMultimap<NonTerminal, Rule> rules) {
        // Sum all probabilities for left corners
        nonTerminals.forEach(leftHandSide -> {
            final Collection<Rule> rulesOnNonTerminal = rules.get(leftHandSide);
            if (rulesOnNonTerminal != null) {
                for (final Rule yRule : rulesOnNonTerminal) {
                    final boolean startsWithNonTerminal = yRule.right.length > 0 && yRule.right[0] instanceof NonTerminal;
                    if (startsWithNonTerminal) {
                        plusRawProbability(leftHandSide, (NonTerminal) yRule.right[0], yRule.probability);
                    }
                }
            }
        });
    }

    /**
     * Copy all matrix values into a new {@link LeftCorners} object
     *
     * @param r_L          matrix to copy values from
     * @param nonTerminals indexes of matrix
     */
    LeftCorners(final Matrix r_L, final NonTerminal[] nonTerminals) {
        final int bound = r_L.getRowDimension();
        for (int i = 0; i < bound; i++) {
            final int row = i;
            IntStream.range(0, r_L.getColumnDimension()).forEach(col -> setRawProbability(nonTerminals[row], nonTerminals[col], r_L.get(row, col)));
        }
    }

    /**
     * Will instantiate empty map if it does not exist yet.
     *
     * @param element LHS
     * @return map for given LHS.
     */
    private static TObjectDoubleMap<Category> getYToProbs(final Map<Category, TObjectDoubleMap<Category>> mapToProb, final Category element) {
        if (mapToProb.containsKey(element))
            return mapToProb.get(element);
        else {
            final TObjectDoubleMap<Category> yToProb = (new TObjectDoubleHashMap<>(10, 0.5F, 0.0));
            mapToProb.put(element, yToProb);
            return yToProb;
        }
    }

    /**
     * @param from        From category
     * @param to          To category
     * @param probability Between 0.0 and 1.0
     */
    void plusRawProbability(final NonTerminal from, final NonTerminal to, final double probability) {
        //setPlusElement(from, to, semiring.fromProbability(probability), semiring);
        setPlusProbability(from, to, probability);
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
        final TObjectDoubleMap<Category> yToProb = getYToProbs(mapToProb, x);
        final double newProbability = yToProb.get(y) + probability;
        if (Double.isNaN(newProbability)) throw new Error();


        yToProb.put(y, newProbability);
        if (probability != 0.0) {
            nonZeroScores.put(x, y);
        }
    }

    /**
     * @return stored value in left-corner relationship. zero by default
     */
    public double getProbability(final Category x, final Category y) {
        return getYToProbs(mapToProb, x).get(y)/*defaults to zero*/;
    }

    private void putProb_(final Map<Category, TObjectDoubleMap<Category>> mapToProb,
                          final NonTerminal x,
                          final NonTerminal y,
                          final double val,
                          final double zero) {
        final TObjectDoubleMap<Category> yToElements = getYToProbs(mapToProb, x);
        yToElements.put(y, val);
        if (val != zero) {
            nonZeroScores.put(x, y);
        }
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

    Collection<NonTerminal> getNonZeroScores(final NonTerminal Y) {
        return nonZeroScores.get(Y);
    }
}
