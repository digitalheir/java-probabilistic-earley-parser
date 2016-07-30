
package org.leibnizcenter.cfg.earleyparser.exception;


/**
 */
@SuppressWarnings("unused")
public class ParseException extends Exception {
    private static final long serialVersionUID = 1L;
    public ParseException(String message) {
        super(message);
    }
    public ParseException(Throwable cause) {
        super(cause);
    }
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
