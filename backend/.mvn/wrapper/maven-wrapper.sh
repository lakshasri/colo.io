#!/bin/sh

# Get the directory where this script is located
WRAPPER_DIR=$(cd "$(dirname "$0")" && pwd)
PROPERTIES_FILE="$WRAPPER_DIR/maven-wrapper.properties"

# Cache directory for Maven
MAVEN_CACHE="$HOME/.m2/colo-io-wrapper"

# Extract distribution URL
DIST_URL=$(grep "^distributionUrl=" "$PROPERTIES_FILE" | cut -d'=' -f2-)
MAVEN_VERSION=$(echo "$DIST_URL" | sed 's/.*apache-maven-//' | sed 's/-.*//')
MAVEN_HOME="$MAVEN_CACHE/maven-$MAVEN_VERSION"

# Download Maven if not cached
if [ ! -d "$MAVEN_HOME/bin" ]; then
  mkdir -p "$MAVEN_CACHE"
  cd "$MAVEN_CACHE"

  MAVEN_FILE="maven-$MAVEN_VERSION-bin.zip"
  if [ ! -f "$MAVEN_FILE" ]; then
    echo "Downloading Maven $MAVEN_VERSION..."
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL -o "$MAVEN_FILE" "$DIST_URL" || exit 1
    elif command -v wget >/dev/null 2>&1; then
      wget -q -O "$MAVEN_FILE" "$DIST_URL" || exit 1
    else
      echo "curl or wget required to download Maven"
      exit 1
    fi
  fi

  echo "Extracting Maven..."
  unzip -q "$MAVEN_FILE"

  # Move the extracted folder to the target location
  EXTRACTED_DIR=$(ls -d apache-maven-* 2>/dev/null | head -1)
  if [ -n "$EXTRACTED_DIR" ]; then
    if [ -d "maven-$MAVEN_VERSION" ]; then
      rm -rf "maven-$MAVEN_VERSION"
    fi
    mv "$EXTRACTED_DIR" "maven-$MAVEN_VERSION"
  fi
fi

# Execute Maven
if [ -x "$MAVEN_HOME/bin/mvn" ]; then
  exec "$MAVEN_HOME/bin/mvn" "$@"
else
  echo "Maven executable not found at $MAVEN_HOME/bin/mvn"
  exit 1
fi
