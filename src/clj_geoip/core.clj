(ns clj-geoip.core
  "Thin Clojure layer on top of the GeoIP Java API.
Use `geoip-init` and `geoip-close` to start and stop the service and `lookup` to
lookup information about the given IP."
  (import com.maxmind.geoip.LookupService))

;; Setup
;; ============

(def ^{:private true :dynamic true
       :doc "Location of the GeoIP DB files."}
  *dbs*
  {:city "resources/GeoLiteCity.dat"
   :asn  "resources/GeoIPASNum.dat"})

;; Refs to hold the two GeoIP DB files.
;; They should be private so no one messes around with them.
(def ^{:private true} geoip-city (ref nil))
(def ^{:private true} geoip-asn (ref nil))

(defn- geoip-mode
  "Looks up the matching mode to the given keyword."
  [mode]
  (case mode
      :memory 1
      :check  2
      :index  4
      0))

(defn- geoip-init-db
  "Initializes a new LookupService with the given file and mode."
  [db mode]
  (if mode
    (LookupService. db (geoip-mode mode))
    (LookupService. db)))

(defn geoip-init
  "Initializes the GeoIP service.
The modes `:memory`, `:check` or `:index` are possible."
  [& [mode]]
  (dosync
   (let [city (geoip-init-db (:city *dbs*) mode)
         asn (geoip-init-db (:asn *dbs*) mode)]
     (ref-set geoip-city city)
     (ref-set geoip-asn asn)
     true)))

(defn geoip-close
  "Shuts down the GeoIP service."
  []
  (dosync
   (.close @geoip-asn)
   (ref-set geoip-asn nil)
   (.close @geoip-city)
   (ref-set geoip-city nil)
   true))


;; Helper
;; ============

(defn initialized?
  "Checks whether the GeoIP service is initialized or not."
  []
  (and (not (nil? @geoip-city)) (not (nil? @geoip-asn))))

(defmacro with-init-check
  "Wrapps the given statements with an init check."
  [body]
  `(if (initialized?)
     ~body
     (throw (IllegalStateException. "GeoIP not initialized."))))


;; Lookup
;; =============

(defn- lookup-location
  "Looks up IP location information."
  [ip]
  (with-init-check 
    (if-let [location (.getLocation @geoip-city ip)]
      {:ip ip
       :countryCode (.countryCode location)
       :countryName (.countryName location)
       :region (.region location)
       :city (.city location)
       :postalCode (.postalCode location)
       :latitude (.latitude location)
       :longitude (.longitude location)
       :dma-code (.dma_code location)
       :area-code (.area_code location)
       :metro-code (.metro_code location)})))

(defn- lookup-asn
  "Looks up IP provider information."
  [ip]
  (with-init-check
    (if-let [asn (.getOrg @geoip-asn ip)]
      {:ip ip
       :asn asn})))

(defn lookup
  "Looks up all available IP information."
  [ip]
  (if-let [geoinfo (merge (lookup-location ip)
                          (lookup-asn ip))]
    geoinfo
    {:error "IP not localized"}))