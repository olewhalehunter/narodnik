(defproject narodnik-server "0.1.2"
  :description "Distributed user agent network server."
  :url "http://github.com/olewhalehunter/narodnik"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [compojure "1.1.6"]
                 [aleph "0.3.2"]
                 [postgresql "9.1-901.jdbc4"]
                 [com.novemberain/validateur "2.1.0"]]

  :main narodnik-server.handler)
