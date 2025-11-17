#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: $0 <new-version>"
    echo "Example: $0 1.2.6-SNAPSHOT"
    exit 1
fi

NEW_VERSION=$1
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ROOT_POM="$PROJECT_ROOT/pom.xml"

echo "Updating version to $NEW_VERSION..."
echo "Project root: $PROJECT_ROOT"

# Extract current project version (skip parent section)
CURRENT_VERSION=$(awk '/<\/parent>/,/<version>/ {if (/<version>/) {gsub(/.*<version>|<\/version>.*/, ""); print; exit}}' "$ROOT_POM")
echo "Current version: $CURRENT_VERSION"

if [[ "$OSTYPE" == "darwin"* ]]; then
    awk -v old="$CURRENT_VERSION" -v new="$NEW_VERSION" '
        /<\/parent>/ {parent_done=1}
        parent_done && !replaced && /<version>/ {
            sub("<version>" old "</version>", "<version>" new "</version>")
            replaced=1
        }
        {print}
    ' "$ROOT_POM" > "$ROOT_POM.tmp" && mv "$ROOT_POM.tmp" "$ROOT_POM"
else
    awk -v old="$CURRENT_VERSION" -v new="$NEW_VERSION" '
        /<\/parent>/ {parent_done=1}
        parent_done && !replaced && /<version>/ {
            sub("<version>" old "</version>", "<version>" new "</version>")
            replaced=1
        }
        {print}
    ' "$ROOT_POM" > "$ROOT_POM.tmp" && mv "$ROOT_POM.tmp" "$ROOT_POM"
fi
echo "Updated root pom version"

find "$PROJECT_ROOT" -name "pom.xml" -type f ! -path "$ROOT_POM" | while read pom_file; do
    echo "Processing: $pom_file"
    
    if grep -q "<artifactId>ali-agentic-adk</artifactId>" "$pom_file"; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' '/<parent>/,/<\/parent>/ {
                /<groupId>com\.alibaba<\/groupId>/,/<\/parent>/ {
                    s|<version>.*</version>|<version>'"$NEW_VERSION"'</version>|
                }
            }' "$pom_file"
        else
            sed -i '/<parent>/,/<\/parent>/ {
                /<groupId>com\.alibaba<\/groupId>/,/<\/parent>/ {
                    s|<version>.*</version>|<version>'"$NEW_VERSION"'</version>|
                }
            }' "$pom_file"
        fi
    fi
done

echo "Version update completed!"
echo "Please verify the changes with: git diff"
