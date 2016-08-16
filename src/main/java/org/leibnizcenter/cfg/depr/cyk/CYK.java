//package org.leibnizcenter.cfg.depr.cyk;
//
//
//import java.security.InvalidParameterException;
//import java.util.*;
//import java.util.stream.IntStream;
//
///**
// * Implements CKY algorithm with unary production. Works on grammar in Chomsky Normal Form (with unary production
// * rules permitted).
// * Created by maarten on 17-4-16.
// */
//public class CYK {
//    /**
//     * Don't instantiate
//     */
//    private CYK() {
//        throw new IllegalStateException();
//    }
//
//    /**
//     * <p>Implements CKY algorithm with unary production. Works on grammar in Chomsky Normal Form (with unary production
//     * rules permitted).</p>
//     * <p>
//     * Complexity: 0.5 * |words|^3 * |grammar|^2
//     * </p>
//     *
//     * @return Parse with the highest score, null if none found
//     */
//    public static ScoreChart.ParseTreeContainer getBestParseTree(List<Terminal> words, Grammar grammar, NonTerminal goal) {
//        MutableMatrix<Map<NonTerminal, ScoreChart.ParseTreeContainer>> scoreMap = getParseTrees(words, grammar);
//
//        return scoreMap
//                .get(0, words.size() - 1)
//                .get(goal);
//    }
//
//    @SuppressWarnings("WeakerAccess")
//    public static MutableMatrix<Map<NonTerminal, ScoreChart.ParseTreeContainer>> getParseTrees(List<Terminal> words, Grammar grammar) {
//        if (!grammar.isInChomskyNormalFormWithUnaries())
//            throw new InvalidParameterException("Given grammar should be in Chomsky normal form (unaries are allowed)");
//
//        MutableMatrix<Map<NonTerminal, ScoreChart.ParseTreeContainer>> mutable = new MutableMatrix<>(words.size(), words.size());
//        for (int i = 0; i < words.size(); i++)
//            for (int j = i; j >= i && j < words.size(); j++)
//                mutable.set(i, j, new LinkedHashMap<>(grammar.variableSet.size()));
//
//        handleTerminals(words, grammar, mutable);
//        handleNonTerminals(words, grammar, mutable);
//        return mutable;
//    }
//
//    private static void handleTerminals(List<Terminal> words,
//                                        Grammar grammar,
//                                        MutableMatrix<Map<NonTerminal, ScoreChart.ParseTreeContainer>> scoreMap) {
//        // Init score keeper
//        Vector<Map<NonTerminal, ScoreChart.ParseTreeContainer>> scoresToAdd = new Vector<>(words.size());
//        for (int i = 0; i < words.size(); i++) scoresToAdd.add(new LinkedHashMap<>(grammar.variableSet.size()));
//
//        ////////////
//        // Handle terminal rules
//        for (int i = 0; i < words.size(); i++) {
//            Terminal terminal = words.get(i);
//            for (Rule nt : grammar.terminals.get(terminal)) {
//                // Add all terminals that can be made, IF they are higher than the current score
//                NonTerminal result = nt.getLHS();
//                ScoreChart.ParseTreeContainer score = new ScoreChart.ParseTreeContainer(nt, terminal);
//                Map<NonTerminal, ScoreChart.ParseTreeContainer> cell = scoresToAdd.get(i);
//                ScoreChart.ParseTreeContainer alreadyPresent = cell.get(result);
//                if ((alreadyPresent == null || score.logProbability > alreadyPresent.logProbability )) {
////                    if(alreadyPresent!=null) System.out.println(score.logProbability +">"+ alreadyPresent.logProbability );
//                    // in log prob, less negative is better (e^0 meaning probability=1)
//                    cell.put(result, score);
//                }
//            }
//        }
//
//        // Add scores
//        for (int i = 0; i < words.size(); i++) {
//            scoreMap.set(i, i, scoresToAdd.get(i));
//        }
//
//        // Handle unary rules
//        for (int i = 0; i < words.size(); i++) {
//            handleUnaryRules(grammar, scoreMap.get(i, i));
//        }
//    }
//
//    private static void handleNonTerminals(final List<Terminal> words,
//                                           Grammar grammar,
//                                           MutableMatrix<Map<NonTerminal, ScoreChart.ParseTreeContainer>> builder) {
//        for (int span = 2; span <= words.size(); span++) {
//            // int numberOfSpans = words.size() - span + 1;
//            // System.out.println(span + " / " + words.size() + " : " + numberOfSpans);
//            int spanSize = span;
//
//            IntStream.range(0, words.size() - span + 1)         // For each possible span
//                    .parallel()                                 // Parallelize for performance
//                    .mapToObj(begin -> {                        // For each possible span, return its cell
//                        final int end = begin + spanSize;       // exclusive end
//                        //noinspection UnnecessaryLocalVariable
//                        final List<Terminal> wordz = words;
//
//                        /**
//                         * LinkedHashMap has somewhat faster iteration, which is done a lot on these cells:
//                         *
//                         * "Iteration over the collection-views of a
//                         * LinkedHashMap requires time proportional to the size of the map, regardless of its capacity.
//                         * Iteration over a HashMap is likely to be more expensive, requiring time proportional to its
//                         * capacity."
//                         */
//                        final Map<NonTerminal, ScoreChart.ParseTreeContainer> cell = new LinkedHashMap<>(grammar.variableSet.size());
//                        // try out each possible split between [begin, end]
//
//                        for (int splitAtIndex = begin + 1; splitAtIndex < end; splitAtIndex++) {
//                            // For each possible split, fill the cell with possible values
//                            fillCellAtSplit(
//                                    grammar,
//                                    builder.get(begin, splitAtIndex - 1),
//                                    builder.get(splitAtIndex, end - 1),
//                                    cell);
//                        }
//                        if (cell.size() > 0) handleUnaryRules(grammar, cell);
////                 else
////                    System.err.println(
////                            "WARNING: No rules found to be applied for span from " + begin + " to " + end
////                    );
//
//                        return new ScoreChart.Cell(
//                                begin,
//                                end - 1,
//                                cell
//                        );
//                    })
//                    .forEach(cell -> builder.set(cell.row, cell.column, cell.cell));
//        }
//    }
//
//    /**
//     * @param grammar   CFG
//     * @param cellLeft  begin of span
//     * @param cellRight end of span
//     * @param cell      cell at scoreChart[begin,end]
//     */
//    private static void fillCellAtSplit(final Grammar grammar,
//                                        final Map<NonTerminal, ScoreChart.ParseTreeContainer> cellLeft,
//                                        final Map<NonTerminal, ScoreChart.ParseTreeContainer> cellRight,
//                                        final Map<NonTerminal, ScoreChart.ParseTreeContainer> cell) {
//        // Try out all rules; add those that stick
//
//        //System.out.println("|B| = " + possibleBValues.size());
//
//        for (Map.Entry<NonTerminal, ScoreChart.ParseTreeContainer> possibleB : cellLeft.entrySet()) {
//            //System.out.println("|C| = " + possibleCValues.size());
//
//
//            for (Map.Entry<NonTerminal, ScoreChart.ParseTreeContainer> possibleC : cellRight.entrySet()) {
//                for (Rule r : grammar.getBinaryProductionRules(possibleB.getKey(), possibleC.getKey())) {
//
//                    // Add rule score, IF it is better than any existing production with the same LHS (if any)
//                    NonTerminal result = r.getLHS();
//                    ScoreChart.ParseTreeContainer alreadyPresent = cell.get(result);
//                    ScoreChart.ParseTreeContainer[] inputs = new ScoreChart.ParseTreeContainer[]{possibleB.getValue(), possibleC.getValue()};
//                    double logProb = r.getLogProbability(inputs);
//                    if ((alreadyPresent == null || logProb > alreadyPresent.logProbability)) // in log prob, less negative is better (e^0 meaning probability=1)
////                        if(alreadyPresent!=null) System.out.println(logProb +">"+ alreadyPresent.logProbability );
//                        cell.put(result, r.apply(logProb, inputs));
//                }
//            }
//
//        }
//    }
//
//    private static void handleUnaryRules(final Grammar grammar, final Map<NonTerminal, ScoreChart.ParseTreeContainer> cell) {
//        boolean addedNewResultType;
//        do {
//            addedNewResultType = false;
//            Map<NonTerminal, ScoreChart.ParseTreeContainer> toAdd = null;
//
//            Set<Map.Entry<NonTerminal, ScoreChart.ParseTreeContainer>> entries = cell.entrySet();
//
//            // Find all applicable unary rules
//            for (Map.Entry<NonTerminal, ScoreChart.ParseTreeContainer> B : entries) {
//                Set<Rule> unaryProductionRules = grammar.getUnaryProductionRules(B.getKey());
//                for (Rule r : unaryProductionRules) {
//                    double logProbCandidateRule = r.getLogProbability(B.getValue());
//                    if (ruleGivesBetterLikelihoodThanExisting(cell, toAdd, r.getLHS(), logProbCandidateRule)) {
//                        toAdd = toAdd == null ? new HashMap<>(entries.size()) : toAdd; // init if necessary
//                        toAdd.put(r.getLHS(), new ScoreChart.ParseTreeContainer(logProbCandidateRule, r, B.getValue()));
//                    }
//                }
//            }
//
//            // See if we can add the results
//            if (toAdd != null) {
//                addedNewResultType = toAdd.keySet().stream().anyMatch(k -> cell.get(k) == null);
//                if (toAdd.entrySet().stream().anyMatch(pair -> !pair.getKey().equals(pair.getValue().getResult())))
//                    throw new IllegalStateException();
//                toAdd.values().forEach(ptc -> cell.put(ptc.getResult(), ptc));
//            }
//        } while (addedNewResultType);
//    }
//
//    private static boolean ruleGivesBetterLikelihoodThanExisting(
//            Map<NonTerminal, ScoreChart.ParseTreeContainer> map1,
//            Map<NonTerminal, ScoreChart.ParseTreeContainer> map2,
//            NonTerminal candidateResult,
//            double logProbCandidateRule) {
//        if (map2 != null) {
//            ScoreChart.ParseTreeContainer inMap2 = map2.get(candidateResult);
//            if (inMap2 != null && Double.compare(logProbCandidateRule, inMap2.logProbability) <= 0) {
//                return false;
//            }
//        }
//
//        if (map1 != null) {
//            ScoreChart.ParseTreeContainer inMap1 = map1.get(candidateResult);
//            if (inMap1 != null && Double.compare(logProbCandidateRule, inMap1.logProbability) <= 0) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public static ScoreChart.ParseTreeContainer getBestParseTree(List<Terminal> words, Grammar dg) {
//        return getBestParseTree(words, dg, dg.getStartSymbol());
//    }
//
//}
