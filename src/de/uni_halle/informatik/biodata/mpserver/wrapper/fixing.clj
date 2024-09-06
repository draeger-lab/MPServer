(ns de.uni-halle.informatik.biodata.mpserver.wrapper.fixing
  (:require
   [clojure.java.io :as java.io]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.io :as io])
  (:import
   (de.uni_halle.informatik.biodata.mp.fixing SBMLFixer)
   (java.io OutputStream)))

(defn fix! [sbml-doc {:keys [mp-parameters]}]
  (let [fixer (SBMLFixer. (.fixing mp-parameters))]
    (.fix fixer sbml-doc 0)))
