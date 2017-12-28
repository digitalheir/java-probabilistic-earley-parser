package org.leibnizcenter.cfg.earleyparser.scan;

import java.util.regex.Pattern;

/**
 * Determine what to do when we can't find token in lexicon
 * Created by maarten on 8-2-17.
 */
public enum ScanMode {
    /**
     * throw an error
     */
    STRICT,
    /**
     * ignore unfound words (act as if it didn't exist)
     */
    DROP,
    /**
     * replace the unfound word with a wildcard that matches all categories
     */
    WILDCARD,
    SYNCHRONIZE;

    private static final Pattern _WILDCARD = Pattern.compile("(?i)wild ?card");
    private static final Pattern _STRICT = Pattern.compile("(?i)strict");
    private static final Pattern _DROP = Pattern.compile("(?i)(drop|ignore)");
    private static final Pattern _SYNCHRONIZE = Pattern.compile("(?i)synchroni[zs](e|ation)");

    public static ScanMode fromString(final String scanMode) {
        if (_STRICT.matcher(scanMode).matches())
            return STRICT;
        else if (_WILDCARD.matcher(scanMode).matches())
            return WILDCARD;
        else if (_DROP.matcher(scanMode).matches())
            return DROP;
        else if (_SYNCHRONIZE.matcher(scanMode).matches())
            return SYNCHRONIZE;
        else
            throw new IllegalArgumentException("Illegal scan mode\"" + scanMode + "\". Choose from \"strict\", \"wildcard\" and \"drop\"");
    }
}
