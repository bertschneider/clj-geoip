#!/bin/bash

# Download the newest archives if they have changed since the last
# update.
echo "Downloading files"
cd scripts
wget -N http://geolite.maxmind.com/download/geoip/database/GeoLiteCityv6-beta/GeoLiteCityv6.dat.gz
wget -N http://geolite.maxmind.com/download/geoip/database/asnum/GeoIPASNumv6.dat.gz

# Extract archives and move files into the resources folder
echo "Extracting files"
gunzip -c GeoLiteCityv6.dat.gz > GeoLiteCityv6.dat
gunzip -c GeoIPASNumv6.dat.gz > GeoIPASNumv6.dat

echo "Copying files to resources folder"
mv ./GeoLiteCityv6.dat ../resources/
mv ./GeoIPASNumv6.dat ../resources/
