(ns narodnik-core.data
  (:use [lamina core api]
        [narodnik-core library])
  (:require 
   [clojure.java.jdbc :as sql]))

(def narodnik-schema 
  [

                      [:machine
                       [:name :text]
                       [:status :text] ; invite recieved, free, busy; as key in clj
                       [:privatekey :text]
                       [:publickey :text]
                       [:hostid :bigint]]

                      [:host
                       [:address :text]
                       [:port :int]
                       [:id :bigint]]

                      [:machinegroup 
                       [:name :text]
                       [:id :bigint]]

                      [:groupmember 
                       [:groupid :bigint]
                       [:machineid :bigint]]

                      [:chunk
                       [:taskid :bigint]
                       [:content :text]
                       [:index :bigint]]

                      [:task
                       [:content :text] ; clojure/json serialized s-exp/obj/message
                       [:id :bigint]]
                      
                      [:job
                       [:slavetype :text] ; machine/group
                       [:slaveid :text] 
                       [:taskid :bigint]
                       [:status :text]    ; undone,assigned,in progress,done
                       [:starttime :text]
                       [:endtime :text]]

                      [:dictionary ; for general purpose storage and status flags
                       [:key :text]
                       [:value :text]
                       [:counter :bigint]
                       [:collectionid :text]
                       [:machineid :bigint]]
])

(let [db-host "localhost"
        db-port 5432
        db-name "narodnik"]
  (def database {
           :subprotocol "postgresql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           :user "postgres"
           :password "Sage@123"}))

(def create-table sql/create-table-ddl)

(def exec-sql! sql/db-do-commands)

(def query-db sql/query) 

(def insert-db! sql/insert!)

(defn create-tables [] 
  (println "Creating tables...")
  (dorun (map (fn [table-design] 
                (try (exec-sql! database (apply create-table table-design))
                     (catch Exception ex (println "Could not create table" 
                                                  (str table-design)
                                                  (str ex)))))
              narodnik-schema)))

(defn query-db [db query]
  (println "Excecuting query : " query)
  (sql/query db query))

(defn db-select [table key value]
  (let [query 
               (str "select * from " (name table)
                   " where " (name key) "=" (str value))]
    (query-db database query)))

(defn db-select-all [table]
  (sql/query database 
              (str "select * from " (name table))))

(defn db-insert! [table object]
  (insert-db! database table object))

(defn db-update! [table condition value update-map ]
  (sql/update! database
    table update-map [(str (name condition) "=?") value] ))
          
(defn db-generate-id [table] 
  (int (rand 100000000)))

(defn drop-all-tables [] 
  (println "Dropping all tables...")
  (exec-sql! database "drop schema public cascade")
  (exec-sql! database "create schema public"))

(defn init-db []
  (println "Setting up database...")
  (create-tables))

(defn most-important-undone-job [] "temp for assignment thread"
  (first (db-select :job :status "'undone'")))

(defn get-task-of-job [job]
  (first (db-select :task :id (:taskid job))))

(defn get-job-by-taskid [taskid]
  (first (db-select :job :taskid taskid)))

(defn get-machine [name]
  (first (db-select :machine :name (str "'" name "'"))))

(defn exists-machine? [name] 
  (not (empty? (db-select :machine :name (str "'" name "'")))))

(defn datetime-now [] (str (new java.util.Date)))

(defn get-host-of-machine [machine]
  (println "Looking up host of machine : " (str machine))
  (first (db-select :host :id (:hostid machine))))

(defn get-slave-of-job [job]
  (println "Looking up slave for job.")
  (get-machine (:slaveid job)))
