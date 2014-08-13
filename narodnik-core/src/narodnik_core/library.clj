(ns narodnik-core.library
  "Macros and higher order fn's.")

(defmacro attempt [desc exp]
  `(try ~exp
       (catch Exception error# (println 
                           "Error attempting " ~desc
                           " : " error#))))

(defn split-string [input-string]
  (clojure.string/split input-string #"\s+"))
