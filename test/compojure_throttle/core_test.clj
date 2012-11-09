(ns compojure-throttle.core-test
  (:require [compojure-throttle.core :refer :all])
  (:require [midje.sweet :refer :all]))

(def ok-or-throttle
  (throttle (fn [req] {:status 200})))

;; We are using the defaults of 3 tokens and 1000 ttl

(fact "A single call does not get throttled"
      (ok-or-throttle {:remote-addr "10.0.0.1"}) => (contains {:status 200}))

(fact "Multiple calls do get throttled"
      (doseq [x (range 3)]
        (ok-or-throttle {:remote-addr "10.0.0.2"}) => (contains {:status 200}))
      (ok-or-throttle {:remote-addr "10.0.0.2"}) => (contains {:status 420}))

(fact "The bucket refills"
      (doseq [x (range 10)]
        (ok-or-throttle {:remote-addr "10.0.0.2"}) => (contains {:status 200})
        (Thread/sleep 400)))
