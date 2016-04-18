(ns crison.main
  (:require [clojure.java.io :refer [file]]
            [clj-webdriver.core :refer [click find-element]]
            [clj-webdriver.taxi :refer [*driver* quit set-driver! take-screenshot to window-resize]]
            [me.rossputin.diskops :as do]
            [crison.core :refer [drive]])
  (:gen-class))

(defn -main [& args]
  (let [fs (do/filter-exts (file-seq (file (first args))) ["csn"])]
    (drive fs (or (second args) (first args)))))
