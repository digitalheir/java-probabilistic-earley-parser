package org.leibnizcenter.cfg.errors;

/**
 * Error that requires a Github issue
 * <p>
 * Created by Maarten on 16-8-2016.
 */
public class IssueRequest extends Error {
    public IssueRequest(String message) {
        super(message + "\nPlease submit an issue at https://github.com/digitalheir/java-probabilistic-earley-parser/issues");
    }

    public static void ensure(boolean b, String s) {
        if (!b) throw new IssueRequest(s);
    }
}
