
package org.leibnizcenter.cfg.earleyparser.parse;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
//TODO
/**
 * A parse tree that represents the derivation of a string based on the
 * rules in a {@link Grammar}. Parse trees recursively contain
 * {@link #getChildren() other parse trees}, so they can be iterated through to
 * find the entire derivation of a category.
 * <p>
 * Parse trees are essentially partial views of a {@link Chart} from a
 * given {@link State} or {@link Category}. They represent the completed
 * category at a given string index and origin position. The special
 * {@link Category#START} category is not included in a parse tree at the root
 * (only category that are actually specified in the corresponding grammar
 * are represented).
 *
 */
public class ParseTree {
    final Category node;
    final Deque<ParseTree> children;

    /**
     * Creates a new parse tree with the specified category and parent parse
     * tree.
     */
    public ParseTree(Category node) {
        this(node,  new ArrayDeque<>());
    }

    /**
     * Creates a new parse tree with the specified category, parent, and
     * child trees.
     *
     * @param node     The category of the {@link #getNode() node} of this parse
     *                 tree.
     * @param children The list of children of this parse tree, in their linear
     *                 order.
     */
    public ParseTree(Category node, Deque<ParseTree> children) {
        this.node = node;
        this.children = children;
    }

    /**
     * Creates a new parse tree based on the specified edge and parent tree.
     *
     * @param edge   The edge to use to create a parse tree. For a parse tree
     *               that is the root, this should be <code>null</code>.
     * @param parent The parent tree of the new parse tree.
     * @return A new parse tree whose {@link #getNode() node} is the
     * specified edge's dotted rule's left side and whose children are based
     * on the {@link State#getBases() bases} of the specified edge.
     */
    public static ParseTree newParseTree(State edge, ParseTree parent) {
//        State e;
//        ParseTree parentTree;
//
//        if (edge.rule.left.equals(START)) { // first child if START
//            e = edge.bases.iterator().next();
//            parentTree = null;
//        } else {
//            e = edge;
//            parentTree = (parent != null && parent.node.equals(START))
//                    ? null : parent;
//        }
//
//        Rule dr = e.rule;
//        ParseTree newTree;
//
//        Optional<Category> activeCategory = e.getActiveCategory();
//        if (!activeCategory.isPresent()) { // basis from a completion?
//            int basisCount = e.bases.size();
//            newTree = new ParseTree(dr.left, parentTree, (basisCount == 0) ? null : new ParseTree[basisCount]);
//
//            if (basisCount > 0) {
//                int i = 0;
//                for (State base : e.bases) {
//                    newTree.children[i] = ParseTree.newParseTree(base, newTree);
//                    i++;
//                }
//            }
//        } else { // from a scan
//            newTree = new ParseTree(activeCategory.get(), parentTree, null);
//        }
//
//        return newTree;
        throw new NotImplementedException();
    }

    /**
     * Gets the node category of this parse tree.
     *
     * @return <code>NP</code> for a subtree <code>NP -> Det N</code>.
     */
    public Category getNode() {
        return node;
    }

    /**
     * Gets the child parse trees of this parse tree, retaining their linear
     * ordering.
     *
     * @return For a subtree <code>NP -> Det N</code>, returns an array
     * that contains parse trees whose {@link #getNode() node} is
     * <code>Det, N</code> in that order, or <code>null</code> if this parse
     * tree has no children.
     */
    public Deque<ParseTree> getChildren() {
        return children;
    }


    /**
     * Gets a string representation of this parse tree.
     *
     * @return For the string &quot;the boy left&quot;, possibly something like:
     * <blockquote><code>[S[NP[Det[the]][N[boy]]][VP[left]]]</code></blockquote>
     * (The actual string would depend on the grammar rules in effect for the
     * parse).
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(node.toString());

        // recursively append children
        if (children != null)  for (ParseTree child : children) sb.append(child.toString());

        sb.append(']');

        return sb.toString();
    }

    public void addRightMost(ParseTree tree) {
        children.addLast(tree);
    }
}
