package org.leibnizcenter.cfg.grammar;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UnitStarScores {
    private final Map<Category, TObjectDoubleMap<Category>> map = new HashMap<>();
    private final MyMultimap<NonTerminal, NonTerminal> nonZeroNonTerminalScores;

    public UnitStarScores(LeftCorners leftCorners, AtomFactory atoms, ExpressionSemiring semiring) {
        this.nonZeroNonTerminalScores = leftCorners.nonZeroScores;
        leftCorners.map.forEach(
                (cat, m) -> {
                    boolean has = map.containsKey(cat);
                    TObjectDoubleMap<Category> map2 = has ? map.get(cat) : new TObjectDoubleHashMap<>(500, 0.5F, semiring.zero());
                    if (!has) map.put(cat, map2);
                    m.forEachEntry((cat2, dbl) -> {
                        map2.put(cat2, dbl);
                        return true;
                    });
                }
        );
    }


    public Collection<NonTerminal> getNonZeroNonTerminals(NonTerminal Y) {
        return nonZeroNonTerminalScores.get(Y);
    }

    public double get(Category lhs, NonTerminal rhs) {
        return map.get(lhs).get(rhs);
    }
}
