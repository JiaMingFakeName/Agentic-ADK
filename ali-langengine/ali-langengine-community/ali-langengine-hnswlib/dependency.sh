#!/bin/bash

# Hnswlib dependency script
echo "Installing Hnswlib dependencies..."

mvn dependency:resolve
mvn dependency:copy-dependencies

echo "Hnswlib dependencies installed successfully."
