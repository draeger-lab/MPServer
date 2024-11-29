(ns de.uni-halle.informatik.biodata.mpserver.http.handler
  (:require
   [clojure.tools.logging :as log]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.annotation :as annotation]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.fixing :as fixing]
   [de.uni-halle.informatik.biodata.mpserver.validate :as validate]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.io :as io]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.parameters :as parameters]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.polishing :as polishing]))

(defn submit-handler [{:keys [params run-id] :as req}]
  (let [{:keys [context sbml-doc]} params
        pre-validation-results     (validate/validate (:tempfile params))
        mp-parameters              (-> context :mp-parameters)]
    (do
      
      (when (and (not (parameters/dont-fix? mp-parameters))
                 (not-empty (:validation pre-validation-results)))
        (fixing/fix! sbml-doc context))

      (let [polish? (or (empty? (:validation pre-validation-results))
                            (parameters/polish-even-if-model-invalid? mp-parameters))]
        (when polish?
          (polishing/polish! sbml-doc context)

          (when (parameters/annotate-with-bigg? mp-parameters)
            (log/debug "Annotating with BiGG")
            (annotation/annotate-with-bigg! sbml-doc context))

          (when (parameters/annotate-with-adb? mp-parameters)
            (log/debug "Annotating with ADB")
            (annotation/annotate-with-adb! sbml-doc context))))

      (let [base64-model (io/sbml-doc->base64 context sbml-doc)
            tmp          (java.io.File/createTempFile "postpolish-" ".xml")]
        (io/write-file sbml-doc tmp)
        (let [post-validation-results (validate/validate tmp)]
          {:status 200
           :body   {:runId          run-id
                    :modelFile      base64-model
                    :preValidation  pre-validation-results
                    :postValidation post-validation-results}})))))
