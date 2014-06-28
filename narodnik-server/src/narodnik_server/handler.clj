(ns narodnik-server.handler (:gen-class)
  (:use 
   [aleph.udp]
   [gloss core io]
   [lamina core api]
   [validateur validation]
   [narodnik-server library]
   [narodnik-server data]
   [narodnik-server exchange]))

(comment "Slave Invitation process:"
(personal?) private key sent via third party or sent with slave install
transport-layer string encrypted and authorized with private key
slave registers via publickey and machineid
master stores credentials and further requests from 
that :machineid are validated against the :publickey
)

(defn authenticated? [provided-name provided-publickey]
  (try 
    (let [claimed-machine (db-select :machine :name provided-name)]
      (= (:publickey claimed-machine) 
         provided-publickey))
    (catch Exception e false)))

(defn handle-authorized-message [message host]
  (println "Handling authorized message " (str (:body message)) 
           " from machine '" (:name message) "'" ))

(defn greet-slave [machine host]
  (println "Greeting slave :" (str machine))
  (greet-slave-job! machine))

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
        (greet-slave machine host)))))

(defn handle-bad-message [message instance] 
  (println "Recieved bad message :" (str message)))

(defn handle-inbound-message [message instance host] 
  ( comment "Message Format"
    {:message 
     {:body {
             :command "(increment! :num-widgets)"
             :value 0}
      :privatekey "narodnikkey"
      :publickey "password123"
      :name "friendcomputer"
      }})
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
           (handle-bad-message message instance))))
     (handle-bad-message message instance)))

(defn handler-thread [instance]
  "For updating status updates 
   and CRUD operations from slaves."

  (let [timeout 500
        handler-interval 3000
        inbound-channel @(udp-object-socket 
                          {:port (:inbound-port instance)})] ; miliseconds
    (while true
      (try
        (let [datagram (wait-for-message inbound-channel 3000)]
          (println "Recieved from host " (str (:host datagram)))
          (future (handle-inbound-message 
                   (:message datagram) 
                   instance
                   {:host (:host datagram)
                    :port (:port datagram)})))
        (catch Exception e 
          (comment println "Waiting for inbouyyynd messages..."))
        (finally (close inbound-channel)))
        (Thread/sleep handler-interval)
        (handler-thread instance))))

(defn process-job! [job instance]
  (println "Processing job : " (str job) )
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
                          :message (:content task)
                          :host (:address slave-host)
                          :port (:port slave-host)}]
             (print "Sending message : " (str message))
             (attempt "send message channel " 
                      (enqueue outbound-channel message))))
  (println "Job processed.")))

(defn task-assign-thread [instance]
  "Assigning tasks from jobs as sent messages to slaves."
  (while true
    (Thread/sleep 3000)
    (comment println "Taking job from queue...")
    (let [outbound-port (:outbound-port instance)
          jobs (db-select :job :status "'undone'")]
      (if (not (empty? jobs))
        (process-job! (first jobs) instance)
        (comment println "No jobs to process."
        (comment
        (let 
            [outbound-channel @(udp-object-socket)
             task (get-task-of-job (first jobs))
             slave (get-slave-of-job (first jobs))]
          (println "Looking up host of slave : " (str slave))
          (let [slave-host (get-host-of-machine slave)] ;; need confirm loop
            (println "Sending task as message :" (str task))
            (enqueue outbound-channel 
                     {:message (:message task)
                      :host (:host slave-host)
                      :port (:port slave-host) }))))))))
  (task-assign-thread instance))

(defn -main [& args]
  (println "Narodnik master starting...")
  (drop-all-tables)
  (init-db)
  (let [master-instance {:privatekey "narodnikkey" 
                         :outbound-port 10201
                         :inbound-port 10666}]
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
