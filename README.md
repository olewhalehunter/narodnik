# Narodnik

Distributed Heterogenous Actors Agreements protocol:
* s-expression contract and schedule specifications
* subscribe or publish channel specifications
* process, publish or subscribe to channel schedules
* declared constraint graph subscriptions
* channel modules

# Theory & Implementation

* influenced by Protocol Buffers, Prolog, and the Actor Model
* "DHA VM" compile to Common Lisp, Clojure, and Python

# TODO

security and networking/package:

* byte compiled package messages
* encryption/compression
* p2p communication and chaining with common state
* dynamic port allocation/reassignment
* request per-ip throttle
* buffer limit
* tunneling over http

speed & optimization:

* caching; machines, hosts, dynamic?
* compile final exchange/package interface messages to byte encoding

future other:

* iterative state tracking on db, each insert with timetstamp
* dependent state-changing fn track, store each stateful! sexp eval'd
* ^ example of above is db inserts created from existing state data, (foreach widget insert...)
* ...for cross-db state merging (for scaling vm instances)
* schema tinkering, batch query optimization

installer:

* linux install script
* windows msi

install order:

* client .jar
* client narodnik core install module
* find firefox dir
* install scriptish .xpi
* find scriptish dir
* install narodnik js runtime there


