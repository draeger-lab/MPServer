(ns edu.tue.csb.mpserver.validate
  (:require
   [cheshire.core :as json]
   [clj-http.client :as client]
   [clojure.data]
   [clojure.java.io :as java.io]
   [clojure.tools.logging :as log]
   [edu.tue.csb.mpserver.config :refer [app-config]]
   [edu.tue.csb.mpserver.wrapper.io :as io]
   [edu.tue.csb.mpserver.wrapper.parameters :as parameters]
   [mount.core :as mount :refer [defstate]])
  (:import
   (java.lang.reflect Method)
   (org.sbml.jsbml SBMLDocument)))

(defn validate [sbml-file]
  (let [response (client/post (str "http://" (-> app-config :validator :host) ":" (-> app-config :validator :port) "/validate")
                              {:multipart [{:name "model_file" :content sbml-file}]})]
    (log/debug "Validate " (.getPath sbml-file))
    (-> response :body (json/parse-string true))))

#_(def new-doc (io/read-file (java.io/file (java.io/resource "tst.xml")) parameters/default-parameters))
#_(def old-doc (io/read-file (java.io/file (java.io/resource "iAF1260.xml")) parameters/default-parameters))
