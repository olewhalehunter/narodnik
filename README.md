# NARODNIK
# народник

A distributed user agent network.

Use Narodnik to...
*automate networked testing
*start a botnet
*create distributed systems

Licensed under the GNU Affero Public License.

See files in \dev-environ.

## TO-DO

### narodnik-server

*msg authentication
*setup new slave
*security, publickey

###todo security and networking: 
*invite keys, (nonglobal :private keys per master->slave relationship)
*request per-ip throttle
*buffer limit
*tune packet traffic

### narodnik-client

*init handshake
*handle-message
*write-userscript templating fn (scriptish)

*create sample workflow packages
*build up with backend


### installer
    
*linux install script
*windows msi

####install order
*client .jar
*client narodnik core install module
*find firefox dir
*install scriptish .xpi
*find scriptish dir
*install narodnik js runtime there

### design notes

*use clj not json for data/fn serialization

*macro library to clean up jvm fns, exception handling

*userscript notes...

*C:\Users\\AppData\Roaming\Mozilla\Firefox\Profiles\x1d8zrt8.default

### Windows
*"C:\Program Files (x86)\Mozilla Firefox\"
*C:\Users\\AppData\Roaming\Mozilla\Firefox\Profiles\x1d8zrt8.default\scriptish_scripts\testjs

    
