#!/bin/bash

# Download the newest archives if they have changed since the last update.
wget -N -q http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz
wget -N -q http://geolite.maxmind.com/download/geoip/database/asnum/GeoIPASNum.dat.gz

# Extract archives and move files into the resources folder
gunzip -c GeoLiteCity.dat.gz > GeoLiteCity.dat
gunzip -c GeoIPASNum.dat.gz > GeoIPASNum.dat

mv ./GeoLiteCity.dat ../resources/
mv ./GeoIPASNum.dat ../resources/
