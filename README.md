# jRIT
A utility for instrumenting Java bytecode at runtime. Put under the Apache License because I'm not that picky about who uses it for what.

## Building
Requires maven and Java 8.

`./build.sh`. If you wish to run it as well, either run `./run.sh` after building, or run `./full.sh` to build everything at once.

## Usage
Have a Java process running. Start the application. Select the correct JVM to attach. Proceed to edit method bytecode.

## How does it work?
A combination of Java's Instrumentation and Attach APIs. After attaching to a JVM instance, the application caches the bytecode of as many classes as it can (part of the intent here is to allow for an 'undo' function in the future). Once this is done, it opens up a GUI so that the user can see the bytecode for methods of various classes and edit it. If something goes wrong with the newly edited bytecode, the application prints it to `stderr` of the JVM it attached.

The different parts of it have a somewhat weird relationship. `jrit-main` is the initial GUI that a user sees to select a VM instance with. Once a VM instance has been selected, it attaches `caching-agent` to that VM. `caching-agent` then proceeds to launch the "main" GUI of the application, allowing one to edit bytecode and apply the changes to see what happens. When changes are applied, `caching-agent` launches `reinstrumentation-agent` to, as its name implies, (re-)instrument the bytecode of the running Java application so tht changes can be observed.

## Known problems
 * Sometimes crashes after applying changes. No idea why
 * Ugly code. Like "500-line-`switch`-block"-type of ugly.
