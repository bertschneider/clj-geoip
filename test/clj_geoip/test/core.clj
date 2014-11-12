(ns clj-geoip.test.core
  (:require [midje.sweet :refer :all]
            [clj-geoip.core :refer :all]))

(let [ip "173.194.112.247"
      ipv6 "2001:4860:4860::8888"]

  (facts "about lookup-service"

         (fact "it has to be initialized with a database file"
               (lookup-service "resources/GeoLiteCityv6.dat") => truthy)

         (fact "it implements the Lookupable protocol"
               (lookup-service "resources/GeoLiteCityv6.dat") => #(extends? Lookupable (class %)))

         (fact "it can be closed"
               (close (lookup-service "resources/GeoLiteCityv6.dat")) => nil)

         (facts "about lookup"
                (let [lookupCity (lookup-service "resources/GeoLiteCityv6.dat")
                      lookupIPASNum (lookup-service "resources/GeoIPASNumv6.dat")]

                  (fact "it returns the looked up ip"
                        (lookup lookupCity ip) => (contains {:ip ip}))

                  (fact "it looks up postal information"
                        (lookup lookupCity ip) => (contains {:country-code "US"
                                                            :country-name "United States"
                                                            :city "Mountain View"
                                                            :postal-code "94043"}))

                  (fact "it looks up gps information"
                        (lookup lookupCity ip) => (contains {:latitude (Float. 37.419205)
                                                            :longitude (Float. -122.0574)
                                                            :area-code 0
                                                            :dma-code 0
                                                            :metro-code 0}))

                  (fact "it looks up the region"
                        (lookup lookupCity ip) => (contains {:region "California"}))


                  (fact "it looks up the timezone"
                        (lookup lookupCity ip) => (contains {:timezone "America/Los_Angeles"}))

                  (fact "it looks up the organization if appropriate db is supplied"
                        (lookup lookupCity ip) =not=> (contains {:org "AS15169 Google Inc."})
                        (lookup lookupIPASNum ip) => (contains {:org "AS15169 Google Inc."}))

                  (fact "it looks up IPv6 addresses"
                        (lookup lookupCity ipv6) => truthy))))


  (facts "about multi-lookup-service"

         (fact "it can be initialized without a database file"
               (multi-lookup-service) => truthy)

         (fact "it implements the Lookupable protocol"
               (multi-lookup-service) => #(extends? Lookupable (class %)))

         (fact "it can be closed"
               (close (multi-lookup-service)) => nil)

         (facts "about lookup"

                (fact "it looks up information from all db files"
                      (lookup (multi-lookup-service) ip) => (contains {:ip "173.194.112.247"
                                                                      :org "AS15169 Google Inc."
                                                                      :country-name "United States"})))))
