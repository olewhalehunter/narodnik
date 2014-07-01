(ns narodnik-core.start (:gen-class)
  (:use
   [narodnik-core master]
   [narodnik-core slave]))

(defn -main [& args]
  (if (= (count args) 0)
    (start-master [])
    (apply start-slave args)))

