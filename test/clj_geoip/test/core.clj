(ns clj-geoip.test.core
  (:use [clj-geoip.core])
  (:use [clojure.test]))

(deftest lookup-ip-with-geoip
  (is (thrown? IllegalStateException (lookup "IP")))
  (is (true? (geoip-init :IPv4)))
  (is (true? (initialized?)))
  (is (map? (lookup "google.com")))
  (let [location (lookup "209.85.148.100")]
    (is (= "209.85.148.100" (:ip location)))
    (is (= "US" (:countryCode location)))
    (is (= "AS15169 Google Inc." (:asn location))))
  (is (true? (geoip-close)))
  (is (false? (initialized?)))
  (is (thrown? IllegalStateException (lookup "IP")))
  (is (true? (geoip-init :IPv4 :memory))))

(deftest lookup-ipv6-with-geoip
  (is (true? (geoip-init :IPv6)))
  (is (true? (initialized? :IPv6)))
  (is (map? (lookup "google.com" :IPv6)))
  (let [location (lookup "2001:4860:4860::8888")]
    (is (= "2001:4860:4860::8888" (:ip location)))
    (is (= "US" (:countryCode location)))
    (is (= "AS15169 Google Inc." (:asn location))))
  (is (true? (geoip-close)))
  (is (false? (initialized? :IPv6)))
  (is (thrown? IllegalStateException (lookup "IP")))
  (is (true? (geoip-init :IPv6 :memory))))
