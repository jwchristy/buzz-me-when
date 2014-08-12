(defproject buzz-me-when "0.1.0-SNAPSHOT"
  :description "Notify me when a stock reaches a price"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.2.2"]
                 [compojure "1.1.3"]
                 [liberator "0.10.0"]
                 [clj-http "0.9.1"]
                 [cheshire "5.3.1"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [com.draines/postal "1.11.1"]
                 [clj-time "0.7.0"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler buzz-me-when.core/handler})
