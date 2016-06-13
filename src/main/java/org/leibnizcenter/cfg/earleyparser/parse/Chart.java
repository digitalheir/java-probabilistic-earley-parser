
package org.leibnizcenter.cfg.earleyparser.parse;

import org.leibnizcenter.cfg.category.Category;

import java.util.*;
import java.util.stream.Collectors;


/**
 * A chart produced by an {@link ParseTokens Earley parser}.
 * <p>
 * Charts contain sets of {@link Edge edges} mapped to the string indices where
 * they originate. Since the edge sets are {@link Set sets}, an edge can only
 * be added at a given index once (as sets do not permit duplicate members).
 * Edge sets are not guaranteed to maintain edges in their order of insertion.
 *
 */
public class Chart {
    private static final int NULL_INDEX = -1;

    public final SortedMap<Integer, Set<Edge>> edgeSets;

    /**
     * Creates a new chart, initializing its internal data structure.
     */
    public Chart() {
        this(new TreeMap<>());
    }

    /**
     * Creates a new chart based on the specified chart. The newly created
     * chart contains all the edges as the specified chart at all the same
     * indices.
     *
     * @param chart The chart to base the newly created chart upon.
     */
    public Chart(Chart chart) {
        this(new TreeMap<>(chart.edgeSets));
    }

    /**
     * Creates a new chart from the specified sorted map of indices mapped to
     * edge sets.
     *
     * @param edgeSets The map of integer-mapped edge sets to use as this
     *                 chart's backing data structure.
     */
    public Chart(SortedMap<Integer, Set<Edge>> edgeSets) {
        this.edgeSets = edgeSets;
    }

    /**
     * Gets the set of indices at which this chart contains edges. For any
     * member of this set, {@link #getEdges(int)} will return a non-empty
     * set of edges.
     *
     * @return A set containing every index in this chart where edges have
     * been added, sorted in ascending order (<code>0 ... <em>n</em></code>).
     */
    public Set<Integer> indices() {
        return edgeSets.keySet();
    }

    /**
     * Gets the first index in this chart that contains edges.
     *
     * @return The minimal member of {@link #indices()}. In most cases, this
     * will return <code>0</code> (unless this chart is a
     * {@link #subChart(int, int) subchart} of another chart).
     */
    public int firstIndex() {
        return edgeSets.firstKey();
    }

    /**
     * Gets the last index in this chart that contains edges.
     *
     * @return The maximal member of {@link #indices()}.
     */
    public int lastIndex() {
        return edgeSets.lastKey();
    }

    /**
     * Gets a sub chart of this chart.
     *
     * @param from The low endpoint (inclusive) of the sub chart.
     * @param to   The high endpoint (exclusive) of the subchart.
     * @return A new chart containing only the edge sets in this chart where
     * <code>from &lt;= index &lt; to</code>.
     * @throws NullPointerException If either <code>from</code> or
     *                              <code>to</code> is <code>null</code>.
     * @see java.util.SortedMap#subMap(Object, Object)
     */
    public Chart subChart(int from, int to) {
        return new Chart(edgeSets.subMap(from, to));
    }

    /**
     * Gets a head chart of this chart (a chart containing only the indices
     * from <code>0</code> to <code>to</code>).
     *
     * @param to The high endpoint (exclusive) of the new chart.
     * @return A chart containing all the indices strictly less than
     * <code>to</code>.
     * @see java.util.SortedMap#headMap(Object)
     */
    public Chart headChart(int to) {
        return new Chart(edgeSets.headMap(to));
    }

    /**
     * Gets a tail chart of this chart (a chart containing only the indices
     * from <code>from</code> to the size of its {@link #indices()}).
     *
     * @param from The low endpoint (inclusive) of the new chart.
     * @return A chart containing all the indices greater than or equal to
     * <code>from</code>.
     * @see java.util.SortedMap#tailMap(Object)
     */
    public Chart tailChart(int from) {
        return new Chart(edgeSets.tailMap(from));
    }

    /**
     * Tests whether this chart contains the specified edge.
     *
     * @param edge The edge to test whether this chart contains.
     * @return true iff this chart contains the specified edge at some index.
     * @see #indexOf(Edge)
     */
    public boolean contains(Edge edge) {
        return indexOf(edge) != Chart.NULL_INDEX;
    }

