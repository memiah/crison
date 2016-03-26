(ns crison.main
  (:require [crison.core :refer [run]])
  (:gen-class))

(defn -main [& args] (run "resources" "./"))
