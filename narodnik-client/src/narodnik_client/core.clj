(ns narodnik-client.core (:gen-class)
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

(defmacro attempt [desc exp]
  `(try ~exp
       (catch Exception error# (println 
                           "Error attempting " ~desc
                           " : " error#))))

(defn init-follow-master-thread [client-channel instance]
  (println "Contacting master...")
  (let [invite-request {:message {
                          :name (:machineid instance)
                          :privatekey (:privatekey instance)
                          :publickey (:publickey instance)}
                        :host (:host (:master-host instance))
                        :port (:port (:master-host instance))}
        number-request-attempts 3
        request-timeout 5000]
    
    (attempt "contact master" 
             (dotimes [n number-request-attempts]   
               (let [inbound-channel client-channel] ; miliseconds
                 (while true
                   (println "Attempting to contact master...")
                   (enqueue client-channel invite-request)
                   (println "Message sent.")
                   (try
                     (println "Waiting for message back...")
                     (let [datagram (wait-for-message client-channel 
                                                      request-timeout)]
                       (println "Recieved from host " (str (:host datagram))
                                " " (str datagram)))
                     (catch Exception e 
                       (println "Waiting for inbound messages..." e))
                     (finally (close inbound-channel)))
                   (Thread/sleep request-timeout)))))))

(defn handle-message [message client-channel instance]
  (println "Handling :" message)
  (eval (read-string message)))

(defn slave-handler-thread [instance]
  "Slave inbound thread.")

(defn -main [& args] ; args -> slave-instance
  (let
      [slave-instance {:machineid "nyman"
                       :publickey "Ha79000"
                       :privatekey "narodnikkey" 
                       :master-host {:host "localhost"
                                     :port 10666}
                       :inbound-port 6613}]

    (println "Starting Narodnik slave...")
    ;(.start (Thread. (slave-handler-thread slave-instance)))
    ;^ after join/authorize from master
    (let [client-channel @(udp-object-socket 
                           {:port (:inbound-port slave-instance)})]
      (init-follow-master-thread client-channel slave-instance))))

