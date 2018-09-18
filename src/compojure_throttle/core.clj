(ns compojure-throttle.core
  (:require [clj.ip :as ip]
            [clj-time.core :as core-time]
            [clj-time.local :as local-time]
            [clojure.core.cache :as cache]
            [environ.core :refer [env]]))

(def ^:private defaults
  {:service-compojure-throttle-lax-ips       nil
   :service-compojure-throttle-ttl           1000
   :service-compojure-throttle-tokens        3
   :service-compojure-throttle-response-code 429})

(defn ip-lax-subnet
  []
  (or (env :service-compojure-throttle-lax-ips)
      (defaults :service-compojure-throttle-lax-ips)))

(def in-lax-subnet? (if (ip-lax-subnet) 
                      (ip/compile (ip-lax-subnet))
                      (constantly false)))

(defn- prop
  [key]
  (Integer. (or (env key)
                (defaults key))))

(def ^:private requests
  (atom (cache/ttl-cache-factory {} :ttl (prop :service-compojure-throttle-ttl))))

(defn reset-cache
  "Testing helper that resets the content of the cache - should allow tests to run from a known base"
  []
  (reset! requests (cache/ttl-cache-factory {} :ttl (prop :service-compojure-throttle-ttl))))

(defn- update-cache
  [id tokens]
  (swap! requests cache/miss id tokens))

(defn token-period
  []
  (/ (prop :service-compojure-throttle-ttl)
     (prop :service-compojure-throttle-tokens)))

(defn- record
  [tokens]
  {:tokens   tokens
   :datetime (local-time/local-now)})

(defn- throttle?
  [id]
  (when-not (cache/has? @requests id)
    (update-cache id (record (prop :service-compojure-throttle-tokens))))
  (let [entry     (cache/lookup @requests id)
        spares    (int (/ (core-time/in-millis (core-time/interval
                                                 (:datetime entry)
                                                 (local-time/local-now)))
                          (token-period)))
        remaining (+ (:tokens entry) spares)]
    (update-cache id (record (dec remaining)))
    (not (pos? remaining))))

(defn- by-ip
  [req]
  (:remote-addr req))

(defn throttle
  "Throttle incoming connections from a given source. By default this is based on IP.

   Throttling is controlled by both :service-compojure-throttle-enabled and 
   :service-compojure-throttle-lax-ips. If
   :service-compojure-throttle-enabled is false, throttling will still happen 
   to any IP not covered by :service-compojure-throttle-lax-ips.
  
  Optionally takes a second argument which is a function used to lookup the 'token'
  that determines whether or not the request is unique. For example a function that
  returns a user token to limit by user id rather than ip. This function should accept
  the request as its single argument"
  ([finder handler]
   (fn [req]
     (if (and (or (nil? (ip-lax-subnet))
                  (not (in-lax-subnet? (:remote-addr req))))
              (throttle? (finder req)))
       {:status (prop :service-compojure-throttle-response-code)
        :body   "You have sent too many requests. Please wait before retrying."}
       (handler req))))
  ([handler]
   (throttle by-ip handler)))
