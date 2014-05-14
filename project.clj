(defproject compojure-throttle "0.1.6"

  :description "Throttling middleware for compojure"

  :url "http://github.com/whostolebfrog/compojure-throttle"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
            
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/core.cache "0.6.2"]
                 [midje "1.5.1"]
                 [clj-time "0.4.3"]
                 [environ "0.3.0"]]

  :lein-release {:deploy-via :clojars}

  :profiles {:dev {:plugins [[lein-midje "2.0.0-SNAPSHOT"]
                             [lein-release "1.0.5"]]}})
