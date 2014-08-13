(ns narodnik-core.slave (:gen-class)
    (:use 
     [aleph udp]
     [aleph http]
     [aleph formats]
     [lamina core api]
     [narodnik-core library]
     [narodnik-core exchange] ))

(def slave-speed 75)

(def slave-config {
                   :privatekey "narodnikkey"
                   :master-host "localhost" 
                   :master-port 10777
                   :http-port 7070
                   :suppress-output false
                   :num-contact-attempts 20
                   :handler-interval (* 10 slave-speed)
                   :listener-timout (* 10 slave-speed)
                   :browser-runtime true
                   :http-tunnel false
                   :tunnel-port 445})

(defn update-slave-tasks [new-task]
  (swap! slave-task-cache
         (fn [x] (clojure.set/union 
                 x
                 #{new-task}
                 ))))

(def total-inbound-packets (atom 0))
(def successful-inbound-packets (atom 0))

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

(defn already-recieved-task? [taskid] false)

(defn process-task [taskid command client-channel instance]
  (attempt (str "processing task (" (str taskid) "): " command) 
           (cond 
            (not (already-recieved-task? taskid)) 
            (slave-api command)
            :else (println "THAT TASK WAS ALREADY RECIEVED")))
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
     :else 
     (process-task 
      taskid command client-channel instance))))

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
  (comment
    (let [body (bytes->string (:body request))]
      (println "Recieved request : " request)
      (if body
        (cond 
         :else (doall
                (println "Body was " body
                         (enqueue http-channel
                                  {:status 200
                                   :headers {"content-type" "text/plain"}
                                   :body "Hello Narodnik!!!!"})
                         (attempt "loading browser request"
                                  (merge-slave-cache (load-string body)))
                         (println "State cache after request is " slave-cache)))))
      (enqueue http-channel
               {:status 200
                :headers {"content-type" "text/plain"}
                :body "Hello Narodnik!!!!"}))))

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

