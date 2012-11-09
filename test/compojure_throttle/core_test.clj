(ns compojure-throttle.core-test
  (:require [compojure-throttle.core :refer :all])
  (:require [midje.sweet :refer :all]))

(def ok-or-throttle
  (throttle (fn [req] {:status 200})))

;; We are using the defaults of 3 tokens and 1000 ttl

(fact "A single call does not get throttled"
      (ok-or-throttle {:remote-addr "10.0.0.1"}) => (contains {:status 200}))

(fact "Multiple calls do get throttled"
      (dotimes [x 3]
        (ok-or-throttle {:remote-addr "10.0.0.2"}) => (contains {:status 200}))
      (ok-or-throttle {:remote-addr "10.0.0.2"}) => (contains {:status 420}))

(fact "The bucket refills"
      (dotimes [x 10]
        (ok-or-throttle {:remote-addr "10.0.0.3"}) => (contains {:status 200})
        (Thread/sleep 334)))

(fact "Calls get throttled for custom tokens"
      (let [handler (throttle (fn [req] (:user req)) (fn [req] {:status 200}))]
        (dotimes [x 3]
            (ok-or-throttle {:user "token-blah"}) => (contains {:status 200})))
      (ok-or-throttle {:user "token-blah"}) => (contains {:status 420}))
