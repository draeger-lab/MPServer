(ns edu.tue.csb.mpserver.wrapper.db
  (:require
   [mount.core :refer [defstate] :as mount]
   [clojure.tools.logging :as log])
  (:import
   (edu.ucsd.sbrg.db.bigg BiGGDB)
   (edu.ucsd.sbrg.db.adb AnnotateDB)
   (edu.ucsd.sbrg.parameters DBParameters)))

(defn init-bigg-db [{:keys [db-name host user port password]}]
  (BiGGDB/init (DBParameters. db-name host user (int port) password)))

(defn init-adb-db [{:keys [db-name host user port password]}]
  (AnnotateDB/init (DBParameters. db-name host user (int port) password)))

;; see core namespace: this is some magic component framework stuff
;; which serves to start up system components/objects on startup
(defstate bigg-db
  :start
  (do (log/info "Initializing BiGG DB.")
      (init-bigg-db (:bigg (mount/args)))))

(defstate adb
  :start
  (do (log/info "Initializing AnnotateDB.")
      (init-adb-db (:adb (mount/args)))))


;; (mount/start #'bigg-db)
;; (mount/start #'adb)
