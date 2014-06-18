(ns narodnik-server.handler
  (:use 
   [aleph.udp]
   [gloss core io]
   [lamina core api]
   [clojure java jdbc]
  :require 
  [clojure.java.jdbc :as sql]))

(let [db-host "localhost"
      db-port 5432
      db-name "narodnik"]
 
  (def db {:classname "org.postgresql.Driver"
           :subprotocol "postgresql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           :user "postgre")))

(defn init-db []
  (sql/create-table :machines
                    [:location "varchar(50)"]
                    [:id "bigint"]))

(def task-assign-interval 1000)

(defn handler-thread []
  "For updating status updates 
   and CRUD operations from slaves"

  (let [inbound-channel @(udp-object-socket 
                          {:port 10201})
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

  (Thread/sleep task-assign-interval)
  (let [outbound-channel @(udp-object-socket)
        inbound-channel @(udp-object-socket {:port 10202})
        text-msg "(println \"NARODNIK IS GO\")"]
    (try
      (enqueue outbound-channel 
               {:message text-msg 
                :host "localhost" 
                :port 10201})
       (finally
        (close outbound-channel)
        (close inbound-channel))))
  (task-assign-thread))

(defn -main [& args]
  (sql/with-connection db
    (init-db))
  (.start (Thread. task-assign-thread)) 
  (.start (Thread. handler-thread))

  (println "NARODNIK SERVER!")
)
