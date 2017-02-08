package org.leibnizcenter.cfg.earleyparser.scan;

/**
 * Created by maarten on 8-2-17.
 */
public class ScanException extends RuntimeException {
    public ScanException() {
    }

    public ScanException(String message) {
        super(message);
    }

    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScanException(Throwable cause) {
        super(cause);
    }

    public ScanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
