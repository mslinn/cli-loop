# cli-loop

[![Build Status](https://travis-ci.org/mslinn/cli-loop.svg?branch=master)](https://travis-ci.org/mslinn/cli-loop)
[![GitHub version](https://badge.fury.io/gh/mslinn%2Fcli-loop.svg)](https://badge.fury.io/gh/mslinn%2Fcli-loop)

## Running the Program
The `bin/run` Bash script assembles this project into a fat jar and runs it.
Sample usage, which runs the `Hello` entry point in `src/main/scala/Main.java`:

```
$ bin/run
```

The `-j` option forces a rebuild of the fat jar.
Use it after modifying the source code.

```
$ bin/run -j
```

You can specify an arbitrary entry point.
This runs the Scala test program:
```
$ bin/run Main
```

This runs the Java test program:
```
$ bin/run JavaMain
```

## Developers
The `bin/rerun` script is handy when you want to see the changes made to the running program.
Press `Control-d` to exit the program, and it will automatically be rebuilt with the latest changes and rerun.
