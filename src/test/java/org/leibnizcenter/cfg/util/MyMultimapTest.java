package org.leibnizcenter.cfg.util;

import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by maarten on 27-1-17.
 */
public class MyMultimapTest {
    public static MyMultimap<String, String> EMPTY_MULTIMAP = new MyMultimap<String, String>();
    public static MyMultimap<String, String> NON_EMPTY_MULTIMAP = new MyMultimap<String, String>();
    public static MyMultimap<String, String> NON_EMPTY_MULTIMAP_2 = new MyMultimap<String, String>();

    static {
        NON_EMPTY_MULTIMAP.put("a", "b");
        NON_EMPTY_MULTIMAP_2.put("a", "b");
        NON_EMPTY_MULTIMAP_2.put("a", "c");
    }

    @Test
    public void get() throws Exception {
        assertNull(EMPTY_MULTIMAP.get("a"));
        assertEquals(Stream.of("b").collect(Collectors.toSet()), NON_EMPTY_MULTIMAP.get("a"));
        assertEquals(Stream.of("b", "c").collect(Collectors.toSet()), NON_EMPTY_MULTIMAP_2.get("a"));
    }


    @Test
    public void containsKey() throws Exception {
        assertEquals(true, NON_EMPTY_MULTIMAP.containsKey("a"));
        assertEquals(false, NON_EMPTY_MULTIMAP.containsKey("b"));
        assertEquals(false, EMPTY_MULTIMAP.containsKey("a"));
        assertEquals(false, EMPTY_MULTIMAP.containsKey("b"));
    }

    @Test(expected = java.lang.UnsupportedOperationException.class)
    public void lock() throws Exception {
        final MyMultimap<String, String> multimap = new MyMultimap<>();
        multimap.lock();
        multimap.put("a", "b");
    }

    @Test
    public void values() throws Exception {
        assertEquals(0, EMPTY_MULTIMAP.values().size());
        assertEquals(1, NON_EMPTY_MULTIMAP.values().size());
    }

}