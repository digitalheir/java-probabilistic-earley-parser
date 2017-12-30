package org.leibnizcenter.cfg.grammar;

import org.junit.Test;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ProbabilitySemiring;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.category.terminal.stringterminal.ExactStringTerminal;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.util.MyMultimap;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class GrammarAnalysisTest {
    @Test
    public void computeReflexiveTransitiveClosure() throws Exception {
        final Set<NonTerminal> nonTerminals = new HashSet<>();
        final NonTerminal a = (new NonTerminal("A"));
        final Terminal a_ = (new ExactStringTerminal("a"));
        final NonTerminal b = (new NonTerminal("B"));
        final Terminal b_ = (new ExactStringTerminal("b"));
        nonTerminals.add(a);
        nonTerminals.add(b);
        final MyMultimap<NonTerminal, Rule> rules = new MyMultimap<>();
//        rules.put(a, Rule.create(ProbabilitySemiring.get(), 1.0, a, a, a_));
        rules.put(a, Rule.create(ProbabilitySemiring.get(), 0.7, a, a_));
        rules.put(b, Rule.create(ProbabilitySemiring.get(), 0.5, b, a));

        rules.put(b, Rule.create(ProbabilitySemiring.get(), 0.2, b, b));
        rules.put(b, Rule.create(ProbabilitySemiring.get(), 0.1, b, b, b_));

        final LeftCorners leftCorners = new LeftCorners(rules, nonTerminals.toArray(new NonTerminal[nonTerminals.size()]));

        final LeftCorners leftStar = GrammarAnalysis.computeReflexiveTransitiveClosure(leftCorners, nonTerminals.toArray(new NonTerminal[nonTerminals.size()]));

        // TODO don't crash for singular matrices

        System.out.println(leftStar);
    }

}