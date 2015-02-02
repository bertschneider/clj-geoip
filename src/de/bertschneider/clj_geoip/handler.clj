(ns de.bertschneider.clj-geoip.handler
  "GeoIP ring handler."
  (:require [de.bertschneider.clj-geoip.core :as geoip]))

(defn geoip-handler
  "GeoIP ring handler to add location information to the request map."
  [handler]
  (let [lookup-service (geoip/multi-lookup-service)]
    (fn [request]
      (let [ip (:remote-addr request)
            location (geoip/lookup lookup-service ip)
            req (assoc request :location location)]
        (handler req)))))
