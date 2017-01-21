package org.leibnizcenter.cfg.util;

import java.util.*;

/**
 * Created by maarten on 21-1-17.
 */
public class MyMultimap<T, T1> {
    private Map<T, Set<T1>> map = new HashMap<T, Set<T1>>();
    private Set<T1> values = new HashSet<T1>();

    public Collection<T1> get(T y) {
        if (map.containsKey(y)) return map.get(y);
        else return null;
    }

    public void put(T k, T1 v) {
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

    public boolean containsKey(T s) {
        return map.containsKey(s);
    }

    public Collection<T1> values() {
        return Collections.unmodifiableSet(values);
    }

}
