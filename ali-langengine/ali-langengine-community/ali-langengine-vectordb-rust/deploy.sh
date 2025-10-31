#!/bin/bash
mvn clean deploy -DskipTests -Dgpg.skip=false
