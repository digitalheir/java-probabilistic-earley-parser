public class Path extends Deque<State> {
  private double pathScore;
  
  public  Path(DblSemiring sr){
    this.semiring = sr;
    pathScore = sr.one();
  }
  
  public void add(State s, double viterbiScore){
    super.add(s);
    semiring.times(pathScore, viterbiScore);
  }
  
  public double getScore(){
    return viterbiScore;
  }
}