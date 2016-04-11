(ns crison.main
  (:require [crison.core :refer [drive]]
            [clj-webdriver.core :refer [click find-element]]
            [clj-webdriver.taxi :refer [*driver* quit set-driver! take-screenshot to window-resize]])
  (:gen-class))

(defn -main [& args] (drive (first args) (or (second args) (first args))))
