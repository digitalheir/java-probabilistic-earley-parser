package org.leibnizcenter.cfg.earleyparser;

import com.sun.istack.internal.NotNull;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ParseTree;
import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;

/**
 * Created by Maarten on 15/08/2016.
 */
public class ParseTreeWithScore {
    public final ParseTree parseTree;
    public final State.ViterbiScore score;
    public final DblSemiring semiring;

    public ParseTreeWithScore(@NotNull ParseTree parseTree, @NotNull State.ViterbiScore score, @NotNull DblSemiring semiring) {
        this.parseTree = parseTree;
        this.score = score;
        this.semiring = semiring;
    }

    double getProbability() {
        return semiring.toProbability(score.getScore());
    }

    @NotNull
    public ParseTree getParseTree() {
        return parseTree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParseTreeWithScore that = (ParseTreeWithScore) o;
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
                "p = " + getProbability() +
                ", parseTree = " + parseTree +
                '}';
    }
}
