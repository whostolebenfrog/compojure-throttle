# Compojure-throttle

A Clojure Compojure middleware library for limiting the rate at which a user
can access a resource. Going over this rate will return an error response.

## Usage

Dependency via [cloars](https://clojars.org/compojure-throttle)

    [compojure-throttle "0.1.3"]

Then use with 

    (:require [compojure-throttle.core :as throttler])

Then add to your middleware stack

    (def app
      (handler/site
       throttler/throttle
       main-routes))

Or add to a specific route

    (defroutes main-routes

      (throttler/throttle
        (POST "/data" req "OK")))

By default compojure-throttle throttles on IP. You can also pass an optional function
that allows it to throttle based on other attributes. This function should accept a
single argument (the request) and return a token that uniquely identifies the attribute you wish to throttle on.

For example, let's assume we have a :user entry in our request map that contains a
unique user id and that we want to throttle based on this.

    (throttler/throttle (fn [req] (:user req)) ...)

To configure the rate at which we throttle use two environment variables:

    COMPOJURE_THROTTLE_TTL=1000
    COMPOJURE_THROTTLE_TOKENS=3

TTL defines the period for which we are throttling e.g. 1000 milliseconds
TOKENS defines the number of tries a user is allowed within that period.
For example we might allow 3 responses a second.

This (token-bucket) approach allows us to handle small bursts in traffic without
throttling whilst still throttling sustained high traffic.

We can also configure the response code for throttled requests using:

    COMPOJURE_THROTTLE_RESPONSE_CODE=420

# Building #

    lein jar

# Testing #

    lein midje

# Author #

Benjamin Griffiths (whostolebenfrog)

## License

Copyright © 2012 Ben Griffiths

Distributed under the Eclipse Public License, the same as Clojure.
