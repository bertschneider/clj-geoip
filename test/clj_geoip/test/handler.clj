(ns clj-geoip.handler
  (:use [clojure.test]
        [clj-geoip.handler]))

(deftest handler_should_add_location_to_request
  (let [request {:remote-addr "209.85.148.100"}
        handler (geoip-handler #(:location %))]
    (is (= "United States" (:countryName (handler request))))))

