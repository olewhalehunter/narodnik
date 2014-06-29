(ns narodnik-server.exchange
  (:use 
   [lamina core api]
   [narodnik-server data]))

(defn make-task [content] 
  {:content content :id (db-generate-id :task)})

(defn make-job [task machine]{
                              :slavetype "machine"
                              :slaveid (str (:name machine) )
                              :taskid (:id task)
                              :status "undone"
                              :starttime (datetime-now)
                              :endtime "NULL"
                              })

(defn give-all-job [message]
  (println "Giving all machines job : " message)
  (let [machines (db-select-all :machine)
        task (make-task message)]
    (map (fn [slave] (let [task (make-task message)]
                  (db-insert! :task task)
                  (db-insert! :job (make-job task slave))))
         machines))) ; redef as macro? <3<3<3

(defn greet-slave-job! [machine]
           (let [task (make-task "\"Greetings.\"")] 
             (let [greeting (make-job task machine) ]
               (db-insert! :job greeting)
               (db-insert! :task task))))


(defn test-package []

  (give-all-job "(println \"Hello World!\")"))

 
