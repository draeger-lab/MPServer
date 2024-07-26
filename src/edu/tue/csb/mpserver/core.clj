(ns edu.tue.csb.mpserver.core
  (:gen-class)
  (:require
   [edu.tue.csb.mpserver.http.server]
   [mount.core :as mount]))

(defn -main [& args]
  (mount/start))
