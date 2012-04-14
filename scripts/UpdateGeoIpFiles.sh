#!/bin/bash

echo "Change to script dir"
cd ./scripts

# Download the newest archives if they have changed since the last
# update.
echo "Downloading files"
wget -N http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz
wget -N http://geolite.maxmind.com/download/geoip/database/asnum/GeoIPASNum.dat.gz

# Extract archives and move files into the resources folder
echo "Extracting files"
gunzip -c GeoLiteCity.dat.gz > GeoLiteCity.dat
gunzip -c GeoIPASNum.dat.gz > GeoIPASNum.dat

echo "Copying files to resources folder"
mv ./GeoLiteCity.dat ../resources/
mv ./GeoIPASNum.dat ../resources/
