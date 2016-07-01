package org.leibnizcenter.cfg.rule;

import org.leibnizcenter.cfg.category.Category;

import java.security.InvalidParameterException;

/**
 * Created by maarten on 24-6-16.
 */
public class InvalidDotPosition extends InvalidParameterException {
    public InvalidDotPosition(int dotPosition, Category[] right) {
        super("Dot could not be placed at position " + dotPosition + " for a RHS of length " + right.length);
    }
}
