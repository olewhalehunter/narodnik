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
  (let [invite-request {:message
                        {:message {
                          :privatekey (:privatekey instance)
                          :publickey (:publickey instance)}}
                        :host (:host (:master-host instance))
                        :port (:port (:master-host instance))}]
    
    (enqueue client-channel invite-request )

    (let [master-reply 
          (wait-for-message client-channel)]
      (println "Got reply from master :" (str (master-reply))))))
  

(defn handle-message [message client-channel instance]
  (println "Handling :" message)
  (eval (read-string message))
)


(defn slave-handler-thread [instance]
  "Slave inbound thread."

  (let [inbound-port (:inbound-port instance)
        slave-handler-interval 1000]

    (Thread/sleep slave-handler-interval)

    (let [client-channel @(udp-object-socket {:port inbound-port})] ; rip-out
      (try (let [message 
                 (:message (wait-for-message client-channel))]
             (try 
               (handle-message message client-channel instance)
               (catch 
                   Exception bad-message 
                 (println "Bad message: " bad-message))
             (catch 
                 Exception network-error
               (println "Error reading from port" 
                        (inbound-port network-error)))
             (finally
               (close client-channel))))))
    (slave-handler-thread instance)))


(defn -main [& args] ; args -> slave-instance
  (let
      [slave-instance {:machineid "daisy"
                       :publickey "Ha79000"
                       :privatekey "narodnikkey" 
                       :master-host {:host "localhost"
                                     :port 10202}
                       :inbound-port 10201}]

    (println "Starting Narodnik slave...")
    ;(.start (Thread. (slave-handler-thread slave-instance)))
    ;^ after join/authorize from master
    (let [client-channel @(udp-object-socket 
                           {:port (:inbound-port slave-instance)})]
      (init-follow-master-thread client-channel slave-instance))
    (println "NARODNIK CLIENT!")))



