package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@SuppressWarnings("unused")
public class StateToDoubleMap {
    private final HashMap<Rule,
                    /*index*/
            TIntObjectMap<
                            /*rule start*/
                    TIntObjectMap<
                                    /*dot position to double */
                            TIntDoubleMap
                            >
                    >> map;
    private final double defaultValue;
    private Set<State> keys = new HashSet<>();
    //    private TObjectIntMap<T> values = new TObjectIntHashMap<>(25, 0.5F, 0);
    private int size = 0;


    @SuppressWarnings("unused")
    public StateToDoubleMap(int i, float v, double zero) {
        this.map = new HashMap<>();
        defaultValue = zero;
    }


    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(Object key) {
        if (!(key instanceof State))
            return false;
        else {
            State s = (State) key;
            return contains(s.rule, s.position, s.ruleStartPosition, s.ruleDotPosition);
        }
    }

    public boolean contains(Rule rule, int index, int ruleStart, int dot) {
        if (!map.containsKey(rule)) return false;
        TIntObjectMap<TIntObjectMap<TIntDoubleMap>> positions = map.get(rule);
        if (!positions.containsKey(index)) return false;
        TIntObjectMap<TIntDoubleMap> ruleStarts = positions.get(index);
        if (!ruleStarts.containsKey(ruleStart)) return false;
        TIntDoubleMap dots = ruleStarts.get(ruleStart);
        return dots.containsKey(dot);
    }

    public double get(Object key) {
        if (!(key != null && key instanceof State))
            throw new IllegalArgumentException("Did not contain " + key);
        else {
            State s = (State) key;
            return this.get(s.rule, s.position, s.ruleStartPosition, s.ruleDotPosition);
        }
    }

    public double get(Rule rule, int index, int ruleStart, int dot) {
        if (!contains(rule, index, ruleStart, dot))
            return defaultValue;//throw new IllegalArgumentException("Did not contain key");
        return map.get(rule).get(index).get(ruleStart).get(dot);
    }

    public double getOrPut(State key, double fallback) {
        if (containsKey(key))
            return get(key);
        else {
            put(key, fallback);
            return fallback;
        }
    }

    public double put(State key, double value) {
        double prev = get(key);
        TIntObjectMap<TIntObjectMap<TIntDoubleMap>> orCreateR = getOrCreateR(map, key.rule);
        TIntDoubleMap m = getOrCreateHM(getOrCreate(
                orCreateR,
                key.position),
                key.ruleStartPosition);
        m.put(key.ruleDotPosition, value);
        this.keys.add(key);
//        this.values.put(value, this.values.get(value) + 1);
        this.size++;
        return prev;
    }


    public double put(Rule rule, int index, int ruleStart, int dotPosition, double value) {
        double prev = get(rule, index, ruleStart, dotPosition);

        TIntObjectMap<TIntObjectMap<TIntDoubleMap>> orCreateR = getOrCreateR(map, rule);
        TIntObjectMap<TIntDoubleMap> orCreate3 = getOrCreate(orCreateR, index);

        TIntDoubleMap m = getOrCreateHM(orCreate3, ruleStart);
        m.put(dotPosition, value);
        this.keys.add(new State(rule, dotPosition, ruleStart, dotPosition));
//        this.values.put(value, this.values.get(value) + 1);
        this.size++;
        return prev;
    }

    @SuppressWarnings("Duplicates")
    private <K, V2> TIntDoubleMap getOrCreate(Map<K, TIntDoubleMap> m, K key) {
        if (m.containsKey(key))
            return m.get(key);
        else {
            final TIntDoubleHashMap m2 = new TIntDoubleHashMap(10, 0.5F, -1, defaultValue);
            m.put(key, m2);
            return m2;
        }
    }

    @SuppressWarnings("Duplicates")
    private TIntDoubleMap getOrCreateHM(TIntObjectMap<TIntDoubleMap> m, int key) {
        if (m.containsKey(key))
            return m.get(key);
        else {
            final TIntDoubleHashMap m2 = new TIntDoubleHashMap(10, 0.5F, -1, defaultValue);
            m.put(key, m2);
            return m2;
        }
    }

    @SuppressWarnings("Duplicates")
    private <V2> TIntObjectMap<V2> getOrCreate(TIntObjectMap<TIntObjectMap<V2>> m, int key) {
        if (m.containsKey(key))
            return m.get(key);
        else {
            final TIntObjectMap<V2> m2 = new TIntObjectHashMap<>(10, 0.5F, -1);
            m.put(key, m2);
            return m2;
        }
    }

    @SuppressWarnings("Duplicates")
    private <V2> TIntObjectMap<TIntObjectMap<TIntDoubleMap>> getOrCreateR(Map<Rule, TIntObjectMap<TIntObjectMap<TIntDoubleMap>>> map, Rule rule) {
        if (!map.containsKey(rule)) {
            TIntObjectMap<TIntObjectMap<TIntDoubleMap>> m2 = new TIntObjectHashMap<>(10, 0.5F, -1);
            map.put(rule, m2);
            return m2;
        } else {
            return map.get(rule);
        }
    }

//    public double remove(Object k) {
//        if (!(k instanceof State)) return null;
//        State key = (State) k;
//        double prev = get(key);
//        m = getOrCreate(getOrCreate(
//                getOrCreate(map, key.rule),
//                key.position),
//                key.ruleStartPosition);
//        m.remove(key.ruleDotPosition);
//        this.keys.remove(key);
//        this.values.put(prev, Math.max(0, this.values.get(prev)));
//        this.size--;
//        return prev;
//    }

    public void clear() {
        this.map.clear();
        this.keys.clear();
        this.size = 0;
    }

    public Set<State> keySet() {
        return this.keys;
    }


    public void forEachEntry(StateHandler h) {
        //noinspection Duplicates
        map.forEach((rule, tIntObjectMapTIntObjectMap) ->
                tIntObjectMapTIntObjectMap.forEachEntry((position, tIntDoubleMapTIntObjectMap) -> {
                    tIntDoubleMapTIntObjectMap.forEachEntry((ruleStart, tIntDoubleMap) -> {
                        tIntDoubleMap.forEachEntry((dot, score) -> {
                            h.consume(position, ruleStart, dot, rule, score);
                            return true;
                        });
                        return true;
                    });
                    return true;
                }));
    }

    public interface StateHandler {
        void consume(int position, int ruleStart, int dot, Rule rule, double score);
    }
}
