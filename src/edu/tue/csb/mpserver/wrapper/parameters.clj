(ns edu.tue.csb.mpserver.wrapper.parameters
  (:import
   (edu.ucsd.sbrg.parameters Parameters ParametersParser)))

(defn annotate-with-bigg? [^Parameters params]
  (.. params (annotation) (biggAnnotationParameters) (annotateWithBiGG)))

(defn annotate-with-adb? [^Parameters params]
  (.. params (annotation) (adbAnnotationParameters) (annotateWithAdb)))

(defn output-type [^Parameters params]
  (.. params (outputType)))
