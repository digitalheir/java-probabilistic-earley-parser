package org.leibnizcenter.cfg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions for {@link java.util.ArrayList ArrayLists}
 * Created by maarten on 9-6-16.
 */
public class ArrayLists {
    /**
     * Adds element to given List, or instantiates a new ArrayList if it doesn't exist
     *
     * @param list  List to add to, may be null
     * @param toAdd Element to add
     * @return List with element added
     */
    public static <T> List<T> add(List<T> list, T toAdd) {
        if (list == null) list = new ArrayList<>();
        list.add(toAdd);
        return list;
    }
}
