(ns narodnik-server.handler (:gen-class)
  (:use 
   [aleph.udp]
   [gloss core io]
   [lamina core api]
   [validateur validation]
   [narodnik-server data]
))

(comment "Slave Invitation process:"
(personal?) private key sent via third party or sent with slave install
transport-layer string encrypted and authorized with private key
slave registers via publickey and machineid
master stores credentials and further requests from 
that :machineid are validated against the :publickey
)

(defn authenticated? [provided-name provided-publickey]
  (try 
    (let [claimed-machine 
          (db-select :machine :name provided-name)]
      (= (:publickey claimed-machine) 
         provided-publickey))
    (catch Exception e false)))

(defn handle-authorized-message [message host]
  (println "Handling authorized message " (str (:body message)) 
           " from machine '" (:name message) "'" ))

(defn greet-slave [machine host]
  (println (str machine))) ;; or just task?

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

(defn handle-bad-message [message instance] ()
  (println "Recieved bad message :" (str message)))

(defn handle-inbound-message [message instance host] ()
  ( comment "message structure"
    {:message 
     {:body {
             :command "(increment :num-widgets)"
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
           message-keycount-equals?
           (fn [other] (= (count (keys message)) other))
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
          (println "Waiting for inbound messages..."))
        (finally (close inbound-channel)))
        (Thread/sleep handler-interval)
        (handler-thread instance))))

(defn task-assign-thread [instance]
  "Master-Slave instructions outbound."

  (let [task-handler-interval 1000
        inbound-port (:inbound-port instance)
        outbound-port (:outbound-port instance)]

     (Thread/sleep task-handler-interval)

    (let [outbound-channel @(udp-object-socket)]
          ;inbound-channel (udp-object-socket {:port inbound-port})] ; need local eval
      (try
        (enqueue outbound-channel 
                 {:message "(println \"testing\")"
                  :host "localhost"
                  :port outbound-port})
        (catch Exception e 
          (println "Error sending outbound messages on port " (str outbound-port)))
        (finally
          (close outbound-channel)
          (System/exit 0)
        ;  (close inbound-channel)
          )))
    (task-assign-thread instance)))

(defn -main [& args]
  (println "Narodnik master starting...")
  (init-db)
  (let [master-instance {:privatekey "narodnikkey" 
                         :outbound-port 10201
                         :inbound-port 10666}]
    ;; (.start (Thread. 
    ;;          (task-assign-thread master-instance)))
    
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


