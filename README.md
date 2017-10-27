# cli-loop

[![Build Status](https://travis-ci.org/mslinn/cli-loop.svg?branch=master)](https://travis-ci.org/mslinn/cli-loop)
[![GitHub version](https://badge.fury.io/gh/mslinn%2Fcli-loop.svg)](https://badge.fury.io/gh/mslinn%2Fcli-loop)

## Running the Program
The `bin/run` Bash script assembles this project into a fat jar and runs it.
Sample usage, which runs the `Hello` entry point in `src/main/scala/Hello.java`:

```
$ bin/run Hello
```

The `-j` option forces a rebuild of the fat jar.
Use it after modifying the source code.

```
$ bin/run -j Hello
```
