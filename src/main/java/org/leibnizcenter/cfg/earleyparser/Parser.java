package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.earleyparser.parse.Chart;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.token.Token;

import java.util.Set;

/**
 * Created by Maarten on 31-7-2016.
 */
public class Parser {

    public static <E> boolean recognize(NonTerminal S,
                                        Grammar grammar,
                                        Iterable<Token<E>> tokens) {
        final Chart parse = parse(S, grammar, tokens);
        return parse.getCompletedStates(parse.length, S).size() > 0;
    }

      public static <E> Path getViterbiPath(NonTerminal S,
                                  Grammar grammar,
                                  Iterable<Token<E>> tokens) {
        Chart chart = parse(S, grammar, tokens, null);
        Set<State> endStates = chart.getCompletedStates(i, Category.START);
        
        Path best = null;
        for(State endState:endStates){
          Path candidate = getViterbiPath(endState, chart);
          if(best == null || best.score < candidate.score)
            best = candidate;
        }
        return best;
    }
            
             /**
              * Performs the backward part of the forward-backward algorithm
              */
    public static Path getViterbiPath(State endState, Chart chart, Path pathSoFar){
      //TODO index these relations      
      while(endState!=null){
          State bestState = chart.getStatesThatLeadTo(endState).reduce();
          endState = ((Category.START == bestState.getRule().getLeft()) ? null : bestState);
          path.push(bestState);
        }
      return pathSoFar;
    }
  
    public static <E> Chart parse(NonTerminal S,
                                  Grammar grammar,
                                  Iterable<Token<E>> tokens) {
        return parse(S, grammar, tokens, null);
    }

    public static <E> Chart parse(NonTerminal S,
                                  Grammar grammar,
                                  Iterable<Token<E>> tokens,
                                  ScanProbability scanProbability) {
        Chart chart = new Chart(grammar);
        DblSemiring sr = grammar.getSemiring();

        // Initial state
        State initialState = new State(Rule.create(sr.one(), Category.START, S), 0);
        chart.addState(0, initialState, sr.one(), sr.one());

        // Cycle through input
        int i = 0;
        for (Token<E> token : tokens) {
            chart.predict(i);

            chart.scan(i, token, scanProbability);
            chart.completeTruncated(i + 1);
            chart.completeNonTruncatedNonLooping(i + 1);
            i++;
        }
        //Set<State> completed = chart.getCompletedStates(i, Category.START);
        //if (completed.size() > 1) throw new Error("This is a bug");
        chart.length = i;
        return chart;
    }
}
