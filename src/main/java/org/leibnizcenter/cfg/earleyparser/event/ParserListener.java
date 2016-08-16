//
//package org.leibnizcenter.cfg.earleyparser.event;
//
//import org.leibnizcenter.cfg.Grammar;
//import org.leibnizcenter.cfg.errors.ParseException;
//import org.leibnizcenter.cfg.earleyparser.parse.Parse;
//
//import java.util.EventListener;
//
//
///**
// * Listens to an {@link EarleyParser Earley parser}, receiving notifications
// * as the parser adds edges to the chart or scans the input string. The parser
// * also notifies a listener whenever a parse completes or an error occurs
// * in a parse.
// * <p>
// * A listener can be specified for an Earley parser when the parser is
// * created.
// *
// * @see EarleyParser#EarleyParser(Grammar, ParserListener)
// */
//@SuppressWarnings("UnnecessaryInterfaceModifier")
//public interface ParserListener extends EventListener {
//// TODO  make callbacks not to event objects
//    /**
//     * Notifies a listener that the parser was seeded with a start category.
//     *
//     * @param stateEvent An event containing the added edge.
//     */
//    public void parserSeeded(StateEvent stateEvent);
//
//    /**
//     * Signals to a listener that an edge was added to the chart based on a
//     * prediction from the {@link Grammar grammar}.
//     *
//     * @param stateEvent An event containing the added edge.
//     */
//    public void edgePredicted(StateEvent stateEvent);
//
//    /**
//     * Lets the listener know that a token was scanned from the input string.
//     *
//     * @param stateEvent An event containing the edge created on the basis of
//     *                  scanning a token.
//     */
//    public void edgeScanned(StateEvent stateEvent);
//
//    /**
//     * Notifies a listener that an edge was added to the chart based on
//     * completion.
//     *
//     * @param stateEvent An event containing the added edge.
//     */
//    public void edgeCompleted(StateEvent stateEvent);
//
//    /**
//     * Called when parsing completes.
//     *
//     * @param parseEvent An event containing the completed {@link Parse parse}.
//     */
//    public void parseComplete(ParseEvent parseEvent);
//
//    /**
//     * Notifies the listener of a message from the parser.
//     *
//     * @param parseEvent A parse event containing the parse in progress.
//     * @param message    The message.
//     */
//    public void parseMessage(ParseEvent parseEvent, String message);
//
//    /**
//     * Notifies a listener that an error occurred during parsing.
//     *
//     * @param parseErrorEvent An error event containing the parse that was
//     *                        in progress and the error that occurred.
//     * @throws ParseException If the listener does not wish to handle the
//     *                      errors, it can re-throw the cause of the parse error event.
//     */
//    public void parseError(ParseErrorEvent parseErrorEvent) throws ParseException;
//}
