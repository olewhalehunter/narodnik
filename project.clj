(defproject app "1.0.0-SNAPSHOT"
  :description "Narodnik: Distributed user agent network."
  :dependencies [[org.clojure/clojure "1.3.0"]
                [org.clojure/clojurescript "0.0-2202"]
                [org.clojure/java.jdbc "0.3.2"]
                [http-kit "2.1.16"]
                [postgresql "9.1-901.jdbc4"]]
  :main app.core
)

