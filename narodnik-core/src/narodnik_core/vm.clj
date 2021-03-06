(ns narodnik-core.actor (:gen-class)
  (:use 
   [aleph udp]
   [aleph http]
   [gloss core io]
   [lamina core api]
   [validateur validation]
   [narodnik-core library]
   [narodnik-core data]
   [narodnik-core exchange]))

(comment todo... 

(def machine-cache (atom {}))

(def actor-cache (atom {}))

)

(def actor-speed 40)

(def actor-config { 
                    :privatekey "narodnikkey"
                    :handler-interval (* actor-speed 10)
                    :listener-timeout (* actor-speed 100)
                    :assigner-interval (* actor-speed 10)
                    :inbound-port 10777
                    :http-tunnel false})

(defn authenticated? [provided-name provided-publickey]
  (do 
    (println "Authenticating : " provided-name 
             " with provided publickey : " provided-publickey)
    (try 
      (let [claimed-machine (get-machine provided-name)]
        (println "Stored machine was " claimed-machine)
        (println "Stored publickey was" (:publickey claimed-machine))
        (= (:publickey claimed-machine) 
           provided-publickey))
      (catch Exception e 
        (println "Could not authenticate " provided-name " , " (str e)))
      (finally false))))

(defn update-machine-status [machine status]
  (attempt "updating machine status" (do
                                       (println "Updating machine : " machine " to status : " status)
                                       (db-update! :machine :name (:name machine) {:status status}))))

(defn update-job-status [taskid status]
  (attempt "updating job status" (do
                                   (let [job (get-job-by-taskid taskid)
                                         change-map {
                                                     :status status
                                                     :endtime (datetime-now)}]
                                     (println "Updating job : " job " with map : " change-map)
                                     (db-update! :job :taskid (:taskid job) change-map)))))

(defn handle-authorized-message [message host]
  (println "Handling authorized message "  message)
  (attempt "handling authorized message"
           (let [command (:command (:body message))
                 taskid (:taskid (:body message))
                 machine-name (:name message)]
             (println "Command is " command)
             (println "Machine name is " machine-name)
             (cond (= command "Joined.")
                   (update-machine-status (get-machine machine-name) "free"))
              (update-job-status taskid "completed"))))

(defn handle-tunneled-message [request]
  (println "Handling tunneled message."))

(defn handle-invite-message [message host]
  (println "Handling invite message from " (str host))
  (if (exists-machine? (:name message))
    (println "Machine " (:name message) " is already registered.")
    (let [host-id (db-generate-id :host)]
      (println "Machine " (:name message) " doesn't exist")
      (let [machine {
                     :name (:name message)
                     :publickey (:publickey message)
                     :privatekey (:privatekey message)
                     :status "invited"
                     :hostid host-id}
            host {:address (:host host)
                  :port (:port host)
                  :id host-id 
                  }]
        (db-insert! :machine machine)
        (db-insert! :host host)
        (println "Greeting actor :" (str machine))
        (greet-actor-job! machine)))))

(defn handle-bad-message [message instance] 
  (println " <!> RECIEVED BAD MESSAGE <!> :" (str message)))

(defn handle-inbound-message [message instance host] 
  (println "Handling inbound message" (str message))
  (if (= (:privatekey message) 
         (:privatekey instance))
    (let [authorized-msg (validation-set
                          (presence-of :body)
                          (presence-of :name))
          message-keycount-equals? (fn [other-message]
                                     (= (count (keys message)) other-message))
          authorized-msg-keycount 4
          invite-msg-keycount 3]
      
      (if (and (message-keycount-equals? authorized-msg-keycount)
               (valid? authorized-msg message)
               (authenticated? (:name message) 
                               (:publickey message)))
        (handle-authorized-message message host)
        (if (message-keycount-equals? invite-msg-keycount)
          (handle-invite-message message host)
          (println "Other bad message path."))))
    (handle-bad-message message instance)))

(defn handler-thread [instance]
  "For updating status updates 
   and CRUD operations from actors."
  (try 
    (let [timeout (:listener-timeout actor-config)
          handler-interval  (:handler-interval actor-config)
          inbound-channel @(udp-object-socket 
                            {:port (:inbound-port instance)})] ; miliseconds
      (try
        (let [datagram (wait-for-message inbound-channel timeout)]
          (println "Recieved from host " (str (:host datagram)) ":" (str (:port datagram))
                   " : " (str datagram))
          (future (handle-inbound-message 
                   (:message datagram) 
                   instance
                   {:host (:host datagram)
                    :port (:port datagram)})))
        (catch Throwable e 
          (comment println "Waiting for inbound messages..."))
        (finally (close inbound-channel)))
      (Thread/sleep handler-interval))
    (catch Exception e
      (println "Error listening on port" (str (:inbound-port instance))
               " , " (str e)))
    (finally
      (handler-thread instance))))

(defn process-job! [job instance]
  (comment println "Processing job : " (str job) )
    (let
         [outbound-channel @(udp-object-socket)
          task (get-task-of-job job)
          actor (get-actor-of-job job)]
         (println "Found actor : " actor " for job.")
         (let [actor-host (get-host-of-machine actor)]
           (println "Sending task as message :" (str task) 
                    "to host" (str actor-host))
           (let [message {
                          :message {
                                    :body task
                                    :privatekey (:privatekey instance)}
                          :host (:address actor-host)
                          :port (:port actor-host)}]
             (comment print "Sending message : " (str message))
             (attempt "send message channel " 
                      (enqueue outbound-channel message)))
           (println "Job processed."))))

(defn task-assign-thread [instance]
  (Thread/sleep (:assigner-interval actor-config))
  (println "Assigning tasks from jobs as sent messages to actors.")
  (attempt "assigning jobs" (doall
           (let [jobs (db-select :job :status "'undone'")]
             (if (not (empty? jobs))
               (map (fn [job] 
                      (process-job! job instance))
                    jobs)))))
  (task-assign-thread instance))

(defn actor-tunnel-handler [http-channel request]
  (println "Recieved request : " request )
  (enqueue http-channel
           {:status 200
            :headers {"content-type" "text/html"}
            :body "Narodnik actor"}))

(defn init-actor-http-tunnel []
  (println "Starting UDP->HTTP tunnel server for actor...")
  (start-http-server 
   actor-tunnel-handler {:port (:inbound-port actor-config)}))

(defn init-actor-handlers [instance]
  (future (task-assign-thread instance))
  (future (handler-thread instance)))

(defn start-vm [& args]
  (println "Narodnik Distributed Actor VM starting...")
  (drop-all-tables)
  (init-db)
  (let [actor-instance {:privatekey (:privatekey actor-config) 
                         :inbound-port (:inbound-port actor-config)}]
    (if (:http-tunnel actor-config)
      (init-actor-http-tunnel)
      (init-actor-handlers actor-instance))
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
