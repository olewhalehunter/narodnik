(ns narodnik-server.handler (:gen-class)
  (:use 
   [aleph.udp]
   [gloss core io]
   [lamina core api]
   [validateur validation]
   [narodnik-server library]
   [narodnik-server data]
   [narodnik-server exchange]))

(comment todo... (def machine-cache (atom {}))

(def host-cache (atom {}))

(defn dynamic-cache ()) ;... in seperate lookup file?
)

(def master-config { 
                    :privatekey "narodnikkey"
                    :handler-interval 1000
                    :listener-timeout 1000
                    :assigner-interval 3000
                    :inbound-port 10666})

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

(defn handle-authorized-message [message host]
  (println "Handling authorized message " (str (:body message)) 
           " from machine '" (:name message) "'" ))

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
        (println "Greeting slave :" (str machine))
        (greet-slave-job! machine)))))

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
   and CRUD operations from slaves."

  (let [timeout (:listener-timeout master-config)
        handler-interval  (:handler-interval master-config)
        inbound-channel @(udp-object-socket 
                          {:port (:inbound-port instance)})] ; miliseconds
    (while true
      (try
        (let [datagram (wait-for-message inbound-channel timeout)]
          (println "Recieved from host " (str (:host datagram)) ":" (str (:port datagram))
                   " : " (str datagram))
          (future (handle-inbound-message 
                   (:message datagram) 
                   instance
                   {:host (:host datagram)
                    :port (:port datagram)})))
        (catch Exception e 
          (comment println "Waiting for inbound messages..."))
        (finally (close inbound-channel)))
        (Thread/sleep handler-interval)
        (handler-thread instance))))

(defn process-job! [job instance]
  (comment println "Processing job : " (str job) )
    ;; (db-update! :job :taskid (:taskid job) 
    ;;             {:status "assigned"})
    (let
         [outbound-channel @(udp-object-socket)
          task (get-task-of-job job)
          slave (get-slave-of-job job)]
         (println "Found slave : " slave " for job.")
         (let [slave-host (get-host-of-machine slave)]
           (println "Sending task as message :" (str task) 
                    "to host" (str slave-host))
           (let [message {
                          :message {
                                    :body task
                                    :privatekey (:privatekey instance)}
                          :host (:address slave-host)
                          :port (:port slave-host)}]
             (comment print "Sending message : " (str message))
             (attempt "send message channel " 
                      (enqueue outbound-channel message)))
           (println "Job processed."))))

(defn task-assign-thread [instance]
  (Thread/sleep (:assigner-interval master-config))
  (println "Assigning tasks from jobs as sent messages to slaves.")
  (attempt "assigning jobs" (doall
           (let [jobs (db-select :job :status "'undone'")]
             (if (not (empty? jobs))
               (map (fn [job] 
                      (process-job! job instance))
                    jobs)))))
  (task-assign-thread instance))

(defn -main [& args]
  (println "Narodnik master starting...")
  (drop-all-tables)
  (init-db)
  (let [master-instance {:privatekey (:privatekey master-config) 
                         :inbound-port (:inbound-port master-config)}]
    (future
      (task-assign-thread master-instance))
    (future
      (handler-thread master-instance))
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
