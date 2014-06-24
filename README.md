# NARODNIK
# народник

A distributed user agent network.

Use Narodnik to...:

* make a networked virtual machine
* start a botnet
* automate web testing
* create distributed systems

Licensed under the GNU Affero Public License.

See files in \dev-environ.

## TO-DO

narodnik-server:

* assign task slave
* load package
* crud api and sample workflow

future security and networking/package:

* eval scoping/isolation -> API?, limited to package definitions
* request per-ip throttle
* buffer limit
* multiple-master subscription
* dynamic port allocation/reassignment
l
future other:
* tune packet traffic
* iterative state tracking on db, each insert with timetstamp
* dependent state-changing fn track, store each stateful! sexp eval'd
* ^ example of above is db inserts created from existing state data, (foreach widget insert...)
* ...for cross-db state merging (for scaling master instances)


narodnik-client:

* handle-task
* slave deploy/install folder structure
* write-userscript templating fn (scriptish)
* create sample workflow packages

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
    
