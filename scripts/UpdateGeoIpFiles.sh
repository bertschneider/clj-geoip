#!/bin/bash

# Download the newest archives if they have changed since the last
# update.
echo "Downloading files"
wget -N http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz
wget -N http://geolite.maxmind.com/download/geoip/database/asnum/GeoIPASNum.dat.gz
wget -N http://geolite.maxmind.com/download/geoip/database/GeoLiteCityv6.dat.gz
wget -N http://geolite.maxmind.com/download/geoip/database/asnum/GeoIPASNumv6.dat.gz

# Extract archives and move files into the resources folder
echo "Extracting files"
gunzip -c GeoLiteCity.dat.gz > GeoLiteCity.dat
gunzip -c GeoIPASNum.dat.gz > GeoIPASNum.dat

echo "Copying files to resources folder"
mv ./GeoLiteCity.dat ../resources/
mv ./GeoIPASNum.dat ../resources/
mv ./GeoLiteCityv6.dat ../resources/
mv ./GeoIPASNumv6.dat ../resources/
