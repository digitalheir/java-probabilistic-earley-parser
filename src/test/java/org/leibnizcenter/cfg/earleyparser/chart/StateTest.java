//package org.leibnizcenter.cfg.earleyparser.chart;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.leibnizcenter.cfg.category.Category;
//import org.leibnizcenter.cfg.category.terminal.ExactStringTerminal;
//import org.leibnizcenter.cfg.rule.Rule;
//
//import static org.junit.Assert.*;
//
///**
// * Created by maarten on 26-6-16.
// */
//public class StateTest {
//    private static final Category S = Category.nonTerminal("S");
//    private static final Category a = new ExactStringTerminal("a");
//    private static final Rule RULE_TERMINAL = new Rule(S, a);
//    private static State STATE_COMPLETED;
//
//    @Test
//    public void isCompleted() throws Exception {
//        final State state = new State(RULE_TERMINAL, 0, 0);
//        assertTrue(!state.isCompleted());
//        state.advanceDot();
//        assertTrue(state.isCompleted());
//    }
//
//    @Test
//    public void getActiveCategory() throws Exception {
//        final State state = new State(RULE_TERMINAL, 0, 0);
//        assertEquals(state.getActiveCategory(), (a));
//        state.advanceDot();
//        assertEquals(state.getActiveCategory(), null);
//    }
//
//    @Test
//    public void getRule() throws Exception {
//        final State state = new State(RULE_TERMINAL, 0, 0);
//        assertEquals(state.getRule(), RULE_TERMINAL);
//    }
//
//    @Test
//    public void getPosition() throws Exception {
//        final State state = new State(RULE_TERMINAL, 0, 0);
//        assertEquals(state.getPosition(), 0);
//        state.advanceDot();
//        assertEquals(state.getPosition(), 1);
//    }
//
//    @Test
//    public void complete() throws Exception {
//
//    }
//
//}