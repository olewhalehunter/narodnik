(ns narodnik-server.handler
  (:use 
   [aleph.udp]
   [gloss core io]
   [lamina core api]
   [narodnik-server data]
))

(defn handler-thread []
  "For updating status updates 
   and CRUD operations from slaves"

  (let [inbound-channel @(udp-object-socket {:port 10201})
        timeout 4000]

      (Thread/sleep timeout)
      (eval (read-string (:message (wait-for-message inbound-channel 12000))))

      (close inbound-channel))
  (handler-thread))

(defn task-assign-thread []
  "testing handler thread"

  (let [task-handler-interval 1000
        inbound-port 10202
        outbound-port 10201
        host "localhost"
        text-msg "(println \"NARODNIK IS GO\")"]

     (Thread/sleep task-handler-interval)

    (let [outbound-channel @(udp-object-socket)
          inbound-channel @(udp-object-socket {:port inbound-port})]
      (try
        (enqueue outbound-channel 
                 {:message text-msg 
                  :host host 
                  :port outbound-port})
        (finally
          (close outbound-channel)
          (close inbound-channel))))
    (task-assign-thread)))


(defn -main [& args]
  (println "Narodnik starting up...")
  (init-db)
  (.start (Thread. task-assign-thread)) 
  (.start (Thread. handler-thread))
  (println "NARODNIK SERVER:")
  (loop [lines (repeatedly read-line)]
    (let [input (first lines)]
      (when (not= "exit" input)
        (try 
          (load-string input) 
             (catch Exception e 
               (println "Could not evaluate '" input "'.")))
        (recur (next lines))
        )))
  (println "Narodnik shutting down...")
  (System/exit 0)
  )


