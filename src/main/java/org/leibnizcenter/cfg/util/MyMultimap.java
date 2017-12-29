package org.leibnizcenter.cfg.util;

import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.*;

/**
 * Dumb implementation of a multimap
 * Created by maarten on 21-1-17.
 */
public class MyMultimap<T, T1> {
    private Map<T, Set<T1>> map = new HashMap<>();
    private Set<T1> values = new HashSet<>();
    private boolean isLocked = false;

    public Collection<T1> get(final T el) {
        return map.getOrDefault(el, null);
    }

    public void put(final T k, final T1 v) {
        final Set<T1> s;
        if (map.containsKey(k)) {
            s = map.get(k);
        } else {
            s = new HashSet<>();
            map.put(k, s);
        }
        s.add(v);
        values.add(v);
    }

    public void putAll(final T k, final Collection<T1> v) {
        final Set<T1> s;
        if (map.containsKey(k)) {
            s = map.get(k);
        } else {
            s = new HashSet<>();
            map.put(k, s);
        }

        s.addAll(v);
        values.addAll(v);
    }

    public boolean containsKey(final T s) {
        return map.containsKey(s);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean lock() {
        map = Collections.unmodifiableMap(map);
        values = Collections.unmodifiableSet(values);

        final boolean wasLocked = isLocked;
        isLocked = true;
        return wasLocked;
    }

    public Collection<T1> values() {
        return values;
    }

    public Set<T> keys() {
        return map.keySet();
    }

    public Set<Map.Entry<T, Set<T1>>> entries() {
        return map.entrySet();
    }

}
