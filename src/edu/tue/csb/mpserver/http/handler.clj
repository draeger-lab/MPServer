(ns edu.tue.csb.mpserver.http.handler
  (:require
   [clojure.tools.logging :as log]
   [edu.tue.csb.mpserver.wrapper.annotation :as annotation]
   [edu.tue.csb.mpserver.wrapper.fixing :as fixing]
   [edu.tue.csb.mpserver.validate :as validate]
   [edu.tue.csb.mpserver.wrapper.io :as io]
   [edu.tue.csb.mpserver.wrapper.parameters :as parameters]
   [edu.tue.csb.mpserver.wrapper.polishing :as polishing]))

(defn submit-handler [{:keys [params run-id] :as req}]
  (let [{:keys [context sbml-doc]} params
        pre-validation-results     (validate/validate (:tempfile params))
        mp-parameters              (-> context :mp-parameters)]
    (log/debug "Validation results:" pre-validation-results)
    (do
      
      (when (and (not (parameters/dont-fix? mp-parameters))
                 (not-empty (:validation pre-validation-results)))
        (fixing/fix! sbml-doc))

      (when (or (empty? (:validation pre-validation-results))
                (parameters/polish-even-if-model-invalid? mp-parameters))
        (polishing/polish! sbml-doc context))

      (when (parameters/annotate-with-bigg? mp-parameters)
        (log/debug "Annotating with BiGG")
        (annotation/annotate-with-bigg! sbml-doc context))

      (when (parameters/annotate-with-adb? mp-parameters)
        (log/debug "Annotating with ADB")
        (annotation/annotate-with-adb! sbml-doc context))

      (let [base64-model (io/sbml-doc->base64 context sbml-doc)
            tmp          (java.io.File/createTempFile "postpolish-" ".xml")]
        (io/write-file sbml-doc tmp)
        (let [post-validation-results (validate/validate tmp)]
          {:status 200
           :body   {:runId          run-id
                    :modelFile      base64-model
                    :preValidation  (:validation pre-validation-results)
                    :postValidation (:validation post-validation-results)}})))))
