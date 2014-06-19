(ns narodnik-server.handler
  (:use 
   [aleph.udp]
   [gloss core io]
   [lamina core api]
))

(require '[clojure.java.jdbc :as sql])

(defn init-db []
  (let [db-host "localhost"
        db-port 5432
        db-name "narodnik"]
    
    (def db {
             :subprotocol "postgresql"
             :subname (str "//" db-host ":" db-port "/" db-name)
             :user "postgres"
             :password "password"}))

  (sql/db-do-commands 
   db 
   (sql/create-table-ddl :machines 
                         [:name :text]
                         [:id :bigint])))


(defn handler-thread []
  "For updating status updates 
   and CRUD operations from slaves"

  (let [inbound-channel @(udp-object-socket {:port 10201})
        timeout 4000]

      (Thread/sleep timeout)
      (println "Starting inbound...")
      (eval (read-string
             (:message (wait-for-message 
                        inbound-channel 12000))))
      (close inbound-channel))
  (handler-thread))


(defn task-assign-thread []
  "testing handler thread"

  (let [task-handler-interval 1000
        inbound-port 10202
        outbound-port 10201
        host "localhost"
        text-msg "(println \"NARODNIK IS GO\")"]

    (Thread/sleep task-handler-interval)

    (let [outbound-channel @(udp-object-socket)
          inbound-channel @(udp-object-socket {:port inbound-port})]
      (try
        (enqueue outbound-channel 
                 {:message text-msg 
                  :host host 
                  :port outbound-port})
        (finally
          (close outbound-channel)
          (close inbound-channel))))
    (task-assign-thread)))


(defn -main [& args]
  (init-db)
  (.start (Thread. task-assign-thread)) 
  (.start (Thread. handler-thread))

  (println "NARODNIK SERVER!")
)

