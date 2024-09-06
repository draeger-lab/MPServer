(ns de.uni-halle.informatik.biodata.mpserver.wrapper.db
  (:require
   [mount.core :refer [defstate]]
   [clojure.tools.logging :as log]
   [de.uni-halle.informatik.biodata.mpserver.config :refer [app-config]])
  (:import
   (de.uni_halle.informatik.biodata.mp.db.bigg BiGGDB)
   (de.uni_halle.informatik.biodata.mp.db.adb AnnotateDB)
   (de.uni_halle.informatik.biodata.mp.parameters DBParameters)))

(defn init-bigg-db [{:keys [db-name host user port password]}]
  (BiGGDB/init (DBParameters. db-name host user (int port) password)))

(defn init-adb-db [{:keys [db-name host user port password]}]
  (AnnotateDB/init (DBParameters. db-name host user (int port) password)))

;; see core namespace: this is some magic component framework stuff
;; which serves to start up system components/objects on startup
(defstate bigg-db
  :start
  (do (log/info "Initializing BiGG DB.")
      (init-bigg-db (:bigg app-config))))

(defstate adb
  :start
  (do (log/info "Initializing AnnotateDB.")
      (init-adb-db (:adb app-config))))


;; (mount/start #'bigg-db)
;; (mount/start #'adb)
