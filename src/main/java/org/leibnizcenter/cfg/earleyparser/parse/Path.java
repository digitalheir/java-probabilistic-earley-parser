//TODO deleteme
//package org.leibnizcenter.cfg.earleyparser.parse;
//
//import org.leibnizcenter.cfg.earleyparser.chart.State;
//import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;
//
//import java.util.ArrayDeque;
//import java.util.Deque;
//import java.util.Iterator;
//import java.util.Spliterator;
//import java.util.function.Consumer;
//
//public class Path implements Iterable<State>{
//    private final DblSemiring semiring;
//    private double pathScore;
//    private Deque<State> data = new ArrayDeque<>();
//
//    public Path(DblSemiring sr) {
//        this.semiring = sr;
//        pathScore = sr.one();
//    }
//
//    public void push(State s, double viterbiScore) {
//        data.push(s);
//        semiring.times(pathScore, viterbiScore);
//    }
//
//    public double getScore() {
//        return pathScore;
//    }
//
//    @Override
//    public Iterator<State> iterator() {
//        return data.iterator();
//    }
//
//    @Override
//    public void forEach(Consumer<? super State> action) {
//        data.forEach(action);
//    }
//
//    @Override
//    public Spliterator<State> spliterator() {
//        return data.spliterator();
//    }
//}