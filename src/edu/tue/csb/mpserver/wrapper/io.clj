(ns edu.tue.csb.mpserver.wrapper.io
  (:require [clojure.java.io :as io])
  (:import
   (edu.ucsd.sbrg.io ModelReader)
   (edu.ucsd.sbrg.parameters Parameters ParametersParser)
   (edu.ucsd.sbrg.resolver.identifiersorg IdentifiersOrg)))

(def p (let [input-stream (-> (slurp (io/resource "default-config.json"))
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

(defn read-file [input ^Parameters params]
  (try
    (let [registry (IdentifiersOrg.)]
      (.read (ModelReader. (.sboTerms params) registry) input))
    (catch Exception e
      (throw (ex-info "Reading file failed"
                      {:type   :parse/file
                       :input  input
                       :params params}
                      e)))))
