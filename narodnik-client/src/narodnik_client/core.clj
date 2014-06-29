(ns narodnik-client.core (:gen-class)
  (:use 
   [aleph udp]
   [lamina core api]
   [criterium core]))

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

(def total-inbound-packets (atom 0))
(def successful-inbound-packets (atom 0))

(defmacro attempt [desc exp]
  `(try (do ~exp)
       (catch Exception error# (println 
                           "Error attempting " ~desc
                           " : " error#))))

(defn handle-message [message client-channel instance]
  (println "Handling :" message))

(defn slave-handler-thread [client-channel instance]
  "Slave inbound thread."
  (let [handler-interval 2]
    (println "Slave handler thread...")
      (Thread/sleep handler-interval)
      (try
        (println "Waiting for job...")
        (let [datagram (wait-for-message client-channel 
                                         handler-interval)]
          (comment println "Recieved from host " (str (:host datagram))
                   " " (str datagram))
          (swap! successful-inbound-packets inc)
          (attempt "handle message"
                   (handle-message (:message datagram)
                                   client-channel instance)))
        (catch java.util.concurrent.TimeoutException e 
          (comment println "Timeout reading inbound messages..."))
        (finally 
          (comment close client-channel)
          (swap! total-inbound-packets inc)
          (slave-handler-thread client-channel instance)))))

(defn contact-master 

  [client-channel instance
   num-attempts request-timeout
   invite-request]

    (println "Attempting to contact master...")
    (enqueue client-channel invite-request)
    (println "Message sent.")
    (try
      (println "Waiting for greeting back from master...")
      (let [datagram (wait-for-message client-channel 
                                       request-timeout)]
        (println "Recieved from host " (str (:host datagram))
                 " " (str datagram))
        (slave-handler-thread client-channel instance))
      (catch java.util.concurrent.TimeoutException e 
        (println "Request for greeting timed out...")
        (if (> num-attempts 0) 
          (attempt "contacting master" (contact-master 
                                        client-channel 
                                        instance (- num-attempts 1)
                                        request-timeout
                                        invite-request))))
      (finally 
        (comment close client-channel))))

(defn init-follow-master-thread [client-channel instance]
  (println "Contacting master...")
  (let [invite-request {
                        :message {
                                  :name (:machineid instance)
                                  :privatekey (:privatekey instance)
                                  :publickey (:publickey instance)}
                        :host (:host (:master-host instance))
                        :port (:port (:master-host instance))}
        num-attempts 10
        request-timeout 500]
    (attempt "conctacting master" (contact-master 
                                   client-channel instance 
                                   num-attempts request-timeout
                                   invite-request))))

(defn calculate-throughput [instance]
  (println "Throughput was " 
           (str @successful-inbound-packets)
           " / " (str @total-inbound-packets)))

(defn slave-runtime [instance client-channel]
  (future (init-follow-master-thread 
           client-channel
           instance))
  (loop [lines (repeatedly read-line)]
    (let [input (first lines)]
      (if (not= "exit" input)
        (try 
          (load-string input) 
          (catch Exception e 
            (println "Could not evaluate '" input "'.")))
        (recur (next lines))))))

(defn -main [& args] ; args -> (machineid, publickey, inbound-port)
  (println "Starting Narodnik slave with args '" (str args) "'...")
  (if (not (= (count args) 3))
    (println  
     "\n\rArguments should be machineid, publickey, and inbound port.\n\r")
    (attempt 
     "narodnik slave-startup"   (time (doall
     (let 
         [slave-instance {:machineid (first args)
                          :publickey (second args)
                          :privatekey "narodnikkey" 
                          :master-host {:host "localhost"
                                        :port 10666}
                          :inbound-port (bigdec (nth args 2))
                          :suppress-output true}
          client-channel @(udp-object-socket 
                           {:port (:inbound-port slave-instance)})]
       
       (slave-runtime slave-instance
                             client-channel)
       (close client-channel)
       (calculate-throughput slave-instance))))))
    (println "Narodnik slave shutting down...")    
    (System/exit 0))

