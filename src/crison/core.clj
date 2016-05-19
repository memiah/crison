(ns crison.core
  (:require [clojure.java.io :refer [file writer]]
            [clojure.set :refer [intersection]]
            [clojure.string :refer [replace]]
            [clojure.test :refer [deftest is run-tests *test-out*]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [environ.core :refer [env]]
            [clj-webdriver.core :as wc]
            [clj-webdriver.form-helpers :as wf]
            [clj-webdriver.window :refer :all])
  (:use [taoensso.timbre :as timbre :only (trace debug info warn error)])
  (:refer-clojure :exclude [int replace]))

(System/setProperty "phantomjs.binary.path" (env :phantom-path))

(def ^:dynamic *crison-file*)
(def ^:dynamic *crisons*)
(def ^:dynamic *output-dir*)

(def def-pause 1000)
(def driver (delay (wc/new-driver {:browser :phantomjs})))
(def built-in-formatter (f/formatters :basic-date-time))

(defn int [x] (if (string? x) (Integer. (re-find  #"\d+" x)) x))

(def custom-time-formatter (f/with-zone (f/formatter "yyyy-MM-dd_HH:mm:ss")
                                        (t/default-time-zone)))
(defn date [] (f/unparse custom-time-formatter (t/now)))

(defn render-pause [pause-ms]
  (when-not (= pause-ms 0)
    (println "pausing for : " pause-ms " ms")
    (Thread/sleep pause-ms)))

;; TODO can not do this when using S3 inputs
(defn screenshot
  [name]
  (when name
    (let [s-file (str *output-dir* "/" (date) "_"  "-screenshot-" name ".png")]
      (wc/get-screenshot @driver :file s-file))))

(defn source
  ([name]
   (let [nm (if name (str "-" name) "")
         s-file (str *output-dir* "/" (date) "_" "-source" nm ".txt")]
      (when name (spit s-file (wc/page-source @driver)))))
  ([] (source nil)))

(defn go [x] (wc/to @driver x))

(defn el [x] (-> @driver (wc/find-element x)))

;; currently a catchall from the multimethod
(defn ? [x] (is (-> @driver (wc/find-element x) wc/exists?) (str "Fail on : " x)))

(defmulti decode (fn[x] (ffirst x)))

(defmethod decode :url! [x]
  (go (:url! x))
  (render-pause (or (:pause x) def-pause))
  (screenshot (:screenshot x))
  (source (:source x)))

(defmethod decode :click! [x]
  (let [e (:click! x)
        href (or (get-in e [:href]) e)
        orig-handles (count (wc/windows @driver))]
    (if (string? e)
      (-> @driver (wc/find-element {:id e}) wc/click)
      (-> @driver (wc/find-element e) wc/click))
    (when (> (count (wc/windows @driver)) orig-handles)
    (let [w (or (wc/find-window @driver {:url href})
                (wc/find-window @driver {:url (replace href #"http" "https")})
                (wc/find-window @driver {:url (-> href (replace #"http" "https") (replace #"www." ""))}))]
      (wc/switch-to-window @driver w)))
    (render-pause (or (:pause x) def-pause))
    (screenshot (:screenshot x))
    (source (:source x))))

(defn compute-fill [x fill-mode]
  (let [xs (if (= fill-mode :fill-submit) (butlast (:fill-submit! x)) (:fill! x))]
    (doseq [e xs]
      (cond
        (:clear! e) (let [field (dissoc e :clear!)]
                      (-> @driver (wc/find-element field) (wc/clear)))
        (:select! e) (let [sel-val (:select! e)
                           field (dissoc e :select!)]
                      (-> @driver (wc/find-element field) (wc/select-by-value sel-val)))
        :else (let [txt-val (:text! e)
                    field (dissoc e :text!)]
                (-> @driver (wc/find-element field) (wc/input-text txt-val)))))))

(defmethod decode :fill-submit! [{:keys [fill-submit! pause] :as x}]
  (if (seq (filter #(intersection #{:clear! :select! :text!} (set (keys %))) (butlast fill-submit!)))
    (compute-fill x :fill-submit)
    (wf/quick-fill @driver (:fill! x)))
  (-> @driver (wc/find-element (last fill-submit!)) wc/click)
  (render-pause (or pause def-pause))
  (screenshot (:screenshot x)))

(defmethod decode :fill! [{:keys [fill! pause] :as x}]
  (if (seq (filter #(seq (intersection #{:clear! :select! :text!} (set (keys %)))) fill!))
    (compute-fill x :fill)
    (wf/quick-fill @driver (:fill! x)))
  (render-pause (or pause def-pause))
  (screenshot (:screenshot x)))

(defmethod decode :match [{:keys [match text]}]
  (let [el (if (string? match)
             (-> @driver (wc/find-element {:id match}))
             (-> @driver (wc/find-element match)))
        textval (wc/text el)]
    (is (= true (.contains textval text)) (str "Fail on match for element : " match ", substring : " text))))

(defmethod decode :default [x] (? x))

(deftest tests
  (let [width (env :crison-width 1024)
        height (env :crison-height 800)]
    (println "crison-width : " width)
    (println "crison-height : " height)
    (resize @driver {:width (int width) :height (int height)})
    (doseq [f *crisons*]
      (binding [*crison-file* f]
        (println "*crison-file : " *crison-file*)
        (doseq [t (read-string (slurp f))] (decode t))
        (screenshot "final")
        (source)))))

(defn drive
  "if bucket exists we are using s3
   crisons is a sequence of crison files to drive
   output-dir is where we write our test results and artefacts to"
  [crisons out-dir]
  (debug "crisons : " crisons)
  (debug "out-dir : " out-dir)
  (binding [*crisons* crisons
            *output-dir* out-dir
            *test-out* (writer (str out-dir "/" (date) "-tests.txt"))]
    (run-tests 'crison.core)))
