(ns narodnik-core.actor (:gen-class)
    (:use 
     [aleph udp]
     [aleph http]
     [aleph formats]
     [lamina core api]
     [narodnik-core library]
     [narodnik-core exchange] ))

(def actor-speed 75)

(def actor-config {
                   :privatekey "narodnikkey"
                   :vm-host "localhost" 
                   :vm-port 10777
                   :http-port 7070
                   :suppress-output false
                   :num-contact-attempts 20
                   :handler-interval (* 10 actor-speed)
                   :listener-timout (* 10 actor-speed)
                   :browser-runtime true
                   :http-tunnel false
                   :tunnel-port 445})

(defn update-actor-tasks [new-taskid]
  (attempt "updating actor task"
           (swap! actor-task-cache
                  (fn [x] (clojure.set/union 
                          x
                          #{new-taskid}
                          ))))
  (println "actor task updated!"
           @actor-task-cache))

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
   :host (:host (:vm-host instance))
   :port (:port (:vm-host instance))})

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

(defn already-recieved-task? [taskid] 
  (contains? @actor-task-cache taskid))

(defn process-task [taskid command client-channel instance]
  (attempt (str "processing task (" (str taskid) "): " command) 
           (cond 
            (not (already-recieved-task? taskid)) (doall
                                                   (actor-api command)
                                                   (update-actor-tasks taskid))
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

(defn actor-handler-thread [client-channel instance]
  "actor inbound thread."
  (let [handler-interval (:handler-interval actor-config)]
    (println "actor handler thread...")
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
          (actor-handler-thread client-channel instance)))))

(defn contact-vm 

  [client-channel instance
   num-attempts request-timeout
   invite-message]

    (println "Attempting to contact vm...")
    (enqueue client-channel invite-message)
    (println "Message sent.")
    (try
      (println "Waiting for greeting back from vm...")
      (let [datagram (wait-for-message client-channel 
                                       1000)]
        (println "Recieved from host " (str (:host datagram))
                 " " (str datagram))
        (actor-handler-thread client-channel instance))
      (catch java.util.concurrent.TimeoutException e 
        (println "Request for greeting timed out...")
        (if (> num-attempts 0) 
          (attempt "contacting vm" (contact-vm 
                                        client-channel 
                                        instance (- num-attempts 1)
                                        request-timeout
                                        invite-message))))
      (finally 
        (comment close client-channel))))

(defn init-follow-vm-thread [client-channel instance]
  (println "Contacting vm...")
  (let [invite-request {
                        :message {
                                  :name (:machineid instance)
                                  :privatekey (:privatekey instance)
                                  :publickey (:publickey instance)}
                        :host (:host (:vm-host instance))
                        :port (:port (:vm-host instance))}
        num-attempts (:num-contact-attempts actor-config)
        request-timeout (:listener-timeout actor-config)]
    (attempt "conctacting vm" (contact-vm 
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
                                  (merge-actor-cache (load-string body)))
                         (println "State cache after request is " actor-cache)))))
      (enqueue http-channel
               {:status 200
                :headers {"content-type" "text/plain"}
                :body "Hello Narodnik!!!!"}))))

(defn start-browser-listener []
  (println "Starting HTTP server on "  {:port (:http-port actor-config)}
           " for browser callbacks...")
  (start-http-server http-handler {:port (:http-port actor-config)}))

(defn actor-tunnel-handler [browser-channel request]
  (println request)
  (enqueue browser-channel
           {:status 200
            :headers {"content-type" "text/html"}
            :body "Inbound tunnel server."}))

(defn init-actor-http-tunnel []
  (println "Starting UDP->HTTP tunnel server for actor...")
  (start-http-server 
   actor-tunnel-handler {:port (:vm-port actor-config)}))

(defn init-actor-runtime [instance client-channel]
  (if (:http-tunnel actor-config)
    (init-actor-http-tunnel)
    (future 
      (init-follow-vm-thread 
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

(defn start-actor [& args] 
  (println "Starting Narodnik actor with args '" (str args) "'...")
  (if (:browser-runtime actor-config)
    (start-browser-listener))
  (if (not (= (count args) 3))
    (println  
     "\n\rArguments should be machineid, publickey, and inbound port.\n\r")
    (attempt 
     "narodnik actor-startup"   
     (let 
         [actor-instance {
                          :machineid (first args)
                          :publickey (second args)
                          :privatekey (:privatekey actor-config) 
                          :vm-host {:host (:vm-host actor-config)
                                        :port (:vm-port actor-config)}
                          :inbound-port (bigdec (nth args 2))
                          :suppress-output (:suppress-output actor-config)}
          client-channel @(udp-object-socket 
                           {:port (:inbound-port actor-instance)})]
       (time (doall
              (init-actor-runtime actor-instance
                             client-channel)))
       (close client-channel)
       (calculate-throughput actor-instance))))
    (println "Narodnik actor shutting down...")    
    (System/exit 0))

