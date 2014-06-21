(ns narodnik-server.handler
  (:use 
   [aleph.udp]
   [gloss core io]
   [lamina core api]
   [narodnik-server data]))

(defn handler-thread [instance]
  "For updating status updates 
   and CRUD operations from slaves"

  (let [inbound-channel @(udp-object-socket {:port 10202})
        timeout 4000]

      (Thread/sleep timeout)
      (eval (read-string (:message (wait-for-message inbound-channel 12000))))

      (close inbound-channel))
  (handler-thread instance))

(defn task-assign-thread [instance]
  "testing handler thread"

  (let [task-handler-interval 1000
        inbound-port (:inbound-port instance)
        outbound-port (:outbound-port instance)]

     (Thread/sleep task-handler-interval)

    (let [outbound-channel @(udp-object-socket)]
          ;inbound-channel (udp-object-socket {:port inbound-port})] ; need local eval
      (try
        (enqueue outbound-channel 
                 {:message "(println \"testing\")"
                  :host "localhost"
                  :port outbound-port})
        (finally
          (close outbound-channel)
        ;  (close inbound-channel)
          )))
    (task-assign-thread instance)))

(defn -main [& args]
  (println "Narodnik master starting...")
  (init-db)
  (let [master-instance {
                         :privatekey "key" 
                         :outbound-port 10201
                         :inbound-port 10202}]
    (.start (Thread. 
             (task-assign-thread  master-instance))) 
    (.start (Thread. 
             (handler-thread master-instance)))
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
    (System/exit 0)))


