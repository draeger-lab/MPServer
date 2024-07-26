(ns edu.tue.csb.mpserver.diff
    (:require
   [clojure.java.io :as java.io]
   [clojure.data]
   [clojure.pprint :as pprint]
   [clojure.string :as str]
   [edu.tue.csb.mpserver.wrapper.annotation :as annotation]
   [edu.tue.csb.mpserver.wrapper.io :as io]
   [edu.tue.csb.mpserver.wrapper.polishing :as polishing]
   [edu.tue.csb.mpserver.wrapper.parameters :as parameters]
   [clojure.tools.logging :as log])
  (:import
   (java.lang.reflect Method)
   (edu.ucsd.sbrg.resolver.identifiersorg IdentifiersOrg)
   (edu.ucsd.sbrg.io ModelWriter)
   (org.slf4j MDC)
   (edu.ucsd.sbrg.reporting ProgressObserver ProgressUpdate ReportType)
   (org.sbml.jsbml SBMLDocument Model)))


(defn model [doc] (.. doc (getModel)))
(defn units [doc] (.. doc (getModel) (getListOfUnitDefinitions)))
(defn compartments [doc] (.. doc (getModel) (getListOfCompartments)))
(defn species [doc] (.. doc (getModel) (getListOfSpecies)))
(defn parameters [doc] (.. doc (getModel) (getListOfParameters)))
(defn reactions [doc] (.. doc (getModel) (getListOfReactions)))
(defn fbc [doc] (.. doc (getModel) (getPlugin "fbc")))
(defn objectives [doc] (-> doc fbc (.. (getListOfObjectives))))
(defn gene-products [doc] (-> doc fbc (.. (getListOfGeneProducts))))

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

(def deleted-reactions (partial deleted-stuff reactions))
(def deleted-species (partial deleted-stuff species))


(defn contained-in-both? [accessor old-doc new-doc]
  (complement #((deleted-stuff accessor old-doc new-doc) (id %))))

(def my-diff)

(defn map-of-diff-maps
  ([accessor diff-fn old-stuff new-stuff]
   (->> (map (fn [x y]
               [(id x) (diff-fn x y)])
             (filter (contained-in-both? accessor old-stuff new-stuff)
                     (accessor old-stuff))
             (accessor new-stuff))
        (into {})))
  ([accessor old-stuff new-stuff]
   (map-of-diff-maps accessor my-diff old-stuff new-stuff)))


(defn cv-term-diff [cv-old cv-new]
  (let [diff                    (my-diff cv-old cv-new)
        old-resources           (into (sorted-set) (resources cv-old))
        new-resources           (into (sorted-set) (resources cv-new))
        [only-a only-b in-both] (clojure.data/diff old-resources
                                                   new-resources)]
    (if (and (not-empty only-a)
             (not-empty only-b))
      (assoc diff :resources [old-resources new-resources])
      diff)))

(defn annotation-diff [a-old a-new]
  (let [diff         (my-diff a-old a-new)
        new-cv-terms (map id (new-stuff cv-terms a-old a-new))
        cv-terms     (map-of-diff-maps cv-terms cv-term-diff a-old a-new)]
    (cond-> diff
      (not-empty new-cv-terms) (assoc :new-cv-terms new-cv-terms)
      (not-empty cv-terms) (assoc :cv-terms cv-terms))))

(defn my-diff [e1 e2]
  (let [diff      (->> (clojure.data/diff (reduced-bean e1)
                                          (reduced-bean e2))
                       ((fn [[only-in-a only-in-b]]
                          (let [missing-keys-in-a (clojure.set/difference (set (keys only-in-b))
                                                                          (set (keys only-in-a)))
                                missing-keys-in-b (clojure.set/difference (set (keys only-in-a))
                                                                          (set (keys only-in-b)))]
                            [(into (or only-in-a {}) (zipmap missing-keys-in-a (repeat nil)))
                             (into (or only-in-b {}) (zipmap missing-keys-in-b (repeat nil)))])))
                       (apply (partial merge-with vector))
                       (map (fn [[k v]]
                              (if-not (vector? v)
                                [k [nil v]]
                                [k v])))
                       (filter (fn [[k [v1 v2]]] (not (both-nan? v1 v2))))
                       (into {}))
        a1        (annotation e1)
        a2        (annotation e2)
        anno-diff (when-not (= nil a1 a2 #_#_#_org.sbml.jsbml.Annotation (type e1) (type e2))
                    (annotation-diff a1 a2))]
    (if (not-empty anno-diff)
      (assoc diff :annotation anno-diff)
      diff)))


(defn diff [^SBMLDocument old-doc ^SBMLDocument new-doc]
  {:sbml                 (my-diff old-doc new-doc)
   :model                (my-diff (model old-doc) (model new-doc))
   :units                (map-of-diff-maps units old-doc new-doc)
   :newUnits            (ids (new-units old-doc new-doc))
   :compartments         (map-of-diff-maps compartments old-doc new-doc)
   #_#_:deleted-species-ids  (deleted-species old-doc new-doc) 
   :species              (map-of-diff-maps species old-doc new-doc)
   :parameters           (map-of-diff-maps parameters old-doc new-doc)
   :annotation           (annotation-diff (annotation old-doc) (annotation new-doc))
   #_#_:deleted-reaction-ids (deleted-reactions old-doc new-doc) 
   :reactions            (map-of-diff-maps reactions old-doc new-doc)
   :fbc                  {:objectives    (map-of-diff-maps objectives old-doc new-doc)
                          :geneProducts (map-of-diff-maps gene-products old-doc new-doc)}})







;; das hier ist ein Bug!
#_[#{"http://identifiers.org/rhea/16109"
    "http://identifiers.org/bigg.reaction/PFK"
    "http://identifiers.org/metanetx.reaction/MNXR102507"
    "http://identifiers.org/rhea/16112"}
  #{"https://identifiers.org/ec-code/2.7.1.11"
    "https://identifiers.org/metanetx.reaction/MNXR102507"
    "https://identifiers.org/rhea/16111"
    "https://identifiers.org/rhea/16110"
    "https://identifiers.org/rhea/16112"
    "https://identifiers.org/rhea/16109"
    "https://identifiers.org/bigg.reaction/PFK"}
  #{"http://identifiers.org/ec-code/2.7.1.11"
    "http://identifiers.org/rhea/16111"
    "http://identifiers.org/rhea/16110"}]
