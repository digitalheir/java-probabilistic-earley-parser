//
//package org.leibnizcenter.cfg.rule;
//
//
//import org.leibnizcenter.cfg.category.Category;
//
//import org.leibnizcenter.cfg.earleyparser.parse.Edge;
//
///**
// * Extension of {@link Rule} that maintains a dot position within the
// * rule.
// * <p>
// * Dotted rules are used by {@link EarleyParser Earley parsers} to keep
// * track of how far within a rule processing has succeeded. In a dotted rule,
// * the {@link DottedEdge#getActiveCategory() active category} is the first
// * category after the {@link DottedEdge#getPosition() dot position}, and is
// * <code>null</code> when processing has fully covered the underlying rule.
// * {@link Edge Edges} test the active category of dotted rules to determine
// * when an edge is active or passive.
// *
// // // * @see Rule
// * @see Edge
// */
//@Deprecated
//public class DottedRule extends Rule {
//    public int position;
//    public Category activeCategory;
//
//    /**
//     * Creates a new dotted rule for the given rule, with a dot position at
//     * the beginning of the rule's right side (position <code>0</code>).
//     *
//     * @see DottedRule#DottedRule(Rule, int)
//     */
//    public DottedRule(Rule rule) {
//        this(rule, 0);
//    }
//
//    /**
//     * Creates a dotted rule maintaining the dot position within the right side
//     * category sequence of the underlying rule.
//     *
//     * @param rule     The underlying rule.
//     * @param position The zero-based position within <code>rule</code> right
//     *                 side category where this dotted rule's dot is maintained.
//     * @throws IndexOutOfBoundsException If <code>position &lt; 0</code> or
//     *                                   <code>position</code> is greater than the length of the
//     *                                   {@link Rule#getRight() right side sequence} in <code>rule</code>.
//     */
//    public DottedRule(Rule rule, int position) {
//        super(rule.left, rule.right);
//
//        if (position < 0 || position > right.length) throw new IndexOutOfBoundsException(
//                "illegal position: " + position);
//
//        this.position = position;
//
//        // determine active category
//        activeCategory = (position < right.length) ? right[position] : null;
//    }
//
//    /**
//     * Creates and returns a new dotted rule exactly like the one provided
//     * except that its {@link #getPosition() dot position} is advanced by
//     * <code>1</code>.
//     *
//     * @param dottedRule The dotted rule whose dot position should be advanced.
//     * @return A new dotted rule wrapping this rule with its position
//     * incremented.
//     * @throws IndexOutOfBoundsException If thw dotted rule's dot position
//     *                                   is already at the end of its right side.
//     * @see #DottedRule(Rule, int)
//     //     */
//    public static DottedRule advanceDot(DottedRule dottedRule) {
//        return new DottedRule(dottedRule, dottedRule.position + 1);
//    }
//
//    /**
//     * Creates a new start rule for a given seed category.
//     *
//     * @param seed The seed category to use.
//     * @return A dotted rule that has the {@link Category#START special start
//     * rule} on the {@link #getLeft() left} and the specified <code>seed</code>
//     * on the {@link #getRight() right}. This method is used by Earley parsers
//     * for seeding.
//     * @throws NullPointerException     If the seed category is <code>null</code>.
//     * @throws IllegalArgumentException If the seed category is a
//     *                                  {@link Category#isTerminal(Category) terminal}.
//     * @see Category#START
//     * @see EarleyParser#parse(Iterable, Category)
//     */
//    public static DottedRule startRule(Category seed) {
//        if (seed == null) throw new NullPointerException("null seed");
//        if (Category.isTerminal(seed)) throw new IllegalArgumentException("seed is a terminal: " + seed);
//
//        return new DottedRule(new Rule(Category.START, seed), 0);
//    }
//
//    /**
//     * Gets the dot position within the underlying rule's
//     * {@link Rule#getRight() right side category sequence}.
//     *
//     * @return The dot position that was specified for this dotted rule when
//     * it was created.
//     */
//    public int getPosition() {
//        return position;
//    }
//
//    /**
//     * Gets the active category in the underlying rule, if any.
//     *
//     * @return The category at this dotted rule's
//     * {@link #getPosition() dot position} in the underlying rule's
//     * {@link Rule#getRight() right side category sequence}. If this rule's
//     * dot position is already at the end of the right side category sequence,
//     * returns <code>null</code>.
//     */
//    public Category getActiveCategory() {
//        return activeCategory;
//    }
//
//    /**
//     * Tests whether this dotted rule is equal to another dotted rule by
//     * comparing their underlying rules and dot positions.
//     *
//     * @return <code>true</code> iff the specified object is an instance of
//     * <code>DottedRule</code> and its underlying rule and position are equal
//     * to this dotted rule's rule and position.
//     * @see Rule#equals(Object)
//     */
//    @Override
//    public boolean equals(Object obj) {
//        return (obj instanceof DottedRule && super.equals(obj)
//                && position == ((DottedRule) obj).position);
//    }
//
//    /**
//     * Computes a hash code for this dotted rule based on its underlying rule
//     * and dot position.
//     *
//     * @see Rule#hashCode()
//     */
//    @Override
//    public int hashCode() {
//        return (super.hashCode() * (31 + position));
//    }
//
//    /**
//     * Gets a string representation of this dotted rule.
//     *
//     * @return &quot;<code>S -> NP * VP</code>&quot; for a dotted rule with
//     * an underlying rule <code>S -> NP VP</code> and a dot position
//     * <code>1</code>.
//     * @see Rule#toString()
//     */
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder(left.toString());
//        sb.append(" ->");
//
//        for (int i = 0; i <= right.length; i++) {
//            if (i == position) {
//                sb.append(" *"); // insert dot at position
//            }
//
//            if (i < right.length) {
//                sb.append(' '); // space between category
//                sb.append(right[i].toString());
//            }
//        }
//
//        return sb.toString();
//    }
//}
