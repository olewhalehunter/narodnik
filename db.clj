(ns app.core)

(require '[clojure.java.jdbc :as sql])

(let [db-host "localhost"
      db-port 5432
      db-name "narodnik"]
 
  (def db {:classname "org.postgresql.Driver"
           :subprotocol "postgresql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           :user "postgres"
           :password "password"}))
