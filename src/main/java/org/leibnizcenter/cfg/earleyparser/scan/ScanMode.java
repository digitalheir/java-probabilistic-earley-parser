package org.leibnizcenter.cfg.earleyparser.scan;

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

    public static ScanMode fromString(String scanMode) {
        if (scanMode.matches("(?i)strict"))
            return STRICT;
        else if (scanMode.matches("(?i)wild ?card"))
            return WILDCARD;
        else if (scanMode.matches("(?i)(drop|ignore)"))
            return DROP;
        else if (scanMode.matches("(?i)synchroni[zs](e|ation)"))
            return SYNCHRONIZE;
        else
            throw new IllegalArgumentException("Illegal scan mode\"" + scanMode + "\". Choose from \"strict\", \"wildcard\" and \"drop\"");
    }
}
