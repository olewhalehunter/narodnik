(ns narodnik-server.handler
  (:use compojure.core)
  
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defn main-route []
  (println "Request recieved.")
  (spit "c:/development/output.exe" "BITSNBYTES")
  "(println \"lets have some fun\")")

(defroutes app-routes
  (GET "/" [] ( main-route ))
  (route/resources "/")
  (route/not-found "Route not found."))

(def app
  (handler/site app-routes))
