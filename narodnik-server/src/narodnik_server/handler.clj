(ns narodnik-server.handler
  (:use [aleph.udp]
        [gloss core io]
        [lamina core api]))

(defn message-thread []
  (println "sending message...")
  (Thread/sleep 500)
  (let 
      [outbound @(udp-object-socket {:port 9999})
       message {:host "localhost", 
              :port 9997, 
              :message "hello"}]
    (enqueue outbound message)
    (close outbound))
  (message-thread)
)

(defn listen-thread []
  (Thread/sleep 3000)
  (println "getting message...")
  (let 
      [inbound @(udp-object-socket {:port 9997})]
    (println (:message (read-channel inbound)))
    (close inbound))
  (listen-thread)
)

(println (:message (read-channel (udp-object-socket 
                                  {:port 9997}))))

(defn -main [& args]
  (.start (Thread. message-thread))
  (.start (Thread. listen-thread))
  (println "NARODNIK SERVER!"))
