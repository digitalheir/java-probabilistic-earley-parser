//
//package org.leibnizcenter.cfg.earleyparser.parse;
//
//
///**
// * An option used to configure an {@link EarleyParser Earley parser}.
// *
// */
//public enum ParserOption {
//
//    //TODO
//    /**
//     * Signals whether an Earley parser should ignore the case of terminal
//     * tokens. If this is set to <code>true</code>, the tokens <code>John</code>
//     * and <code>john</code> will be treated as equivalent, but they will be
//     * treated as distinct tokens if this is set to <code>false</code>.
//     * <p>
//     * Default value: {@link Boolean#FALSE}.
//     *
//     */
//    IGNORE_TERMINAL_CASE(Boolean.FALSE),
//
//    /**
//     * Whether the Earley parser should make predictions based on pre-terminal
//     * category. If this is <code>true</code>, the parser will make a
//     * prediction for every grammar entry where the left side is a category
//     * it has predicted for and the right side is a terminal. For example,
//     * in a grammar where the pre-terminal category <code>N</code> can expand
//     * to <code>dog</code>, <code>boy</code>, or <code>man</code> and a rule
//     * exists where <code>NP</code> expands to <code>Det N</code>, the parser
//     * will predict
//     * <ul>
//     * <li><code>N -> dog</code></li>
//     * <li><code>N -> boy</code></li>
//     * <li><code>N -> man</code></li>
//     * </ul>
//     * whenever it is able to predict <code>NP</code> (or any other rule that
//     * can expand to <code>N</code>).
//     * <p>
//     * Alternatively, if this option is specified as <code>false</code>, the
//     * parser will only <em>complete</em> (as opposed to predict) for those
//     * terminals it <em>actually encounters</em> in the input string, instead
//     * of predicting for every possible terminal.
//     * <p>
//     * Default value: {@link Boolean#FALSE}.
//     */
//    PREDICT_FOR_PRETERMINALS(Boolean.FALSE);
//
//    public final Boolean defaultValue;
//
//    private ParserOption(Boolean defaultValue) {
//        this.defaultValue = defaultValue;
//    }
//
//    /**
//     * Gets the default value for this option.
//     */
//    public Boolean getDefaultValue() {
//        return defaultValue;
//    }
//
//    /**
//     * Gets a string representation of this Earley parser option.
//     *
//     * @return <code>OPTION_NAME=option default value</code>
//     */
//    @Override
//    public String toString() {
//        return name() + '=' +
//                defaultValue.toString();
//    }
//}
