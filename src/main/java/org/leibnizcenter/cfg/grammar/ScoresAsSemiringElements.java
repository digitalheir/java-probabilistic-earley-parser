package org.leibnizcenter.cfg.grammar;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.Map;

/**
 * Contains probabilities as semiring elements
 */
public class ScoresAsSemiringElements {
    private final double[][] mapToSemiringElements;
    private final MyMultimap<NonTerminal, NonTerminal> nonZeroNonTerminalScores;
    private final Map<Category, Integer> mapToIndex;

    ScoresAsSemiringElements(final LeftCorners leftCorners, final DblSemiring semiring) {
        this.nonZeroNonTerminalScores = leftCorners.nonZeroScores;
        this.mapToIndex = leftCorners.mapToIndex;
        mapToSemiringElements = new double[leftCorners.mapToProb.length][leftCorners.mapToProb.length];
        final double[][] mapToProb = leftCorners.mapToProb;
        for (int catFrom = 0; catFrom < mapToProb.length; catFrom++) {
            final double[] yToProb = mapToProb[catFrom];
            final double[] toScore = getCategoryToScoreMap(semiring.zero(), catFrom);
            for (int catTo = 0, yToProbLength = yToProb.length; catTo < yToProbLength; catTo++) {
                final double v = yToProb[catTo];
                toScore[catTo] = semiring.fromProbability(v);
            }
        }
    }

    private double[] getCategoryToScoreMap(final double zero, final int catFrom) {
        final double[] mapToSemiringElement = mapToSemiringElements[catFrom];
        if (mapToSemiringElement != null) return mapToSemiringElement;
        else return initializeValueRow(zero, catFrom);
    }

    private double[] initializeValueRow(final double zero, final int catFrom) {
        final double[] toScore = new double[mapToSemiringElements.length];
        for (int i = 0; i < toScore.length; i++) toScore[i] = zero;
        mapToSemiringElements[catFrom] = toScore;
        return toScore;
    }

    // todo param int not category?
    public Collection<NonTerminal> getNonZeroNonTerminals(final NonTerminal Y) {
        return nonZeroNonTerminalScores.get(Y);
    }

    // todo param int not category?
    double get(final Category lhs, final Category rhs) {
        return mapToSemiringElements[mapToIndex.get(lhs)][mapToIndex.get(rhs)];
    }
}
