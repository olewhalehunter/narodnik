(ns narodnik-client.core
  (:require [org.httpkit.client :as http]))

(defn handle-response [body]
  (println "Recieved:" body)
  (eval (read-string body))
  )

(defn thread []
  (Thread/sleep 1000)
  (let [{:keys [status headers body error] :as resp} @(http/get "http://localhost:3000")]
    (if error
      (println "Failed, exception: " error)
      (handle-response body)))
  (println "Listening...")
  (thread)
)

(defn -main
  [& args]
  (.start (Thread. thread))
  (println "NARODNIK CLIENT!"))


