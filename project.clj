(defproject model-polisher-server "1.0.0-SNAPSHOT"
  :main edu.tue.csb.mpserver.core
  :profiles {:uberjar {:aot :all}}
  :plugins [[refactor-nrepl "3.10.0"]
            [cider/cider-nrepl "0.49.2"]]
  :repositories {"uni-rostock" {:url "https://mvn.bio.informatik.uni-rostock.de/releases/"}}
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
                 ;; [org.apache.logging.log4j/log4j-api "2.23.1"]
                 ;; [org.apache.logging.log4j/log4j-1.2-api "2.23.1"]
                 ;; [org.apache.logging.log4j/log4j-jcl "2.23.1"]
                 ;; [org.apache.logging.log4j/log4j-jul "2.23.1"]
                 ;; [org.apache.logging.log4j/log4j-slf4j-impl "2.23.1"]
                 ;; [org.apache.logging.log4j/log4j-slf4j2-impl "2.23.1"]
                 [org.slf4j/slf4j-api "1.7.32"]
                 [org.slf4j/jul-to-slf4j "1.7.32"]
                 [org.slf4j/jcl-over-slf4j "1.7.32"]
                 [org.slf4j/log4j-over-slf4j "1.7.32"]
                 [org.slf4j/osgi-over-slf4j "1.7.32"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 
                 [edu.ucsd.sbrg.ModelPolisher "2.1.2.14-lib"]
                 [de.zbit.SysBio "1390"]
                 [org.sbml.jsbml/jsbml "1.6.1" :exclusions
                  [commons-logging
                   org.apache.logging.log4j/log4j-1.2-api
                   org.apache.logging.log4j/log4j-core
                   org.apache.logging.log4j/log4j-api
                   org.apache.logging.log4j/log4j-slf4j-impl]]
                 [org.jdom/jdom2 "2.0.6"]
                 [de.unirostock.sems/CombineArchive "1.3.0"]



                 ;; dev time
                 [ring/ring-devel "1.12.2"]
                 [clj-http "3.13.0"]
                 [cheshire "5.13.0"]
                 [compojure "1.7.1"]
                 [metosin/reitit-dev "0.7.1"]
                 [expound "0.9.0"]]
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"
             #_"-Dclojure.tools.logging.factory=clojure.tools.logging.impl/log4j2-factory"])
