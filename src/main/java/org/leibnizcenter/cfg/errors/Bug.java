package org.leibnizcenter.cfg.errors;

/**
 * Error that requires a Github issue
 * <p>
 * Created by Maarten on 16-8-2016.
 */
public class Bug extends IssueRequest {
    public Bug(final String message) {
        super(message);
    }
}
