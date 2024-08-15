(ns edu.tue.csb.mpserver.wrapper.annotation
  (:import
   (edu.ucsd.sbrg.annotation.bigg BiGGSBMLAnnotator)
   (edu.ucsd.sbrg.db.bigg BiGGDB)
   (org.sbml.jsbml SBMLDocument)
   (edu.ucsd.sbrg.parameters DBParameters)))

(defn annotate-with-bigg! [^SBMLDocument doc {:keys [parameters registry observers]}]
  (let [bigg-params    (.. parameters (annotation) (biggAnnotationParameters))
        sbo-params     (.. parameters (sboTerms))
        bigg           (BiGGDB.)
        bigg-annotator (BiGGSBMLAnnotator. bigg bigg-params sbo-params registry observers)]
    (.annotate bigg-annotator doc)))

