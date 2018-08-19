(ns narodnik-core.start (:gen-class)
  (:use
   [narodnik-core vm]
   [narodnik-core actor]))

(defn -main [& args]
  (if (= (count args) 0)
    (start-vm [])
    (apply start-actor args)))

