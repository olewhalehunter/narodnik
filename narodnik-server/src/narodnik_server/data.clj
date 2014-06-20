(ns narodnik-server.data
  (:use [lamina core api]
))
(require '[clojure.java.jdbc :as sql])

(def narodnik-schema [

  [:machine[
  [:name :text]
  [:id :bigint]
  [:privatekey :text]]]

  
  [:group[ ; :groupmember [:groupid bigint] [:machineid bigint] 
  [:name :text]
  [:id :bigint]]]

  [:task[
  [:content :text] ; clojure/json serialized s-exp/obj/message
  [:id :bigint]]]

  [:job[
  [:slavetype :text] ; machine or group
  [:slaveid :bigint] 
  [:taskid :bigint]
  [:status :text]    ; undone, asked, in progress, done
  [:starttime :text]
  [:endttime :text]]]

  [:dictionary[   ; for general purpose storage and status flags
  [:key :text]
  [:value :text]
  [:counter :bigint]
  [:machineid :bigint]]]

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
           :password "URA!URA!URA!"}))


(defn create-tables [db schema]
  (let [
        table-name first
        table-rows (fn [x] 
                     (doseq [row (second x)] (row)))
        ]
    (map (fn [x] 
           (apply x create-table))
         schema)))

(defn init-db []
  (create-tables database narodnik-schema))
               


  
