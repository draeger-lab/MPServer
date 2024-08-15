(ns edu.tue.csb.mpserver.wrapper.db
  (:require
   [mount.core :refer [defstate]]
   [clojure.tools.logging :as log])
  (:import
   (edu.ucsd.sbrg.db.bigg BiGGDB)
   (edu.ucsd.sbrg.db.adb AnnotateDB)
   (edu.ucsd.sbrg.parameters DBParameters)))

(defn init-bigg-db []
  (BiGGDB/init (DBParameters.)))

(defn init-adb-db []
  (AnnotateDB/init (DBParameters.)))

(defstate bigg-db
  :start
  (do (log/info "Initializing BiGG DB.")
      (init-bigg-db)))

(defstate adb
  :start
  (do (log/info "Initializing AnnotateDB.")
      (init-adb-db)))
