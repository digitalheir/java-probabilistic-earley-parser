//package org.leibnizcenter.cfg.earleyparser.parse;
//
//import java.util.EnumMap;
//
///**
// * Created by maarten on 13-6-16.
// */
//public class ParserOptions extends EnumMap<ParserOption, Boolean> {
//
//    public ParserOptions() {
//        super(ParserOption.class);
//    }
//
//    /**
//     * Gets the value of the option with the specified name.
//     *
//     * @param optionName The option to fetch a value for.
//     * @return The defined value of the specified option, or its
//     * {@link ParserOption#getDefaultValue() default value} if it has
//     * not been set.
//     */
//    public Boolean getOption(ParserOption optionName) {
//        Boolean o = get(optionName);
//        return (o == null) ? optionName.defaultValue : o;
//    }
//
//    public ParserOptions set(ParserOption predictForPreterminals, boolean b) {
//        put(predictForPreterminals, b);
//        return this;
//    }
//}
