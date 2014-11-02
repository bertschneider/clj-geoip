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
   :asn  "resources/GeoIPASNum.dat"
   :city-ipv6 "resources/GeoLiteCityv6.dat"
   :asn-ipv6  "resources/GeoIPASNumv6.dat"})

;; Refs to hold the two GeoIP DB files.
;; They should be private so no one messes around with them.
(def ^{:private true} geoip-city (ref nil))
(def ^{:private true} geoip-asn (ref nil))
(def ^{:private true} geoip-city-ipv6 (ref nil))
(def ^{:private true} geoip-asn-ipv6 (ref nil))


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
The modes `:memory`, `:check` or `:index` are possible.
ip-version `:IPv4` (default), `:IPv4+6`, or just `IPv6`."
  [ip-version & [mode]]
   (if (:or (= ip-version :IPv4) (= ip-version :IPv4+6))
      (dosync
       (let [city (geoip-init-db (:city *dbs*) mode)
             asn (geoip-init-db (:asn *dbs*) mode)]
         (ref-set geoip-city city)
         (ref-set geoip-asn asn)
         true)))
   (if (:or (= ip-version :IPv6) (= ip-version :IPv4+6))
      (dosync
       (let [city (geoip-init-db (:city-ipv6 *dbs*) mode)
             asn (geoip-init-db (:asn-ipv6 *dbs*) mode)]
         (ref-set geoip-city city)
         (ref-set geoip-asn asn)
         true))))

(defn geoip-close
  "Shuts down the GeoIP service."
  []
  (dosync
   (.close @geoip-asn)
   (ref-set geoip-asn nil)
   (.close @geoip-city)
   (ref-set geoip-city nil)
   (.close @geoip-asn-ipv6)
   (ref-set geoip-asn-ipv6 nil)
   (.close @geoip-city-ipv6)
   (ref-set geoip-city-ipv6 nil)
   true))


;; Helper
;; ============

(defn initialized?
  "Checks whether the GeoIP service is initialized or not.
DB type ip-version `:IPv4` (default), `:IPv4+6`, or just `:IPv6`."
  [ & [ip-version-in]]
  (let [ip-version (if (not ip-version-in) :IPv4 ip-version-in)]
   (if (:or (= ip-version :IPv4) (= ip-version :IPv4+6))
    (and (not (nil? @geoip-city)) (not (nil? @geoip-asn))))
   (if (:or (= ip-version :IPv6) (= ip-version :IPv4+6))
    (and (not (nil? @geoip-city-ipv6)) (not (nil? @geoip-asn-ipv6))))))

(defmacro with-init-check
  "Wraps the given statements with an init check."
  [body]
  `(if (initialized?)
     ~body
     (throw (IllegalStateException. "GeoIP db not initialized."))))


;; Lookup
;; =============
(defn- get-location
  [ip ip-version]
  (cond
   (= ip-version :IPv6) (.getLocationV6 @geoip-city-ipv6 ip)
   :else (.getLocation @geoip-city ip)))

(defn- lookup-location
  "Looks up IP location information."
  [ip ip-version]
  (with-init-check
    (if-let [location (get-location ip ip-version)]
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

(defn- get-asn
  [ip ip-version]
  (cond
   (= ip-version :IPv6) (.getOrgV6 @geoip-asn-ipv6 ip)
   :else (.getOrg @geoip-asn ip)))

(defn- lookup-asn
  "Looks up IP provider information."
  [ip ip-version]
  (with-init-check
   (if-let [asn (get-asn ip ip-version)]
      {:ip ip
       :asn asn})))

(defn lookup
  "Looks up all available IP information.
Assumes IPv6 address if '::' in string, defaults to IPv4."
  [ip]
  (let [ip-version (if (re-find #"::" ip) :IPv6 :IPv4)]
    (if-let [geoinfo (merge (lookup-location ip ip-version)
                            (lookup-asn ip ip-version))]
      geoinfo
      {:error "IP not localized"})))
