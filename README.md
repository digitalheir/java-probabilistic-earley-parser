# **WORK IN PROGRESS!!!**

# Stochastic Earley parser

This is an implementation of a stochastic Earley parsing algorithm 
in Java, which can parse any Probabilistic Context Free Grammar (PCFG), 
or equivalently any language described in Backus-Naur Form (BNF),
where rewrite rules may be non-deterministic and have a probability 
attached to them.

For a theoretical grounding of this work, refer to e.g.
[*Stolcke, An Efficient Probabilistic Context-Free
           Parsing Algorithm that Computes Prefix
           Probabilities*](http://www.aclweb.org/anthology/J95-2002).
  
## Usage 
TODO
  
## Runtime complexity
The Earley algorithm has some nice properties. In particular, it can 
parse:

* any CFG in O(n³), 
* unambiguous CFGs in O(n²)
* left-recursive unambiguous grammars in O(n)

Note that this implementation does not apply innovations such as [Joop Leo's improvement](http://www.sciencedirect.com/science/article/pii/030439759190180A) to run linearly on on right-recursive grammars as well. 

For a faster parser that work on non-probabilistic grammars, look into [Marpa](http://lukasatkinson.de/2015/marpa-overview/#earley-and-marpa). Marpa is a C library with a Perl interface, and a Lua interface is underway. It is currently painful to embed within a Java project, however.

## License
This software is licensed under a permissive [MIT license](https://opensource.org/licenses/MIT).