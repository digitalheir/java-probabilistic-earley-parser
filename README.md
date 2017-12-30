# Probabilistic Earley parser
[![Build Status](https://travis-ci.org/digitalheir/java-probabilistic-earley-parser.svg?branch=master)](https://travis-ci.org/digitalheir/java-probabilistic-earley-parser)
[![GitHub version](https://badge.fury.io/gh/digitalheir%2Fjava-probabilistic-earley-parser.svg)](https://github.com/digitalheir/java-probabilistic-earley-parser/releases)
[![License](https://img.shields.io/npm/l/probabilistic-earley-parser.svg)](https://github.com/digitalheir/java-probabilistic-earley-parser/blob/master/LICENSE)


This is a library for parsing a sequence of tokens (like words) into tree structures, along with the probability that the particular sequence generates that tree structure. This is mainly useful for linguistic purposes, such as morphological parsing, speech recognition and generally information extraction. It also finds applications in computational biology. 

For example:

* As a computational linguist, you want to [derive the most probable interpretation of an English sentence](https://web.stanford.edu/~jurafsky/icassp95-tc.pdf)

|tokens|parse tree|
|---|---|
|[i, want, british, food]|![i want british food](https://cloud.githubusercontent.com/assets/178797/21772897/64838a1e-d68d-11e6-9a9d-11c7c17cb996.png)|

* As a computational biologist, you want to [predict the secondary structure for an RNA sequence](https://en.wikipedia.org/wiki/Stochastic_context-free_grammar#RNA_structure_prediction) (which you can encode in a tree structure)

|tokens|parse tree|
|---|---|
|`GGGC``UAUU``AGCU``CAGU`<br>`UGGU``UAGA``GCGC``ACCC`<br>`CUGA``UAAG``GGUG``AGGU`<br>`CGCU``GAUU``CGAA``UUCA`<br>`GCAU``AGCC``CA` |![rna secondary structure](https://cloud.githubusercontent.com/assets/178797/21773797/af94f972-d690-11e6-97b4-0aad06071634.jpg)|

* As a cognitive scientist, you want to [compute the cognitive load of interpreting sentences](https://www.hlp.rochester.edu/resources/workshop_materials/EVELIN12/Hale01_NAACL_probabilistic%20early%20parser_N01-1021.pdf) as a function of "surprises" that happen as you read a sentence

* As a computational linguist, [you want to know the most likely table of contents structure for a list of paragraphs](https://digitalheir.github.io/java-rechtspraak-library/document-structure/)



This library allows you to do these things [efficiently](#runtime-complexity), as long as you can describe the rules as a [Context-free Grammar](https://en.wikipedia.org/wiki/Context-free_grammar) (CFG).

The innovation of this library with respect to the many other parsing libraries is that this one allows the production rules in your grammar to have a probability attached to them. That is: it parses sentences using [Stochastic Context-free Grammars](https://en.wikipedia.org/wiki/Stochastic_context-free_grammar). This allows us to make better choices in case of ambiguous sentences, because we can order multiple interpretations of the same sentence by probability. Furthermore, this parser does not limit token types to strings.

For a theoretical grounding of this work, refer to [*Stolcke, An Efficient Probabilistic Context-Free
           Parsing Algorithm that Computes Prefix
           Probabilities*](http://www.aclweb.org/anthology/J95-2002).
   
## Usage
You can use this project as a library in your Java application or as a standalone command-line app.
 
### Command line

Download [the latest JAR](https://github.com/digitalheir/java-probabilistic-earley-parser/releases/latest)

By default, the parser will assume that you distinguish non-terminals from terminals by capitalizing them. You can also add a custom category handler if you call the API from Java code. 

Create a UTF8-encoded `.cfg` file that contains your grammar, such as the following:

```
# grammar.cfg

S   -> NP VP   (0.8)    # specify probability between 0 and 1 by appending between parentheses
S   -> NP      (0.2)
NP  -> Det N            # probability defaults to 1.0
NP  -> Det Nom          # all rules for a given category must sum to 1, so the builder normalizes probabilities to ensure they sum to 1.0
Nom -> Adj N
VP  →  V                # Use '->' or '→'
Det →  the              # probability defaults to 1.0
N   →  heavy   (0.2)
Adj →  heavy   (0.8)
V   →  heave   (0.8)
N   →  /heave[r]?/i   (0.2) # You can specify terminals as regular expressions by enclosing them in '/'. 
```

Execute runnable jar on the terminal:
```
java -jar probabilistic-earley-parser-jar-with-dependencies.jar -i grammar.cfg -goal S the heavy heave
```

This will give the Viterbi parse to the ambiguous sentence "the heavy heave":

```
0.128 (= 0.8 * 0.2 * 0.8)
└── <start>
    └── S
        ├── NP
        │   ├── Det
        │   │   └── the (the)
        │   └── N
        │       └── heavy (heavy)
        └── VP
            └── V
                └── heave (heave)
```

This is the parse with the semantic of "heavy people heave"

In contrast, the less likely parse was "a heave that is heavy":

```
0.032 (= 0.2 * 0.8 * 0.2)
└── <start>
    └── S
        └── NP
            ├── Det
            │   └── the (the)
            └── Nom
                ├── Adj
                │   └── heavy (heavy)
                └── N
                    └── heave (heave)
```

The command line interface is meant for quickly trying out a simple grammar. For actual real-life parsing stuff, you probably want to use the Java API. 

### Java API

Grab from Maven:

```xml
<dependencies>
        <dependency>
            <groupId>org.leibnizcenter</groupId>
            <artifactId>probabilistic-earley-parser</artifactId>
            <version>0.9.12</version>
        </dependency>
</dependencies>
```

or Gradle:
```groovy
compile 'org.leibnizcenter:probabilistic-earley-parser:0.9.12'
```

Or just include the [the latest JAR](https://github.com/digitalheir/java-probabilistic-earley-parser/releases/latest) in your project.

Most applications will want to interface with `Parser`, which you instantiate with a grammar:

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

    // Terminal types are realized by implementing the Terminal interface, specifically the function hasCategory. Terminal is a functional interface.
    // Note that tokens can be of multiple terminal types (homographs: "bank" as a noun or "bank" as a verb), so you can use this method to pool many words to a single terminal 
    private static final Terminal<String> transitiveVerb = token -> token.obj.matches("(hit|chased)");
    // Some utility terminal types are pre-defined:
    private static final Terminal<String> the = new CaseInsensitiveStringTerminal("the");
    private static final Terminal<String> a = new CaseInsensitiveStringTerminal("a");
    private static final Terminal<String> man = new ExactStringTerminal("man");
    private static final Terminal<String> stick = new ExactStringTerminal("stick");
    private static final Terminal<String> with = new ExactStringTerminal("with");
    
    private static final Grammar grammar = new Grammar.Builder("test")
            .setSemiring(LogSemiring.get()) // If not set, defaults to Log semiring which is probably what you want. The builder takes care of converting probabilties to semiring elements
            .addRule(
                    1.0,   // Probability between 0.0 and 1.0, defaults to 1.0
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
        Parser<String> parser = new Parser<>(grammar);
        System.out.println(
                parser.recognize(S, Tokens.tokenize("The man     chased the man \n\t with a stick")) // true
        );
        System.out.println(
                parser.getViterbiParseWithScore(S, Tokens.tokenize("the", "stick", "chased", "the", "man")) // Most likely parse tree with probability
        );
    }
}
```
 
You can parse `.cfg` files as follows:

```
Grammar<String> g = Grammar.fromString(Paths.get("path", "to", "grammar.cfg"), Charset.forName("UTF-8"));
```

One of the advantages of Earley parsing is the top-down control you can exert while parsing.
You can pass the parser callbacks to influence the parsing process. Only use this if you really know what you're doing. It may mess up your results if you are not careful.


```
new ParseCallbacks.Builder()
                        .withOnPreScan((position, token, chart) -> System.out.println("Scan about to happen for token " + token))
                        .withScanProbability((position, token) -> {
                            if (token.getCategories().contains(anUnexpectedTerminalForThisWord)) {
                                return grammar.semiring.fromProbability(0.5);
                            } else {
                                return grammar.semiring.one();
                            }
                        })
                        .withOnPostScan((position, token, chart) -> System.out.println("Scan happened for token " + token))
                        .withOnPostComplete((position, token, chart) -> System.out.println("Complete happened for token " + token))
                        .build()
```

## Some notes on implementation

### Runtime complexity
The Earley algorithm has nice complexity properties. In particular, it can
parse:

* any CFG in O(n³), 
* unambiguous CFGs in O(n²)
* left-recursive unambiguous grammars in O(n)

Note that this implementation does not apply innovations such as [Joop Leo's improvement](http://www.sciencedirect.com/science/article/pii/030439759190180A) to run linearly on on right-recursive grammars as well. It might be complicated to implement these ideas and still have a probabilistic parser.

For an efficient parser that works only on non-probabilistic context-free grammars, look into [Marpa](http://lukasatkinson.de/2015/marpa-overview/#earley-and-marpa). Marpa is a C library with a Perl interface, and a Lua interface is underway. It is currently painful to embed within a Java project, however.

### Limitations
Pull requests for these issues are welcome:
* I have not provisioned for ε-rules (empty right-hand sign). [Issue.](https://github.com/digitalheir/java-probabilistic-earley-parser/issues/6)
* Rule probability estimation may be performed using the inside-outside algorithm, but is not currently implemented. [Issue.](https://github.com/digitalheir/java-probabilistic-earley-parser/issues/3)
* Higher level concepts such as * and + are not implemented
* Error handling / logging could be better, available as an experimental feature. [Issue.](https://github.com/digitalheir/java-probabilistic-earley-parser/issues/5)
* Viterbi parsing only returns one single parse. In the case of an ambiguous sentence, the returned parse is not guaranteed the left-most parse.
* Behavior for strangely defined grammars is not defined, such as when the same rule is defined multiple times with
  a different probability. [Issue](https://github.com/digitalheir/java-probabilistic-earley-parser/issues/2)

### Semirings
The probability of a parse is defined as the product of the probabilities of all the applied rules. Usually,
we define probability as a number between 0 and 1 inclusive, and use common algebraic notions of addition and
multiplication.

Implementing this naively can lead to problems: imagine a computation like 0.1 * 0.1 * ... * 0.1. At some point, floating point arithmetic will be unable to represent a number so small (arithmetic underflow). To counter, we map numbers between 0 and 1 to numbers between 0 and infinity with the Log semiring, which allows us to represent a lot more numbers (everything between 0 and `Double.MAX_VALUE`). 

This project makes it possible to use *any* commutative [semiring](https://en.wikipedia.org/wiki/Semiring) that can have its elements
represented as doubles. I can't really imagine a use case for using another semiring than the Log semiring, but who knows, maybe you can.

## License
This software is licensed under a permissive [MIT license](https://opensource.org/licenses/MIT).

## References
1. [Stolcke, Andreas. "An efficient probabilistic context-free parsing algorithm that computes prefix probabilities. *Computational linguistics* 21.2 (1995): 165-201.](http://www.aclweb.org/anthology/J95-2002)
2. [Leo, Joop MIM. "A general context-free parsing algorithm running in linear time on every LR (k) grammar without using lookahead." Theoretical computer science 82.1 (1991): 165-176.](http://www.sciencedirect.com/science/article/pii/030439759190180A)
3. [Kegler, Jeffrey. "Marpa, A Practical General Parser: The Recognizer." (2012): 115.](http://dinhe.net/~aredridel/.notmine/PDFs/Parsing/KEGLER,%20Jeffrey%20-%20Marpa,%20a%20practical%20general%20parser:%20the%20recognizer.pdf)

## Contact
Inquiries go to Maarten Trompper at maarten.trompper@gmail.com
