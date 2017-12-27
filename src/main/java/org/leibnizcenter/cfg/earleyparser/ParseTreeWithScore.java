package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

/**
 * Parse tree along with the viterbi score
 *
 * Created by Maarten on 15/08/2016.
 */
@SuppressWarnings("WeakerAccess")
public class ParseTreeWithScore {
    public final ParseTree parseTree;
    public final State.ViterbiScore score;
    public final DblSemiring semiring;

    public ParseTreeWithScore(final ParseTree parseTree, final State.ViterbiScore score, final DblSemiring semiring) {
        this.parseTree = parseTree;
        this.score = score;
        this.semiring = semiring;
    }

    double getProbability() {
        return score.getProbability();
    }

    double getProbabilityAsSemiringElement() {
        return score.probabilityAsSemiringElement;
    }

    
    public ParseTree getParseTree() {
        return parseTree;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ParseTreeWithScore that = (ParseTreeWithScore) o;
        return parseTree.equals(that.parseTree) && score.equals(that.score) && semiring.equals(that.semiring);

    }

    @Override
    public int hashCode() {
        int result = parseTree.hashCode();
        result = 31 * result + score.hashCode();
        result = 31 * result + semiring.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ParseTree{" +
                "\np = " + getProbability() +
                ", \nparseTree = \n" + parseTree +
                '}';
    }
}
