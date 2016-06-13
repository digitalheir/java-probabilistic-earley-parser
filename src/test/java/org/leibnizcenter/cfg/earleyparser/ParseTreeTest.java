
package org.leibnizcenter.cfg.earleyparser;

import org.junit.Assert;
import org.junit.Test;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.terminal.ExactStringTerminal;
import org.leibnizcenter.cfg.earleyparser.exception.PepException;
import org.leibnizcenter.cfg.earleyparser.parse.*;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.leibnizcenter.cfg.earleyparser.PepFixture.*;

/**
 */
public class ParseTreeTest {

    private static Parse<String> parse;
    private static Set<ParseTree> parseTrees;

    private static Category VI = Category.nonTerminal("VI"), VT = Category.nonTerminal("VT"),
            VS = Category.nonTerminal("VS"), saw = new ExactStringTerminal("saw"),
            duck = new ExactStringTerminal("duck"),
            her = new ExactStringTerminal("her"),
            he = new ExactStringTerminal("he");

    static Grammar mygrammar = new Grammar.Builder("test")
                .addRule(new Rule(S, NP, VP))
                .addRule(new Rule(NP, he))
                .addRule(new Rule(NP, her))
                .addRule(new Rule(NP, Det, N))
                .addRule(new Rule(VT, saw))
                .addRule(new Rule(VS, saw))
                .addRule(new Rule(VI, duck))
                .addRule(new Rule(N, duck))
                .addRule(new Rule(Det, her))
                .addRule(new Rule(VP, VT, NP))
                .addRule(new Rule(VP, VS, S))
                .addRule(new Rule(VP, VI)).build();

    static {
        tokens.clear();
        tokens.add(new Token<>("he"));
        tokens.add(new Token<>("saw"));
        tokens.add(new Token<>("her"));
        tokens.add(new Token<>("duck"));

        try {
            ParseTokens<String> parser = new ParseTokens<>(mygrammar, S, tokens, new ParserOptions(), null);
            parse = parser.parseAllRemainigTokens();
        } catch (PepException e) {
            throw new Error(e);
        }
        parseTrees = parse.getParseTrees();
    }

    @Test
    public final void testParseTrees() {
        // structural ambiguity reflected?
        Assert.assertEquals("problem with parse trees: " + parseTrees,
                2, parseTrees.size());

        // sub trees
        Set<ParseTree> vpSubTrees = parse.getParseTreesFor(VP, 1, 4);
        Assert.assertEquals("problem with VP sub trees: " + vpSubTrees,
                2, vpSubTrees.size());

        // should contain all parse trees at index 4
        for (Edge edge : parse.chart.getEdges(4)) {
            if (edge.origin == 1 && edge.isPassive()
                    && edge.rule.left.equals(VP)) {
                ParseTree pt = parse.getParseTreeFor(edge);
                Assert.assertTrue("VP sub trees does not contain: " + pt,
                        vpSubTrees.contains(pt));
            }
        }

        // VI "duck" at end
        Set<ParseTree> viSubTrees = parse.getParseTreesFor(VI, 3, 4);
        Assert.assertEquals("problem with VI sub trees: " + viSubTrees,
                1, viSubTrees.size());

        Set<ParseTree> npSubTrees = parse.getParseTreesFor(NP, 0, 1);
        Assert.assertEquals("problem with NP sub trees: " + npSubTrees,
                1, npSubTrees.size());

        // NP "her duck"
        Set<ParseTree> npSubTrees2 = parse.getParseTreesFor(NP, 2, 4);
        Assert.assertEquals("problem with NP sub trees: " + npSubTrees2,
                1, npSubTrees2.size());

        // sentential complement "her duck"
        Set<ParseTree> sSubTrees = parse.getParseTreesFor(S, 2, 4);
        Assert.assertEquals("problem with S sub trees: " + sSubTrees,
                1, sSubTrees.size());

        ParseTree sSubTree = sSubTrees.iterator().next();
        ParseTree[] sChildren = sSubTree.getChildren();
        //Iterator<ParseTree> sci = sChildren.iterator();
        Assert.assertEquals(NP, sChildren[0].getNode());
        ParseTree sVPSubTree = sChildren[1];
        Assert.assertEquals(VP, sVPSubTree.getNode());
        ParseTree viSubTree = sVPSubTree.getChildren()[0];
        Assert.assertEquals(VI, viSubTree.getNode());
        ParseTree duckSubTree = viSubTree.getChildren()[0];
        Assert.assertEquals(duck, duckSubTree.getNode());

        // back up
        Assert.assertEquals(viSubTree, duckSubTree.getParent());
        Assert.assertEquals(sVPSubTree, viSubTree.getParent());
        Assert.assertEquals(sSubTree, sVPSubTree.getParent());
        Assert.assertTrue(sSubTree.getParent() == null);

        // wrong stuff in seed
        Assert.assertEquals(Collections.emptySet(),
                parse.getParseTreesFor(NP, 0, tokens.size()));
    }

