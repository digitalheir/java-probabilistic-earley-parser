package org.leibnizcenter.cfg.grammar;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.Atom;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
 * to {@link Category} with some utility functions to deal with probabilities.
 */
public class LeftCorners {
    final Map<Category, TObjectDoubleMap<Category>> map = new HashMap<>();
    private final MyMultimap<Category, Category> nonZeroScores = new MyMultimap<>();
    final MyMultimap<Category, NonTerminal> nonZeroNonTerminalScores = new MyMultimap<>();
    private final DblSemiring semiring;
    private final AtomMap atoms;

    /**
     * Information holder for left-corner relations and left*-corner relations. Essentially a map from {@link Category}
     * to {@link Category} with some utility functions to deal with probabilities.
     */
    LeftCorners(DblSemiring semiring,AtomMap atoms) {
        this.atoms=atoms;this.semiring = semiring;
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
     * @param val Dbl to set table entry to
     */
    public void set(NonTerminal x, NonTerminal y, final double val) {
        TObjectDoubleMap<Category> yToProb = getYToProb(x);
        set(x, y, yToProb, val);
    }

    Collection<Category> getNonZeroScores(Category Y) {
        return nonZeroScores.get(Y);
    }


    private void set(Category x, Category y, TObjectDoubleMap<Category> yToProb, double val) {
        if (val != semiring.zero()) {
            nonZeroScores.put(x, y);
            if (y instanceof NonTerminal) nonZeroNonTerminalScores.put(x, (NonTerminal) y);
        }

        yToProb.put(y, val);
    }

    void forEach(Consumer c) {
        this.map.forEach(
                (cat, m) -> m.forEachEntry((cat2, dbl) -> {
                    c.consume(cat, cat2, dbl);
                    return true;
                })
        );
    }

    interface Consumer {
        void consume(Category X, Category Y, double value);
    }
}
