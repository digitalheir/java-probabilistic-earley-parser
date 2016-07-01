
package org.leibnizcenter.cfg.rule;


import org.leibnizcenter.cfg.earleyparser.chart.State;

/**
 * Helper methods to work with a rule dotted that is not encapsulated in a class for performance reasons.
 * <p>
 * Extension of {@link Rule} that maintains a dot position within the
 * rule.
 * <p>
 * Dotted rules are used by {@link EarleyParser Earley parsers} to keep
 * track of how far within a rule processing has succeeded. In a dotted rule,
 * the {@link Rule#getActiveCategory(Rule, int) active category} is the first
 * category after the dot position, and is
 * <code>null</code> when processing has fully covered the underlying rule.
 * {@link Edge Edges} test the active category of dotted rules to determine
 * when an edge is active or passive.
 *
 * @see Rule
 * @see State
 */
@Deprecated
public final class PrimitiveDottedRule {
    private PrimitiveDottedRule() {
        throw new Error();
    }

    //    /**
//     * Creates a new start rule for a given seed category.
//     *
//     * @param seed The seed category to use.
//     * @return A dotted rule that has the {@link Category#START special start
//     * rule} on the {@link Rule#getLeft() left} and the specified <code>seed</code>
//     * on the {@link Rule#getRight() right}. This method is used by Earley parsers
//     * for seeding.
//     * @throws NullPointerException     If the seed category is <code>null</code>.
//     * @throws IllegalArgumentException If the seed category is a
//     *                                  {@link Category#isTerminal(Category) terminal}.
//     * @see Category#START
//     * @see EarleyParser#parse(Iterable, Category)
//     */
//    public static 666 startRule(Category seed) {
//        if (seed == null) throw new NullPointerException("null seed");
//        if (Category.isTerminal(seed)) throw new IllegalArgumentException("seed is a terminal: " + seed);
//
//        return new 666 (new Rule(Category.START, seed), 0);
//    }

}
