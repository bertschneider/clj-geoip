(defproject clj-geoip "0.2"
  :description "Thin Clojure layer on top of the Java GeoIP API.
Please have a look at the GeoIP homepage at http://www.maxmind.com/app/ip-location."
  :url "https://github.com/bertschneider/clj-geoip"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :min-lein-version "2.1.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.maxmind.geoip/geoip-api "1.2.14"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]] ;TravisCI needs this
                   }})
