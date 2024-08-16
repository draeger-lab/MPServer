(ns edu.tue.csb.mpserver.http.middleware
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as java.io]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [edu.tue.csb.mpserver.diff :as diff]
   [edu.tue.csb.mpserver.wrapper.io :as io]
   [edu.tue.csb.mpserver.wrapper.parameters :as parameters])
  (:import
   (edu.ucsd.sbrg.resolver.identifiersorg IdentifiersOrg)
   (org.slf4j MDC)))


(defn wrap-exception
  "Try to catch all kinds of exceptions and just give the user a JSON error map."
  [handler]
  (fn wrapped-with-exception-handler
    [request]
    (try (handler request)
         (catch clojure.lang.ExceptionInfo e
           (log/error e)
           {:status 500
            :body   (json/generate-string
                     {:message "Server Error. Apologies! Your 'runId' can be used on the server side to track down the failed execution."
                      :runId   (:run-id request)})})
         (catch Exception e
           (log/error e)
           {:status 500
            :body   (json/generate-string
                     {:message "Server Error. Apologies! Your 'runId' can be used on the server side to track down the failed execution."
                      :runId   (:run-id request)})})
         (catch Throwable e
           (log/error e)
           {:status 500
            :body   (json/generate-string
                     {:message "Server Error. Apologies! Your 'runId' can be used on the server side to track down the failed execution."
                      :runId   (:run-id request)})}))))


(defn wrap-mdc!
  "Adds a UUID to the Mapped Diagnostic Context for this run.
  See https://www.baeldung.com/mdc-in-log4j-2-logback for context."
  [handler]
  (fn wrapped-with-mdc-handler
    [request]
    (let [run-id (str (random-uuid))]
      (log/trace "Setting MDC.")
      (MDC/put "run.id" run-id)
      (let [response (handler (assoc request :run-id run-id))]
        (MDC/clear)
        (log/trace "Clearing MDC.")
        response))))


(defn- file-extension [filename]
  (str "." (last (str/split filename #"\."))))


(defn- temp-file [{:keys [tempfile filename] :as req}]
  (log/debug "file parameters:" req)
  (let [ext         (file-extension filename)
        custom-path (str/replace
                     (.getAbsolutePath tempfile) ".tmp" ext)]
    (java.io/copy (java.io/file tempfile)
                  (java.io/file custom-path))
    (java.io/file custom-path)))


(defn wrap-with-context
  "Add context, SBML document and the tempfile of the document to the request map."
  [handler]
  (fn wrapped-with-context-handler
    [{:keys [multipart-params run-id] :as req}]
    (let [config     (some-> multipart-params
                             (get "config"))
          parameters (or (parameters/parameters-from-json config)
                         parameters/default-parameters)
          file       (-> multipart-params
                         (get "modelFile")
                         temp-file)
          context    {:parameters parameters
                      :registry   (IdentifiersOrg.)
                      :observers  []}
          doc        (io/read-file file parameters)]
      (log/debug "Received config:" (prn-str config))
      (log/debug "Running with parameters:" (prn-str parameters))
      (let [response (-> req
                         (assoc-in [:params :context] context)
                         (assoc-in [:params :sbml-doc] doc)
                         (assoc-in [:params :tempfile] file)
                         (assoc-in [:params :config] config)
                         (handler))]
        (assoc-in response [:body :parameters] (str parameters))))))


(defn wrap-with-diff
  "Attempt to calculate a diff by extracting the sbml-doc from the params of the request map,
  cloning it and putting the diff in the diff key in the response body.
  This assumes somebody put the doc in the request map before and that the result body is a map (i.e. JSON)."
  [handler]
  (fn wrapped-with-diff-handler
    [{:keys [params] :as req}]
    (let [{:keys [sbml-doc]} params
          old-doc            (.clone sbml-doc)
          resp               (handler req)]
      (log/debug "Calculating diff.")
      (try
        (let [diff  (diff/diff old-doc sbml-doc)]
          (log/debug "Done calculating diff.")
          (assoc-in resp [:body :diff] diff))
        (catch Throwable t
          (log/error t "Failed diff.")
          (assoc-in resp [:body :diff] {:message "Sorry, calculating the diff failed for some reason."}))))))


(defn wrap-with-save-file!
  "If the parameters tell us we are allowed to save the model to disc,
  save it."
  [handler]
  (fn wrapped-with-save-file-handler
    [{:keys [params] :as req}]
    (let [{:keys [config sbml-doc tempfile]} params]
      (when (:allow-model-to-be-saved-on-server config)
        (log/debug (str "Saving model to /var/lib/models/"
                        (.getName tempfile)))
        (java.io/copy (java.io/file tempfile)
                      (java.io/file (str "/var/lib/models/"
                                         (.getName tempfile)))))
      (handler req))))


;; (defn wrap-remove-x-id  [handler-fn]
;;   (fn [req]
;;     (let [resp (handler-fn req)]
;;       (update-in resp [:body] dissoc :x-id))))
