# Probabilistic Earley parser

This is an implementation of a probabilistic Earley parsing algorithm
in Java, which can parse any Probabilistic Context Free Grammar (PCFG) (also
known as Stochastic Context Free Grammar (SCFG)),
or equivalently any language described in Backus-Naur Form (BNF),
where rewrite rules may be non-deterministic and have a probability 
attached to them.

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
Most internal parsing stuff is available through he public API, in case you need a slightly different parser than usual. 
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

    // Token types are realized by implementing Terminal, and implementing hasCategory. This is a functional interface.
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
                Parser.recognize(S, grammar, Tokens.tokenize("The man     chased the man \n\t with a stick")) // 1.0
        );
        System.out.println(
                Parser.recognize(S, grammar, Tokens.tokenize("the", "stick", "chased", "the", "man")) // 0.6
        );
    }
}
```


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
