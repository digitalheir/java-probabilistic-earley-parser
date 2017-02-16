
package org.leibnizcenter.cfg.rule;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.ErrorSection;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.stream.Stream;


/**
 * Represents a fallback rule for when a production encounters an error.
 *
 * @see Category
 * @see Rule
 * @see Grammar
 */
public class SynchronizingRule extends Rule {
    /**
     * Creates a new rule with the specified left side category and series of
     * category on the right side.
     *
     * @param left           The left side (trigger) for this production rule.
     * @param right          The right side (productions) licensed for this rule's left side.
     * @param rawProbability Double that reflects the probability of this rule according to some semiring
     *                       (for probability semiring, between 0.0 and 1.0; for Log semiring between 0 and infinity)
     */
    SynchronizingRule(double rawProbability, NonTerminal left, Category... right) {
        super(rawProbability, left, right);
        assert Stream.of(right).anyMatch(r -> r instanceof ErrorSection);
    }

    /**
     * Defaults to rule probability 1.0
     *
     * @param semiring Semiring to use, usually {@link org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring LogSemiring}
     * @param LHS      LHS
     * @param RHS      RHS
     * @return Rule with p = 1.0
     */
    public static SynchronizingRule create(DblSemiring semiring, NonTerminal LHS, Category... RHS) {
        return new SynchronizingRule(semiring.one(), LHS, RHS);
    }

    /**
     * @param semiring    semiring to use, usually you'll want {@link org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring LogSemiring}
     * @param probability probability (between 0.0 and 1.0)
     * @param LHS
     * @param RHS
     */
    public static SynchronizingRule create(DblSemiring semiring, double probability, NonTerminal LHS, Category... RHS) {
        return new SynchronizingRule(semiring.fromProbability(probability), LHS, RHS);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SynchronizingRule && super.equals(o);
    }
}
