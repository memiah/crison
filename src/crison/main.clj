(ns crison.main
  (:require [crison.core :refer [drive]])
  (:gen-class))

(defn -main [& args] (drive (first args) (or (second args) (first args))))
