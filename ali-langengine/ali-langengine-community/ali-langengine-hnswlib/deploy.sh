#!/bin/bash

# Hnswlib deploy script
echo "Deploying Hnswlib module..."

mvn clean deploy -DskipTests

echo "Hnswlib module deployed successfully."
