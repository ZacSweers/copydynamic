#!/usr/bin/env bash

echo "Cloning osstrich..."
mkdir tmp
cd tmp
git clone git@github.com:square/osstrich.git
cd osstrich
echo "Packaging..."
mvn package
echo "Running..."
rm -rf tmp/copydynamic && java -jar target/osstrich-cli.jar tmp/copydynamic git@github.com:hzsweers/copydynamic.git io.sweers.copydynamic
echo "Cleaning up..."
cd ../..
rm -rf tmp
echo "Finished!"
