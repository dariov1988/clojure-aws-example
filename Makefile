.PHONY: help test examples run clean deps check-localstack

# Default target
help:
	@echo "Available targets:"
	@echo "  make test          - Run connection test to verify LocalStack connectivity"
	@echo "  make examples      - Run all AWS service examples (S3, SNS, SQS, Integration)"
	@echo "  make run           - Alias for 'examples'"
	@echo "  make deps          - Download/update dependencies"
	@echo "  make check-localstack - Check if LocalStack is running"
	@echo "  make clean         - Clean build artifacts"
	@echo ""
	@echo "Prerequisites:"
	@echo "  - LocalStack must be running on http://localhost:4566"
	@echo "  - Start LocalStack: cd ~/localstack && docker-compose up -d"

# Run connection test
test:
	@echo "Running connection test..."
	@clj -M test_connection.clj

# Run all examples
examples:
	@echo "Running AWS service examples..."
	@clj -M -m clojure-aws-example.core

# Alias for examples
run: examples

# Download/update dependencies
deps:
	@echo "Downloading dependencies..."
	@clj -P

# Check if LocalStack is running
check-localstack:
	@echo "Checking LocalStack status..."
	@curl -s http://localhost:4566/_localstack/health > /dev/null 2>&1 && \
		echo "✓ LocalStack is running" || \
		(echo "✗ LocalStack is not running. Start it with:" && \
		 echo "  cd ~/localstack && docker-compose up -d" && exit 1)

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	@rm -rf .cpcache
	@rm -rf .clojure
	@rm -rf target
	@echo "✓ Cleaned"

# Run test with LocalStack check
test-safe: check-localstack test

# Run examples with LocalStack check
examples-safe: check-localstack examples

# Run both test and examples
all: check-localstack test examples
