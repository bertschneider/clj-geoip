(ns de.bertschneider.clj-geoip.core
  "Thin Clojure layer on top of the GeoIP Java API."
  (import [com.maxmind.geoip LookupService regionName timeZone]))

;; ------ Lookupable ------

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

;; ------ Cache Options ------

(def cache-option-mapping
  {:standard 0
   :memory-cache 1
   :check-cache 2
   :index-cache 4})

(defn cache-option
  "Calculates the cache option based on the given cache-option-mapping keywords."
  [cache-options]
  (if (seq cache-options)
    (reduce + (map #(get cache-option-mapping % 0) cache-options))
    (cache-option-mapping :standard)))

;; ------ API ------

(defn lookup-service
  "Creates a LookupService / Lookupable from the given db file."
  [db-file & cache-options]
  (LookupService. db-file (cache-option cache-options)))

(defn multi-lookup-service
  "Creates a Lookupable from multiple db files.
  The lookup function returns the combination of all individual responses.
  If no db files are specified the following files are used:
  \"resources/GeoLiteCityv6.dat\",\"resources/GeoIPASNumv6.dat\""
  ([]
     (multi-lookup-service ["resources/GeoLiteCityv6.dat" "resources/GeoIPASNumv6.dat"] nil))
  ([cache-options]
     (multi-lookup-service ["resources/GeoLiteCityv6.dat" "resources/GeoIPASNumv6.dat"] cache-options))
  ([db-files cache-options]
     (let [lookup-services (map #(lookup-service % cache-options) db-files)]
       (reify
         Lookupable
         (lookup [this ip]
           (apply merge (map #(lookup % ip) lookup-services)))
         (close [this]
           (do
             (map close lookup-services)
             nil))))))
