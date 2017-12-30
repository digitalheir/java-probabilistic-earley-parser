package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.Complete;
import org.leibnizcenter.cfg.earleyparser.Scan;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedToken;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.util.Collections2;

import java.util.*;

import static org.leibnizcenter.cfg.util.Collections2.addSafe;
import static org.leibnizcenter.cfg.util.Collections2.containsKey;

/**
 * Represents an index of states, indexed by many different aspects
 * <p>
 * Created by maarten on 31/10/16.
 */
@SuppressWarnings("WeakerAccess")
public class StateSets<T> {
    /**
     * The forward probability <code>α_i</code> of a state is
     * the sum of the probabilities of
     * all constrained paths of length i that end in that state, do all
     * paths from start to position i. So this includes multiple
     * instances of the same history, which may happen because of recursion.
     */
    public final ForwardScores forwardScores;
    /**
     * The inner probability <code>γ_{i}</code> of a state
     * is the sum of the probabilities of all
     * paths of length (i - k) that start at position k (the rule's start position),
     * and end at the current state and generate the input the input symbols up to k.
     * Note that this is conditional on the state happening at position k with
     * a certain non-terminal X
     */
    public final InnerScores innerScores;
    public final Map<State, State.ViterbiScore> viterbiScores = new HashMap<>(500);
    public final CompletedStates completedStates = new CompletedStates();
    public final ActiveStates<T> activeStates = new ActiveStates<>();
    public final Grammar<T> grammar;
    private final Set<State> states = new HashSet<>(500);
    private final List<Set<State>> byIndex = new ArrayList<>(500);
    private final Map<State, ScannedToken<T>> scannedTokens = new HashMap<>(50);
    private final List<Token<T>> scannedTokensAtPosition = new ArrayList<>(50);


    public StateSets(final Grammar<T> grammar) {
        this.grammar = grammar;
        final DblSemiring semiring = grammar.semiring;
        this.forwardScores = new ForwardScores(grammar);
        this.innerScores = new InnerScores(semiring, grammar.atoms);
    }


    /**
     * Runs in O(1) (expected time of map put)
     */
    static void add(final List<Set<State>> states, final int position, final State state) {
        Collections2.add(states, position, state);
    }


    /**
     * Adds state if it does not exist yet
     *
     * @param scannedToken The token that was scanned to create this state
     * @return State specified by parameter. May or may not be in the state table. If not, it is added.
     */
    public State getOrCreate(final State state, final Token<T> scannedToken) {
        if (contains(state)) {
            return state;
        } else {
            addState(state, scannedToken);
            return state;
        }
    }

    /**
     * Runs in O(N) for N is the number of NonTerminals with non-zero unit-star score on active category, which is bounded by the total number of non-terminals
     *
     * @param state State to add
     */
    private void addState(final State state, final Token<T> scannedToken) {
        final int index = state.position;

        states.add(state);
        add(byIndex, index, state);
//        if (state.position>0 && state.rule.right.length>0 && state.rule.right[state.position-1] instanceof NonLexicalToken) {
//            // Just scanned <NonLexicalToken>
//            incrementCompletedErrorRulesCount(state.position);
//        }
        completedStates.addIfCompleted(state);
        activeStates.addIfActive(index, state, grammar.unitStarScores);
        if (scannedToken != null) {
            final ScannedToken<T> eScannedToken = new ScannedToken<>(
                    scannedToken,
                    state.rule,
                    state.ruleDotPosition
            );
            scannedTokens.put(state, eScannedToken);

            if (!containsKey(scannedTokensAtPosition, index))
                addSafe(scannedTokensAtPosition, index, eScannedToken.scannedToken);
            else assert scannedTokensAtPosition.get(index).equals(eScannedToken.scannedToken);

        }
    }

    public Set<State> getStates(final int index) {
        return byIndex.get(index);
    }

    /**
     * @param state {@link State} to add
     * @return whether state was new
     */
    public boolean addIfNew(final State state) {
        if (!contains(state)) {
            getOrCreate(state);
            return true;
        } else
            return false;
    }


    public int countStates() {
        return byIndex.stream().filter(Objects::nonNull)
                .mapToInt(Set::size).sum();
    }

    public boolean contains(final State s) {
        return states.contains(s);
    }

    public void createStateAndSetScores(
            final Token<T> token, final State preScanState,
            final double postScanForward,
            final double postScanInner,
            final State nextState
    ) {
        Objects.requireNonNull(token);
        final DblSemiring sr = this.grammar.semiring;
        final State postScanState = this.getOrCreate(
                nextState, token
        );

//                    if (checkNoNewStatesAreDoubles.contains(rule, position, ruleStart, dot))
//                        throw new IssueRequest("Tried to scan same state twice. This is a bug.");
//                    else checkNoNewStatesAreDoubles.put(postScanState, postScanState);

        // Set forward score
        forwardScores.put(postScanState, postScanForward);
        // Set inner score
        innerScores.put(postScanState, postScanInner);
        // Set Viterbi score
        setViterbiScore(new State.ViterbiScore(postScanInner, preScanState, postScanState, sr));
    }

    @Deprecated
    public void createStateAndSetScores(final Scan.Delta<T> score) {
        createStateAndSetScores(score.token, score.preScanState, score.postScanForward, score.postScanInner, score.nextState);
    }

    public void setViterbiScore(final State.ViterbiScore viterbiScore) {
        this.viterbiScores.put(viterbiScore.resultingState, viterbiScore);
    }

    public double getViterbiScoreDbl(final State s) {
        final State.ViterbiScore viterbiScore = viterbiScores.get(s);
        if (viterbiScore == null) return Double.NaN;
        return viterbiScore.probabilityAsSemiringElement;
    }

    public ScannedToken<T> getScannedToken(final State state) {
        return scannedTokens.get(state);
    }

    public Token<T> getScannedToken(final int pos) {
        return scannedTokensAtPosition.get(pos);
    }


    public State getOrCreate(final State state) {
        if (contains(state)) {
            return state;
        } else {
            states.add(state);
            add(byIndex, state.position, state);
            completedStates.addIfCompleted(state);
            activeStates.addIfActive(state.position, state, grammar.unitStarScores);
            return state;
        }
    }

    public void processDelta(final Complete.ViterbiDelta delta) {
        // Add new states to chart
        if (delta.isNewState) addIfNew(delta.resultingState);
        if (delta.newViterbiScore != null) setViterbiScore(delta.newViterbiScore);
    }
}
