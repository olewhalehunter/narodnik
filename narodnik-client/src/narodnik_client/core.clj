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
     contains api from master repl to slave runtime
     ;dir resources

  ;; sample logic.clj, master script

     (let [machine (first-available-machine)]
          (tell machine 
                give-me-sad-news-from
                []))
                
     
  ;; fn run on slave

    (defn run-browser-on-page-and-wait-for-json-results [page-set]
      (Thread. (start. (start-browser pages)))
      (doseq ;or list?
       (while (notempty page-set
                       (handle-http-requests (parse-json))))))
                             
      ;json back tells slave to tell master to update
      
    (defn sad? [] (incanter...))

     (defn give-me-sad-news-from [page-set]
        (activate-user-script gimme-sad-news) ; -> push userscript to browser folder
        (run-browser-on-pages-and-wait-for-json-results)
        (message-back-to-master 
         filter sad? result-set))

  ;; sample inbound message master->slave

     {:message 
      {:body {
        :command "(give-me-sad-news-titles ['www.page-set...])"
        :value 0}
       :key "ab527c43dh324efa34f"
       :machineid 3
       }}

  ;; sample outbound message slave->master

  {:message 
   {:body {
    :command "(increment :num-widgets)"
    :value 0}
   :privatekey "narodnikkey"
   :publickey "Ha79000"
   :machineid 3
   }}
)

(defn init-follow-master-thread [client-channel instance]
  (println "Contacting master...")
  (let [invite-request {:message {
                          :name (:machineid instance)
                          :privatekey (:privatekey instance)
                          :publickey (:publickey instance)}
                        :host (:host (:master-host instance))
                        :port (:port (:master-host instance))}
        number-request-attempts 10
        request-timeout 10000]
    
    (dotimes [n number-request-attempts]
      (println "Attempting to contact master...")
      (enqueue client-channel invite-request)
      (try
        (let [master-reply 
              (wait-for-message 
               client-channel request-timeout)]
          (println "Got reply from master :" (str (master-reply))))
        (catch Exception timeout-exception
          (println "Still waiting for reply...")))))
 (close client-channel)
 (println "Could not contact master."))

(defn handle-message [message client-channel instance]
  (println "Handling :" message)
  (eval (read-string message)))


(defn slave-handler-thread [instance]
  "Slave inbound thread."
)


(defn -main [& args] ; args -> slave-instance
  (let
      [slave-instance {:machineid "daisy"
                       :publickey "Ha79000"
                       :privatekey "narodnikkey" 
                       :master-host {:host "localhost"
                                     :port 10666}
                       :inbound-port 10201}]

    (println "Starting Narodnik slave...")
    ;(.start (Thread. (slave-handler-thread slave-instance)))
    ;^ after join/authorize from master
    (let [client-channel @(udp-object-socket 
                           {:port (:inbound-port slave-instance)})]
      (init-follow-master-thread client-channel slave-instance)))
  (System/exit 0))



(comment "tests"
         (let [outbound-channel @(udp-object-socket {:port 10999})
               message {:message {:name "test"}}]

           (enqueue message outbound-channel))

         (let [inbound-channel @(udp-object-socket {:port 10999})
               message {:message {:name "test"}}
               timeout 5000] 
           (try (wait-for-message inbound-channel timeout)
                (catch Exception e (println "Error waiting for message"))
                (finally (close inbound-channel))))
)
