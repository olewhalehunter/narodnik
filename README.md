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

assign task slave
schedule task
load package
crud api and sample workflow

todo security and networking/package

* client-eval scoping/isolation -> API?, limited to package definitions
* request per-ip throttle
* buffer limit
* tune packet traffic
* multiple-master subscription
* dynamic port allocation/reassignment

narodnik-client:

* handle-task
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
    
