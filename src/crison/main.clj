(ns crison.main
  (:require [clojure.edn :refer [read-string]]
            [environ.core :refer [env]]
            [clojure.test :refer :all]
            ;[taoensso.truss :as truss :refer (have have! have?)]
            [webdriver.core :as wc]
            [webdriver.form :as wf])
  (:gen-class))

(System/setProperty "phantomjs.binary.path" (env :phantom-path))

(def driver (wc/new-webdriver {:browser :phantomjs}))

(defn title [] (wc/title driver))

(defn title? [x] (is (= x (title)) x))

(defn go [x] (wc/to driver x))

(defn el [x] (-> driver (wc/find-element x)))

;; currently a catchall from the multimethod
(defn ? [x] (is (-> driver (wc/find-element x) wc/exists?) (str "Fail on : " x)))

(defn text? [x]
  (println (wc/text (-> driver (wc/find-element (dissoc x :text?)))))
  (is (= (:text? x)
        (wc/text (-> driver (wc/find-element (dissoc x :text?)))))))

(defmulti decode (fn[x] (ffirst x)))

(defmethod decode :url! [x] (go (:url! x)))

(defmethod decode :click! [x]
  (let [e (:click! x)]
    (if (string? e)
      (-> driver (wc/find-element {:id e}) wc/click)
      (-> driver (wc/find-element e) wc/click))))

(defmethod decode :search! [x] (wf/quick-fill-submit driver (:search! x)))

(defmethod decode :title [x] (title? (:title x)))

(defmethod decode :text? [x] (text? x))

(defmethod decode :default [x] (? x))

(def screenshot-file (str "screenshot_test.png"))
(defn take-screenshot
  [driver]
  (is (string? (wc/get-screenshot driver :base64)))
  (is (> (count (wc/get-screenshot driver :bytes)) 0))
  (is (= (class (wc/get-screenshot driver :file)) java.io.File))
  (is (= (class (wc/get-screenshot driver :file screenshot-file)) java.io.File))
  ;; the following will throw an exception if deletion fails, hence our test
  ;(clojure.java.io/delete-file screenshot-file)
  )

(deftest counselling
  (wc/resize driver {:width 1024 :height 800})
  (let [tests (read-string (slurp "resources/counselling-directory.edn"))]
    (doseq [x tests] (decode x))
    (take-screenshot driver)
    ))

(def test-file (clojure.java.io/writer "tests.txt"))

(defn -main [& args]
  (binding [*test-out* test-file]
    (run-tests 'crison.main)))
