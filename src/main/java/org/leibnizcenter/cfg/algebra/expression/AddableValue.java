package org.leibnizcenter.cfg.algebra.expression;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring.*;
import org.leibnizcenter.cfg.rule.Rule;

/**
 * Contains references to deferred computation, supplies with an addition method
 *
 * Created by Maarten on 23-8-2016.
 */
public class AddableValue extends ScoreRefs {

    public AddableValue(int capacity, ExpressionSemiring semiring) {
        super(capacity, semiring);
    }

    public void add(Rule rule, int index, int ruleStart, int dotPosition, Value addValue) {
        Value current = getExpression(rule, index, ruleStart, dotPosition);
        final Value newValue = addValue.plus(current);
        if (current == null) {
            current = newValue;
            setScore(rule, index, ruleStart, dotPosition, current);
        } else
            current.setExpression(newValue.getExpression());
    }

}
