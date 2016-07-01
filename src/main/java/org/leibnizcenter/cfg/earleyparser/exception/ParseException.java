
package org.leibnizcenter.cfg.earleyparser.exception;


/**
 * An exception thrown in the process of running Pep.
 *
 */
@SuppressWarnings("unused")
public class ParseException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a pep exception with the specified message.
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Creates a pep exception with the specified underlying cause.
     */
    public ParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a pep exception with the specified message and cause.
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
