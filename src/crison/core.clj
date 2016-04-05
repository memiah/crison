(ns crison.core
  (:require [clojure.java.io :refer [file writer]]
            [clojure.test :refer [deftest is run-tests *test-out*]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [environ.core :refer [env]]
            [webdriver.core :as wc]
            [webdriver.form :as wf]
            [me.rossputin.diskops :as do]))

(System/setProperty "phantomjs.binary.path" (env :phantom-path))

(def ^:dynamic *crison-file*)
(def ^:dynamic *input-dir*)
(def ^:dynamic *output-dir*)

(def def-pause 1000)
(def driver (wc/new-webdriver {:browser :phantomjs}))
(def built-in-formatter (f/formatters :basic-date-time))

(defn date []
  (let [nlocal (t/to-time-zone (t/now) (t/time-zone-for-offset -0))]
    (f/unparse (f/formatter-local "yyyy-MM-dd_hh:mm")
               nlocal)))

(defn screenshot
  ([name]
   (let [nm (if name (str "-" name) "")
         s-file (str *output-dir* "/" (date) "_" (.getName *crison-file*) "-screenshot" nm ".png")]
      (wc/get-screenshot driver :file s-file)))
  ([] (screenshot nil)))

(defn source
  ([name]
   (let [nm (if name (str "-" name) "")
         s-file (str *output-dir* "/" (date) "_" (.getName *crison-file*) "-source" nm ".txt")]
      (spit s-file (wc/page-source driver))))
  ([] (source nil)))

(defn title [] (wc/title driver))

(defn title? [x] (is (= x (title)) x))

(defn go [x] (wc/to driver x))

(defn el [x] (-> driver (wc/find-element x)))

;; currently a catchall from the multimethod
(defn ? [x] (is (-> driver (wc/find-element x) wc/exists?) (str "Fail on : " x)))

(defmulti decode (fn[x] (ffirst x)))

(defmethod decode :url! [x]
  (go (:url! x))
  (Thread/sleep (or (:pause x) def-pause))
  (screenshot (:screenshot x))
  (source (:source x)))

(defmethod decode :click! [x]
  (let [e (:click! x)]
    (if (string? e)
      (-> driver (wc/find-element {:id e}) wc/click)
      (-> driver (wc/find-element e) wc/click))
    (Thread/sleep (or (:pause x) def-pause))
    (screenshot (:screenshot x))
    (source (:source x))))

(defn compute-fill [{:keys [fill-submit!] :as x}]
  (let [head (butlast fill-submit!)]
    (doseq [e head]
      (let [txt-val (:text! e)
            field (dissoc e :text!)]
        (-> driver (wc/find-element field) (wc/input-text txt-val))))))

(defmethod decode :fill-submit! [{:keys [fill-submit! pause] :as x}]
  (if (seq (filter #(:text! %) (butlast fill-submit!)))
    (compute-fill x)
    (wf/quick-fill-submit driver (:fill! x)))
  (-> driver (wc/find-element (last fill-submit!)) wc/click)
  (Thread/sleep (or pause def-pause))
  (screenshot (:screenshot x)))

(defmethod decode :fill! [{:keys [fill! pause] :as x}]
  (if (seq (filter #(:text! %) (butlast fill!)))
    (compute-fill x)
    (wf/quick-fill-submit driver (:fill! x)))
  (Thread/sleep (or pause def-pause))
  (screenshot (:screenshot x)))

(defmethod decode :title [x] (title? (:title x)))

(defmethod decode :default [x] (? x))

(deftest tests
  (wc/resize driver {:width 1024 :height 800})
  (let [fs (do/filter-exts (file-seq (file *input-dir*)) ["csn"])]
    (doseq [f fs]
      (binding [*crison-file* f]
        (doseq [t (read-string (slurp f))] (decode t))
        (screenshot)
        (source)))))

(defn drive [input-d output-d]
  (binding [*input-dir* input-d
            *output-dir* output-d
            *test-out* (writer (str output-d "/" (date) "-tests.txt"))]
    (run-tests 'crison.core)))
