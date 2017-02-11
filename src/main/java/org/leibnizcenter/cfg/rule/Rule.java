
package org.leibnizcenter.cfg.rule;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.Arrays;


/**
 * Represents a production rule in a {@link Grammar context-free grammar}.
 * <p>
 * Rules contain a single {@link Category category} on the
 * {@link Rule#getLeft() left side} that produces the series of category on
 * the {@link Rule#getRight() right side}. Rules that license empty productions
 * (have an empty right side) can be specified with a rule that has a right
 * side of length <code>1</code> whose member is an empty string. A rule that
 * contains a terminal on the right must contain <em>only</em> that terminal.
 * <p>
 * Rules are immutable and cannot be changed once instantiated.
 *
 * @see Category
 * @see Grammar
 */
public class Rule {

    public final NonTerminal left;
    public final Category[] right;
    /**
     * Double that reflects the probability of this rule according to some semiring
     * (for probability semiring, between 0.0 and 1.0; for Log semiring between 0 and infinity)
     */
    private final double rawProbability;
    private final int hashCode;

    /**
     * Creates a new rule with the specified left side category and series of
     * category on the right side.
     *
     * @param left           The left side (trigger) for this production rule.
     * @param right          The right side (productions) licensed for this rule's
     *                       left side.
     * @param rawProbability Double that reflects the probability of this rule according to some semiring
     *                       (for probability semiring, between 0.0 and 1.0; for Log semiring between 0 and infinity)
     * @throws IllegalArgumentException If
     *                                  <ol>
     *                                  <li>the specified left or right category are <code>null</code>,</li>
     *                                  <li>the right series is zero-length,</li>
     *                                  <li>the right side contains a <code>null</code> category.</li>
     *                                  </ol>
     */
    Rule(double rawProbability, NonTerminal left, Category... right) {
        this.rawProbability = rawProbability == -0.0 ? 0.0 : rawProbability;
        if (left == null) throw new IllegalArgumentException("empty left category");
        if (right == null || right.length == 0) throw new IllegalArgumentException("no right category");

        // check for nulls on right
        for (Category r : right)
            if (r == null) throw new IllegalArgumentException(
                    "right contains null category: " + Arrays.toString(right));

        //// check for multiple terminals
        // TODO what about "A rule that contains a terminal on the right must contain <em>only</em> that terminal."?
        // if (right.length > 0) for (Category r : right)
        //    if (r.isTerminal()) throw new IllegalArgumentException(
        //            "other category found in RHS in addition to terminal"
        //    );

        this.left = left;
        this.right = right;

        this.hashCode = computeHashCode();
    }


    /**
     * Instantiates a new rule with a rawProbability score of 1.0 (assuming we use the Probability semiring, which
     * has 1.0 for "one")
     *
     * @param left  LHS
     * @param right RHS
     */
    @Deprecated
    protected Rule(NonTerminal left, Category... right) {
        this(1.0, left, right);
    }

    /**
     * Instantiates a new rule with a rawProbability score of one (whatever that means for the given semiring)
     *
     * @param semiring Semiring to query for the rawProbability of "one"
     * @param left     LHS
     * @param right    RHS
     */
    @Deprecated
    protected Rule(DblSemiring semiring, NonTerminal left, Category... right) {
        this(semiring.one(), left, right);
    }

    @Deprecated
    public static Rule create(double probability, NonTerminal LHS, Category... RHS) {
        return new Rule(probability, LHS, RHS);
    }

    /**
     * Defaults to rule probability 1.0
     *
     * @param semiring Semiring to use, for example LogSemiring
     * @param LHS      LHS
     * @param RHS      RHS
     * @return Rule with p=1.0
     */
    public static Rule create(DblSemiring semiring, NonTerminal LHS, Category... RHS) {
        return new Rule(semiring.one(), LHS, RHS);
    }

