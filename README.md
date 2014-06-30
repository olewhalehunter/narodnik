# NARODNIK
# народник

A distributed user agent network in Clojure.

Use Narodnik to...:

* make a networked virtual machine
* start a botnet
* automate web testing
* create distributed and real-time systems

Licensed under the GNU Affero Public License.

See files in \dev-environ.

## TO-DO

narodnik-server:

* job assign retry/proper job status flags, msg confirmation workflow/fn
* load package
* ? allowed evals lookup for client callbacks in each package?
* crud api and sample workflow

security and networking/package:

* eval scoping/isolation api, limited to package defns, byte-compiled msgs
* p2p communication and chaining with common state
* dynamic port allocation/reassignment
* request per-ip throttle
* buffer limit
* tunneling over non-udp protocol

speed & optimization:

* caching; machines, hosts, dynamic?
* compile final exchange/package interface messages to byte encoding

future other:

* iterative state tracking on db, each insert with timetstamp
* dependent state-changing fn track, store each stateful! sexp eval'd
* ^ example of above is db inserts created from existing state data, (foreach widget insert...)
* ...for cross-db state merging (for scaling master instances)
* schema tinkering, batch query optimization

narodnik-client:

* slave deploy/install folder structure
* write-userscript templating lib (scriptish)
* create sample workflow packages
* same lib and distributable as server, config as uberjar profile

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

design notes:

* use clj not json for data/fn serialization
* macro library to clean up jvm fns, exception handling
* userscript notes...
* C:\Users\\AppData\Roaming\Mozilla\Firefox\Profiles\x1d8zrt8.default

Windows:

* "C:\Program Files (x86)\Mozilla Firefox\"
* C:\Users\\AppData\Roaming\Mozilla\Firefox\Profiles\x1d8zrt8.default\scriptish_scripts\testjs
* taskkill /F /IM java.exe
    
