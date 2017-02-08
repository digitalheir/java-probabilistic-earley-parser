package org.leibnizcenter.cfg.util;

import java.util.Collection;

/**
 * Collection utils
 * Created by maarten on 8-2-17.
 */
public class Collections2 {

    public static <T> boolean nullOrEmpty(Collection<T> categories) {
        return categories != null && categories.size() <= 0;
    }
}
