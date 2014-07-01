(defproject narodnik-core "0.1.2"
  :description "Distributed user agent network."
  :url "http://github.com/olewhalehunter/narodnik"
  :plugins [[lein-cljsbuild "1.0.3"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2069"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [compojure "1.1.6"]
                 [aleph "0.3.2"]
                 [postgresql "9.1-901.jdbc4"]
                 [com.novemberain/validateur "2.1.0"]]
  :profiles {:uberjar  {:aot :all}}
  :main narodnik-core.start)
