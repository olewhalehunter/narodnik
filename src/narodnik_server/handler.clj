(ns narodnik-server.handler
  (:use compojure.core)
  
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defn main-route []
  (println "GOTS ROUTED!")
  (spit "C:/development/output.exe" "101010010110")
  "<center><h2>NARODNIK</h2>></center>")

(defroutes app-routes
  (GET "/" [] ( main-route ))
  (route/resources "/")
  (route/not-found "FILE NOT FOUND"))

(def app
  (handler/site app-routes))
