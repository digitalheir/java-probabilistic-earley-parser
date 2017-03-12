package org.leibnizcenter.cfg.earleyparser.scan;

/**
 * When scanning goes wrong
 * Created by maarten on 8-2-17.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
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
