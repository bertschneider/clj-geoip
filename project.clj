(defproject clj-geoip "0.1-SNAPSHOT"
  :description "Thin Clojure layer on top of the Java GeoIP API.
Please have a look at the GeoIP homepage at http://www.maxmind.com/app/ip-location."
  :min-lein-version "2.0.0"
  :java-source-paths ["java"]
  :aot :all
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :dev-dependencies [[lein-marginalia "0.7.0"]]
  :plugins [[lein-swank "1.4.4"]])
