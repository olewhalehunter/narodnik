(ns narodnik-server.data
  (:use [lamina core api])
  (:require [clojure.java.jdbc :as sql]))

(def narodnik-schema 
  [

                      [:machine
                       [:name :text]
                       [:id :bigint]
                       [:privatekey :text]
                       [:publickey :text]]

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

                      [:ntask
                       [:content :text] ; clojure/json serialized s-exp/obj/message
                       [:id :bigint]]
                      
                      [:njob
                       [:slavetype :text] ; machine/group
                       [:slaveid :bigint] 
                       [:taskid :bigint]
                       [:status :text]    ; undone,asked,in progress,done
                       [:starttime :text]
                       [:endttime :text]]

                      [:ndictionary ; for general purpose storage and status flags
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



(defn drop-all-tables [] 
  (println "Dropping all tables...")
  (exec-sql! database "drop schema public cascade")
  (exec-sql! database "create schema public"))

(defn init-db []
  (println "Setting up database...")
  (create-tables))
