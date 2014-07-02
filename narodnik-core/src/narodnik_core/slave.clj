(ns narodnik-core.slave (:gen-class)
    (:use 
     [aleph udp]
     [aleph http]
     [lamina core api]
     [narodnik-core exchange]
     [narodnik-core slave]))

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

(def slave-speed 75)

(def slave-config {
                   :privatekey "narodnikkey"
                   :master-host "85f54r1.hs.mdmgr.net" 
                   :master-port 445
                   :http-port 7070
                   :suppress-output false
                   :num-contact-attempts 20
                   :handler-interval (* 10 slave-speed)
                   :listener-timout (* 10 slave-speed)
                   :browser-runtime true
                   :http-tunnel false
                   :tunnel-port 445})

(def state-cache (atom {}))

(defn merge-state-cache [subset]
  (swap! state-cache
         (fn [x] (merge x subset))))

(def total-inbound-packets (atom 0))
(def successful-inbound-packets (atom 0))

;(declare slave-cache) ; (atom {}) ; in exchange.clj

(defmacro attempt [desc exp]
  `(try (do ~exp)
       (catch Exception error# (println 
                           "Error attempting " ~desc
                           " : " error#))))

(defn create-message [command taskid instance]
  {:message {
             :body {
                    :command command
                    :taskid taskid}
             :name (:machineid instance)
             :privatekey (:privatekey instance)
             :publickey (:publickey instance)}
   :host (:host (:master-host instance))
   :port (:port (:master-host instance))})

(defn complete-task  [taskid message client-channel instance]
  (attempt "completing task" 
           (let [reply-message 
                 (create-message message taskid instance)]
             (println "Completing task : " (str taskid) " with reply " (str reply-message))
             (enqueue client-channel reply-message))))

(defn complete-invite [taskid client-channel instance]
  (println "Completing invitation...")
  (attempt "completing invitation"
           (complete-task taskid "Joined."
                          client-channel instance)))

(defn process-task [taskid command client-channel instance]
  (attempt (str "processing task (" (str taskid) "): " command) 
           (cond
            (= command "test-package") (test-method)
            :else (load-string command)))
  (complete-task taskid "Timestamp? Stats map?"
                 client-channel instance))

(defn handle-message [message client-channel instance]
  (println "Handling :" message)
  (let [command (:content (:body (:message message)))
        taskid (:id (:body (:message message)))]
    (println "Content is " command)
    (println "Taskid is " (str taskid))
    (cond 
     (= command "\"Greetings.\"") (complete-invite taskid client-channel instance)
    :else (process-task taskid command client-channel instance))
    ))

(defn slave-handler-thread [client-channel instance]
  "Slave inbound thread."
  (let [handler-interval (:handler-interval slave-config)]
    (println "Slave handler thread...")
      (Thread/sleep handler-interval)
      (try
        (println "Waiting for job...")
        (let [datagram (wait-for-message client-channel 
                                         handler-interval)]
          (comment println "Recieved from host " 
                   (str (:host datagram))
                   " , datagram : " (str datagram))
          (swap! successful-inbound-packets inc)
          (attempt "handle message"
                   (handle-message datagram
                                   client-channel instance)))
        (catch Exception e 
          (println ("Timeout reading inbound messages..." (str e))))
        (finally 
          (comment close client-channel)
          (swap! total-inbound-packets inc)
          (slave-handler-thread client-channel instance)))))

(defn contact-master 

  [client-channel instance
   num-attempts request-timeout
   invite-message]

    (println "Attempting to contact master...")
    (enqueue client-channel invite-message)
    (println "Message sent.")
    (try
      (println "Waiting for greeting back from master...")
      (let [datagram (wait-for-message client-channel 
                                       1000)]
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
                                        invite-message))))
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
        num-attempts (:num-contact-attempts slave-config)
        request-timeout (:listener-timeout slave-config)]
    (attempt "conctacting master" (contact-master 
                                   client-channel instance 
                                   num-attempts request-timeout
                                   invite-request))))

(defn calculate-throughput [instance]
  (println "Throughput was " 
           (str @successful-inbound-packets) "inbound tasks"
           " / " (str @total-inbound-packets) " listens"))

(defn http-handler [http-channel request]
  (println request)
  (enqueue http-channel
           {:status 200
            :headers {"content-type" "text/html"}
            :body "Hello Narodnik!!!!"}))

(defn start-browser-listener []
  (println "Starting HTTP server on "  {:port (:http-port slave-config)}
           " for browser callbacks...")
  (start-http-server http-handler {:port (:http-port slave-config)}))

(defn slave-tunnel-handler [browser-channel request]
  (println request)
  (enqueue browser-channel
           {:status 200
            :headers {"content-type" "text/html"}
            :body "Inbound tunnel server."}))

(defn init-slave-http-tunnel []
  (println "Starting UDP->HTTP tunnel server for slave...")
  (start-http-server 
   slave-tunnel-handler {:port (:master-port slave-config)}))

(defn init-slave-runtime [instance client-channel]
  (if (:http-tunnel slave-config)
    (init-slave-http-tunnel)
    (future 
      (init-follow-master-thread 
             client-channel
             instance)))
    (loop [lines (repeatedly read-line)]
      (let [input (first lines)]
        (if (not= "exit" input)
          (try 
            (load-string input) 
            (catch Exception e 
              (println "Could not evaluate '" input "'.")))
          (recur (next lines))))))

(defn start-slave [& args] 
  (println "Starting Narodnik slave with args '" (str args) "'...")
  (if (:browser-runtime slave-config)
    (start-browser-listener))
  (if (not (= (count args) 3))
    (println  
     "\n\rArguments should be machineid, publickey, and inbound port.\n\r")
    (attempt 
     "narodnik slave-startup"   
     (let 
         [slave-instance {
                          :machineid (first args)
                          :publickey (second args)
                          :privatekey (:privatekey slave-config) 
                          :master-host {:host (:master-host slave-config)
                                        :port (:master-port slave-config)}
                          :inbound-port (bigdec (nth args 2))
                          :suppress-output (:suppress-output slave-config)}
          client-channel @(udp-object-socket 
                           {:port (:inbound-port slave-instance)})]
       (time (doall
              (init-slave-runtime slave-instance
                             client-channel)))
       (close client-channel)
       (calculate-throughput slave-instance))))
    (println "Narodnik slave shutting down...")    
    (System/exit 0))

