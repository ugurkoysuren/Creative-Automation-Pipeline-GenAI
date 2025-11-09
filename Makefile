.PHONY: help build test clean run run-mcdonalds run-mercedes package install

help:
	@echo "Creative Automation Pipeline - Available Commands"
	@echo ""
	@echo "Build Commands:"
	@echo "  make build          - Compile the project"
	@echo "  make package        - Build executable JAR"
	@echo "  make clean          - Clean build artifacts"
	@echo ""
	@echo "Test Commands:"
	@echo "  make test           - Run all tests"
	@echo "  make test-verbose   - Run tests with detailed output"
	@echo ""
	@echo "Run Commands:"
	@echo "  make run-mcdonalds  - Run McDonald's example campaign"
	@echo "  make run-mercedes   - Run Mercedes example campaign (requires FAL_KEY)"
	@echo ""
	@echo "Development Commands:"
	@echo "  make install        - Clean, test, and package"
	@echo "  make javadoc        - Generate JavaDoc documentation"

build:
	@echo "Building project..."
	@mvn compile

package:
	@echo "Packaging application..."
	@mvn clean package

clean:
	@echo "Cleaning build artifacts..."
	@mvn clean
	@rm -rf logs/*.log

test:
	@echo "Running tests..."
	@mvn test

test-verbose:
	@echo "Running tests with verbose output..."
	@mvn test -X

run-mcdonalds:
	@echo "Running McDonald's campaign example..."
	@./run.sh -b examples/mcdonalds/campaign.yaml

run-mercedes:
	@echo "Running Mercedes campaign example..."
	@if [ -z "$$FAL_KEY" ]; then \
		echo "Warning: FAL_KEY not set. Using mock image generation."; \
	fi
	@./run.sh -b examples/mercedes/campaign.yaml

install: clean test package
	@echo "Build and test completed successfully!"

javadoc:
	@echo "Generating JavaDoc..."
	@mvn javadoc:javadoc
	@echo "JavaDoc generated in target/site/apidocs/"
