(ns edu.tue.csb.mpserver.core
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [edu.tue.csb.mpserver.http.server]
   [edu.tue.csb.mpserver.wrapper.db]
   [edu.tue.csb.mpserver.validate]
   [mount.core :as mount]))

(def cli-options
  ;; An option with a required argument
  [["-c" "--config-file FILE" "Config file"
    :parse-fn #(edn/read-string (slurp %))]
    ["-h" "--help"]])


(defn usage [options-summary]
  (->> ["ModelPolisher Server"
        ""
        "Options:"
        options-summary]
       (str/join \newline)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or the options."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      :else ; failed custom validation => exit with usage summary
      options)))

;; this corresponds to the main method and uses the 'mount' component library
;; to start the http server and the databases (see http.server and wrapper.db)
(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (mount/start-with-args
       (or (:config-file options)
           (edn/read-string (slurp (io/resource "config.edn"))))))))
