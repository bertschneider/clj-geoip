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
  ([db]
     (geoip-init-db db nil))
  ([db mode]
     (LookupService. db (geoip-mode mode))))

(defn- geoip-ip-version
  "Looks up the matching ip version, if none is found :IPv4 is used."
  [ip-version]
  (get #{:IPv4 :IPv6 :IPv4+6} ip-version :IPv4))

(defn- geoip-ip-version-4?
  "Checks if ip version 4 should be used."
  [ip-version]
  (let [ip-version (geoip-ip-version ip-version)]
    (or (= ip-version :IPv4) (= ip-version :IPv4+6))))

(defn- geoip-ip-version-6?
  "Checks if ip version 6 should be used."
  [ip-version]
  (let [ip-version (geoip-ip-version ip-version)]
    (or (= ip-version :IPv6) (= ip-version :IPv4+6))))

(defn geoip-init
  "Initializes the GeoIP service.
The modes `:memory`, `:check` or `:index` are possible.
IP version `:IPv4` (default), `:IPv4+6`, or just `IPv6`."
  ([]
     (geoip-init nil nil))
  ([ip-version]
     (geoip-init ip-version nil))
  ([ip-version mode]
     (dosync
      (when (geoip-ip-version-4? ip-version)
        (ref-set geoip-city (geoip-init-db (:city *dbs*) mode))
        (ref-set geoip-asn (geoip-init-db (:asn *dbs*) mode)))
      (when (geoip-ip-version-6? ip-version)
        (ref-set geoip-city-ipv6 (geoip-init-db (:city-ipv6 *dbs*) mode))
        (ref-set geoip-asn-ipv6 (geoip-init-db (:asn-ipv6 *dbs*) mode)))
      true)))


;; Helper
;; ============

(defn initialized?
  "Checks whether the GeoIP service is initialized or not.
IP version `:IPv4` (default), `:IPv4+6`, or just `:IPv6`."
  ([]
     (initialized? nil))
  ([ip-version]
     (if (and (geoip-ip-version-4? ip-version) (geoip-ip-version-6? ip-version))
       (and (not (nil? @geoip-city))
          (not (nil? @geoip-asn))
          (not (nil? @geoip-city-ipv6))
          (not (nil? @geoip-asn-ipv6)))
       (if (geoip-ip-version-4? ip-version)
         (and (not (nil? @geoip-city))
            (not (nil? @geoip-asn)))
         (if (geoip-ip-version-6? ip-version)
           (and (not (nil? @geoip-city-ipv6))
              (not (nil? @geoip-asn-ipv6))))))))

(defmacro with-init-check
  "Wraps the given statements with an init check."
  [ip-version body]
  `(if (initialized? ~ip-version)
    ~body
    (throw (IllegalStateException. "GeoIP db not initialized."))))

(defn geoip-close
  "Shuts down the GeoIP service."
  []
  (dosync
     (when (initialized? :IPv4)
       (.close @geoip-asn)
       (ref-set geoip-asn nil)
       (.close @geoip-city)
       (ref-set geoip-city nil))
     (when(initialized? :IPv6)
       (.close @geoip-asn-ipv6)
       (ref-set geoip-asn-ipv6 nil)
       (.close @geoip-city-ipv6)
       (ref-set geoip-city-ipv6 nil))
   true))


;; Lookup
;; =============

(defn- get-location
  [ip ip-version]
  (if (geoip-ip-version-6? ip-version)
    (.getLocationV6 @geoip-city-ipv6 ip)
    (.getLocation @geoip-city ip)))

(defn- lookup-location
  "Looks up IP location information."
  [ip ip-version]
  (with-init-check ip-version
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
  (if (geoip-ip-version-6? ip-version)
    (.getOrgV6 @geoip-asn-ipv6 ip)
    (.getOrg @geoip-asn ip)))

(defn- lookup-asn
  "Looks up IP provider information."
  [ip ip-version]
  (with-init-check ip-version
   (if-let [asn (get-asn ip ip-version)]
      {:ip ip
       :asn asn})))

(defn lookup
  "Looks up all available IP information.
Assumes IPv6 address if '::' in string, defaults to IPv4."
  ([ip]
     (lookup ip (if (re-find #"::" ip) :IPv6 :IPv4)))
  ([ip ip-version]
     (if-let [geoinfo (merge (lookup-location ip ip-version)
                             (lookup-asn ip ip-version))]
       geoinfo
       {:error "IP not localized"})))
