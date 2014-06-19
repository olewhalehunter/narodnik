(comment 

  ;; NARODNIK DATA MODEL!

  :machine
  [:name :text]
  [:id :bigint]

  :task
  [:content :text]
  [:id :bigint]

  :job
  [:taskid :bigint]
  [:time :datetime]

 

)

(ns narodnik-server.data
  (:use [lamina core api]
))

(require '[clojure.java.jdbc :as sql])

(def create-table sql/create-table-ddl)
(def exec-sql sql/db-do-commands)

(let [db-host "localhost"
        db-port 5432
        db-name "narodnik"]
  (def db {
           :subprotocol "postgresql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           :user "postgres"
           :password "URA!URA!URA!"}))


(defn init-db []
  (let [db-host "localhost"
        db-port 5432
        db-name "narodnik"])
  (exec-sql db (create-table 
                :machine
                [:name :text]
                [:id :bigint])))


  
