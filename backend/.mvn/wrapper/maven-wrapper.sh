#!/bin/sh
# Licensed to the Apache Software Foundation (ASF)

BASEDIR=$(dirname "$0")

# ----- Locate Maven Home  -----
if [ -z "$MAVEN_HOME" ]; then
  MAVEN_HOME=$(cd "$BASEDIR/.mvn" > /dev/null; pwd)
fi

# ----- Check Maven Home -----
if [ ! -d "$MAVEN_HOME" ]; then
  echo "MAVEN_HOME is not set and cannot be determined"
  exit 1
fi

# ----- Get Java Home -----
if [ -z "$JAVA_HOME" ]; then
  JAVA_CMD=java
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

# ----- Check Java -----
if ! command -v "$JAVA_CMD" > /dev/null; then
  echo "Java is not installed or JAVA_HOME is incorrect"
  exit 1
fi

# ----- Download Maven Wrapper Jar if needed -----
WRAPPER_JAR="$MAVEN_HOME/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_HOME/wrapper/maven-wrapper.properties"
MAVEN_VERSION=$(grep distributionUrl "$WRAPPER_PROPERTIES" | sed 's/.*apache-maven-//' | sed 's/-.*//')

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven wrapper jar..."
  mkdir -p "$MAVEN_HOME/wrapper"
  WRAPPER_URL=$(grep wrapperUrl "$WRAPPER_PROPERTIES" | cut -d'=' -f2)
  if command -v curl > /dev/null; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget > /dev/null; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "curl or wget is required to download Maven wrapper"
    exit 1
  fi
fi

# ----- Download Maven Distribution if needed -----
MAVEN_HOME_DIR="$MAVEN_HOME/.maven"
if [ ! -d "$MAVEN_HOME_DIR" ]; then
  echo "Downloading Maven distribution..."
  mkdir -p "$MAVEN_HOME_DIR"
  DIST_URL=$(grep distributionUrl "$WRAPPER_PROPERTIES" | cut -d'=' -f2)
  if command -v curl > /dev/null; then
    curl -fsSL -o "$MAVEN_HOME_DIR/maven.zip" "$DIST_URL"
  elif command -v wget > /dev/null; then
    wget -q -O "$MAVEN_HOME_DIR/maven.zip" "$DIST_URL"
  else
    echo "curl or wget is required to download Maven"
    exit 1
  fi
  cd "$MAVEN_HOME_DIR"
  unzip -q maven.zip
  rm maven.zip
  MAVEN_DIR=$(ls -d */|head -1)
  mv "$MAVEN_DIR"/* .
  rmdir "$MAVEN_DIR"
  MAVEN_EXECUTABLE="$MAVEN_HOME_DIR/bin/mvn"
else
  MAVEN_EXECUTABLE="$MAVEN_HOME_DIR/bin/mvn"
fi

# ----- Run Maven -----
if [ ! -x "$MAVEN_EXECUTABLE" ]; then
  echo "Maven executable not found or not executable"
  exit 1
fi

exec "$MAVEN_EXECUTABLE" "$@"
