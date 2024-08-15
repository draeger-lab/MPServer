(ns edu.tue.csb.mpserver.wrapper.annotation
  (:import
   (edu.ucsd.sbrg.annotation.adb ADBSBMLAnnotator)
   (edu.ucsd.sbrg.annotation.bigg BiGGSBMLAnnotator)
   (edu.ucsd.sbrg.db.adb AnnotateDB)
   (edu.ucsd.sbrg.db.bigg BiGGDB)
   (edu.ucsd.sbrg.parameters DBParameters)
   (org.sbml.jsbml SBMLDocument)))

(defn annotate-with-bigg! [^SBMLDocument doc {:keys [parameters registry observers]}]
  (let [bigg-params    (.. parameters (annotation) (biggAnnotationParameters))
        sbo-params     (.. parameters (sboTerms))
        bigg           (BiGGDB.)
        bigg-annotator (BiGGSBMLAnnotator. bigg bigg-params sbo-params registry observers)]
    (.annotate bigg-annotator doc)))

(defn annotate-with-adb! [^SBMLDocument doc {:keys [parameters registry observers]}]
  (let [adb-params     (.. parameters (annotation) (adbAnnotationParameters))
        adb            (AnnotateDB.)
        adb-annotator (ADBSBMLAnnotator. adb adb-params)]
    (.annotate adb-annotator doc)))

