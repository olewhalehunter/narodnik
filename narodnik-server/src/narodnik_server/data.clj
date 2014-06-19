(ns narodnik-server.data
  (:use [lamina core api]
))

(require '[clojure.java.jdbc :as sql])

(def create-table sql/create-table-ddl)
(def exec-sql sql/db-do-commands)

(defn init-db []
  (let [db-host "localhost"
        db-port 5432
        db-name "narodnik"]
    
    (def db {
             :subprotocol "postgresql"
             :subname (str "//" db-host ":" db-port "/" db-name)
             :user "postgres"
             :password "Sage@123"}))
  (exec-sql
   db 
   (create-table :machines 
                         [:name :text]
                         [:id :bigint])))


  
