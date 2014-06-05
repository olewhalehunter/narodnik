(ns narodnik-server.handler
  (:use compojure.core)
  
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defn main-route []
  ;;(println "Request recieved.")
  (spit "c:/development/output.exe" "BITSNBYTES")
  "(println \"NARODNIK SERVER!\")")

(defroutes app-routes
  (GET "/" [] ( main-route ))
  (route/resources "/")
  (route/not-found "Route Not found."))

(def app
  (handler/site app-routes))
