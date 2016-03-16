(ns crison.main
  (:require [clojure.edn :refer [read-string]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [environ.core :refer [env]]
            [clojure.test :refer :all]
            [webdriver.core :as wc]
            [webdriver.form :as wf])
  (:gen-class))

(System/setProperty "phantomjs.binary.path" (env :phantom-path))

(def driver (wc/new-webdriver {:browser :phantomjs}))
(def built-in-formatter (f/formatters :basic-date-time))

(defn date []
  (let [nlocal (t/to-time-zone (t/now) (t/time-zone-for-offset -0))]
    (f/unparse (f/formatter-local "yyyy-MM-dd_hh:mm")
               nlocal)))
(def f-date (date))

(defn title [] (wc/title driver))

(defn title? [x] (is (= x (title)) x))

(defn go [x] (wc/to driver x))

(defn el [x] (-> driver (wc/find-element x)))

;; currently a catchall from the multimethod
(defn ? [x] (is (-> driver (wc/find-element x) wc/exists?) (str "Fail on : " x)))

(defn text? [x]
  (is (= (:text? x)
        (wc/text (-> driver (wc/find-element (dissoc x :text?)))))))

(defmulti decode (fn[x] (ffirst x)))

(defmethod decode :url! [x] (go (:url! x)))

(defmethod decode :click! [x]
  (let [e (:click! x)]
    (if (string? e)
      (-> driver (wc/find-element {:id e}) wc/click)
      (-> driver (wc/find-element e) wc/click))
    (Thread/sleep 2000)))

(defmethod decode :submit! [{:keys [submit!]}]
  (let [input (first submit!)
        v (:text! input)
        srch (dissoc input :text!)]
        (-> driver (wc/find-element srch) (wc/input-text v))
        (-> driver (wc/find-element (last submit!)) wc/click)))

(defmethod decode :search! [x]
  (wf/quick-fill-submit driver (:search! x)) (Thread/sleep 2000))

(defmethod decode :title [x] (title? (:title x)))

(defmethod decode :text? [x] (text? x))

(defmethod decode :default [x] (? x))

(defn take-screenshot
  [driver f]
  (let [s-file (str (date) "_" (.getName f) "-screenshot.png")]
    (wc/get-screenshot driver :file s-file))


  ;; the following will throw an exception if deletion fails, hence our test
  ;(clojure.java.io/delete-file screenshot-file)
  )

(deftest tests
  (wc/resize driver {:width 1024 :height 800})
  (let [fs (file-seq (clojure.java.io/file "resources"))]
    (doseq [f (next fs)]
      (doseq [t (read-string (slurp f))] (decode t))
      (take-screenshot driver f))))

(def test-file (clojure.java.io/writer (str (date) "-tests.txt")))

(defn -main [& args]
  (binding [*test-out* test-file]
    (run-tests 'crison.main)))
