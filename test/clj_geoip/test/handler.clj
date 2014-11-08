(ns clj-geoip.test.handler
  (:require [midje.sweet :refer :all]
            [clj-geoip.core :refer [geoip-close]]
            [clj-geoip.handler :refer :all]))

(with-state-changes [(after :facts (geoip-close))]

  (facts "about the ring handler"

         (fact "it adds location information to the :location key of the request map"
               (let [handler (geoip-handler #(:location %))]
                 (handler {:remote-addr "209.85.148.100"})) => (contains {:countryName "United States"}))))
