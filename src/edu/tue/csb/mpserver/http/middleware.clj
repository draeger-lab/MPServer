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

(defn wrap-remove-x-id  [handler-fn]
  (fn [req]
    (let [resp (handler-fn req)]
      (update-in resp [:body] dissoc :x-id))))

(defn wrap-exception [handler]
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

(defn wrap-mdc! [handler]
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

(defn wrap-with-context [handler]
  (fn wrapped-with-context-handler
    [{:keys [multipart-params run-id] :as req}]
    (let [config     (some-> multipart-params
                             (get "config"))
          parameters (or (parameters/parameters-from-json "{}")
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

(defn wrap-with-diff [handler]
  (fn wrapped-with-diff-handler
    [{:keys [params] :as req}]
    (let [{:keys [sbml-doc]} params
          old-doc            (.clone sbml-doc)
          resp               (handler req)]
      (log/debug "Calculating diff.")
      (let [diff  (diff/diff old-doc sbml-doc)]
        (log/debug "Done calculating diff.")
        (assoc-in resp [:body :diff] diff)))))

(defn wrap-with-save-file! [handler]
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
