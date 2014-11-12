# clj-geoip

[![Build Status](https://secure.travis-ci.org/bertschneider/clj-geoip.png)](http://travis-ci.org/bertschneider/clj-geoip)

`clj-geoip` is a thin [Clojure](http://www.clojure.com) layer on top
of the legacy [MaxMind GeoIP Java API](https://github.com/maxmind/geoip-api-java). It allows
you to query information like the country, city or network provider of
a given IP. Have a look at the usage section for an example.

The new version can be found at [GeoIP2](https://github.com/maxmind/GeoIP2-java).

"This product includes GeoLite data created by MaxMind, available from [http://www.maxmind.com/](http://www.maxmind.cam/)."

## Preparation

To use `clj-geoip` you first need to download the newest version of
the free GeoIP data files. To do so you can use the download script
`UpdateGeoIpFiles.sh` provided in the `scripts` folder.
It simply downloads the newest archives and extracts them into
the `resources` folder.

[MaxMind](http://www.maxmind.com/) provides new versions of the data
files on a monthly basis. So it's a good idea to run the script every
now and then.

## Usage

This API is pretty simple, just have a look at the following code:

    user> (use 'clojure.pprint)
    nil
    user> (require ['clj-geoip.core :refer :all])
    nil
    user> (def mls (multi-lookup-service)
    user> (pprint (lookup mls "87.152.91.74"))
    {:timezone "Europe/Berlin",
     :ip "87.152.91.74",
     :area-code 0,
     :dma-code 0,
     :city "Lindlar",
     :country-code "DE",
     :metro-code 0,
     :longitude 7.366501,
     :postal-code "51789",
     :region "Nordrhein-Westfalen",
     :org "AS3320 Deutsche Telekom AG",
     :latitude 51.033203,
     :country-name "Germany"}
    user=> (pprint (lookup mls "2a00:1450:8003::93"))
    {:timezone "Europe/Dublin",
     :ip "2a00:1450:8003::93",
     :area-code 0,
     :dma-code 0,
     :city nil,
     :country-code "IE",
     :metro-code 0,
     :longitude -8.0,
     :postal-code nil,
     :region nil,
     :org "AS15169 Google Inc.",
     :latitude 53.0,
     :country-name "Ireland"}
    user> (close mls)
    nil

Use `lookup-service` to create a lookup service from a specific db file or `multi-lookup-service` to create one from both db files.
Afterwards the service can be used to look up IPv4 and IPv6 addresses.

## Ring Handler

You can use the provided ring handler to add location information to
the request map. Here is a Noir example:

    (use 'clj-geoip.handler)
    (add-middleware #'geoip-handler)
    (defpage "/" []
        (str (:location (ring-request))))
    ;; -> {:country-name "United States", :area-code 650, :longitude -122.0574 ... }

## Dependencies

This library can be used as dependency in your leiningen project:

    [clj-geoip "0.2"]

## Changelog

### Version 0.2
- Removed global lookup service in favor of the `Lookupable` protocol so that `geoip-init` is not needed anymore.
- Added `:timezone` and `:region` to the `lookup` map. 
- Renamed some keywords in the returned map from the `lookup` function to more clojure idiomatic names.

## License

Copyright (C) 2012--2014

Distributed under the Eclipse Public License, the same as Clojure.
