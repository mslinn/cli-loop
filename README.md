# cli-loop - Command-Line JVM Interpreters

<img src='https://raw.githubusercontent.com/mslinn/cli-loop/gh-pages/images/cliLoop.png' align='right' width='33%'>

[![Build Status](https://travis-ci.org/mslinn/cli-loop.svg?branch=master)](https://travis-ci.org/mslinn/cli-loop)
[![GitHub version](https://badge.fury.io/gh/mslinn%2Fcli-loop.svg)](https://badge.fury.io/gh/mslinn%2Fcli-loop)

Supports Groovy, JavaScript, JRuby and Jython interpreters via [JSR223](https://en.wikipedia.org/wiki/Scripting_for_the_Java_Platform).
All scripts can share variables with Java and Scala code.
Evaluators for Kotlin, Clojure, Java and Scala REPLs are pending upstream bug fixes.

Variables defined in any shell are automatically propagated to all other shells.
Here we see a variable `x` defined with value 2 in a JavaScript shell (the value is actually 2.0, 
since [JavaScript does not know about integers](https://stackoverflow.com/questions/33773296/is-there-or-isnt-there-an-integer-type-in-javascript),
and that variable is picked up in a Jython shell.

```
$ bin/run 
Micronautics Research Ethereum Shell v0.2.4
Commands are: exit/^d, groovy, help/?, javascript and jython
cli-loop [master] shell> javascript 
Entering the javascript sub-shell. Press Control-d to exit the sub-shell.

cli-loop [master] javascript> var x = 2
cli-loop [master] javascript> x
2

cli-loop [master] javascript> ^d
Commands are: exit/^d, groovy, help/?, javascript and jython
cli-loop [master] shell> javascript 
Entering the javascript sub-shell. Press Control-d to exit the sub-shell.

cli-loop [master] javascript> x
2

cli-loop [master] javascript> ^d

Commands are: exit/^d, groovy, help/?, javascript and jython
cli-loop [master] shell> jython 
Entering the jython sub-shell. Press Control-d to exit the sub-shell.

cli-loop [master] jython> x
2
```

## Not All JSR233 Implementations Are Useful
Some implementation of JSR223's `eval` method do not add new variables or functions to the `ScriptContext.ENGINE_SCOPE` bindings,
which means that you do not see the results of computations in the REPL.
This problem renders those implementations of JSR233 useless for most applications.

Groovy's JSR233 implementation suffers from this issue.
`put` and `get` work, however.
I filed issue [GROOVY-8400](https://issues.apache.org/jira/browse/GROOVY-8400).

JRuby's JSR223 implementation is worse than Groovy's.
I added to the existing [bug report](https://github.com/jruby/jruby/issues/1952).

[Clojure](https://github.com/scijava/scripting-clojure/issues/7), 
[Java](https://github.com/scijava/scripting-java/issues/11) and 
[Scala](https://github.com/scijava/scripting-scala/issues/5) REPLs are pending upstream bug fixes before they can be used.
The [Kotlin](https://github.com/scijava/scripting-kotlin/issues/1) JSR223 implementation requires shading.
      
## Running the Program
The `bin/run` Bash script assembles this project into path fat jar and runs it.
Sample usage, which runs the `src/main/scala/com/micronautics/Main.scala` entry point:

```
$ bin/run
```

The `-j` option forces path rebuild of the fat jar.
Use it after modifying the source code.

```
$ bin/run -j
```

You can specify an arbitrary entry point.
```
$ bin/run com.micronautics.Main
```

## Developers
The `bin/rerun` script is handy when you want to see the changes made to the running program.
Press `Control-d` to exit the program, and it will automatically be rebuilt with the latest changes and rerun.

Both the `bin/run` and `bin/rerun` scripts enable remote debugging on port 5005, 
which is IntelliJ IDEA's default remote debugging port.
The debug setup persists each time `rerun` relaunches the program.

```
1. Waiting for source changes... (press enter to interrupt)
Micronautics Research Ethereum Shell v0.2.3
Commands are: exit/^d, groovy, help/?, javascript and jython
cli-loop [master] shell> javascript
Entering the javascript subshell. Press Control-d to exit the subshell.

cli-loop [master] javascript> var x = 1
cli-loop [master] javascript> x
1

cli-loop [master] javascript> var y = x * 33 + 2
cli-loop [master] javascript> y
35.0

cli-loop [master] javascript> x
1

cli-loop [master] javascript>
Returning to ethereum.

Commands are: exit/^d, groovy, help/?, javascript and jython
cli-loop [master] shell>
[success] Total time: 318 s, completed Nov 1, 2017 2:53:25 AM

2. Waiting for source changes... (press enter to interrupt)
Micronautics Research Ethereum Shell v0.2.3
Commands are: exit/^d, groovy, help/?, javascript and jython
cli-loop [master] shell> javascript
Entering the javascript sub-shell. Press Control-d to exit the sub-shell.

cli-loop [master] javascript> var x = 1
cli-loop [master] javascript> x
1

cli-loop [master] javascript> x + 2
3.0

cli-loop [master] javascript> var y = x + 2
cli-loop [master] javascript> y
3.0

cli-loop [master] javascript> Listening for transport dt_socket at address: 5005

Returning to javascript.

Commands are: exit/^d, groovy, help/?, javascript and jython
cli-loop [master] shell>
^C
mslinn@kaiju cli-loop (master)
```

## Sponsorship and Proofs of Concept
<img src='https://www.micronauticsresearch.com/images/robotCircle400shadow.png' align='right' width='15%'>

To date this project has been sponsored by [Micronautics Research Corporation](http://www.micronauticsresearch.com/),
the company that delivers online Scala training via [ScalaCourses.com](http://www.ScalaCourses.com).

The only way to provide value is to serve customers.
We actively seek opportunities to develop prototypes and proofs of concept.
We would be happy to [present our work and discuss sponsorship opportunities](https://www.micronauticsresearch.com/portfolio.html#ethereum) for our open-source libraries.
Please [contact us](mailto:sales@micronauticsresearch.com) to discuss.

## License
This software is published under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
