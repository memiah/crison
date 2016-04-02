(defproject crison "0.2.0-SNAPSHOT"
  :description "A data language for driving websites"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-time "0.11.0"]
                 [environ "1.0.2"]
                 [clj-webdriver "0.7.2-SNAPSHOT"]
                 [org.seleniumhq.selenium/selenium-server "2.47.1"]
                 ;; Needed by core code
                 [org.seleniumhq.selenium/selenium-java "2.47.0"]
                 [org.seleniumhq.selenium/selenium-remote-driver "2.47.1"]
                 [com.codeborne/phantomjsdriver "1.2.1"
                   :exclusion [org.seleniumhq.selenium/selenium-java
                               org.seleniumhq.selenium/selenium-server
                               org.seleniumhq.selenium/selenium-remote-driver]]
                 [me.rossputin/diskops "0.4.1"]]

  :aot :all
  :main crison.main)
