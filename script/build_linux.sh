#!/bin/bash
set -e

# Define variables
APP_NAME="clipper"
APP_VERSION="1.0"
MAIN_JAR="Clipper-1.0-SNAPSHOT.jar"
INPUT_DIR="./target"
OUTPUT_DIR="./target/dist"
ICON_PATH="./assets/clipper.png"

# Ensure clean build
echo "Building project..."
mvn clean package

# Create output directory
mkdir -p $OUTPUT_DIR

# Determine the absolute path of the input jar because jpackage can be picky
INPUT_JAR_PATH="$INPUT_DIR/$MAIN_JAR"

if [ ! -f "$INPUT_JAR_PATH" ]; then
    echo "Error: Jar file $INPUT_JAR_PATH not found!"
    exit 1
fi

echo "Creating Debian package with bundled JRE..."

# Run jpackage
jpackage \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --input "$INPUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "com.mycompany.clipper.Clipper" \
  --type deb \
  --icon "$ICON_PATH" \
  --dest "$OUTPUT_DIR" \
  --description "A useful clipboard manager" \
  --linux-shortcut \
  --linux-menu-group "Utility" \
  --java-options "-Dfile.encoding=UTF-8" \
  --verbose

echo "Package created successfully in $OUTPUT_DIR"
ls -lh $OUTPUT_DIR/*.deb
