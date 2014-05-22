(ns narodnik-client.core
  (:require [org.httpkit.client :as http]))

(defn thread []
  (Thread/sleep 1000)
  (let [{:keys [status headers body error] :as resp} @(http/get "http://localhost:3000")]
    (if error
      (println "Failed, exception: " error)
      (println "HTTP GET success: " body)))
  (println "Listening...")
  (thread)
)

(defn -main
  [& args]
  (.start (Thread. thread))
  (println "NARODNIK CLIENT!"))

;;(doto  ;;background non-exit block daemon
;;   (Thread. forever)
;;   (.setDaemon true)
;;  (.start))

