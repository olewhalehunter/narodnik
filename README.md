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

* cache structure
* slave task/state caching so tasks aren't repeated
* clojure time
* scheduled task/action/job triggers
* package structure~?
* master->slave crud api
* database recycling, task string \/
* (column in task called action like "test-package"/"greetings")

slave:

* slave deploy/install folder structure
* http callback for json feedback from browser runtimes
* write-userscript templating lib (scriptish) **
* create sample workflow packages

Browser Runtime API ** :

* master dictionary state updates
* slave -> master state piping
* default methods/values:
* collection of forms on names with identifiers:
* collection of images on page

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


