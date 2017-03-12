
package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedToken;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A parse tree that represents the derivation of a string based on the
 * rules in a {@link Grammar}. Parse trees recursively contain
 * {@link #getChildren() other parse trees}, so they can be iterated through to
 * find the entire derivation of a category.
 * <p>
 * Parse trees are essentially partial views of a Chart from a
 * given {@link State} or {@link Category}. They represent the completed
 * category at a given string index and origin position. The special
 * {@link Category#START} category is not included in a parse tree at the root
 * (only category that are actually specified in the corresponding grammar
 * are represented).
 */
public abstract class ParseTree {
    public final Category category;
    @SuppressWarnings("WeakerAccess")
    public final List<ParseTree> children;

    /**
     * Creates a new parse tree with the specified category and parent parse
     * tree.
     */
    @SuppressWarnings("WeakerAccess")
    public ParseTree(Category category) {
        this(category, new LinkedList<>());
    }

    /**
     * Creates a new parse tree with the specified category, parent, and
     * child trees.
     *
     * @param category The category of the {@link #getCategory() category} of this parse
     *                 tree.
     * @param children The list of children of this parse tree, in their linear
     *                 order.
     */
    @SuppressWarnings("WeakerAccess")
    public ParseTree(Category category, List<ParseTree> children) {
        this.category = category;
        this.children = children;
    }

    private static Stream<ParseTree> getFlattenedStream(
            BiFunction<List<ParseTree>, ParseTree, FlattenOption> subTreesToKeep,
            List<ParseTree> parents,
            ParseTree tree) {
        switch (subTreesToKeep.apply(parents, tree)) {
            case REMOVE:
                return Stream.empty();
            case KEEP:
                return Stream.of(tree.flatten(parents, subTreesToKeep));
            case KEEP_ONLY_CHILDREN:
                return tree.children.stream().flatMap(c -> getFlattenedStream(subTreesToKeep, parents, c));
            default:
                throw new NullPointerException();
        }
    }

    /**
     * Gets the category category of this parse tree.
     *
     * @return <code>NP</code> for a subtree <code>NP -> Det N</code>.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Gets the child parse trees of this parse tree, retaining their linear
     * ordering.
     *
     * @return For a subtree <code>NP -> Det N</code>, returns an array
     * that contains parse trees whose {@link #getCategory() node} is
     * <code>Det, N</code> in that order, or <code>null</code> if this parse
     * tree has no children.
     */
    @SuppressWarnings("WeakerAccess")
    public List<ParseTree> getChildren() {
        return children;
    }

    void addRightMost(ParseTree tree) {
        children.add(tree);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParseTree parseTree = (ParseTree) o;

        return category.equals(parseTree.category) && (children != null ? children.equals(parseTree.children) : parseTree.children == null);

    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(500);
        toString(sb, "", true);
        return sb.toString();
    }

    private void toString(StringBuilder sb, String prefix, boolean isTail) {
        sb.append(prefix)
                .append(isTail ? "└── " : "├── ")
                .append(category.toString())
                .append((this instanceof Leaf) ? (" (" + ((Leaf) this).token + ')') : "")
                .append('\n');
        if (children != null) {
            for (int i = 0; i < children.size() - 1; i++) {
                children.get(i).toString(sb, prefix + (isTail ? "    " : "│   "), false);
            }
            if (children.size() > 0) {
                children.get(children.size() - 1)
                        .toString(sb, prefix + (isTail ? "    " : "│   "), true);
            }
        }
    }

    @Override
    public int hashCode() {
        int result = category.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("unused")
    public boolean hasChildren() {
        return children == null || children.size() > 0;
    }

    /**
     * Removes all superfluous categories from this tree. What is superfluous is determined by given function.
     *
     * @param subTreesToKeep A function that returns the {@link FlattenOption} that specifies how to deal with the given tree node.
     * @return Newly instantiated tree
     */
    public ParseTree flatten(BiFunction<List<ParseTree>, ParseTree, FlattenOption> subTreesToKeep) {
        return flatten(new ArrayList<>(), subTreesToKeep);
    }

    private ParseTree flatten(List<ParseTree> parents, BiFunction<List<ParseTree>, ParseTree, FlattenOption> subTreesToKeep) {
        List<ParseTree> parents2 = new ArrayList<>(parents);
        parents2.add(this);


        return this instanceof NonLeaf ? new NonLeaf(
                category,
                children.stream()
                        .flatMap(child -> getFlattenedStream(subTreesToKeep, parents2, child))
                        .collect(Collectors.toList())
        ) : this;
    }

    /**
     * Specifies how to {@link #flatten(List, BiFunction) flatten} a {@link ParseTree}.
     */
    public enum FlattenOption {
        /**
         * Remove node and all children
         */
        REMOVE,
        /**
         * Retain node and all children
         */
        KEEP,
        /**
         * Remove node, but keep children
         */
        KEEP_ONLY_CHILDREN
    }

    public static class Leaf<E> extends ParseTree {
        public final org.leibnizcenter.cfg.token.Token<E> token;

        @SuppressWarnings("WeakerAccess")
        public Leaf(org.leibnizcenter.cfg.token.Token<E> scannedToken, Category category) {
            super(category, null);
            this.token = scannedToken;
        }

        @SuppressWarnings("WeakerAccess")
        public Leaf(ScannedToken<E> scannedState) {
            this(scannedState.scannedToken, scannedState.scannedCategory);
        }


        @Override
        public int hashCode() {
            return super.hashCode() + token.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Leaf && super.equals(o) && token.equals(((Leaf) o).token);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class NonLeaf extends ParseTree {
        public NonLeaf(Category node) {
            super(node);
        }

        public NonLeaf(Category node, List<ParseTree> children) {
            super(node, children);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof NonLeaf && super.equals(o);
        }

        @Override
        public String toString() {
            if (super.category instanceof NonLexicalToken)
                return "[!" + super.toString() + ']';
            else
                return super.toString();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
