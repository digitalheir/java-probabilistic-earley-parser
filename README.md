

# Earley parser
This is an implementation of the Earley algorithm in Java, which can parse any Context Free Grammar (CFG), or equivalently any language described in Backus-Naur Form (BNF).
 
It is based on Scott Martin's implementation called [PEP](http://www.coffeeblack.org/#projects-pep), but uses Java 8 APIs and is more timid about instantiating objects.
 
## Runtime complexity
The Earley algorithm has some nice properties. In particular, it can 
parse:

* any CFG in O(n³), 
* unambiguous CFGs and O(n²)
* left-recursive unambiguous grammars in O(n)

Note that this implementation does not apply [Joop Leo's improvement](http://www.sciencedirect.com/science/article/pii/030439759190180A) to run linearly on on right-recursive grammars as well, nor does it implement, nor [Aycock and Horspool's fix for nullable grammars](http://webhome.cs.uvic.ca/~nigelh/Publications/PracticalEarleyParsing.pdf). I would have liked to, but it takes considerable effort.

For a better parser, look into [Marpa](http://lukasatkinson.de/2015/marpa-overview/#earley-and-marpa). Marpa is a C library with a Perl interface, and a Lua interface is underway. It is currently painful to embed within a Java project, however.