    @Test
    public final void testNewParseTree() {
        Edge startEdge = new Edge(Rule.startRule(S), 0, 0);
        startEdge = Edge.complete(startEdge,
                new Edge(new Rule(S, NP, VP), 2, 0));

        ParseTree startTree = ParseTree.newParseTree(startEdge);
        Assert.assertTrue(startTree.getChildren() == null);

        Edge sEdge = new Edge(new Rule(S, NP, VP), 0, 0);
        sEdge = Edge.complete(sEdge,
                new Edge(new Rule(NP, Det, N), 2, 0));
        sEdge = Edge.complete(sEdge,
                new Edge(new Rule(VP, VT, NP), 2, 3));

        ParseTree sTree = ParseTree.newParseTree(sEdge, null);
        ParseTree[] sChildren = sTree.getChildren();

        Assert.assertEquals(NP, sChildren[0].getNode());
        Assert.assertEquals(VP, sChildren[1].getNode());
    }

    /**
     * Test method for {@link ParseTree#getNode()}.
     */
    @Test
    public final void testGetParent() {
        for (ParseTree pt : parseTrees) {
            Assert.assertEquals(S, pt.getNode());
        }
    }

    /**
     * Test method for {@link ParseTree#getChildren()}.
     */
    @Test
    public final void testGetChildren() throws PepException {
        for (ParseTree pt : parseTrees) {
            ParseTree[] i = pt.getChildren();
            Assert.assertEquals(NP, i[0].getNode());
            Assert.assertEquals(VP, i[1].getNode());
        }

        // test for Jim Slattery's bug
        Grammar g = new Grammar.Builder("g")
                .addRule(new Rule(S, NP, NP))
                .addRule(new Rule(NP, he)).build();

        List<String> t = new ArrayList<>(2);
        t.add("he");
        t.add("he");

        ParseTokens<String> p = new ParseTokens<>(g, S,
                t.stream().map(Token::new).collect(Collectors.toList()),
                new ParserOptions(), null);
        Parse<String> prse = p.parseAllRemainigTokens();
        ParseTree tree = prse.getParseTrees().iterator().next();

        int npCount = 0;
        for (ParseTree c : tree.getChildren()) {
            for (ParseTree x : c.getChildren()) {
                if (x.getNode().equals(he)) {
                    npCount++;
                }
            }
        }

        Assert.assertEquals(2, npCount);
    }

    /**
     * Test method for {@link ParseTree#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
        ParseTree test = new ParseTree(edge1.rule.left, null);
        for (ParseTree pt : parseTrees) {
            Assert.assertFalse(test.equals(pt));
        }
    }

    /**
     * Test method for {@link ParseTree#toString()}.
     */
    @Test
    public final void testToString() {
        String s1 = "[S[NP[he]][VP[VT[saw]][NP[Det[her]][N[duck]]]]]",
                s2 = "[S[NP[he]][VP[VS[saw]][S[NP[her]][VP[VI[duck]]]]]]";

        for (ParseTree pt : parseTrees) {
            Assert.assertTrue("problem with toString(): " + pt.toString(),
                    pt.toString().equals(s1) || pt.toString().equals(s2));
        }
    }

}
