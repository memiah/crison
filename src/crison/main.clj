(ns crison.main
  (:require [crison.core :refer [drive]])
  (:gen-class))

(defn -main [& args] (drive "resources" "./"))
