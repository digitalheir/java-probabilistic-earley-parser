package org.leibnizcenter.cfg.grammar;

import org.leibnizcenter.cfg.algebra.matrix.LUDecomposition;
import org.leibnizcenter.cfg.algebra.matrix.Matrix;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.*;

import static java.util.Collections.unmodifiableMap;

class GrammarAnalysis {
    private GrammarAnalysis() {
        throw new IllegalStateException();
    }

    /**
     * Uses a trick to compute left*Corners (R_L), the reflexive transitive closure of leftCorners (P_L):
     *
     * <code>R_L = I + P_L R_L = (I - P_L)^-1</code>
     *
     * @param leftCorners left corners
     */
    static LeftCorners computeReflexiveTransitiveClosure(final LeftCorners leftCorners, final NonTerminal[] nonterminalsArr) {
        final int nonTerminalsCount = nonterminalsArr.length;
        final Matrix R_L_inverse = new Matrix(nonTerminalsCount, nonTerminalsCount);
        R_L_inverse.forEach((row, col, value) -> {
            final NonTerminal X = nonterminalsArr[row];
            final NonTerminal Y = nonterminalsArr[col];
            // I - P_L
            R_L_inverse.set(row, col, (row == col ? 1 : 0) - leftCorners.getProbability(X, Y));
        });
        final Matrix R_L = inverseMatrix(R_L_inverse);
        return new LeftCorners(R_L, nonterminalsArr);
    }

    private static Matrix inverseMatrix(final Matrix R_L_inverse) {
        final LUDecomposition luDecomposition = new LUDecomposition(R_L_inverse);
        if (luDecomposition.isNonsingular()) {
            return R_L_inverse.inverse(luDecomposition);
        } else {
            // return R_L_inverse.inverse(new QRDecomposition(R_L_inverse));
            throw new IssueRequest("Matrix is singular");
        }
    }

    static Map<Category, Set<Rule>> findNonZeroLeftStartRules(final LeftCorners leftStarCorners,
                                                              final Set<NonTerminal> nonTerminals,
                                                              final MyMultimap<NonTerminal, Rule> rules) {
        final Map<Category, Set<Rule>> nonZeroLeftStartRules_ = new HashMap<>();
        nonTerminals.forEach(lhsCategory -> {
            final Collection<NonTerminal> nonZeroScores = leftStarCorners.getNonZeroScores(lhsCategory);
            if (nonZeroScores != null) {
                final Set<Rule> nonEmptyRules = new HashSet<>();
                for (final NonTerminal rhsCategory : nonZeroScores) {
                    final Collection<Rule> rulesForY = rules.get(rhsCategory);
                    if (rulesForY != null) nonEmptyRules.addAll(rulesForY);
                }
                nonZeroLeftStartRules_.put(lhsCategory, nonEmptyRules);
            }
        });

        return unmodifiableMap(nonZeroLeftStartRules_);
    }

    /**
     * Sum all probabilities for unit relations (form X -> Y)
     */
    static LeftCorners computeUnitStarCorners(final MyMultimap<NonTerminal, Rule> rules, final NonTerminal[] nonterminalsArr) {
        final LeftCorners unitRuleProbabilities = new LeftCorners(nonterminalsArr);
        for (int i = 0; i < nonterminalsArr.length; i++) {
            final NonTerminal leftHandSide = nonterminalsArr[i];
            final Collection<Rule> rulesForCategory = rules.get(leftHandSide);
            if (rulesForCategory != null && !rulesForCategory.isEmpty()) {
                for (final Rule unitProduction : rulesForCategory) {
                    if (unitProduction.isUnitProduction()) {
                        unitRuleProbabilities.plusRawProbability(i,
                                (NonTerminal) unitProduction.right[0], unitProduction.probability);
                    }
                }
            }
        }

        // R_U = (I - P_U)
        return computeReflexiveTransitiveClosure(unitRuleProbabilities, nonterminalsArr);
    }
}
