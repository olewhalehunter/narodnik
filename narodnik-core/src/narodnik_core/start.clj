(ns narodnik-core.start (:gen-class)
  (:use
   [narodnik-core master]
   [narodnik-core slave]))

(defn -main [& args]
  (start-slave "bob" "bobspass" "10456")
  ;(start-master [])
)
