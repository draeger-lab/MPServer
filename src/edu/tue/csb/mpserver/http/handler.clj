(ns edu.tue.csb.mpserver.http.handler
  (:require
   [clojure.java.io :as java.io]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [edu.tue.csb.mpserver.wrapper.annotation :as annotation]
   [edu.tue.csb.mpserver.wrapper.io :as io]
   [edu.tue.csb.mpserver.wrapper.parameters :as parameters]
   [edu.tue.csb.mpserver.wrapper.polishing :as polishing]
   [edu.tue.csb.mpserver.diff :as diff])
  (:import
   (edu.ucsd.sbrg.parameters ModelPolisherOptions$OutputType)
   (edu.ucsd.sbrg.io ModelWriter)
   (edu.ucsd.sbrg.resolver.identifiersorg IdentifiersOrg)
   (java.util Base64)
   (org.slf4j MDC)
   (org.apache.commons.io IOUtils)))

;; das hier ist ein workaround, weil der file-type check den der MP verwendet
;; kaputt ist
(defn save-file! [file]
  (let [custom-path (str/replace (.getAbsolutePath file) ".tmp" ".xml")]
    (java.io/copy (java.io/file file) (java.io/file custom-path))
    (java.io/file custom-path)))


(defn submit-handler [{:keys [multipart-params] :as req}]
  (let [run-id (str (random-uuid))] 
    (MDC/put "run.id" run-id)
    (let [parameters (-> multipart-params (get "config") io/parameters-from-json)
          file       (-> multipart-params (get "modelFile") :tempfile)
          saved-file (save-file! file)
          context    {:parameters parameters
                      :registry   (IdentifiersOrg.)
                      :observers  []}
          doc        (io/read-file saved-file parameters)
          old-doc    (.clone doc)]
      (do
        (polishing/polish! doc context)
        (when (parameters/annotate-with-bigg? parameters)
          (annotation/annotate-with-bigg! doc context))
        (let [input-stream (.write (ModelWriter. (parameters/output-type parameters))
                                   doc)]
          (log/debug "Done Writing.")
          (MDC/clear)
          {:status 200
           :body   {:runId run-id
                    :diff  (diff/diff old-doc doc)
                    :modelFile (.encodeToString (Base64/getEncoder)
                                                (IOUtils/toByteArray input-stream))}})))))

;; (def doc (io/read-file (clojure.java.io/file "e_coli_core.xml") io/p))
;; (def doc2 (.clone doc))

;; (polishing/polish! doc {:parameters io/p
;;                         :registry (IdentifiersOrg.)
;;                         :observers []})

;; (keys (diff/diff doc2 doc))



;; Zeug


;; (def x (atom {}))

;; (def r
;;   (proxy [ProgressObserver] []
;;       (initialize [init]
;;         (reset! x {}))
;;       (update [^ProgressUpdate up]
;;         (swap! x assoc (rand-int 1000000) up))
;;       (finish [fin]
;;         (prn @x))))

;; (->> (vals @x)
;;      (filter (fn [^ProgressUpdate up]
;;                (= ReportType/DATA (.reportType up))))
;;      (map #(.obj %))
;;      first
;;      ((fn [[x y]]
;;         (= x y))))

;; (.obj (second (last @x)))

#_(.update r (ProgressUpdate. "Hi!" {} ReportType/STATUS))

;; (.finish r nil)
