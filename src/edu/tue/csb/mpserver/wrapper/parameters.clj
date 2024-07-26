(ns edu.tue.csb.mpserver.wrapper.parameters
  (:import
   (edu.ucsd.sbrg.parameters Parameters ParametersParser)))

(defn annotate-with-bigg? [^Parameters params]
  (.. params (annotation) (biggAnnotationParameters) (annotateWithBiGG)))

(defn output-type [^Parameters params]
  (.. params (outputType)))
