(ns de.uni-halle.informatik.biodata.mpserver.core
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [de.uni-halle.informatik.biodata.mpserver.args :as args]
   [de.uni-halle.informatik.biodata.mpserver.http.server]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.db]
   [de.uni-halle.informatik.biodata.mpserver.validate]
   [mount.core :as mount]
   [clojure.tools.logging :as log]))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;; this corresponds to the main method and uses the 'mount' component library
;; to start the http server and the databases (see http.server and wrapper.db)
(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (args/validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (do
        (log/info "Running with config:" (:config-file options))
        (mount/start-with-args
         (or (:config-file options)
             (edn/read-string (slurp (io/resource "server-config.edn")))))))))
