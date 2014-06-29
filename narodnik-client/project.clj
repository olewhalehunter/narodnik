(defproject narodnik-client "0.1.0-SNAPSHOT"
  :description "Distributed user agent network client."
  :url "http://github.com/olewhalehunter/narodnik"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljsbuild "1.0.3"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.1.18"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2069"]
                 [aleph "0.3.2"]
                 [criterium "0.4.3"]]
  :main narodnik-client.core
  :profile {:uberjar {:aot narodnik-client.core}}
)

