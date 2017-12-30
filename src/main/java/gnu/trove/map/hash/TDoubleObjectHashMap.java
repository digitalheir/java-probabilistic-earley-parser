///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Rob Eden All Rights Reserved.
// Copyright (c) 2009, Jeff Randall All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package gnu.trove.map.hash;

import gnu.trove.impl.hash.TDoubleHash;
import gnu.trove.impl.hash.THashPrimitiveIterator;
import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.impl.Constants;
import gnu.trove.impl.HashFunctions;
import gnu.trove.procedure.TDoubleObjectProcedure;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TDoubleObjectIterator;
import gnu.trove.set.TDoubleSet;
import gnu.trove.TDoubleCollection;

import java.io.*;
import java.util.*;
import java.util.function.Function;


//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. and it's also HAND EDIT!ed //
//////////////////////////////////////////////////


/**
 * An open addressed Map implementation for double keys and Object values.
 *
 * Created: Sun Nov  4 08:52:45 2001
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 */
public class TDoubleObjectHashMap<V> extends TDoubleHash implements
        TDoubleObjectMap<V>, Externalizable {

    static final long serialVersionUID = 1L;

    private final TDoubleObjectProcedure<V> PUT_ALL_PROC = (key, value) -> {
        put(key, value);
        return false;
    };

    /**
     * the values of the map
     */
    private transient V[] _values;

    /**
     * the value that represents null in the key set.
     */
    private double no_entry_key;


    /**
     * Creates a new <code>TDoubleObjectHashMap</code> instance with the default
     * capacity and load factor.
     */
    public TDoubleObjectHashMap() {
        super();
    }


    /**
     * Creates a new <code>TDoubleObjectHashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    @SuppressWarnings("unused")
    public TDoubleObjectHashMap(final int initialCapacity) {
        super(initialCapacity);
        no_entry_key = Constants.DEFAULT_DOUBLE_NO_ENTRY_VALUE;
    }


    /**
     * Creates a new <code>TDoubleObjectHashMap</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the specified load factor.
     *
     * @param initialCapacity an <code>int</code> value
     * @param loadFactor      a <code>float</code> value
     */
    @SuppressWarnings("unused")
    public TDoubleObjectHashMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
        no_entry_key = Constants.DEFAULT_DOUBLE_NO_ENTRY_VALUE;
    }


    /**
     * Creates a new <code>TDoubleObjectHashMap</code> instance with a prime
     * value at or near the specified capacity and load factor.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor      used to calculate the threshold over which
     *                        rehashing takes place.
     * @param noEntryKey      the value used to represent null in the key set.
     */
    private TDoubleObjectHashMap(final int initialCapacity, final float loadFactor, final double noEntryKey) {
        super(initialCapacity, loadFactor);
        no_entry_key = noEntryKey;
    }


    /**
     * Creates a new <code>TDoubleObjectHashMap</code> that contains the entries
     * in the map passed to it.
     *
     * @param map the <tt>TDoubleObjectMap</tt> to be copied.
     */
    @SuppressWarnings("unused")
    public TDoubleObjectHashMap(final TDoubleObjectMap<? extends V> map) {
        this(map.size(), 0.5f, map.getNoEntryKey());
        putAll(map);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    protected int setUp(final int initialCapacity) {
        final int capacity;

        capacity = super.setUp(initialCapacity);
        _values = (V[]) new Object[capacity];
        return capacity;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    protected void rehash(final int newCapacity) {
        final int oldCapacity = _set.length;

        final double[] oldKeys = _set;
        final V[] oldVals = _values;
        final byte[] oldStates = _states;

        _set = new double[newCapacity];
        _values = (V[]) new Object[newCapacity];
        _states = new byte[newCapacity];

        for (int i = oldCapacity; i-- > 0; ) {
            if (oldStates[i] == FULL) {
                final double o = oldKeys[i];
                final int index = insertKey(o);
                _values[index] = oldVals[i];
            }
        }
    }


    // Query Operations

    /**
     * {@inheritDoc}
     */
    public double getNoEntryKey() {
        return no_entry_key;
    }


    /**
     * {@inheritDoc}
     */
    public boolean containsKey(final double key) {
        return contains(key);
    }


    /**
     * {@inheritDoc}
     */
    public boolean containsValue(final Object val) {
        final byte[] states = _states;
        final V[] vals = _values;

        // special case null values so that we don't have to
        // perform null checks before every call to equals()
        if (null == val) {
            for (int i = vals.length; i-- > 0; ) {
                if (states[i] == FULL && null == vals[i]) {
                    return true;
                }
            }
        } else {
            for (int i = vals.length; i-- > 0; ) {
                if (states[i] == FULL &&
                        (val == vals[i] || val.equals(vals[i]))) {
                    return true;
                }
            }
        } // end of else
        return false;
    }


    /**
     * {@inheritDoc}
     */
    public V get(final double key) {
        final int index = index(key);
        return index < 0 ? null : _values[index];
    }


    // Modification Operations

    /**
     * {@inheritDoc}
     */
    public V put(final double key, final V value) {
        final int index = insertKey(key);
        return doPut(value, index);
    }


    /**
     * {@inheritDoc}
     */
    public V putIfAbsent(final double key, final V value) {
        final int index = insertKey(key);
        if (index < 0)
            return _values[-index - 1];
        return doPut(value, index);
    }


    @SuppressWarnings({"unchecked"})
    private V doPut(final V value, int index) {
        V previous = null;
        boolean isNewMapping = true;
        if (index < 0) {
            index = -index - 1;
            previous = _values[index];
            isNewMapping = false;
        }

        _values[index] = value;

        if (isNewMapping) {
            postInsertHook(consumeFreeSlot);
        }

        return previous;
    }


    /**
     * {@inheritDoc}
     */
    public V remove(final double key) {
        V prev = null;
        final int index = index(key);
        if (index >= 0) {
            prev = _values[index];
            removeAt(index);    // clear key,state; adjust size
        }
        return prev;
    }


    /**
     * {@inheritDoc}
     */
    protected void removeAt(final int index) {
        _values[index] = null;
        super.removeAt(index);  // clear key, state; adjust size
    }


    // Bulk Operations

    /**
     * {@inheritDoc}
     */
    public void putAll(final Map<? extends Double, ? extends V> map) {
        final Set<? extends Map.Entry<? extends Double, ? extends V>> set = map.entrySet();
        for (final Map.Entry<? extends Double, ? extends V> entry : set) {
            put(entry.getKey(), entry.getValue());
        }
    }


    /**
     * {@inheritDoc}
     */
    public void putAll(final TDoubleObjectMap<? extends V> map) {
        map.forEachEntry(PUT_ALL_PROC);
    }


    /**
     * {@inheritDoc}
     */
    public void clear() {
        super.clear();
        Arrays.fill(_set, 0, _set.length, no_entry_key);
        Arrays.fill(_states, 0, _states.length, FREE);
        Arrays.fill(_values, 0, _values.length, null);
    }


    // Views

    /**
     * {@inheritDoc}
     */
    public TDoubleSet keySet() {
        return new KeyView();
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    public double[] keys() {
        final double[] keys = new double[size()];
        final double[] k = _set;
        final byte[] states = _states;

        for (int i = k.length, j = 0; i-- > 0; ) {
            if (states[i] == FULL) {
                keys[j++] = k[i];
            }
        }
        return keys;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    public double[] keys(double[] dest) {
        if (dest.length < _size) {
            dest = new double[_size];
        }

        final double[] k = _set;
        final byte[] states = _states;

        for (int i = k.length, j = 0; i-- > 0; ) {
            if (states[i] == FULL) {
                dest[j++] = k[i];
            }
        }
        return dest;
    }


    /**
     * {@inheritDoc}
     */
    public Collection<V> valueCollection() {
        return new ValueView();
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    public Object[] values() {
        final Object[] vals = new Object[size()];
        final V[] v = _values;
        final byte[] states = _states;

        for (int i = v.length, j = 0; i-- > 0; ) {
            if (states[i] == FULL) {
                vals[j++] = v[i];
            }
        }
        return vals;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    public V[] values(V[] dest) {
        if (dest.length < _size) {
            dest = (V[]) java.lang.reflect.Array.newInstance(
                    dest.getClass().getComponentType(), _size);
        }

        final V[] v = _values;
        final byte[] states = _states;

        for (int i = v.length, j = 0; i-- > 0; ) {
            if (states[i] == FULL) {
                dest[j++] = v[i];
            }
        }
        return dest;
    }


    /**
     * {@inheritDoc}
     */
    public TDoubleObjectIterator<V> iterator() {
        return new TDoubleObjectHashIterator<>(this);
    }


    /**
     * {@inheritDoc}
     */
    public boolean forEachKey(final TDoubleProcedure procedure) {
        return forEach(procedure);
    }


    /**
     * {@inheritDoc}
     */
    public boolean forEachValue(final TObjectProcedure<? super V> procedure) {
        final byte[] states = _states;
        final V[] values = _values;
        for (int i = values.length; i-- > 0; ) {
            if (states[i] == FULL && procedure.execute(values[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    public boolean forEachEntry(final TDoubleObjectProcedure<? super V> procedure) {
        final byte[] states = _states;
        final double[] keys = _set;
        final V[] values = _values;
        for (int i = keys.length; i-- > 0; ) {
            if (states[i] == FULL && procedure.execute(keys[i], values[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked"})
    public boolean retainEntries(final TDoubleObjectProcedure<? super V> procedure) {
        boolean modified = false;
        final byte[] states = _states;
        final double[] keys = _set;
        final V[] values = _values;

        // Temporarily disable compaction. This is a fix for bug #1738760
        tempDisableAutoCompaction();
        try {
            for (int i = keys.length; i-- > 0; ) {
                if (states[i] == FULL && procedure.execute(keys[i], values[i])) {
                    removeAt(i);
                    modified = true;
                }
            }
        } finally {
            reenableAutoCompaction(true);
        }

        return modified;
    }


    /**
     * {@inheritDoc}
     */
    public void transformValues(final Function<V, V> function) {
        final byte[] states = _states;
        final V[] values = _values;
        for (int i = values.length; i-- > 0; ) {
            if (states[i] == FULL) {
                values[i] = function.apply(values[i]);
            }
        }
    }


    // Comparison and hashing

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (!(other instanceof TDoubleObjectMap)) {
            return false;
        }
        final TDoubleObjectMap that = (TDoubleObjectMap) other;
        if (that.size() != this.size()) {
            return false;
        }
        try {
            final TDoubleObjectIterator iter = this.iterator();
            while (iter.hasNext()) {
                iter.advance();
                final double key = iter.key();
                final Object value = iter.value();
                if (value == null) {
                    if (!(that.get(key) == null && that.containsKey(key))) {
                        return false;
                    }
                } else {
                    if (!value.equals(that.get(key))) {
                        return false;
                    }
                }
            }
        } catch (final ClassCastException ex) {
            // unused.
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int hashcode = 0;
        final V[] values = _values;
        final byte[] states = _states;
        for (int i = values.length; i-- > 0; ) {
            if (states[i] == FULL) {
                hashcode += HashFunctions.hash(_set[i]) ^
                        (values[i] == null ? 0 : values[i].hashCode());
            }
        }
        return hashcode;
    }


    class KeyView implements TDoubleSet {


        public double getNoEntryValue() {
            return no_entry_key;
        }


        public int size() {
            return _size;
        }


        public boolean isEmpty() {
            return _size == 0;
        }


        public boolean contains(final double entry) {
            return !TDoubleObjectHashMap.this.containsKey(entry);
        }


        public TDoubleIterator iterator() {
            return new TDoubleHashIterator(TDoubleObjectHashMap.this);
        }


        public double[] toArray() {
            return keys();
        }


        public double[] toArray(final double[] dest) {
            return keys(dest);
        }


        public boolean add(final double entry) {
            throw new UnsupportedOperationException();
        }


        public boolean remove(final double entry) {
            return null != TDoubleObjectHashMap.this.remove(entry);
        }


        public boolean containsAll(final Collection<?> collection) {
            for (final Object element : collection) {
                if (!TDoubleObjectHashMap.this.containsKey((Double) element)) {
                    return false;
                }
            }
            return true;
        }


        public boolean containsAll(final TDoubleCollection collection) {
            if (collection == this) {
                return true;
            }
            final TDoubleIterator iter = collection.iterator();
            while (iter.hasNext()) {
                if (!TDoubleObjectHashMap.this.containsKey(iter.next())) {
                    return false;
                }
            }
            return true;
        }


        public boolean containsAll(final double[] array) {
            for (final double element : array) {
                if (!TDoubleObjectHashMap.this.containsKey(element)) {
                    return false;
                }
            }
            return true;
        }


        public boolean addAll(final Collection<? extends Double> collection) {
            throw new UnsupportedOperationException();
        }


        public boolean addAll(final TDoubleCollection collection) {
            throw new UnsupportedOperationException();
        }


        public boolean addAll(final double[] array) {
            throw new UnsupportedOperationException();
        }


        public boolean retainAll(final Collection<?> collection) {
            boolean modified = false;
            final TDoubleIterator iter = iterator();
            while (iter.hasNext()) {
                //noinspection SuspiciousMethodCalls
                if (!collection.contains(iter.next())) {
                    iter.remove();
                    modified = true;
                }
            }
            return modified;
        }


        @SuppressWarnings("Duplicates")
        public boolean retainAll(final TDoubleCollection collection) {
            if (this == collection) {
                return false;
            }
            boolean modified = false;
            final TDoubleIterator iter = iterator();
            while (iter.hasNext()) {
                if (collection.contains(iter.next())) {
                    iter.remove();
                    modified = true;
                }
            }
            return modified;
        }


        public boolean retainAll(final double[] array) {
            boolean changed = false;
            Arrays.sort(array);
            final double[] set = _set;
            final byte[] states = _states;

            for (int i = set.length; i-- > 0; ) {
                if (states[i] == FULL && (Arrays.binarySearch(array, set[i]) < 0)) {
                    removeAt(i);
                    changed = true;
                }
            }
            return changed;
        }


        @SuppressWarnings("Duplicates")
        public boolean removeAll(final Collection<?> collection) {
            boolean changed = false;
            for (final Object element : collection) {
                if (element instanceof Double) {
                    final double c = (Double) element;
                    if (remove(c)) {
                        changed = true;
                    }
                }
            }
            return changed;
        }


        @SuppressWarnings("Duplicates")
        public boolean removeAll(final TDoubleCollection collection) {
            if (collection == this) {
                clear();
                return true;
            }
            boolean changed = false;
            final TDoubleIterator iter = collection.iterator();
            while (iter.hasNext()) {
                final double element = iter.next();
                if (remove(element)) {
                    changed = true;
                }
            }
            return changed;
        }


        public boolean removeAll(final double[] array) {
            boolean changed = false;
            for (int i = array.length; i-- > 0; ) {
                if (remove(array[i])) {
                    changed = true;
                }
            }
            return changed;
        }


        public void clear() {
            TDoubleObjectHashMap.this.clear();
        }


        public boolean forEach(final TDoubleProcedure procedure) {
            return TDoubleObjectHashMap.this.forEachKey(procedure);
        }

        public boolean equals(final Object other) {
            if (!(other instanceof TDoubleSet)) {
                return false;
            }
            final TDoubleSet that = (TDoubleSet) other;
            if (that.size() != this.size()) {
                return false;
            }
            for (int i = _states.length; i-- > 0; ) {
                if (_states[i] == FULL) {
                    if (that.contains(_set[i])) {
                        return false;
                    }
                }
            }
            return true;
        }


        public int hashCode() {
            int hashcode = 0;
            for (int i = _states.length; i-- > 0; ) {
                if (_states[i] == FULL) {
                    hashcode += HashFunctions.hash(_set[i]);
                }
            }
            return hashcode;
        }


        public String toString() {
            final StringBuilder buf = new StringBuilder("{");
            boolean first = true;
            for (int i = _states.length; i-- > 0; ) {
                if (_states[i] == FULL) {
                    if (first) first = false;
                    else buf.append(",");
                    buf.append(_set[i]);
                }
            }
            return buf.toString();
        }


        class TDoubleHashIterator extends THashPrimitiveIterator implements TDoubleIterator {

            /**
             * the collection on which the iterator operates
             */
            private final TDoubleHash _hash;

            /**
             * {@inheritDoc}
             */
            TDoubleHashIterator(final TDoubleHash hash) {
                super(hash);
                this._hash = hash;
            }

            /**
             * {@inheritDoc}
             */
            public double next() {
                moveToNextIndex();
                return _hash._set[_index];
            }
        }
    }


    /**
     * a view onto the values of the map.
     */
    protected class ValueView extends MapBackedView<V> {

        @SuppressWarnings({"unchecked"})
        public Iterator<V> iterator() {
            return new TDoubleObjectValueHashIterator(TDoubleObjectHashMap.this) {
                protected V objectAtIndex(final int index) {
                    return _values[index];
                }
            };
        }

        public boolean containsElement(final V value) {
            return containsValue(value);
        }

        public boolean removeElement(final V value) {
            final V[] values = _values;
            final byte[] states = _states;

            for (int i = values.length; i-- > 0; ) {
                if (states[i] == FULL) {
                    if (value == values[i] ||
                            (null != values[i] && values[i].equals(value))) {
                        removeAt(i);
                        return true;
                    }
                }
            }
            return false;
        }

        class TDoubleObjectValueHashIterator extends THashPrimitiveIterator
                implements Iterator<V> {

            final TDoubleObjectHashMap _map;

            TDoubleObjectValueHashIterator(final TDoubleObjectHashMap map) {
                super(map);
                _map = map;
            }

            @SuppressWarnings({"unchecked", "unused"})
            protected V objectAtIndex(final int index) {
                final byte[] states = _states;
                final Object value = _map._values[index];
                if (states[index] != FULL) {
                    return null;
                }
                return (V) value;
            }

            /**
             * {@inheritDoc}
             */
            @SuppressWarnings("unchecked")
            public V next() {
                moveToNextIndex();
                return (V) _map._values[_index];
            }
        }
    }


    private abstract class MapBackedView<E> extends AbstractSet<E>
            implements Set<E>, Iterable<E> {

        public abstract Iterator<E> iterator();

        protected abstract boolean removeElement(E key);

        protected abstract boolean containsElement(E key);

        @SuppressWarnings({"unchecked"})
        public boolean contains(final Object key) {
            return containsElement((E) key);
        }

        @SuppressWarnings({"unchecked"})
        public boolean remove(final Object o) {
            return removeElement((E) o);
        }

        public void clear() {
            TDoubleObjectHashMap.this.clear();
        }

        public boolean add(final E obj) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return TDoubleObjectHashMap.this.size();
        }

        public Object[] toArray() {
            final Object[] result = new Object[size()];
            final Iterator<E> e = iterator();
            for (int i = 0; e.hasNext(); i++) {
                result[i] = e.next();
            }
            return result;
        }

        @SuppressWarnings({"unchecked", "Duplicates"})
        public <T> T[] toArray(T[] a) {
            final int size = size();
            if (a.length < size) {
                a = (T[]) java.lang.reflect.Array.newInstance(
                        a.getClass().getComponentType(), size);
            }

            final Iterator<E> it = iterator();
            final Object[] result = a;
            for (int i = 0; i < size; i++) {
                result[i] = it.next();
            }

            if (a.length > size) {
                a[size] = null;
            }

            return a;
        }

        public boolean isEmpty() {
            return TDoubleObjectHashMap.this.isEmpty();
        }

        public boolean addAll(final Collection<? extends E> collection) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings({"SuspiciousMethodCalls"})
        public boolean retainAll(final Collection<?> collection) {
            boolean changed = false;
            final Iterator<E> i = iterator();
            while (i.hasNext()) {
                if (!collection.contains(i.next())) {
                    i.remove();
                    changed = true;
                }
            }
            return changed;
        }
    }


    class TDoubleObjectHashIterator<Vv> extends THashPrimitiveIterator
            implements TDoubleObjectIterator<Vv> {

        /**
         * the collection being iterated over
         */
        private final TDoubleObjectHashMap<Vv> _map;

        /**
         * Creates an iterator over the specified map
         *
         * @param map map to iterate over.
         */
        TDoubleObjectHashIterator(final TDoubleObjectHashMap<Vv> map) {
            super(map);
            this._map = map;
        }


        public void advance() {
            moveToNextIndex();
        }


        public double key() {
            return _map._set[_index];
        }


        public Vv value() {
            return _map._values[_index];
        }


        public Vv setValue(final Vv val) {
            final Vv old = value();
            _map._values[_index] = val;
            return old;
        }
    }


    public void writeExternal(final ObjectOutput out) throws IOException {
        // VERSION
        out.writeByte(0);

        // SUPER
        super.writeExternal(out);

        // NO_ENTRY_KEY
        out.writeDouble(no_entry_key);

        // NUMBER OF ENTRIES
        out.writeInt(_size);

        // ENTRIES
        for (int i = _states.length; i-- > 0; ) {
            if (_states[i] == FULL) {
                out.writeDouble(_set[i]);
                out.writeObject(_values[i]);
            }
        }
    }


    @SuppressWarnings({"unchecked"})
    public void readExternal(final ObjectInput in)
            throws IOException, ClassNotFoundException {

        // VERSION
        in.readByte();

        // SUPER
        super.readExternal(in);

        // NO_ENTRY_KEY
        no_entry_key = in.readDouble();

        // NUMBER OF ENTRIES
        int size = in.readInt();
        setUp(size);

        // ENTRIES
        while (size-- > 0) {
            final double key = in.readDouble();
            final V val = (V) in.readObject();
            put(key, val);
        }
    }


    public String toString() {
        final StringBuilder buf = new StringBuilder("{");
        forEachEntry(new TDoubleObjectProcedure<V>() {
            private boolean first = true;

            public boolean execute(final double key, final Object value) {
                if (first) first = false;
                else buf.append(",");

                buf.append(key);
                buf.append("=");
                buf.append(value);
                return false;
            }
        });
        buf.append("}");
        return buf.toString();
    }
} // TDoubleObjectHashMap
