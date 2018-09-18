(ns compojure-throttle.core-test
  (:require [clj.ip :as ip]
            [compojure-throttle.core :refer :all]
            [midje.sweet :refer :all]))

(def ok-or-throttle
  (throttle (fn [req] {:status 200})))

;; We are using the defaults of 3 tokens and 1000 ttl

(fact-group :unit
            (with-state-changes [(before :facts (reset-cache))]

              (fact "A single call does not get throttled"
                    (ok-or-throttle {:remote-addr "10.0.0.1"}) => (contains {:status 200}))

              (fact "Multiple calls do get throttled"
                    (dotimes [x 3]
                      (ok-or-throttle {:remote-addr "10.0.0.2"}) => (contains {:status 200})
                      (provided (token-period) => 100000))
                    (ok-or-throttle {:remote-addr "10.0.0.2"}) => (contains {:status 429}))

              (fact "The bucket refills"
                    (dotimes [x 10]
                      (ok-or-throttle {:remote-addr "10.0.0.3"}) => (contains {:status 200})
                      (Thread/sleep 334)))

              (fact "Calls get throttled for custom tokens"
                    (let [handler (throttle (fn [req] (:user req)) (fn [req] {:status 200}))]
                      (dotimes [x 3]
                        (handler {:user        "token-blah"
                                  :remote-addr "10.0.0.4"}) => (contains {:status 200}))
                      (handler {:user        "token-blah"
                                :remote-addr "10.0.0.4"}) => (contains {:status 429})))

              (fact "Calls do not get throttled when ip is in lax subnet"
                    (dotimes [x 4]
                      (ok-or-throttle {:remote-addr "127.0.0.1"}) => (contains {:status 200})))

              (fact "Reset cache resets the cache"
                    (dotimes [x 10]
                      (reset-cache)
                      (ok-or-throttle {:remote-addr "10.0.0.4"}) => (contains {:status 200}))))) 
