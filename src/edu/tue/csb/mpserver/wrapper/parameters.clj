(ns edu.tue.csb.mpserver.wrapper.parameters
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io])
  (:import
   (edu.ucsd.sbrg.parameters Parameters ParametersParser)))


(def default-parameters
  (let [input-stream (-> (slurp (io/resource "default-request-config.json"))
                         (.getBytes)
                         (java.io.ByteArrayInputStream.))
        params (.parse (ParametersParser.)
                       input-stream)]
    params))


(defn parameters-from-json [input]
  (try
    (let [default      (json/parse-string (slurp (io/resource "default-request-config.json")))
          ;; this somewhat awkward bit serves to define server-side defaults (in particular: use annotation)
          defaulted-input (->> input
                               json/parse-string
                               (merge default)
                               json/generate-string)
          input-stream (-> defaulted-input
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

(defn dont-fix? [^Parameters params]
  (.. params (fixing) (dontFix)))

(defn polish-even-if-model-invalid? [^Parameters params]
  (.. params (fixing) (polishEvenIfModelInvalid)))
