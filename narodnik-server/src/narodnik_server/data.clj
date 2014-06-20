(ns narodnik-server.data
  (:use [lamina core api])
  (:require [clojure.java.jdbc :as sql])
)

(def narodnik-schema 
  [

                      [:machine
                       [:name :text]
                       [:id :bigint]
                       [:privatekey :text]]

                      [:machinegroup ; :groupmember [:groupid bigint] [:machineid bigint] 
                       [:name :text]
                       [:id :bigint]]

                      [:chunk
                       [:taskid :bigint]
                       [:content :text]
                       [:index :bigint]]

                      [:task
                       [:content :text] ; clojure/json serialized s-exp/obj/message
                       [:id :bigint]]

                      [:job
                       [:slavetype :text] ; machine or group
                       [:slaveid :bigint] 
                       [:taskid :bigint]
                       [:status :text]    ; undone, asked, in progress, done
                       [:starttime :text]
                       [:endttime :text]]

                      [:dictionary ; for general purpose storage and status flags
                       [:key :text]
                       [:value :text]
                       [:counter :bigint]
                       [:machineid :bigint]] 
])

(def create-table sql/create-table-ddl)
(def exec-sql sql/db-do-commands)

(let [db-host "localhost"
        db-port 5432
        db-name "narodnik"]
  (def database {
           :subprotocol "postgresql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           :user "postgres"
           :password "Sage@123"})) 


(defn create-tables [] 
  (println "Creating tables...")
  (dorun (map (fn [table-design] 
                (try (exec-sql database (apply create-table table-design))
                     (catch Exception ex (println "Could not create table" 
                                                  (str table-design)
                                                  (str ex)))))
              narodnik-schema)))

(defn drop-all-tables [] 
  (println "Dropping all tables...")
  (exec-sql database "drop schema public cascade")
  (exec-sql database "create schema public"))

(defn init-db []
  (println "Setting up database...")
  (create-tables))
