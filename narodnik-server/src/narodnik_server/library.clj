(ns narodnik-server.library)
"Macros and higher order functions."

(defmacro attempt [desc exp]
  `(try ~exp
       (catch Exception error# (println 
                           "Error attempting " ~desc
                           " : " error#))))
