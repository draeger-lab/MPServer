(ns edu.tue.csb.mpserver.wrapper.fixing
  (:require
   [clojure.java.io :as java.io]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [edu.tue.csb.mpserver.wrapper.io :as io])
  (:import
   (edu.ucsd.sbrg.fixing SBMLFixer)
   (java.io OutputStream)))

(defn fix! [sbml-doc]
  (let [fixer (SBMLFixer.)]
    (.fix fixer sbml-doc 0)))
