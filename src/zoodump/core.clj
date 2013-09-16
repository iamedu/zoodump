(ns zoodump.core
  (:require [clojure.tools.cli :refer [cli]])
  (:gen-class))

(defn process-request [opts]
  (println opts))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [[options args banner] (cli args
                                  ["-h" "--help" "Show help" :default false :flag true])]
    (if (:help options)
      (println banner)
      (process-request options))))
