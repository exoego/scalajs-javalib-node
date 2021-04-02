# Java API ported for Node.js

This library provides some of Java's important APIs, like IO/NIO/Child processes, by re-implementing them on top of Node.js libs.
So Scala.js users may reduce efforts into Node.js APIs and their type facade. 


## Goal

Provide core parts of Java APIs, e.g. :

* `java.io`
* `java.nio`
* `java.lang` like (`Process` and `ProcessBuilder`)


## Non-Goal

This project may not provide the following APIs, or may provide only skelton/stub implementation if needed, since those are considered useless for writing Node.js apps.

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

Some implementations and tests are based on [Apache Harmony project](https://github.com/apache/harmony), not OpenJDK.
This is because Harmony uses Apache License, which is very flexible and non-invasive, but OpenJDK uses GPL. 

Also some implementations are based on [Scala.js](https://github.com/scala-js/scala-js) to modify them specifically for Node.js.
