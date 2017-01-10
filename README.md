# Probabilistic Earley parser
[![Build Status](https://travis-ci.org/digitalheir/java-probabilistic-earley-parser.svg?branch=master)](https://travis-ci.org/digitalheir/java-probabilistic-earley-parser)
[![GitHub version](https://badge.fury.io/gh/digitalheir%2Fjava-probabilistic-earley-parser.svg)](http://badge.fury.io/gh/digitalheir%2Fjava-probabilistic-earley-parser)
[![License](https://img.shields.io/npm/l/probabilistic-earley-parser.svg)](https://github.com/digitalheir/java-probabilistic-earley-parser/blob/master/LICENSE)


This is a library for parsing a sequence of tokens (like words) into tree structures, along with the probability that the particular sequence generates that tree structure. This is mainly useful for linguistic purposes, such as morphological parsing, speech recognition and generally information extraction. It also finds applications in computational biology. 

For example:

* As a computational linguist, you want [derive all ways to interpret an English sentence along with probabilities](https://web.stanford.edu/~jurafsky/icassp95-tc.pdf)

|tokens|parse tree|
|---|---|
|[i, want, british, food]|![i want british food](https://cloud.githubusercontent.com/assets/178797/21772897/64838a1e-d68d-11e6-9a9d-11c7c17cb996.png)|

* As a computational biologist, you want to [predict the secondary structure for an RNA sequence](https://en.wikipedia.org/wiki/Stochastic_context-free_grammar#RNA_structure_prediction)

|tokens|parse tree|
|---|---|
|`GGGC``UAUU``AGCU``CAGU`<br>`UGGU``UAGA``GCGC``ACCC`<br>`CUGA``UAAG``GGUG``AGGU`<br>`CGCU``GAUU``CGAA``UUCA`<br>`GCAU``AGCC``CA` |![rna secondary structure](https://cloud.githubusercontent.com/assets/178797/21773797/af94f972-d690-11e6-97b4-0aad06071634.jpg)|

* As a computational linguist, [you want to know the most likely table of contents structure for a list of paragraphs](https://digitalheir.github.io/java-rechtspraak-library/document-structure/)



This library allows you to do these things [efficiently](https://github.com/digitalheir/probabilistic-earley-parser-javascript#runtime-complexity), as long as you can describe the rules as a [Context-free Grammar](https://en.wikipedia.org/wiki/Context-free_grammar) (CFG).

The innovation of this library with respect to the many other parsing libraries is that this one allows the production rules in your grammar to have a probability attached to them. That is: it parses [Stochastic Context-free Grammars](https://en.wikipedia.org/wiki/Stochastic_context-free_grammar). This allows us to make better choices in case of ambiguous sentences: we can order them by probability.

The parser seems to work correctly and efficiently, but is still < v1.0 because I have not added as much utility functions and tests as I would like.

For a theoretical grounding of this work, refer to [*Stolcke, An Efficient Probabilistic Context-Free
           Parsing Algorithm that Computes Prefix
           Probabilities*](http://www.aclweb.org/anthology/J95-2002).
  
## Motivation
I made this library because I could not find an existing Java 
implementation of the Probabilistic Earley Parser. 

I have made a stochastic CYK parser before, but I wanted something
more top down that makes it easier to intervene in the parsing process,
for instance when an unexpected token is encountered.
 
Furthermore, I needed a efficient parser that does not limit token types 
to strings.
   
## Usage
Download [the latest JAR](https://github.com/digitalheir/java-probabilistic-earley-parser/releases/latest) or grab from Maven:

```xml
<dependencies>
        <dependency>
            <groupId>org.leibnizcenter</groupId>
            <artifactId>probabilistic-earley-parser</artifactId>
            <version>0.9.7</version>
        </dependency>
</dependencies>
```

or Gradle:
```groovy
compile 'org.leibnizcenter:probabilistic-earley-parser:0.9.7'
```

Most applications will want to interface with the static functions in `Parser`:

```java
public class Example {
    // NonTerminals are just wrappers around a string
    private static final NonTerminal S = Category.nonTerminal("S");
    private static final NonTerminal NP = Category.nonTerminal("NP");
    private static final NonTerminal VP = Category.nonTerminal("VP");
    private static final NonTerminal TV = Category.nonTerminal("TV");
    private static final NonTerminal Det = Category.nonTerminal("Det");
    private static final NonTerminal N = Category.nonTerminal("N");
    private static final NonTerminal Mod = Category.nonTerminal("Mod");

    // Token types are realized by implementing Terminal, specifically the function hasCategory. Terminal is a functional interface.
    private static final Terminal transitiveVerb = (StringTerminal) token -> token.obj.matches("(hit|chased)");
    // Some utility terminal types are pre-defined:
    private static final Terminal the = new CaseInsenstiveStringTerminal("the");
    private static final Terminal a = new CaseInsenstiveStringTerminal("a");
    private static final Terminal man = new ExactStringTerminal("man");
    private static final Terminal stick = new ExactStringTerminal("stick");
    private static final Terminal with = new ExactStringTerminal("with");
    
    private static final Grammar grammar = new Grammar.Builder("test")
            .setSemiring(new LogSemiring()) // If not set, defaults to Log semiring which is probably what you want
            .addRule(
                    1.0,   // Probability between 0.0 and 1.0, defaults to 1.0. The builder takes care of converting it to the semiring element
                    S,     // Left hand side of the rule
                    NP, VP // Right hand side of the rule
            )
            .addRule(
                    NP,
                    Det, N // eg. The man
            )
            .addRule(
                    NP,
                    Det, N, Mod // eg. The man (with a stick)
            )
            .addRule(
                    0.4,
                    VP,
                    TV, NP, Mod // eg. (chased) (the man) (with a stick)
            )
            .addRule(
                    0.6,
                    VP,
                    TV, NP // eg. (chased) (the man with a stick)
            )
            .addRule(Det, a)
            .addRule(Det, the)
            .addRule(N, man)
            .addRule(N, stick)
            .addRule(TV, transitiveVerb)
            .addRule(Mod, with, NP) // eg. with a stick
            .build();

    public static void main(String[] args) {
        System.out.println(
                Parser.recognize(S, grammar, Tokens.tokenize("The man     chased the man \n\t with a stick")) 
        );
        System.out.println(
                Parser.recognize(S, grammar, Tokens.tokenize("the", "stick", "chased", "the", "man")) 
        );
    }
}
```

Most internal parsing stuff is available through he public API, should you need a slightly different parser than usual.

## Some notes on implementation
The probability of a parse is defined as the product of the probalities all the applied rules. Usually,
we define probability as a number between 0 and 1 inclusive, and use common algebraic notions of addition and
multiplication.

This code makes it possible to use *any* [semiring](https://en.wikipedia.org/wiki/Semiring) that can have its elements
represented as doubles. My use for this is to avoid arithmetic underflow: imagine a computation like 0.1 * 0.1 * ... * 0.1.
At some point, floating point arithmetic will be unable to represent a number so small. To counter, we use the Log
semiring which holds the minus log of the probability. So that maps the numbers 0 and 1 to the numbers
between infinity and zero, skewed towards lower probabilities:

#### Graph plot of f(x) = -log(x)
![Graph for f(x) = -log x](https://leibniz.cloudant.com/assets/_design/ddoc/graph%20for%20-log%20x.PNG)


### Runtime complexity
The Earley algorithm has nice complexity properties. In particular, it can
parse:

* any CFG in O(n³), 
* unambiguous CFGs in O(n²)
* left-recursive unambiguous grammars in O(n)

Note that this implementation does not apply innovations such as [Joop Leo's improvement](http://www.sciencedirect.com/science/article/pii/030439759190180A) to run linearly on on right-recursive grammars as well. It might be complicated to implement this, and still have a probabilistic parser.

For a faster parser that work on non-probabilistic grammars, look into [Marpa](http://lukasatkinson.de/2015/marpa-overview/#earley-and-marpa). Marpa is a C library with a Perl interface, and a Lua interface is underway. It is currently painful to embed within a Java project, however.

### Limitations
* I have not provisioned for ε-rules
* Rule probability estimation may be performed using the inside-outside algorithm, but is not currently implemented
* Higher level concepts such as wildcards, * and + are not implemented
* Viterbi parsing only returns one single parse. In the case of an ambiguous sentence, the returned parse is not guaranteed the left-most parse.
* Behavior for strangely defined grammars is not defined, such as when the same rule is defined multiple times with
  a different probability

## License
This software is licensed under a permissive [MIT license](https://opensource.org/licenses/MIT).

## References
[Stolcke, Andreas. "An efficient probabilistic context-free parsing algorithm that computes prefix probabilities." *Computational linguistics* 21.2 (1995): 165-201.
APA](http://www.aclweb.org/anthology/J95-2002)
