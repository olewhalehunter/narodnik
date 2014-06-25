(ns narodnik-server.data
  (:use [lamina core api]
        [narodnik-server library])
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

(defn db-select [table key value]
  (query-db database 
            (str "select * from " (name table)
                 " where " (name key) "=" (str value))))

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

(defn most-important-undone-job [] "for assignment thread (pipeline)"
  (first (db-select :job :status "'undone'")))

(defn get-task-of-job [job]
  (db-select :task :id (:taskid job)))

(defn get-machine [name]
  (db-select :machine :name (str "'" name "'")))

(defn exists-machine? [name] 
  (not (empty? (db-select :machine :name (str "'" name "'")))))

(defn datetime-now [] (str (new java.util.Date)))

(defn get-host-of-machine [machine]
  (db-select :host :id (:hostid machine)))

(defn get-slave-of-job [job]
  (db-select :machine :name (:slaveid job)))
