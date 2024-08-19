(ns edu.tue.csb.mpserver.wrapper.io
  (:require
   [clojure.tools.logging :as log]
   [edu.tue.csb.mpserver.wrapper.parameters :as parameters])
  (:import
   (edu.ucsd.sbrg.io ModelReader ModelWriter)
   (edu.ucsd.sbrg.parameters Parameters)
   (edu.ucsd.sbrg.resolver.identifiersorg IdentifiersOrg)
   (org.apache.commons.io IOUtils)
   (java.util Base64)))

(defn read-file
  "Wrapper for ModelPolisher's ModelReader."
  [input ^Parameters params]
  (try
    (let [registry (IdentifiersOrg.)]
      (.read (ModelReader. (.sboParameters params) registry) input))
    (catch Exception e
      (throw (ex-info "Reading file failed"
                      {:type   :parse/file
                       :input  input
                       :params params}
                      e)))))

(defn write-file
  [sbml-doc file]
  (let [writer (ModelWriter.
                (.. (Parameters.) (outputType)))]
    (.write writer sbml-doc file)))


(defn- input-stream->base64
  [input-stream]
  (.encodeToString (Base64/getEncoder)
                   (IOUtils/toByteArray input-stream)))


(defn sbml-doc->base64
  [context sbml-doc]
  (log/debug "Encoding model to base64.")
  (let [writer       (ModelWriter.
                      (parameters/output-type (-> context :mp-parameters)))
        input-stream (.write writer sbml-doc)
        result       (input-stream->base64 input-stream)]
    (log/debug "Done encoding.")
    result))
