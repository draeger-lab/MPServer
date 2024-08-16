(ns edu.tue.csb.mpserver.http.handler
  (:require
   [clojure.tools.logging :as log]
   [edu.tue.csb.mpserver.wrapper.annotation :as annotation]
   [edu.tue.csb.mpserver.wrapper.io :as io]
   [edu.tue.csb.mpserver.wrapper.parameters :as parameters]
   [edu.tue.csb.mpserver.wrapper.polishing :as polishing]))

(defn submit-handler [{:keys [params run-id] :as req}]
  (let [{:keys [context sbml-doc]} params]
    (do
      (polishing/polish! sbml-doc context)
      (when (parameters/annotate-with-bigg? (-> context :parameters))
        (annotation/annotate-with-bigg! sbml-doc context))
      (when (parameters/annotate-with-adb? (-> context :parameters))
        (annotation/annotate-with-adb! sbml-doc context))
      (let [base64-model (io/write-doc-to-base64 context sbml-doc)]
        {:status 200
         :body   {:runId     run-id
                  :modelFile base64-model}}))))