    public static Rule create(DblSemiring semiring, double probability, NonTerminal LHS, Category... RHS) {
        return new Rule(semiring.fromProbability(probability), LHS, RHS);
    }

//    /**
//     * Tests whether this rule is a pre-terminal production rule. A rule is a
//     * preterminal rule if its right side contains a
//     * {@link Category#isTerminal(Category) terminal category}.
//     *
//     * @return <code>true</code> iff this rule's right side contains a
//     * terminal category.
//     */
//    public boolean isPreterminal() {
//        return isPreTerminal;
//    }
//
//    /**
//     * Tests whether this rule is a pre-terminal with a right side of length
//     * <code>1</code>.
//     *
//     * @see #isPreterminal()
//     * @see #getRight()
//     */
//    public boolean isSingletonPreterminal() {
//        return (isPreterminal() && right.length == 1);
//    }


    /**
     * Gets the active category in the underlying rule, if any.
     * Runs in O(1).
     *
     * @return The category at this dotted rule's
     * dot position in the underlying rule's
     * {@link Rule#getRight() right side category sequence}. If this rule's
     * dot position is already at the end of the right side category sequence,
     * returns <code>null</code>.
     */
    public Category getActiveCategory(int dotPosition) {
        if (dotPosition < 0 || dotPosition > right.length) throw new InvalidDotPosition(dotPosition, right);

        if (dotPosition < right.length) {
            Category returnValue = right[dotPosition];
            if (returnValue == null) throw new NullPointerException();
            else return returnValue;
        } else return null;
    }

    /**
     * Tests whether this is a completed edge or not. An edge is completed when
     * its dotted rule contains no
     * {@link #getActiveCategory(int) active category}, or equivalently the dot is at position == |RHS|.
     * Runs in O(1)
     *
     * @return <code>true</code> iff the active category of this edge's dotted
     * rule is <code>null</code>.
     */
    public boolean isPassive(int dotPosition) {
        if (dotPosition < 0 || dotPosition > right.length) throw new InvalidDotPosition(dotPosition, right);
        return dotPosition == right.length;
    }

    /**
     * Gets the left side category of this rule.
     */
    public NonTerminal getLeft() {
        return left;
    }

    /**
     * Gets the series of category on the right side of this rule.
     */
    public Category[] getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        else if (o.hashCode() != hashCode) return false;
        else {
            Rule rule = (Rule) o;
            if (right.length != rule.right.length) return false;
            else if (Double.compare(rule.rawProbability, rawProbability) != 0) return false;
            else if (!left.equals(rule.left)) return false;
            for (int i = 0; i < right.length; i++) if (!rule.right[i].equals(right[i])) return false;
            return true;
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int computeHashCode() {
        long temp = Double.doubleToLongBits(rawProbability);
        return 31 * (31 * left.hashCode() + Arrays.hashCode(right)) + (int) (temp ^ (temp >>> 32));
    }

    /**
     * Gets a string representation of this rule.
     *
     * @return &quot;<code>S → NP VP</code>&quot; for a rule with a left side
     * category of <code>S</code> and a right side sequence
     * <code>[NP, VP]</code>.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(left.toString());
        sb.append(" →");

        for (Category aRight : right) {
            sb.append(' '); // space between category
            sb.append(aRight.toString());
        }

        return sb.toString();
    }

    /**
     * Gets a string representation of this dotted rule.
     *
     * @return E.g. &quot;<code>S → NP · VP</code>&quot; for a dotted rule with
     * an underlying rule <code>S → NP VP</code> and a dot position
     * <code>1</code>.
     * @see Rule#toString()
     */
    public String toString(int dotPosition) {
        if (dotPosition < 0 || dotPosition > right.length) throw new InvalidDotPosition(dotPosition, right);
        StringBuilder sb = new StringBuilder(left.toString());
        sb.append(" →");

        for (int i = 0; i <= right.length; i++) {
            if (i == dotPosition) sb.append(" ·"); // insert dot at position

            if (i < right.length) {
                sb.append(' '); // space between categories
                sb.append(right[i].toString());
            }
        }

        return sb.toString();
    }

    /**
     * @return Double that reflects the probability of this rule according to some semiring
     * (for probability semiring, between 0.0 and 1.0; for Log semiring between 0 and infinity)
     */
    public double getScore() {
        return rawProbability;
    }

    /**
     * Runs in O(1)
     *
     * @return Whether this state is a unit production
     */
    public boolean isUnitProduction() {
        return getRight().length == 1 && getRight()[0] instanceof NonTerminal;
    }
}
