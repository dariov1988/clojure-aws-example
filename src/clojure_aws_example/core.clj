(ns clojure-aws-example.core
  "Main entry point for the Clojure AWS example application."
  (:require [clojure-aws-example.s3 :as s3]
            [clojure-aws-example.sns :as sns]
            [clojure-aws-example.sqs :as sqs]
            [clojure-aws-example.integration :as integration]
            [clojure.tools.logging :as log]))

(defn -main
  "Main function demonstrating AWS services usage."
  [& _args]
  (log/info "Starting Clojure AWS Example Application")
  (log/info "Make sure LocalStack is running on http://localhost:4566")
  
  (try
    ;; Example 1: S3 Operations
    (println "\n=== S3 Operations ===")
    (let [bucket-name "example-bucket"]
      (println "Creating bucket:" bucket-name)
      (s3/create-bucket bucket-name)
      
      (println "Listing buckets:")
      (println (s3/list-buckets))
      
      (println "Uploading object to S3")
      (s3/upload-object bucket-name "hello.txt" "Hello, World from S3!")
      
      (println "Listing objects in bucket:")
      (println (s3/list-objects bucket-name))
      
      (println "Downloading object from S3")
      (let [obj (s3/download-object bucket-name "hello.txt")]
        (println "Object content:" (slurp (:Body obj))))
      
      (println "Deleting object from S3")
      (s3/delete-object bucket-name "hello.txt")
      
      (println "Deleting bucket")
      (s3/delete-bucket bucket-name))
    
    ;; Example 2: SNS Operations
    (println "\n=== SNS Operations ===")
    (let [topic-name "example-topic"]
      (println "Creating topic:" topic-name)
      (let [topic-arn (sns/create-topic topic-name)]
        (println "Topic ARN:" topic-arn)
        
        (println "Listing topics:")
        (println (sns/list-topics))
        
        (println "Publishing message to topic")
        (sns/publish-message topic-arn "Hello from SNS!" :subject "Test Message")
        
        (println "Getting topic attributes:")
        (println (sns/get-topic-attributes topic-arn))
        
        (println "Deleting topic")
        (sns/delete-topic topic-arn)))
    
    ;; Example 3: SQS Operations
    (println "\n=== SQS Operations ===")
    (let [queue-name "example-queue"]
      (println "Creating queue:" queue-name)
      (let [queue-url (sqs/create-queue queue-name)]
        (println "Queue URL:" queue-url)
        
        (println "Listing queues:")
        (println (sqs/list-queues))
        
        (println "Sending message to queue")
        (sqs/send-message queue-url "Hello from SQS!")
        
        (println "Receiving messages from queue")
        (let [messages (sqs/receive-messages queue-url :max-messages 1 :wait-time-seconds 2)]
          (if (seq messages)
            (let [message (first messages)]
              (println "Received message:" (:Body message))
              (sqs/delete-message queue-url (:ReceiptHandle message))
              (println "Message deleted"))
            (println "No messages received")))
        
        (println "Deleting queue")
        (sqs/delete-queue queue-url)))
    
    ;; Example 4: Integration Example
    (println "\n=== Integration Example (S3 + SNS + SQS) ===")
    (integration/setup-integration-example)
    
    (println "\n=== All examples completed successfully! ===")
    
    (catch Exception e
      (log/error e "Error running examples")
      (println "Error:" (.getMessage e))
      (System/exit 1))))
