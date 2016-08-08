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
  
## Usage 
Most internal parsing stuff is available through he public API, in case you need a slightly different parser than usual. 
Most applications will want to interface with the static functions in `Parser`:

```java
Parser.getViterbiPath(tokens, )
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
* Higher level concepts such as wildcards, * and + are not currently implemented
* Behavior for strangely defined grammars is not defined, such as when the same rule is defined multiple times with
  a different probability

## License
This software is licensed under a permissive [MIT license](https://opensource.org/licenses/MIT).

## References
[Stolcke, Andreas. "An efficient probabilistic context-free parsing algorithm that computes prefix probabilities." *Computational linguistics* 21.2 (1995): 165-201.
APA](http://www.aclweb.org/anthology/J95-2002)
