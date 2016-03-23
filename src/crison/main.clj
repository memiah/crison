(ns crison.main
  (:require [clojure.edn :refer [read-string]]
            [clojure.java.io :refer [writer]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [environ.core :refer [env]]
            [clojure.test :refer :all]
            [webdriver.core :as wc]
            [webdriver.form :as wf]
            [crison.core :refer [date ]])
  (:gen-class))

(def test-file (writer (str (date) "-tests.txt")))

(defn -main [& args]
  (binding [*test-out* test-file]
    (run-tests 'crison.core)))
