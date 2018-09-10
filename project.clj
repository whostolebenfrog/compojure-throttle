(defproject compojure-throttle "0.1.8"

  :description "Throttling middleware for compojure"

  :url "http://github.com/whostolebfrog/compojure-throttle"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
            
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.cache "0.6.4"]
                 [clj-time "0.11.0"]
                 [environ "1.0.1"]]

  :lein-release {:deploy-via :clojars}

  :profiles {:dev {:dependencies [[midje "1.8.2"]]
                   :plugins [[lein-midje "2.0.0-SNAPSHOT"]
                             [lein-release "1.0.5"]]}})