    /**
     * Gets the {@link #indices() index} of the specified edge in this
     * chart.
     *
     * @param edge The edge to find the index of.
     * @return The index of the specified edge, or <code>-1</code> if the
     * specified edge is <code>null</code> or is not contained in this chart.
     */
    public int indexOf(Edge edge) {
        if (edge != null) {
            for (Map.Entry<Integer, Set<Edge>> entry : edgeSets.entrySet()) {
                if (entry.getValue().contains(edge)) {
                    return entry.getKey();
                }
            }
        }

        return Chart.NULL_INDEX;
    }

    /**
     * Removes all edges from this map at all indices (if any are present).
     */
    public void clear() {
        edgeSets.clear();
    }

    /**
     * Tests whether this chart contains any edges at any index.
     *
     * @return <code>true</code> if an edge is present at some index,
     * <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return edgeSets.isEmpty();
    }

    /**
     * Tests whether this chart contains any edges at a given string index.
     *
     * @param index The string index to check for edges.
     * @return <code>true</code> iff this chart contains an edge set at
     * <code>index</code>.
     */
    public boolean containsEdges(int index) {
        return edgeSets.containsKey(index);
    }

    /**
     * Counts the total number of edges contained in this chart, at any
     * index.
     *
     * @return The total number of edges contained.
     */
    public int countEdges() {
        int count = 0;

        for (Set<Edge> edgeSet : edgeSets.values()) {
            count += edgeSet.size();
        }

        return count;
    }

    /**
     * Gets the edges in this chart at a given index.
     *
     * @param index The index to return edges for.
     * @return The {@link Set set} of {@link Edge edges} this chart contains
     * at <code>index</code>, or <code>null</code> if no edge set exists in
     * this chart for the given index. The edge set returned by this
     * method is <em>not</em> guaranteed to contain the edges in the order in
     * which they were {@link #addEdge(int, Edge) added}. This method
     * returns a set of edges that is not modifiable.
     * @throws NullPointerException If <code>index</code> is <code>null</code>.
     * @see java.util.Collections#unmodifiableSet(Set)
     */
    public Set<Edge> getEdges(int index) {
        return Collections.unmodifiableSet(edgeSets.get(index));
    }

    /**
     * Adds an edge to this chart at the given index. If no other edges
     * exist in this chart at the same index, a new edge set is created before
     * adding the edge.
     *
     * @param index The index for <code>edge</code>.
     * @param edge  The edge to add.
     * @return <code>true</code> iff this chart did not already contain
     * <code>edge</code> at the given index.
     * @throws IndexOutOfBoundsException If <code>index < 0</code>.
     * @throws NullPointerException      If <code>index</code> or
     *                                   <code>edge</code> is <code>null</code>.
     */
    public boolean addEdge(int index, Edge edge) {
        if (index < 0) throw new IndexOutOfBoundsException("invalid index: " + index);
        if (edge == null) throw new NullPointerException("null edge");


        Set<Edge> edges = edgeSets.get(index); // already edges at index?
        if (edges == null) { // create and add edge set if none exists at index
            edges = new HashSet<>();
            edgeSets.put(index, edges);
        }

        return edges.add(edge); // always true for new edge set
    }

    /**
     * Tests whether this chart is equal to another by comparing their
     * internal data structures.
     *
     * @return <code>true</code> iff the specified object is an instance of
     * <code>Chart</code> and it contains the same edges at the same indices
     * as this chart.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Chart && edgeSets.equals(((Chart) obj).edgeSets));
    }

    /**
     * Computes a hash code for this chart based on its internal data
     * structure.
     */
    @Override
    public int hashCode() {
        return 37 * (1 + edgeSets.hashCode());
    }

    /**
     * Gets a string representation of this chart.
     */
    @Override
    public String toString() {
        return edgeSets.toString();
    }

    public Set<Edge> getPassiveEdges(int index) {
        return getEdges(index).stream().filter(Edge::isPassive).collect(Collectors.toSet()); // TODO store map with passive edges
    }

    public Iterable<? extends Edge> getEdgesWithActiveCategory(int index, Category category) {
        return getEdges(index).stream()
                .filter(e -> (e.getActiveCategory().filter(category::equals).isPresent()))
                .collect(Collectors.toSet()); // TODO store map with passive edges
    }
}
