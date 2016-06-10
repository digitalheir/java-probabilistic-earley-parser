/*
 * $Id: Grammar.java 1781 2010-01-19 04:21:54Z scott $ 
 * Copyright (C) 2007 Scott Martin
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. The GNU Lesser General Public License is
 * distributed with this software in the file COPYING.
 */
package edu.osu.ling.pep.grammar;

import com.google.common.collect.ImmutableMultimap;
import edu.osu.ling.pep.grammar.categories.Category;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Represents a context-free grammar (set of production rules).
 * <p>
 * Grammars maintain their rules indexed by
 * {@link Rule#getLeft() left side category}. The rule sets contained for
 * any given {@link Category left category} are not guaranteed to be
 * maintained in the order of insertion.
 * <p>
 * Once the Grammar is instantiated, it is immutable
 *
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $LastChangedRevision: 1781 $
 */
public class Grammar {
    public final String name;
    public final ImmutableMultimap<Category, Rule> rules;

    /**
     * Creates a grammar with the given name, and given rules.
     *
     * @param name  The mnemonic name for this grammar.
     * @param rules Rules for the grammar
     */
    public Grammar(String name, ImmutableMultimap<Category, Rule> rules) {
        this.name = name;
        this.rules = rules;
    }

    /**
     * Gets the name of this grammar.
     *
     * @return The value specified when this grammar was created.
     */
    public String getName() {
        return name;
    }


    /**
     * Tests whether this grammar contains rules for the specified left side
     * category.
     *
     * @param left The left category of the rules to test for.
     * @return <code>true</code> iff this grammar contains rules with the
     * specified category as their {@link Rule#getLeft() left side}.
     */
    public boolean containsRules(Category left) {
        return rules.containsKey(left);
    }

    /**
     * Gets the set of rules contained by this grammar with the given left
     * side category.
     *
     * @param left The {@link Rule#getLeft() left side} of the rules to find.
     * @return A set containing the rules in this grammar whose
     * {@link Rule#getLeft() left side} is
     * {@link Category#equals(Object) the same} as <code>left</code>, or
     * <code>null</code> if no such rules are contained in this grammar. The
     * rule set returned by this method is <em>not</em> guaranteed to contain
     * the rules in the order in which they were {@link Builder#addRule(Rule) added}.
     */
    public Collection<Rule> getRules(Category left) {
        return rules.get(left);
    }

    /**
     * Gets every rule in this grammar.
     */
    public Collection<Rule> getAllRules() {
        return rules.entries().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a singleton preterminal rule with the specified left category,
     * producing the given string token.
     *
     * @param left  The left side of the preterminal rule.
     * @param token The right side of the preterminal rule.
     * @return A preterminal rule of the form <code>left -> token</code> if
     * any exists within this grammar, or <code>null</code> if none exists.
     * @see Rule#isSingletonPreterminal()
     */
    public Rule getSingletonPreterminal(Category left, Token token) {
        if (rules.containsKey(left))
            for (Rule r : rules.get(left))
                if (r.isSingletonPreterminal() && Tokens.hasCategory(token, r.right[0]))
                    return r;
        return null;
    }

    /**
     * Gets a string representation of this grammar.
     *
     * @return A string listing all of the rules contained by this grammar.
     * @see Rule#toString()
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() +
                ' ' +
                name +
                ": {" +
                rules.entries().stream()
                        .map(Map.Entry::getValue)
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")) +
                "}]";
    }

    public static class Builder {
        private final ImmutableMultimap.Builder<Category, Rule> rules;
        private String name;

        public Builder(String name) {
            this.name = name;
            this.rules = new ImmutableMultimap.Builder<>();
        }

        public Builder() {
            this.rules = new ImmutableMultimap.Builder<>();
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a production rule.
         *
         * @param rule The rule to add.
         * @throws NullPointerException If <code>rule</code> is <code>null</code>.
         */
        public Builder addRule(Rule rule) {
            if (rule == null) throw new NullPointerException("null rule");
            rules.put(rule.left, rule);
            return this;
        }

        public Grammar build() {
            return new Grammar(name, rules.build());
        }

        public Builder addRules(Collection<Rule> rules) {
            rules.forEach(this::addRule);
            return this;
        }
    }
}
