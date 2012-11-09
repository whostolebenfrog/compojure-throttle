(ns compojure-throttle.core
  (:require [clojure.core.cache :as cache]
            [environ.core :refer [env]]))

(def defaults
  {:compojure-throttle-ttl    1000
   :compojure-throttle-tokens 3})

(defn- prop
  [key]
  (Integer. (or (env key)
                (defaults key))))

(def requests (atom
               (cache/ttl-cache-factory {} :ttl (prop :compojure-throttle-ttl))))

(defn- update-cache
  [id tokens]
  (reset! requests (cache/miss @requests id tokens)))

(defn- throttle?
  [id]
  (if (cache/has? @requests id)
    (let [tokens (cache/lookup @requests id)]
      (update-cache id (dec tokens))
      (if (pos? tokens)
        false
        true))
    (do
      (update-cache id (dec (prop :compojure-throttle-tokens)))
      false)))

(defn- by-ip
  [req]
  (:remote-addr req))

(defn throttle
"Throttle incoming connections from a given source. By default this is based on IP.

Optionally takes a second argument which is a function used to lookup the 'token'
that determines whether or not the request is unique. For example a function that
returns a user token to limit by user id rather than ip. This function should accept
the request as its single argument"
([handler finder]
   (fn [req]
     (if (throttle? (finder req))
       {:status 420 :body "You have sent too many requests. Please wait before retrying."}
       (handler req))))
([handler]
   (throttle handler by-ip)))
