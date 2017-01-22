package org.leibnizcenter.cfg.grammar;

import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import org.leibnizcenter.cfg.earleyparser.Atom;

/**
 * For not re-creating atom objects all the time
 * <p>
 * Created by maarten on 22/01/17.
 */
public class AtomMap {
    private final TDoubleObjectMap<Atom> atoms = new TDoubleObjectHashMap<>();

    public Atom getAtom(double dbl) {
        boolean has = atoms.containsKey(dbl);
        Atom atom = has ? atoms.get(dbl) : new Atom(dbl);
        if (!has) atoms.put(dbl, atom);
        return atom;
    }
}
