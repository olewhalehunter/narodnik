(ns narodnik-client.core
  (:use 
   [aleph udp]
   [lamina core api]))

(comment
  ;; workflow structure

  logic is deployed to slaves
    as Narodnik .CLJ Package 

    clojure package pullable from repository = wget from repl
     logic.clj -> loads files for resources
     ;dir resources

  ;; sample outbound message slave->master
  
  {:message 
   {:body {
    :command "(increment :num-widgets)"
    :value 0}
   :key "ab527c43dh324efa34f"
   :machineid 3
   }}
)

(defn handle-message [message client-channel]
  (println "Handling :" message)
  (eval (read-string message))
)

(defn slave-handler-thread []
  "Slave inbound thread."

  (let [inbound-port 10201
        host "localhost"
        response "got it"
        slave-handler-interval 1000]

    (Thread/sleep slave-handler-interval)

    (let [client-channel @(udp-object-socket {:port inbound-port})]
      (try (let [message 
                 (:message (wait-for-message client-channel))]
             (try 
               (handle-message message client-channel)
               (catch 
                   Exception bad-message 
                 (println "Bad message: " bad-message))
             (catch 
                 Exception network-error
               (println "Error reading from port" 
                        inbound-port network-error))
             (finally
               (close client-channel))))
      (slave-handler-thread)))))


(defn -main [& args]
  (println "Starting Narodnik slave...")
  (.start (Thread. slave-handler-thread))
  (println "NARODNIK CLIENT!"))



