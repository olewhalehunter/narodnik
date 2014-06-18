(ns narodnik-server.handler
  (:use [aleph.udp]
        [gloss core io]
        [lamina core api]))

(def task-assign-interval 1000)

(defn handler-thread []
  "For updating status updates 
   and CRUD operations from slaves"

  (let [inbound-channel @(udp-object-socket 
                          {:port 10201})
        timeout 4000]
      (Thread/sleep timeout)
      (println "Starting inbound...")
      (println (:message (wait-for-message 
                         inbound-channel 12000)))
      (close inbound-channel))
  (handler-thread))

(defn task-assign-thread []
  "testing handler thread"

  (Thread/sleep task-assign-interval)
  (let [outbound-channel @(udp-object-socket)
        inbound-channel @(udp-object-socket {:port 10202})
        text-msg "(println \"NARODNIK IS LIVE\")"]
    (try
      (enqueue outbound-channel 
               {:message text-msg 
                :host "localhost" 
                :port 10201})
       (finally
        (close outbound-channel)
        (close inbound-channel))))
  (task-assign-thread))

(defn -main [& args]
 (.start (Thread. task-assign-thread)) 
 (.start (Thread. handler-thread))

  (println "NARODNIK SERVER!")
)
