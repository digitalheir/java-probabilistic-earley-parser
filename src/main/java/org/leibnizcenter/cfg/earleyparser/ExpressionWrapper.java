package org.leibnizcenter.cfg.earleyparser;


import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ResolvableLockable;

@SuppressWarnings("WeakerAccess")
public final class ExpressionWrapper extends ResolvableLockable {
  private double literal = Double.NaN;
  private double plusLiteral = Double.NaN;
  private double timesLiteral = Double.NaN;
  private Resolvable expression = null;

  public ExpressionWrapper(final Resolvable expression) {
    if (expression == null) throw new NullPointerException();
    this.expression = expression;
  }

  public ExpressionWrapper(final double literal) {
    this.literal = literal;
  }

  public Resolvable getExpression() {
    if (lock) throw new IllegalStateException("Value already locked");
    if (expression == null) throw new NullPointerException();
    return expression;
  }

  public void setExpression(final Resolvable expression) {
    if (lock) throw new IllegalStateException("Value already locked");
    if (expression == null) throw new NullPointerException();
    this.literal = Double.NaN;
    plusLiteral = Double.NaN;
    timesLiteral = Double.NaN;
    this.expression = expression;
  }

  public void setExpression(final double expression) {
    if (lock) throw new IllegalStateException("Value already locked");
    this.literal = expression;
    plusLiteral = Double.NaN;
    timesLiteral = Double.NaN;
    this.expression = null;
  }

  public void setPlusLiteral(final double plus) {
    plusLiteral = plus;
  }
  
  public void setTimesLiteral(final double times) {
    timesLiteral = times;
  }

  public double resolveAndClean(/*DblSemiring sr*/) {
    if (lock) return cached;
    double expressionValue = expressionValue();
    // TODO
//    if(Double.isFinite(plusLiteral)) expressionValue = sr.plus(expressionValue, plusLiteral);
//    if(Double.isFinite(plusLiteral)) expressionValue = sr.times(expressionValue, timesLiteral);
    return expressionValue;
  }

  private double expressionValue() {
    if (expression == null) return literal;
    else {
      literal = expression.resolveFinal();
      expression = null;
      return literal;
    }
  }

  public boolean hasExpression() {
    return expression != null;
  }

  public double getLiteral() {
    return literal;
  }
}
