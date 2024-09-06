(ns de.uni-halle.informatik.biodata.mpserver.diff
  (:require
   [clojure.data]
   [clojure.java.io :as java.io]
   [clojure.tools.logging :as log]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.io :as io]
   [de.uni-halle.informatik.biodata.mpserver.wrapper.parameters :as parameters])
  (:import
   (java.lang.reflect Method)
   (org.sbml.jsbml SBMLDocument)))

(defn model [doc] (.. doc (getModel)))
(defn units [doc] (vec (.. doc (getModel) (getListOfUnitDefinitions))))
(defn compartments [doc] (vec (.. doc (getModel) (getListOfCompartments))))
(defn species [doc] (vec (.. doc (getModel) (getListOfSpecies))))
(defn parameters [doc] (vec (.. doc (getModel) (getListOfParameters))))
(defn reactions [doc] (vec (.. doc (getModel) (getListOfReactions))))
(defn fbc [doc] (.. doc (getModel) (getPlugin "fbc")))
(defn objectives [doc] (-> doc fbc (.. (getListOfObjectives)) vec))
(defn gene-products [doc] (-> doc fbc (.. (getListOfGeneProducts)) vec))

#_(defn annotation [element] (.. element (getAnnotation)))
(defn has-method? [obj method-name]
  (when obj
    (some #(= (.getName ^Method %) method-name)
          (.getMethods (class obj)))))
(defn call-method [obj method-name & args]
  (let [method (.getMethod (class obj) method-name (into-array Class (map class args)))]
    (.invoke method obj (into-array Object args))))
(defn annotation [obj]
  (when (has-method? obj "getAnnotation")
    (call-method obj "getAnnotation")))
(defn cv-terms [a] (when a (.. a (getListOfCVTerms))))
(defn resources [cv] (.. cv (getResources)))

(defmulti id type)
(defmethod id :default [x] (.getId x))
(defmethod id org.sbml.jsbml.CVTerm [x] (.toString (.getQualifierType x)))
(defmethod id org.sbml.jsbml.SBMLDocument [x] :sbml-doc)
(defmethod id org.sbml.jsbml.Annotation [x] :annotation)

(defn ids [coll] (into #{} (map id coll)))


(defn primitive-types [[k v]]
  (and (#{java.lang.Long
          java.lang.Integer
          java.lang.Double
          java.lang.String
          java.lang.Boolean
          org.sbml.jsbml.CVTerm$Qualifier
          org.sbml.jsbml.CVTerm$Type} (type v))
       (not (#{:treeNodeChangeListenerCount
               :annotationString
               :fullAnnotationString
               :nonRDFannotationAsString
               :locationURI
               "allowsChildren"}
             k))))

(defn both-nan? [x y]
  (and (= java.lang.Double (type x) (type y))
       (Double/isNaN x)
       (Double/isNaN y)))

(defmulti reduced-bean type)
(defmethod reduced-bean org.sbml.jsbml.ext.fbc.GeneProduct [gp]
  {:label             (.getLabel gp)
   :name              (.getName gp)
   :id                (.getId gp)
   :metaId            (.getMetaId gp)
   :SBOTerm           (.getSBOTerm gp)
   :version           (.getVersion gp)
   :level             (.getLevel gp)
   :CVTermCount       (.getCVTermCount gp)
   :notesString       (.getNotesString gp)
   :associatedSpecies (try (.getAssociatedSpecies gp)
                           (catch org.sbml.jsbml.PropertyUndefinedError e))})

(defmethod reduced-bean :default [b]
  (some->> b
           bean
           (filter primitive-types)
           (into {})))

(defn new-stuff [stuff-accessor old-doc new-doc]
  (->> (stuff-accessor new-doc)
       (filter #((clojure.set/difference
                  (ids (stuff-accessor new-doc))
                  (ids (stuff-accessor old-doc))) (id %)))))

(def new-units (partial new-stuff units))
(def new-compartments (partial new-stuff compartments))

(defn deleted-stuff [accessor old-doc new-doc]
  (clojure.set/difference
   (ids (accessor old-doc))
   (ids (accessor new-doc))))

(defn contained-in-both? [accessor old-doc new-doc]
  (complement #((deleted-stuff accessor old-doc new-doc) (id %))))

(defn beanify [s]
  (let [a (annotation s)
        cv (cv-terms a)]
   (assoc (reduced-bean s)
          :annotation
          (assoc (reduced-bean a)
                 :cv-terms (zipmap (map #(.. % getQualifier toString) cv) (map resources cv))))))

(defn ensure-same-keys [m1 m2]
  (let [all-keys (set (concat (keys m1) (keys m2)))]
    [(merge (zipmap all-keys (repeat nil)) m1)
     (merge (zipmap all-keys (repeat nil)) m2)]))

(defn new-diff [accessor old-doc new-doc]
  (let [[c1 c2] (ensure-same-keys
                 (zipmap
                  (map id (accessor old-doc))
                  (map beanify (accessor old-doc)))
                 (zipmap
                  (map id (accessor new-doc))
                  (map beanify (accessor new-doc))))]
       (->> (clojure.data/diff c1 c2)
            (take 2)
            (apply
             clojure.data/diff)
            (apply (partial merge-with vector))
            (map (fn [[k [v1 v2]]]
                   (if-not (or (nil? v1)
                               (nil? v2))
                     (let [[c1 c2] (ensure-same-keys v1 v2)
                           vs (->> (merge-with vector c1 c2)
                                   (filter (fn [[k [v1 v2]]] (not (both-nan? v1 v2))))
                                   (into {}))]
                       (when (not-empty vs)
                         [k vs]))
                     [k [v1 v2]])))
            (filter some?)
            (into {}))))

(defn diff [^SBMLDocument old-doc ^SBMLDocument new-doc]
  (log/debug "Diff!")
  (let [unit-diffs         (future (new-diff units old-doc new-doc))
        compartment-diffs  (future (new-diff compartments old-doc new-doc))
        species-diffs      (future (new-diff species old-doc new-doc))
        parameters-diffs   (future (new-diff parameters old-doc new-doc))
        reactions-diffs    (future (new-diff reactions old-doc new-doc))
        objectives-diffs   (future (new-diff objectives old-doc new-doc))
        gene-product-diffs (future (new-diff gene-products old-doc new-doc))]
    {:sbml                     (do (log/debug "diff sbml") (time (new-diff (comp vector identity) old-doc new-doc)))
     :model                    (do (log/debug "diff model") (time (new-diff (comp vector model) old-doc new-doc)))
     :units                    (do (log/debug "diff units") (time @unit-diffs))
     :compartments             (do (log/debug "diff compartments") (time @compartment-diffs))
     :species                  (do (log/debug "diff species") (time @species-diffs))
     :parameters               (do (log/debug "diff parameters") (time @parameters-diffs))
     :annotation               (do (log/debug "diff annotation") (time (new-diff (comp vector annotation) old-doc new-doc)))
     :reactions                (do (log/debug "diff reactions") (time @reactions-diffs))
     :fbc                      {:objectives   (do (log/debug "diff objectives") (time @objectives-diffs))
                                :geneProducts (do (log/debug "diff geneProducts") (time @gene-product-diffs))}}))

#_(def new-doc (io/read-file (java.io/file (java.io/resource "tst.xml")) parameters/default-parameters))
#_(def old-doc (io/read-file (java.io/file (java.io/resource "iAF1260.xml")) parameters/default-parameters))
