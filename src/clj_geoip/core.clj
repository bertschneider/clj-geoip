(ns clj-geoip.core
  "Thin Clojure layer on top of the GeoIP Java API."
  (import [com.maxmind.geoip LookupService regionName timeZone]))

(defprotocol Lookupable
  "Common protocol to lookup ip addresses."
  (lookup [this ip])
  (close [this]))

;; Extend the maxmind LookupService to implement Lookupable
(extend-type LookupService
  Lookupable
  (lookup [this ip]
    (->> {:ip ip}
       (merge
        (try
          (if-let [location (.getLocationV6 this ip)]
               {:country-code (.countryCode location)
                :country-name (.countryName location)
                :city (.city location)
                :postal-code (.postalCode location)
                :latitude (.latitude location)
                :longitude (.longitude location)
                :dma-code (.dma_code location)
                :area-code (.area_code location)
                :metro-code (.metro_code location)
                :region (regionName/regionNameByCode (.countryCode location) (.region location))
                :timezone (timeZone/timeZoneByCountryAndRegion (.countryCode location) (.region location))})
          (catch RuntimeException e ;getLocation throws an exception for GeoIPASNum.dat db file
            )))
       (merge
        (if-let [org (.getOrgV6 this ip)]
          (when (>= (.length org) 4)
            {:org org})))))
  (close [this]
    (.close this)))


;; ------ API ------

(defn lookup-service
  "Creates a LookupService / Lookupable from the given db file."
  [db-file]
  (LookupService. db-file))

(defn multi-lookup-service
  "Creates a Lookupable from multiple db files.
  The lookup function returns the combination of all individual responses.
  If no db files are specified the following files are used:
  \"resources/GeoLiteCityv6.dat\",\"resources/GeoIPASNumv6.dat\""
  ([] (multi-lookup-service "resources/GeoLiteCityv6.dat" "resources/GeoIPASNumv6.dat"))
  ([& db-files]
     (let [lookup-services (map lookup-service db-files)]
       (reify
         Lookupable
         (lookup [this ip] (apply merge (map #(lookup % ip) lookup-services)))
         (close [this]
           (do
             (map close lookup-services)
             nil))))))
