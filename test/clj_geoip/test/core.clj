(ns clj-geoip.test.core
  (:require [midje.sweet :refer :all]
            [clj-geoip.core :refer :all]))

(facts "about lookup"

       (fact "it has to be initialized"
             (initialized?) => false
             (geoip-init :IPv4+6) => true
             (initialized? :IPv4) => true
             (initialized? :IPv6) => true
             (initialized? :IPv4+6) => true)

       (fact "it looks up IPv4 adresses"
             (lookup "209.85.148.100") => (contains {:ip "209.85.148.100"
                                                    :countryCode "US"
                                                    :asn "AS15169 Google Inc."}))

       (fact "it looks up IPv6 adresses"
             (lookup "2001:4860:4860::8888") => (contains {:ip "2001:4860:4860::8888"
                                                          :countryCode "US"
                                                          :asn "AS15169 Google Inc."}))

       (fact "it has to be shut down"
             (geoip-close) => true))
