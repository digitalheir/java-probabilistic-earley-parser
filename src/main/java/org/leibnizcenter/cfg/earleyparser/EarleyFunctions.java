package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.event.EdgeEvent;
import org.leibnizcenter.cfg.earleyparser.event.ParserListener;
import org.leibnizcenter.cfg.earleyparser.exception.PepException;
import org.leibnizcenter.cfg.earleyparser.parse.Chart;
import org.leibnizcenter.cfg.earleyparser.parse.Edge;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.Tokens;
import org.leibnizcenter.cfg.util.HashSets;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core function for the Earley algorithm. Function are static so that it is easier to reason about state.
 * <p>
 * Created by maarten on 13-6-16.
 */
public class EarleyFunctions {
    private EarleyFunctions() {
        throw new Error();
    }

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param chart    The chart to fill.
     * @param index    The index to make completions at.
     * @param listener Notified of any added edges. May be null.
     */
    public static void complete(Chart chart, int index, ParserListener listener) {
        if (chart.containsEdges(index)) {
            Set<Edge> passiveEdgesToCompleteOn = chart.getPassiveEdges(index);

            while (passiveEdgesToCompleteOn != null && passiveEdgesToCompleteOn.size() > 0) {

                Set<Edge> newEdges = null;
                for (Edge edge : passiveEdgesToCompleteOn) {
                    int origin = edge.origin;
                    if (!edge.isPassive()) throw new Error("can only make completions based on passive edges");
                    // get all edges at this edge's origin
                    for (Edge edgeToAdvance : chart.getEdgesWithActiveCategory(origin, edge.rule.left)) {

                        // add new edge with dot advanced by one if same
                        Edge newEdge = Edge.complete(edgeToAdvance, edge);
                        newEdges = HashSets.add(newEdges, newEdge);
                    }
                }

                passiveEdgesToCompleteOn = null;
                if (newEdges != null) {
                    passiveEdgesToCompleteOn = newEdges.stream()
                            // only notify and recursively complete if the chart did not already contain this edge
                            .filter(newEdge -> {
                                boolean added = chart.addEdge(index, newEdge);
                                if (added && listener != null) listener.edgeCompleted(new EdgeEvent(index, newEdge));

                                return added && newEdge.isPassive();
                            })
                            .collect(Collectors.toSet());
                }
            }
        }
    }

    /**
     * Handles a token scanned from the input string, making completions (and
     * adding edges to the chart) as needed.
     *
     * @param chart The chart to fill.
     * @param index The start index of the scan.
     * @param token The token that was scanned.
     * @throws PepException If <code>token</code> is </code>null</code>.
     */
    public static void scan(Grammar grammar, Chart chart, int index, Token token, boolean predictPreterm, ParserListener listener) throws PepException {
        if (token == null) throw new PepException("null token at index " + index);

        if (chart.containsEdges(index)) { // any predictions at this index?
            Set<Edge> edges = chart.getEdges(index);

            // just-in-time prediction
            if (!predictPreterm) { // using array avoids comodification problems // TODO ... and is unefficient
                for (Edge edge : edges.toArray(new Edge[edges.size()])) {
                    Optional<Category> activeCategory = Edge.getActiveCategory(edge);
                    if (activeCategory.isPresent()) {
                        Rule r = grammar.getSingletonPreterminal(
                                activeCategory.get(),
                                token
                        );
                        if (r != null) {
                            Edge pt = Edge.predictFor(r, index);
                            if (chart.addEdge(index, pt) && listener != null)
                                listener.edgePredicted(new EdgeEvent(index, pt));
                        }
                    }
                }
            }

            for (Edge edge : edges.toArray(new Edge[edges.size()])) {
                // completions for active edges only
                Optional<Category> activeCategory = Edge.getActiveCategory(edge);
                if (activeCategory.isPresent() && Tokens.hasCategory(token, activeCategory.get())) {
                    Edge newEdge = Edge.scan(edge, token);
                    int successor // save next index
                            = index + 1;
                    if (chart.addEdge(successor, newEdge) && listener != null)
                        listener.edgeScanned(new EdgeEvent(index, newEdge));
                }
            }
        }
    }

    /**
     * Makes predictions (adds edges) in the specified chart for a given edge
     * at a given index. This method is recursively called whenever an edge is
     * added to also make predictions for the newly added edge.
     *
     * @param chart The chart to fill.
     * @param edge  The edge to make predictions for.
     * @param index The index in the string under consideration.
     */
    public static void predictForEdge(Grammar grammar, Chart chart, Edge edge, int index, boolean predictPreterm, ParserListener listener) {
        Optional<Category> oactive = Edge.getActiveCategory(edge); // not present if passive

        if (oactive.isPresent() && grammar.containsRules(oactive.get())) {
            Category active = oactive.get();
            // get all rules with the active category on the left
            for (Rule rule : grammar.getRules(active)) {
                if (!predictPreterm && rule.isPreterminal()) {
                    // only predict for rules that aren't preterminals to avoid
                    // filling up the chart with entries for every terminal
                    continue;
                }

                // make new edge at index with dotted rule at position 0
                Edge newEdge = Edge.predictFor(rule, index);
                // only predict for edges the chart did not already contain
                if (chart.addEdge(index, newEdge)) {
                    if (listener != null) listener.edgePredicted(new EdgeEvent(index, newEdge));
                    // recursively predict for the new edge
                    predictForEdge(grammar, chart, newEdge, index, predictPreterm, listener);
                }
            }
        }
    }

    /**
     * Makes predictions in the specified chart at the given index.
     *
     * @param chart The chart to fill with predictions at <code>index</code>.
     * @param index The string index to make predictions at.
     */
    public static void predict(Grammar grammar, Chart chart, int index, boolean predictPreterm, ParserListener listener) {
        if (chart.containsEdges(index)) { // any edges at this index?
            // avoid concurrently modifying chart by getting array // todo more efficient pls
            Set<Edge> edges = chart.getEdges(index);
            for (Edge edge : edges.toArray(new Edge[edges.size()])) {
                predictForEdge(grammar, chart, edge, index, predictPreterm, listener); // predict for each edge
            }
        }
    }
}
