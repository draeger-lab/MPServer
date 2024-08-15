(ns edu.tue.csb.mpserver.core
  (:gen-class)
  (:require
   [edu.tue.csb.mpserver.http.server]
   [edu.tue.csb.mpserver.wrapper.db]
   [mount.core :as mount]))

(defn -main [& args]
  (mount/start))
