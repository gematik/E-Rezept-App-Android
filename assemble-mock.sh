#!/bin/bash

# Get the last tag from Git
LAST_TAG=$(git tag --sort=-version:refname | head -n 1)

# Get the last hash from Git
LAST_HASH=$(git rev-parse HEAD)

# example version name R1.17.2-RC1-2e984eg356g
NEW_VERSION_NAME="$LAST_TAG-$LAST_HASH"

# Get the current branch we are on
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Set the version code dynamically
# shellcheck disable=SC2003
NEW_VERSION_CODE=$(expr "$(git rev-list --count "$CURRENT_BRANCH")")

# Assemble the mock version
# shellcheck disable=SC2086
./gradlew :app:android-mock:assembleDebug -PVERSION_CODE=$NEW_VERSION_CODE -PVERSION_NAME="$NEW_VERSION_NAME"
