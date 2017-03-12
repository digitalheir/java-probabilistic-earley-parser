package org.leibnizcenter.cfg.grammar;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains probabilities as semiring elements
 */
public class ScoresAsSemiringElements {
    private final Map<Category, TObjectDoubleMap<Category>> mapToSemiringElements = new HashMap<>();
    private final MyMultimap<NonTerminal, NonTerminal> nonZeroNonTerminalScores;

    ScoresAsSemiringElements(LeftCorners leftCorners, DblSemiring semiring) {
        this.nonZeroNonTerminalScores = leftCorners.nonZeroScores;
        leftCorners.mapToProb.forEach(
                (catFrom, m) -> {
                    final TObjectDoubleMap<Category> toScore = getCategoryToScoreMap(semiring.zero(), catFrom);
                    m.forEachEntry((catTo, value) -> {
                        toScore.put(catTo, semiring.fromProbability(value));
                        return true;
                    });
                }
        );
    }

    private TObjectDoubleMap<Category> getCategoryToScoreMap(double zero, Category catFrom) {
        if (mapToSemiringElements.containsKey(catFrom))
            return mapToSemiringElements.get(catFrom);
        else {
            TObjectDoubleHashMap<Category> toScore = new TObjectDoubleHashMap<>(500, 0.5F, zero);
            mapToSemiringElements.put(catFrom, toScore);
            return toScore;
        }
    }


    public Collection<NonTerminal> getNonZeroNonTerminals(NonTerminal Y) {
        return nonZeroNonTerminalScores.get(Y);
    }

    double get(Category lhs, Category rhs) {
        return mapToSemiringElements.get(lhs).get(rhs);
    }
}
