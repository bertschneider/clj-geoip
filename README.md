# clj-geoip

`clj-geoip` is a thin [Clojure](http://www.clojure.com) layer on top
of the [MaxMind GeoIP Java API](http://www.maxmind.com/). It allows
you to query information like the country, city or network provider of
a given IP. Have a look at the usage section for an example.

"This product includes GeoLite data created by MaxMind, available from [http://www.maxmind.com/](http://www.maxmind.com/)."

## Preparation

To use `clj-geoip` you first need to download the newest version of
the free GeoIP data files. To do so you can use the download script
`UpdateGeoIpFiles.sh` provided in the `scripts` folder. 
It simply downloads the newest archives and extracts them into
the `resources` folder.

[MaxMind](http://www.maxmind.com/) provides new versions of the data
files on a monthly basis. So it's a good idea to run the script every
now and then.

Furthermore you have to compile the MaxMind Java classes first. Just
run the Leiningen javac command to do so.

    > lein javac

## Usage

This API is pretty simple, just have a look at the following code:

    user> (use 'clj-geoip.core)
    nil
    user> (geoip-init)
    true
    user> (use 'clojure.pprint)
    nil
    user> (pprint (lookup "87.152.91.74"))
    {:countryName "Germany",
     :area-code 0,
     :asn "AS3320 Deutsche Telekom AG",
     :longitude 7.399994,
     :postalCode nil,
     :latitude 50.983307,
     :city "Engelskirchen",
     :metro-code 0,
     :region "07",
     :countryCode "DE",
     :dma-code 0,
     :ip "87.152.91.74"}
    nil
    user> (geoip-close)
    true

Use `geoip-init` and `geoip-close` to start and stop the service and `lookup` to
lookup information about the given IP.

The data files are expected to be in the `resources` folder but it's
possible to bind the locations in the `clj-geoip.core/*dbs*` symbol to a new value.

## Ring Handler

You can use the provided ring handler to add location information to
the request map. Here is a Noir example:
    
    (use 'clj-geoip.handler)
    (add-middleware #'geoip-handler)
    (defpage "/" []
        (str (:location (ring-request))))
    ;; -> {:countryName "United States", :area-code 650, :longitude -122.0574, :postalCode "94043", :latitude 37.419205, :city "Mountain View", :metro-code 807, :region "CA", :countryCode "US", :dma-code 807, :asn "AS15169 Google Inc.", :ip "209.85.148.100"}
    
## TODO

- [X] Pass through of `LookupService` modes.
- [X] Ring handler to inject location information into the request map.
- Is the `geoip-close` method really necessary? 
- Add IPv6 functions.
- Add function to calculate the distance between two IPs.
- Noir test application on Heroku. 

## License

Copyright (C) 2012

Distributed under the Eclipse Public License, the same as Clojure.
