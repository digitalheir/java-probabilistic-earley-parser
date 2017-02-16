package org.leibnizcenter.cfg.grammar;

import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import org.leibnizcenter.cfg.earleyparser.Atom;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * For not re-creating atom objects all the time, a weak cache of atoms. Values might get garbage collected.
 * <p>
 * Created by maarten on 22/01/17.
 */
public class AtomFactory {
    private final TDoubleObjectMap<AtomWeakReference> atoms = new TDoubleObjectHashMap<>();
    private final ReferenceQueue<Atom> referenceQueue = new ReferenceQueue<>();

    public Atom getAtom(double dbl) {
//        return new Atom(dbl);
        final WeakReference<Atom> atomWeakReference = atoms.get(dbl);

        Atom atom;
        if (atomWeakReference == null) {
            atom = putNewAtom(dbl);
        } else {
            atom = atomWeakReference.get();
            if (atom == null) {
                atom = putNewAtom(dbl);
            }
        }

        cleanUpDeadReferences();

        return atom;
    }

    private Atom putNewAtom(double dbl) {
        Atom atom = new Atom(dbl);
        final AtomWeakReference weakReference = new AtomWeakReference(atom, referenceQueue);
        atoms.put(dbl, weakReference);
        return atom;
    }

    private void cleanUpDeadReferences() {
        AtomWeakReference reference = (AtomWeakReference) referenceQueue.poll();
        while (reference != null) {
            if (atoms.get(reference.value).get() == null) atoms.remove(reference.value);
            //System.out.println("cleaning up " + dbl + "(" + atoms.size() + ")");
            reference = (AtomWeakReference) referenceQueue.poll();
        }
    }

    private static class AtomWeakReference extends WeakReference<Atom> {
        final double value;

        AtomWeakReference(Atom atom, ReferenceQueue<Atom> referenceQueue) {
            super(atom, referenceQueue);
            value = atom.value;
        }
    }
}
