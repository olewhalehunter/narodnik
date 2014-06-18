(defproject narodnik-server "0.1.0-SNAPSHOT"
  :description "Distributed user agent network server."
  :url "http://github.com/olewhalehunter/narodnik"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.3.2"]
                 [compojure "1.1.6"]
                 [aleph "0.3.2"]
                 [postgresql "9.1-901.jdbc4"]]
  :main narodnik-server.handler
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        ]}})
