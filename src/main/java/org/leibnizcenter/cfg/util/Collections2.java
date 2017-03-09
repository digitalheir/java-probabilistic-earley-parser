package org.leibnizcenter.cfg.util;

import org.leibnizcenter.cfg.earleyparser.chart.state.State;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Collection utils
 * Created by maarten on 8-2-17.
 */
public class Collections2 {
    public static <T> boolean nullOrEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean isFilled(Collection<T> collection) {
        return !nullOrEmpty(collection);
    }

    public static <T> Collection<T> emptyIfNull(Collection<T> collection) {
        return collection == null ? Collections.emptySet() : collection;
    }
}
