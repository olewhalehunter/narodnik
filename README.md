# народник - Narodnik

A distributed user agent system in Clojure. 

Use Narodnik to...:

* make a networked virtual machine
* start a botnet
* automate web testing
* create distributed and real-time systems

Licensed under the GNU Affero Public License.

See files in \dev-environ.

## TO-DO


master:

* slave task/state caching
* schedule trigger
* package structure
* browser api
* master->slave crud api

slave:

* slave deploy/install folder structure
* http callback for json feedback from browser runtimes
* write-userscript templating lib (scriptish)
* create sample workflow packages

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
* ...for cross-db state merging (for scaling master instances)
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


