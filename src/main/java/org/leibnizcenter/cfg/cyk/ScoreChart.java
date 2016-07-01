//package org.leibnizcenter.cfg.cyk;
//
//import org.leibnizcenter.cfg.category.Category;
//import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
//import org.leibnizcenter.cfg.category.terminal.Terminal;
//import org.leibnizcenter.cfg.rule.Rule;
//
//import java.awt.*;
//import java.util.Map;
//
///**
// * For keeping values while running CYK algorithm
// *
// * Created by Maarten on 2016-04-09.
// */
//public class ScoreChart {
//    public static class Cell {
//        public final int row;
//        public final int column;
//        public final Map<NonTerminal, ParseTreeContainer> cell;
//
//        public Cell(int row, int column, Map<NonTerminal, ParseTreeContainer> cell) {
//            this.row = row;
//            this.column = column;
//            this.cell = cell;
//        }
//    }
//
//    public static class ParseTreeContainer implements TypeContainer {
//        public final double logProbability;
//        public final TypeContainer[] inputs;
//        public final Rule rule;
//        public final NonTerminal lhs;
//
//        public ParseTreeContainer(Rule rule, ParseTreeContainer... inputs) {
//            //            if (inputs.length != rule.getRHS().size()
//            //                    || !rule.getRHS().match(inputs))
//            //                throw new InvalidParameterException();
//            this.rule = rule;
//            this.logProbability = rule.getLogProbability(inputs);
//            this.inputs = inputs;
//            lhs = rule.getLeft();
//        }
//
//        public ParseTreeContainer(Rule rule, Terminal terminal) {
//            this.rule = rule;
//            this.logProbability = rule.getLogProbability();
//            this.inputs = new TypeContainer[]{terminal};
//            lhs = rule.getLHS();
//        }
//
//        public ParseTreeContainer(double logProbCandidateRule, Rule rule, ParseTreeContainer... inputs) {
//            this.rule = rule;
//            this.logProbability = logProbCandidateRule;
//            this.inputs = inputs;
//            lhs = rule.getLHS();
//        }
//
//        public NonTerminal getType() {
//            return lhs;
//        }
//
//        public NonTerminal getResult() {
//            return lhs;
//        }
//
//
//        @Override
//        public String toString() {
//            return rule + " (e^" + logProbability + ")";
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//
//            ParseTreeContainer that = (ParseTreeContainer) o;
//
//            return Double.compare(that.logProbability, logProbability) == 0
//                    && inputs.equals(that.inputs)
//                    && rule.equals(that.rule);
//
//        }
//
//        public TypeContainer[] getInputs() {
//            return inputs;
//        }
//
//        @Override
//        public int hashCode() {
//            int result;
//            long temp;
//            temp = Double.doubleToLongBits(logProbability);
//            result = (int) (temp ^ (temp >>> 32));
//            result = 31 * result + inputs.hashCode();
//            result = 31 * result + rule.hashCode();
//            return result;
//        }
//
//        public double getLogProbability() {
//            return logProbability;
//        }
//    }
//}
