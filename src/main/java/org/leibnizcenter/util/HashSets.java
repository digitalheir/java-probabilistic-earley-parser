package org.leibnizcenter.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by maarten on 9-6-16.
 */
public class HashSets {
    /**
     * Adds element to given Set, or instantiates a new HashSet if it doesn't exist
     *
     * @param set   Set to add to, may be null
     * @param toAdd Element to add
     * @return Set with element added
     */
    public static <T> Set<T> add(Set<T> set, T toAdd) {
        if (set == null) set = new HashSet<>();
        set.add(toAdd);
        return set;
    }
}
