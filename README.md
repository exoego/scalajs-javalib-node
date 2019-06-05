# Java standard libs ported for Node.js

**Forget Node.js libs.<br>
Focus in developing using familiar Scala/Java libs.**

Wanna develop apps for Node.js (Electron or FaaS), but despairing over Node.js libs?

This library provide some important parts of Java libs, like IO/Child processes, by re-implementing them on top of Node.js libs.
So you may reduce effort into Node.js lib and their type facade. 


## Goal

Provide core parts of Java libs, e.g. 

* `java.io` and `java.nio`
* `java.util.concurrency`
* `java.lang` like (`Process` and `ProcessBuilder`)


## Non-Goal

This project may not provide the following libs, or may provide only skelton/stub implementation if needed, since those are considered useless for writing Node.js apps.

* `java.applet`
* `java.awt`
* `java.beans`
* `java.lang.annotation`
* `java.lang.instrument`
* `java.lang.management`
* `java.rmi`
* `java.security`
* `java.sql`
* `java.util.jar`
* `java.util.logging`
* Most of `javax.*` packages

## Note

Many implementations and tests are based on [Apache Harmony project](https://github.com/apache/harmony), not OpenJDK.
This is because Harmony uses Apache License, which is very flexible and non-invasive, but OpenJDK uses GPL. 
