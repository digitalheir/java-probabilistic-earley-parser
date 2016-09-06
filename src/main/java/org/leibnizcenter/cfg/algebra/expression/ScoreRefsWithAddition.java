package org.leibnizcenter.cfg.algebra.expression;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring.*;
import org.leibnizcenter.cfg.rule.Rule;

/**
 * Created by Maarten on 23-8-2016.
 */
public class ScoreRefsWithAddition extends ScoreRefs {

    public ScoreRefsWithAddition(int capacity, ExpressionSemiring semiring) {
        super(capacity, semiring);
    }

    public void add(Rule rule, int index, int ruleStart, int dotPosition, Value addValue) {
        Value current = getExpression(rule, index, ruleStart, dotPosition);
        final Value newValue = addValue.plus(current);
        if (current == null) {
            current = newValue;
            setScore(rule, index, ruleStart, dotPosition, current);
        } else current.setExpression(newValue.getExpression());
    }

}
