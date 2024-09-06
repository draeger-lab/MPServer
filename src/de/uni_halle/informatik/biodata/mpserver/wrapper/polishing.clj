(ns de.uni-halle.informatik.biodata.mpserver.wrapper.polishing
  (:import
   (de.uni_halle.informatik.biodata.mp.polishing SBMLPolisher)
   (org.sbml.jsbml SBMLDocument)))

(def model-polisher-version
  (.getImplementationVersion (.getPackage SBMLPolisher)))

(defn polish! [^SBMLDocument doc {:keys [mp-parameters registry observers]}]
  (let [polisher (SBMLPolisher. (.polishing mp-parameters)
                                (.sboParameters mp-parameters)
                                registry
                                observers)]
    (.polish polisher doc)))

