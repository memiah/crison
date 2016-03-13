(ns crison.main
  (:require [clojure.edn :refer [read-string]]
            [clojure.test :refer :all]
            ;[taoensso.truss :as truss :refer (have have! have?)]
            [webdriver.core :refer :all]
            [webdriver.form :refer :all])
  (:gen-class))

(System/setProperty "phantomjs.binary.path" "/Users/rossputin/Downloads/phantomjs-2.1.1-macosx/bin/phantomjs")

(def driver (new-webdriver {:browser :phantomjs}))

(defn title? [x] (is (= x (title driver))))

(defn go [x] (to driver x))

(defn ? [x] (is (-> driver (find-element x) exists?)))

(defmulti decode (fn[x] (ffirst x)))

(defmethod decode :url! [x] (go (:url! x)))

(defmethod decode :click! [x] (-> driver (find-element {:id (:click! x)}) click))

(defmethod decode :search! [x]
  (quick-fill-submit
    driver
    (:search! x)))

(defmethod decode :title [x] (title? (:title x)))

(defmethod decode :default [x] (? x))

(deftest counselling
  (let [tests (read-string (slurp "resources/counselling-directory.edn"))]
    (doseq [x tests] (decode x))))

(def test-file (clojure.java.io/writer "tests.txt"))
(defn -main [& args]
  (binding [*test-out* test-file]
    (run-tests 'crison.main)))
