# cli-loop - Command-Line JVM Interpreters

<img src='https://raw.githubusercontent.com/mslinn/cli-loop/gh-pages/images/cliLoop.png' align='right' width='33%'>

[![Build Status](https://travis-ci.org/mslinn/cli-loop.svg?branch=master)](https://travis-ci.org/mslinn/cli-loop)
[![GitHub version](https://badge.fury.io/gh/mslinn%2Fcli-loop.svg)](https://badge.fury.io/gh/mslinn%2Fcli-loop)

Supports Groovy, JavaScript and Jython interpreters via [JSR223](https://en.wikipedia.org/wiki/Scripting_for_the_Java_Platform).
All scripts can share variables with Java and Scala code.

Warning: Groovy's implementation of JSR223's `eval` method does not add new variables or functions to the `ScriptContext.ENGINE_SCOPE` bindings.
`put` and `get` work, however.
I filed issue [GROOVY-8400](https://issues.apache.org/jira/browse/GROOVY-8400).
      
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
Micronautics Research Ethereum Shell v0.2.1
Commands are: account, bindkey, exit/^d, help/?, javascript, jython, password, set, testkey and tput
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

Commands are: account, bindkey, exit/^d, help/?, javascript, password, set, testkey and tput
cli-loop [master] shell>
[success] Total time: 318 s, completed Nov 1, 2017 2:53:25 AM

2. Waiting for source changes... (press enter to interrupt)
Micronautics Research Ethereum Shell v0.1.0
Commands are: account, bindkey, exit/^d, help/?, javascript, password, set, testkey and tput
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

Commands are: account, bindkey, exit/^d, help/?, javascript, password, set, testkey and tput
cli-loop [master] shell>
^C
mslinn@kaiju cli-loop (master)
```
