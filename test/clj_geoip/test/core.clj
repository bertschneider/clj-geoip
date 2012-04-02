(ns clj-geoip.test.core
  (:use [clj-geoip.core])
  (:use [clojure.test]))

(deftest lookup-ip-with-geoip
  (is (thrown? IllegalStateException (lookup "IP")))
  (is (true? (geoip-init)))
  (is (true? (initialized?)))
  (is (map? (lookup "google.com")))
  (let [location (lookup "209.85.148.100")]
    (is (= "209.85.148.100" (:ip location)))
    (is (= "US" (:countryCode location)))
    (is (= "AS15169 Google Inc." (:asn location))))
  (is (true? (geoip-close)))
  (is (false? (initialized?)))
  (is (thrown? IllegalStateException (lookup "IP")))
  (is (true? (geoip-init :memory))))