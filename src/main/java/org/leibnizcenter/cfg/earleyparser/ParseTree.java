
package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedToken;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.LinkedList;
import java.util.List;

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
    public final LinkedList<ParseTree> children;

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
    public ParseTree(Category category, LinkedList<ParseTree> children) {
        this.category = category;
        this.children = children;
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
        children.addLast(tree);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParseTree parseTree = (ParseTree) o;

        return category.equals(parseTree.category) && (children != null ? children.equals(parseTree.children) : parseTree.children == null);

    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        toString(sb, "", true);
        return sb.toString();
    }

    private void toString(StringBuilder sb, String prefix, boolean isTail) {
        sb.append(prefix + (isTail ? "└── " : "├── ") + (
                category.toString() + ((this instanceof Token) ? (" (" + ((Token) this).token + ")") : "")
        ) + "\n");
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

    public static class Token<E> extends ParseTree {
        public final org.leibnizcenter.cfg.token.Token<E> token;

        public Token(org.leibnizcenter.cfg.token.Token<E> scannedToken, Category category) {
            super(category, null);
            this.token = scannedToken;
        }

        public Token(ScannedToken<E> scannedState) {
            this(scannedState.scannedToken, scannedState.scannedCategory);
        }


        @Override
        public int hashCode() {
            return super.hashCode() + token.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Token && super.equals(o) && token.equals(((Token) o).token);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class NonToken extends ParseTree {
        public NonToken(Category node) {
            super(node);
        }

        public NonToken(Category node, LinkedList<ParseTree> children) {
            super(node, children);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof NonToken && super.equals(o);
        }
    }
}
