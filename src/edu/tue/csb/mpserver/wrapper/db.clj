(ns edu.tue.csb.mpserver.wrapper.db
  (:require
   [mount.core :refer [defstate] :as mount]
   [clojure.tools.logging :as log])
  (:import
   (edu.ucsd.sbrg.db.bigg BiGGDB)
   (edu.ucsd.sbrg.db.adb AnnotateDB)
   (edu.ucsd.sbrg.parameters DBParameters)))

(defn init-bigg-db []
  (BiGGDB/init #_(DBParameters. "bigg" "localhost" "postgres" (int 1310) "postgres")
               (DBParameters.)))

(defn init-adb-db []
  (AnnotateDB/init #_(DBParameters. "adb" "localhost" "postgres" (int 1013) "postgres")
                   (DBParameters.)))

(defstate bigg-db
  :start
  (do (log/info "Initializing BiGG DB.")
      (init-bigg-db)))

(defstate adb
  :start
  (do (log/info "Initializing AnnotateDB.")
      (init-adb-db)))


;; (mount/start #'bigg-db)
;; (mount/start #'adb)
