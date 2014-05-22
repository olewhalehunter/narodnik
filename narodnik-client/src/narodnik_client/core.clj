(ns narodnik-client.core)

(defn thread []
       (Thread/sleep 1000)
       
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

