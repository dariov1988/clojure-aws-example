# Clojure AWS Example with LocalStack

A comprehensive Clojure application demonstrating interactions with Amazon S3, SNS, and SQS using LocalStack for local development.

## Prerequisites

- Clojure CLI (tools.deps) - [Installation Guide](https://clojure.org/guides/install_clojure)
- LocalStack running on `http://localhost:4566`
- Java 8 or higher

## LocalStack Setup

Make sure LocalStack is running. If you have it configured in `~/localstack/`, you can start it with:

```bash
cd ~/localstack
docker-compose up -d
```

Verify LocalStack is running:
```bash
curl http://localhost:4566/_localstack/health
```

## Project Structure

```
clojure-aws-example/
├── deps.edn                    # Dependencies configuration
├── Makefile                     # Makefile for running examples and tests
├── test_connection.clj          # Connection test script
├── src/
│   └── clojure_aws_example/
│       ├── config.clj          # AWS client configuration for LocalStack
│       ├── s3.clj              # S3 operations
│       ├── sns.clj             # SNS operations
│       ├── sqs.clj             # SQS operations
│       ├── integration.clj     # Integration examples
│       └── core.clj            # Main entry point
└── README.md
```

## Usage

### Using Makefile (Recommended)

The project includes a Makefile for easy execution:

```bash
# Check if LocalStack is running
make check-localstack

# Run connection test
make test

# Run all examples
make examples
# or simply
make run

# Download/update dependencies
make deps

# Clean build artifacts
make clean

# Run test and examples with LocalStack check
make all

# Show all available targets
make help
```

### Running Examples Manually

Run the main application to see all examples:

```bash
clj -M -m clojure-aws-example.core
```

### REPL Development

Start a REPL:

```bash
clj
```

Then in the REPL:

```clojure
(require '[clojure-aws-example.s3 :as s3])
(require '[clojure-aws-example.sns :as sns])
(require '[clojure-aws-example.sqs :as sqs])

;; S3 Examples
(s3/create-bucket "my-bucket")
(s3/upload-object "my-bucket" "key.txt" "Hello, World!")
(s3/list-objects "my-bucket")
(s3/download-object "my-bucket" "key.txt")

;; SNS Examples
(def topic-arn (sns/create-topic "my-topic"))
(sns/publish-message topic-arn "Hello from SNS!")

;; SQS Examples
(def queue-url (sqs/create-queue "my-queue"))
(sqs/send-message queue-url "Hello from SQS!")
(sqs/receive-messages queue-url :max-messages 1)
```

## Features

### S3 Operations
- Create/Delete buckets
- Upload/Download objects
- List buckets and objects
- Check object existence
- Delete objects

### SNS Operations
- Create/Delete topics
- Publish messages
- Subscribe endpoints (SQS, email, HTTP, etc.)
- List topics and subscriptions
- Get topic attributes

### SQS Operations
- Create/Delete queues
- Send/Receive messages
- Delete messages
- Purge queues
- Get/Set queue attributes
- Long polling support

### Integration Example
- Complete workflow connecting S3 → SNS → SQS
- File upload triggers SNS notification
- SNS delivers message to SQS queue
- Message processing from SQS

## Code Quality

This project follows Clojure best practices:

- **Namespacing**: Clear, hierarchical namespace structure
- **Documentation**: Function-level docstrings
- **Error Handling**: Try-catch blocks where appropriate
- **Logging**: Structured logging using `clojure.tools.logging`
- **Configuration**: Centralized configuration management
- **Separation of Concerns**: Each AWS service in its own namespace
- **Idiomatic Clojure**: Uses maps, keywords, and functional patterns

## Dependencies

- `com.cognitect.aws/*` - Official AWS SDK for Clojure
- `org.clojure/clojure` - Clojure language
- `org.clojure/tools.logging` - Logging utilities
- `clj-http` - HTTP client (used by AWS SDK)

## LocalStack Configuration

The application is configured to use LocalStack with:
- Endpoint: `http://localhost:4566`
- Region: `us-east-1`
- Services: S3, SNS, SQS

Configuration can be modified in `src/clojure_aws_example/config.clj`.

## Notes

- LocalStack uses placeholder account IDs (typically `000000000000`)
- Some AWS features may behave slightly differently in LocalStack
- For production use, update the configuration to use real AWS credentials and endpoints

## License

This is an example project for educational purposes.
