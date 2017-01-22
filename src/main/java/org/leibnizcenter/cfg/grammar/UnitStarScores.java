package org.leibnizcenter.cfg.grammar;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.Atom;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UnitStarScores {
    private final Atom zero;
    private final Map<Category, Map<Category, Atom>> map = new HashMap<>();
    private final MyMultimap<Category, NonTerminal> nonZeroNonTerminalScores;
    private final AtomMap atoms;

    public UnitStarScores(LeftCorners leftCorners, DblSemiring semiring, AtomMap atoms) {
        this.zero = new Atom(semiring.zero());
        this.nonZeroNonTerminalScores = leftCorners.nonZeroNonTerminalScores;
        this.atoms = atoms;
        leftCorners.map.forEach(
                (cat, m) -> {
                    boolean has = map.containsKey(cat);
                    Map<Category, Atom> map2 = has ? map.get(cat) : new HashMap<>();
                    if (!has) map.put(cat, map2);
                    m.forEachEntry((cat2, dbl) -> {
                        map2.put(cat2, atoms.getAtom(dbl));
                        return true;
                    });
                }
        );
    }


    public Collection<NonTerminal> getNonZeroNonTerminals(Category Y) {
        return nonZeroNonTerminalScores.get(Y);
    }

    public Atom getAtom(Category lhs, NonTerminal rhs) {
        return map.get(lhs).get(rhs);
    }
}
