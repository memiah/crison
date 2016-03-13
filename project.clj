(defproject crison "0.1.0-SNAPSHOT"
  :description "A data language for driving websites"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-webdriver "0.7.2-SNAPSHOT"]
                 [org.seleniumhq.selenium/selenium-server "2.47.1"]
                                  ;; Needed by core code
                                  [org.seleniumhq.selenium/selenium-java "2.47.0"]
                                  [org.seleniumhq.selenium/selenium-remote-driver "2.47.1"]
                                  [com.codeborne/phantomjsdriver "1.2.1"
                                   :exclusion [org.seleniumhq.selenium/selenium-java
                                               org.seleniumhq.selenium/selenium-server
                                               org.seleniumhq.selenium/selenium-remote-driver]]]

  :aot :all
  :main crison.main)
