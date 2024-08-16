(ns edu.tue.csb.mpserver.wrapper.parameters
  (:require [clojure.java.io :as io])
  (:import
   (edu.ucsd.sbrg.parameters Parameters ParametersParser)))

(def default-parameters
  (let [input-stream (-> (slurp (io/resource "default-config.json"))
                         (.getBytes)
                         (java.io.ByteArrayInputStream.))
        params (.parse (ParametersParser.)
                       input-stream)]
    params))

(defn parameters-from-json [input]
  (try
    (let [input-stream (-> input
                           (.getBytes)
                           (java.io.ByteArrayInputStream.))]
      (.parse (ParametersParser.)
              input-stream))
    (catch Exception e
      (throw (ex-info "Parsing parameters failed"
                      {:type  :parse/parameters
                       :input input}
                      e)))))


(defn annotate-with-bigg? [^Parameters params]
  (.. params (annotation) (biggAnnotationParameters) (annotateWithBiGG)))

(defn annotate-with-adb? [^Parameters params]
  (.. params (annotation) (adbAnnotationParameters) (annotateWithAdb)))

(defn output-type [^Parameters params]
  (.. params (outputType)))

;; (defn allow-save-on-server? [^Parameters params]
;;   (.. params ()))
