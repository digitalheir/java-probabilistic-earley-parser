package org.leibnizcenter.cfg.earleyparser.chart.state;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps states to other objects
 * <p>
 * Created by maarten on 11-1-17.
 */
@SuppressWarnings("unused")
public class StateToXMap<T> implements Map<State, T> {//TODO use this everywhere
    private final HashMap<Rule,
                    /*index*/
            TIntObjectMap<
                            /*rule start*/
                    TIntObjectMap<
                                    /*dot position*/
                            TIntObjectMap<
                                    T
                                    >
                            >
                    >> map;
    private Set<State> keys = new HashSet<>();
    private Multiset<T> values = HashMultiset.create();

    @SuppressWarnings("unused")
    public StateToXMap(int capacity) {
        this.map = new HashMap<>(capacity);
    }

    private int size = 0;

    @SuppressWarnings("unused")
    public StateToXMap() {
        this.map = new HashMap<>();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof State)) return false;
        State s = (State) key;
        final Rule rule = s.getRule();
        if (!map.containsKey(rule)) return false;
        final TIntObjectMap<TIntObjectMap<TIntObjectMap<T>>> positions = map.get(rule);
        if (!positions.containsKey(s.positionInInput)) return false;
        final TIntObjectMap<TIntObjectMap<T>> ruleStarts = positions.get(s.positionInInput);
        if (!ruleStarts.containsKey(s.ruleStartPosition)) return false;
        TIntObjectMap<T> dots = ruleStarts.get(s.ruleStartPosition);
        return dots.containsKey(s.ruleDotPosition);
    }

    @Override
    public boolean containsValue(Object value) {
        //noinspection SuspiciousMethodCalls
        return values.contains(value);
    }

    @Override
    public T get(Object key) {
        if (!containsKey(key))
            return null;
        else {
            State s = (State) key;
            return map.get(s.getRule()).get(s.positionInInput).get(s.ruleStartPosition).get(s.ruleDotPosition);
        }
    }

    public T getOrPut(State key, T fallback) {
        if (containsKey(key))
            return get(key);
        else {
            put(key, fallback);
            return fallback;
        }
    }

//    private static <K, V1, V2> Map<V1, V2> getOrCreate(Map<K, Map<V1, V2>> m, K key) {
//        if (m.containsKey(key)) return m.get(key);
//        else {
//            final HashMap<V1, V2> m2 = new HashMap<>();
//            m.put(key, m2);
//            return m2;
//        }
//    }

    private static <K, V2> TIntObjectMap<V2> getOrCreate(Map<K, TIntObjectMap<V2>> m, K key) {
        if (m.containsKey(key))
            return m.get(key);
        else {
            final TIntObjectMap<V2> m2 = new TIntObjectHashMap<>(10, 0.5F, -1);
            m.put(key, m2);
            return m2;
        }
    }

    private static <V2> TIntObjectMap<V2> getOrCreate(TIntObjectMap<TIntObjectMap<V2>> intObjectMap, int key) {
        if (!intObjectMap.containsKey(key)) {
            final TIntObjectMap<V2> m2 = new TIntObjectHashMap<>(10, 0.5F, -1);
            intObjectMap.put(key, m2);
            return m2;
        } else {
            return intObjectMap.get(key);
        }
    }

    @Override
    public T put(State key, T value) {
        T prev = get(key);
        TIntObjectMap<T> m = getOrCreate(getOrCreate(
                getOrCreate(map, key.rule),
                key.positionInInput),
                key.ruleStartPosition);
        m.put(key.ruleDotPosition, value);
        this.keys.add(key);
        this.values.add(value);
        return prev;
    }

    @Override
    public T remove(Object k) {
        if (!(k instanceof State)) return null;
        State key = (State) k;
        T prev = get(key);
        TIntObjectMap<T> m = getOrCreate(getOrCreate(
                getOrCreate(map, key.rule),
                key.positionInInput),
                key.ruleStartPosition);
        m.remove(key.ruleDotPosition);
        this.keys.remove(key);
        this.values.remove(prev);
        return prev;
    }

    @Override
    public void putAll(Map<? extends State, ? extends T> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<State> keySet() {
        return this.keys;
    }

    @Override
    public Collection<T> values() {
        return values.elementSet();
    }

    @Override
    public Set<Entry<State, T>> entrySet() {
        return keySet().stream().map(k -> Maps.immutableEntry(k, get(k))).collect(Collectors.toSet());
    }
}
