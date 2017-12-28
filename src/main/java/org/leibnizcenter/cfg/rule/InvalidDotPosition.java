package org.leibnizcenter.cfg.rule;

import org.leibnizcenter.cfg.category.Category;

import java.security.InvalidParameterException;

/**
 * Exception that dot position is invalid
 *
 * Created by maarten on 24-6-16.
 */
class InvalidDotPosition extends InvalidParameterException {
    InvalidDotPosition(final int dotPosition, final Category[] right) {
        super("Dot could not be placed at position " + dotPosition + " for a RHS of length " + right.length);
    }
}
