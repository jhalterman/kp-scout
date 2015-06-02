(defproject kp-scout "0.1.0-SNAPSHOT"
  :description "Notifies you when a desired Kaiser doctor becomes available"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [clj-http "1.0.1"]
                 [hickory "0.5.4"]
                 [com.draines/postal "1.11.3"]]
  :uberjar-name "kp-scout.jar"
  :main kp-scout.core)
