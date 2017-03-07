
package org.leibnizcenter.cfg.rule;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonLexicalToken;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.stream.Stream;


/**
 * Represents a fallback rule for when the parser encounters a lexical error.
 *
 * @see Category
 * @see Rule
 * @see Grammar
 */
public class LexicalErrorRule extends Rule {

    LexicalErrorRule(double rawProbability, NonTerminal left, Category... right) {
        super(rawProbability, left, right);
        assert Stream.of(right).anyMatch(r -> r instanceof NonLexicalToken);
    }

    /**
     * Defaults to rule probability 1.0
     *
     * @param semiring Semiring to use, usually {@link org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring LogSemiring}
     * @param LHS      LHS
     * @param RHS      RHS
     * @return Rule with p = 1.0
     */
    public static LexicalErrorRule create(DblSemiring semiring, NonTerminal LHS, Category... RHS) {
        return new LexicalErrorRule(semiring.one(), LHS, RHS);
    }

    /**
     * @param semiring    semiring to use, usually you'll want {@link org.leibnizcenter.cfg.algebra.semiring.dbl.LogSemiring LogSemiring}
     * @param probability probability (between 0.0 and 1.0)
     * @param LHS
     * @param RHS
     */
    public static LexicalErrorRule create(DblSemiring semiring, double probability, NonTerminal LHS, Category... RHS) {
        return new LexicalErrorRule(semiring.fromProbability(probability), LHS, RHS);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LexicalErrorRule && super.equals(o);
    }

}
