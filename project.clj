(defproject compojure-throttle "0.1.0-SNAPSHOT"

  :description "Throttling middleware for compojure"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
            
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/core.cache "0.6.2"]
                 [midje "1.4.0"]
                 [environ "0.3.0"]]

  :profiles {:dev {:plugins [[lein-midje "2.0.0-SNAPSHOT"]]}})
