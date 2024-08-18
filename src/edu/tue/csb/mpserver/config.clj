(ns edu.tue.csb.mpserver.config
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as java.io]
   [clojure.tools.logging :as log]
   [mount.core :as mount :refer [defstate]]))

(defstate app-config
  :start
  #_(edn/read-string (slurp (java.io/resource "server-config.edn")))
  (let [config (mount/args)
        dir    (java.io/file (:save-models-path config))]
    (when (nil? dir)
      (throw (ex-info "Need to set a directory for saving models 'save-models-path' in the config.")))
    (when-not (or (and (.exists dir) (.canWrite dir))
                  (.mkdirs dir))
      (throw (ex-info "Directory for saving models '" (:save-models-path config) "' cannot be created or is not writeable.")))
    config)
  :stop
  nil)
