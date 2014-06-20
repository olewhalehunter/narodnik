(ns narodnik-client.core
  (:use 
   [aleph udp]
   [lamina core api]))

(defn slave-handler-thread []
  "Slave I/O thread."

  (let [slave-handler-interval 1000
        inbound-port 10201
        outbound-port 10202
        host "localhost"
        response "got it"]

    (Thread/sleep slave-handler-interval)

    (let [outbound-channel @(udp-object-socket)
          inbound-channel @(udp-object-socket {:port inbound-port})]
      (try
        (let [message (:message (wait-for-message inbound-channel))]
          (println message))
        (catch Exception e (println "Error listening on port" inbound-port))
        (finally
          (close outbound-channel)
          (close inbound-channel))))
    (slave-handler-thread)))

(defn -main [& args]
  (println "Starting Narodnik slave...")
  (.start (Thread. slave-handler-thread))
  (println "NARODNIK CLIENT!"))



