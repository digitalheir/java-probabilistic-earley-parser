
package org.leibnizcenter.cfg.earleyparser.exception;


/**
 * An exception thrown in the process of running Pep.
 *
 */
@SuppressWarnings("unused")
public class PepException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a pep exception with the specified message.
     */
    public PepException(String message) {
        super(message);
    }

    /**
     * Creates a pep exception with the specified underlying cause.
     */
    public PepException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a pep exception with the specified message and cause.
     */
    public PepException(String message, Throwable cause) {
        super(message, cause);
    }

}
