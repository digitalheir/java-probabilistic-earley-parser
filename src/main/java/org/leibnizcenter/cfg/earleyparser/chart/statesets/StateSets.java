package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.Complete;
import org.leibnizcenter.cfg.earleyparser.Scan;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedToken;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.Token;

import java.util.*;

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
    public final TObjectDoubleHashMap<State> viterbiScoresDbl;
    public final CompletedStates completedStates = new CompletedStates();
    public final ActiveStates<T> activeStates = new ActiveStates<>();
    public final Grammar<T> grammar;
    private final Set<State> states = new HashSet<>(500);
    private final TIntObjectHashMap<Set<State>> byIndex = new TIntObjectHashMap<>(500);
    private final Map<State, ScannedToken<T>> scannedTokens = new HashMap<>(50);
    private final TIntObjectMap<Token<T>> scannedTokensAtPosition = new TIntObjectHashMap<>(50, 0.5F, -1);


    public StateSets(Grammar<T> grammar) {
        this.grammar = grammar;
        DblSemiring semiring = grammar.semiring;
        this.forwardScores = new ForwardScores(grammar);
        this.innerScores = new InnerScores(semiring, grammar.atoms);
        viterbiScoresDbl = new TObjectDoubleHashMap<>(500, 0.5F, Double.NaN);
    }

    /**
     * Runs in O(1) (expected time of map put)
     */
    static void add(TIntObjectHashMap<Set<State>> states, int position, State state) {
        if (!states.containsKey(position)) states.put(position, new HashSet<>());
        states.get(position).add(state);
    }


    /**
     * Adds state if it does not exist yet
     *
     * @param scannedToken The token that was scanned to create this state
     * @return State specified by parameter. May or may not be in the state table. If not, it is added.
     */
    public State getOrCreate(State state, Token<T> scannedToken) {
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
    private void addState(final State state, Token<T> scannedToken) {
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
            ScannedToken<T> eScannedToken = new ScannedToken<>(
                    scannedToken,
                    state.rule,
                    state.ruleDotPosition
            );
            scannedTokens.put(state, eScannedToken);
            assert !scannedTokensAtPosition.containsKey(index)
                    || scannedTokensAtPosition.get(index).equals(eScannedToken.scannedToken);
            scannedTokensAtPosition.putIfAbsent(index, eScannedToken.scannedToken);
        }
    }

    public Set<State> getStates(int index) {
        return byIndex.get(index);
    }

    /**
     * @param state
     * @return whether state was new
     */
    public boolean addIfNew(State state) {
        if (!contains(state)) {
            getOrCreate(state);
            return true;
        } else
            return false;
    }


    public int countStates() {
        //noinspection unchecked
        return Arrays.stream(byIndex.values(new Set[byIndex.size()]))
                .mapToInt(Set::size).sum();
    }

    public boolean contains(State s) {
        return states.contains(s);
    }

    public void createStateAndSetScores(Scan.Delta<T> score) {
        Objects.requireNonNull(score.token);
        final DblSemiring sr = this.grammar.semiring;
        final State postScanState = this.getOrCreate(
                score.nextState, score.token
        );

//                    if (checkNoNewStatesAreDoubles.contains(rule, position, ruleStart, dot))
//                        throw new IssueRequest("Tried to scan same state twice. This is a bug.");
//                    else checkNoNewStatesAreDoubles.put(postScanState, postScanState);

        // Set forward score
        forwardScores.put(postScanState, score.postScanForward);
        // Set inner score
        innerScores.put(postScanState, score.postScanInner);
        // Set Viterbi score
        setViterbiScore(new State.ViterbiScore(score.postScanInner, score.preScanState, postScanState, sr));
    }

    public void setViterbiScore(State.ViterbiScore viterbiScore) {
        this.viterbiScores.put(viterbiScore.resultingState, viterbiScore);
        this.viterbiScoresDbl.put(viterbiScore.resultingState, viterbiScore.probabilityAsSemiringElement);
    }

    public ScannedToken<T> getScannedToken(State state) {
        return scannedTokens.get(state);
    }

    public Token<T> getScannedToken(int pos) {
        return scannedTokensAtPosition.get(pos);
    }


    public State getOrCreate(State state) {
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

    public void processDelta(Complete.ViterbiDelta delta) {
        // Add new states to chart
        if (delta.isNewState) addIfNew(delta.resultingState);
        if (delta.newViterbiScore != null) setViterbiScore(delta.newViterbiScore);
    }
}
