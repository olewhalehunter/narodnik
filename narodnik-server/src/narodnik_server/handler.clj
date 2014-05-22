(ns narodnik-server.handler
  (:use compojure.core)
  
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defn main-route []
  (println "GOTS ROUTED!")
  (spit "/home/frog/projects/narodnik/output.exe" "101010010110")
  "<center><h1  style=\"color:red\">NARODNIK OPEN</h1></center>")

(defroutes app-routes
  (GET "/" [] ( main-route ))
  (route/resources "/")
  (route/not-found "FILE NOT FOUND"))

(def app
  (handler/site app-routes))
