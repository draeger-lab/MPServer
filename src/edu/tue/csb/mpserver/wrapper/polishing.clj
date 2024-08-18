(ns edu.tue.csb.mpserver.wrapper.polishing
  (:import
   (edu.ucsd.sbrg.polishing SBMLPolisher)
   (org.sbml.jsbml SBMLDocument)))

(def model-polisher-version
  (.getImplementationVersion (.getPackage SBMLPolisher)))

(defn polish! [^SBMLDocument doc {:keys [mp-parameters registry observers]}]
  (let [polisher (SBMLPolisher. (.polishing mp-parameters)
                                (.sboTerms mp-parameters)
                                registry
                                observers)]
    (.polish polisher doc)))

