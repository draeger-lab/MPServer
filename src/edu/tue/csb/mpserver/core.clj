(ns edu.tue.csb.mpserver.core
  ;; gen-class is necessary for the namespace that serves as the entrypoint
  ;; for the resulting jar - it just makes sure a corresponding callable class is created
  (:gen-class)
  (:require
   [edu.tue.csb.mpserver.http.server]
   [edu.tue.csb.mpserver.wrapper.db]
   [mount.core :as mount]))

;; this corresponds to the main method and uses the 'mount' component library
;; to start the http server and the databases (see http.server and wrapper.db)
(defn -main [& args]
  (mount/start))
