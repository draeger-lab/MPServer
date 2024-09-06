(ns de.uni-halle.informatik.biodata.mpserver.wrapper.annotation
  (:import
   (de.uni_halle.informatik.biodata.mp.annotation.adb ADBSBMLAnnotator)
   (de.uni_halle.informatik.biodata.mp.annotation.bigg BiGGSBMLAnnotator)
   (de.uni_halle.informatik.biodata.mp.db.adb AnnotateDB)
   (de.uni_halle.informatik.biodata.mp.db.bigg BiGGDB)
   (de.uni_halle.informatik.biodata.mp.parameters DBParameters)
   (org.sbml.jsbml SBMLDocument)))

(defn annotate-with-bigg! [^SBMLDocument doc {:keys [mp-parameters registry observers]}]
  (let [bigg-params    (.. mp-parameters (annotation) (biggAnnotationParameters))
        sbo-params     (.. mp-parameters (sboParameters))
        bigg           (BiGGDB.)
        bigg-annotator (BiGGSBMLAnnotator. bigg bigg-params sbo-params registry observers)]
    (.annotate bigg-annotator doc)))

(defn annotate-with-adb! [^SBMLDocument doc {:keys [mp-parameters registry observers]}]
  (let [adb-params     (.. mp-parameters (annotation) (adbAnnotationParameters))
        adb            (AnnotateDB.)
        adb-annotator (ADBSBMLAnnotator. adb adb-params)]
    (.annotate adb-annotator doc)))

