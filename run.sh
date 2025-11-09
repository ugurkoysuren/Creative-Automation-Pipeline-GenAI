#!/bin/bash

# Creative Automation Pipeline - Convenience Runner Script
# This script builds and runs the creative automation pipeline with proper Java settings

set -e

# Set JAVA_HOME to Java 22 if not already set
if [ -z "$JAVA_HOME" ]; then
    JAVA_22_HOME="/Users/ukoysuren/Library/Java/JavaVirtualMachines/azul-22.0.1/Contents/Home"
    if [ -d "$JAVA_22_HOME" ]; then
        export JAVA_HOME="$JAVA_22_HOME"
    fi
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if JAR exists, if not build it
JAR_FILE="target/creative-automation-pipeline-1.0.0-jar-with-dependencies.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}JAR file not found. Building project...${NC}"
    mvn clean package -q
    if [ $? -ne 0 ]; then
        echo -e "${RED}Build failed. Please check the errors above.${NC}"
        exit 1
    fi
    echo -e "${GREEN}Build successful!${NC}"
fi

# Set JVM options for macOS compatibility and memory
JAVA_OPTS="-Djava.awt.headless=true -Xmx2g"

# Run the application with all passed arguments
echo -e "${GREEN}Running Creative Automation Pipeline...${NC}"
echo ""

java $JAVA_OPTS -jar "$JAR_FILE" "$@"

exit_code=$?

if [ $exit_code -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ Pipeline completed successfully${NC}"
else
    echo ""
    echo -e "${RED}✗ Pipeline failed with exit code $exit_code${NC}"
fi

exit $exit_code
