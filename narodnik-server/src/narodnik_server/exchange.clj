(ns narodnik-server.exchange
  (:use 
   [lamina core api]
   [narodnik-server data]))

(defn make-task [content] 
  {:content content :id (db-generate-id :task)})

(defn greet-slave-job! [machine]
  (try
  (let [task (make-task "\"Greetings.\"")]
    (let [job {
              :slavetype "machine"
              :slaveid (:name machine)
              :taskid (:id task)
              :status "undone"
              :starttime (datetime-now)
              :endtime "NULL"
              }]
         (db-insert! :task task)
         (db-insert! :job job)))
  (catch Exception e (println "Error greeting slave :" e))))


 
