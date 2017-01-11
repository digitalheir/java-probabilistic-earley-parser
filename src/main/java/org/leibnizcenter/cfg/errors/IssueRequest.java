package org.leibnizcenter.cfg.errors;

/**
 * Error that requires a Github issue
 *
 * Created by Maarten on 16-8-2016.
 */
public class IssueRequest extends Error {
    public IssueRequest(String message) {
        super(message + "\nPlease submit an issue at https://github.com/digitalheir/java-probabilistic-earley-parser/issues");
    }
}
