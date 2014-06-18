(ns narodnik-server.handler
  (:use [aleph.udp]
        [gloss core io]
        [lamina core api]))

(defn message-thread []
  (let [inbound-channel 
        @(udp-object-socket {:port 10200})
        outbound-channel 
        @(udp-object-socket)
        text-msg "(println \":)\")"]
    (try
      (enqueue outbound-channel 
               {:message text-msg 
                :host "localhost" 
                :port 10200})
      (eval (read-string
             (:message (wait-for-message 
                          inbound-channel 12000))))
      (finally
        (close inbound-channel)
        (close outbound-channel)
        (System/exit 0)
      ))))


(defn -main [& args]
  (.start (Thread. message-thread))
  (println "NARODNIK SERVER!")
)
