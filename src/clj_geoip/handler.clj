(ns clj-geoip.handler
  "GeoIP ring handler."
  (:require [clj-geoip.core :as geoip]))

(defn geoip-handler
  "GeoIP ring handler to add location information to the request map."
  [handler]
  (geoip/geoip-init)
  (fn [request]
    (let [ip (:remote-addr request)
          location (geoip/lookup ip)
          req (assoc request :location location)]
      (handler req))))