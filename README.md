# NARODNIK
# народник

A disributed user agent network.

Licensed under the GNU Affero Public License.

See files in \dev-environ.

## TO-DO

### narodnik-client

NO CLS, template generated js in native clj scripts
write-userscript templating fn


create sample workflow packages
build up with backend

notes...

C:\Users\\AppData\Roaming\Mozilla\Firefox\Profiles\x1d8zrt8.default

### Windows
"C:\Program Files (x86)\Mozilla Firefox\"
C:\Users\\AppData\Roaming\Mozilla\Firefox\Profiles\x1d8zrt8.default\scriptish_scripts\testjs

    

### narodnik-server
dispatch thread vs slave thread
dispatch hosts and serves
slave thread reads from db and writes to db
schema design
server groups
client groups/subscriptions
event-eval dispatch queue

### installer
    
linux install script
windows msi

####install order
client .jar
client narodnik core install module
find firefox dir
install scriptish .xpi
find scriptish dir
install narodnik js runtime there

### design notes

use clj not json for data/fn serialization
