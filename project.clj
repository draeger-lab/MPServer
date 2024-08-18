(defproject model-polisher-server "1.0.0-SNAPSHOT"
  :main edu.tue.csb.mpserver.core
  :profiles {:uberjar {:aot :all}}
  :plugins [[refactor-nrepl "3.10.0"]
            [cider/cider-nrepl "0.49.2"]]
  :repositories {"uni-rostock" {:url "https://mvn.bio.informatik.uni-rostock.de/releases/"}
                 "uni-halle"   {:url "https://biodata.informatik.uni-halle.de/maven/releases/"}}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 ;; http server abstraction
                 [ring/ring-core "1.12.2"]
                 [ring/ring-json "0.5.1"]
                 ;; use jetty as http server
                 [ring/ring-jetty-adapter "1.12.2"]
                 ;; routing library
                 [metosin/reitit "0.7.1"]
                 ;; state management library
                 [mount "0.1.18"]
                 [org.clojure/tools.logging "1.3.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.13.0"]
                 [clj-http "3.13.0"]
                 [org.clojure/tools.cli "1.1.230"]
                 
                 [edu.ucsd.sbrg.ModelPolisher "2.1.8"]

                 ;; dev time
                 [ring/ring-devel "1.12.2"]]
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"])
