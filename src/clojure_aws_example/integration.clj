(ns clojure-aws-example.integration
  "Integration example connecting S3, SNS, and SQS."
  (:require [clojure-aws-example.s3 :as s3]
            [clojure-aws-example.sns :as sns]
            [clojure-aws-example.sqs :as sqs]
            [clojure.tools.logging :as log]
            [clojure.string :as str]))

(defn setup-integration-example
  "Sets up a complete integration example:
   1. Creates an S3 bucket
   2. Creates an SNS topic
   3. Creates an SQS queue
   4. Subscribes the SQS queue to the SNS topic
   5. Uploads a file to S3
   6. Publishes a notification to SNS (which will be delivered to SQS)
   7. Receives the message from SQS"
  []
  (log/info "Setting up integration example...")
  
  ;; Create S3 bucket
  (let [bucket-name "test-bucket-integration"]
    (log/info "Creating S3 bucket:" bucket-name)
    (s3/create-bucket bucket-name)
    
    ;; Upload a test file
    (log/info "Uploading test file to S3")
    (s3/upload-object bucket-name "test-file.txt" "Hello from S3!")
    
    ;; List objects
    (log/info "Listing objects in bucket")
    (let [objects (s3/list-objects bucket-name)]
      (log/info "Objects in bucket:" (pr-str objects)))
    
    ;; Create SNS topic
    (let [topic-name "test-topic-integration"]
      (log/info "Creating SNS topic:" topic-name)
      (let [topic-arn (sns/create-topic topic-name)]
        (log/info "Topic ARN:" topic-arn)
        
        ;; Create SQS queue
        (let [queue-name "test-queue-integration"]
          (log/info "Creating SQS queue:" queue-name)
          (let [queue-url (sqs/create-queue queue-name)]
            (log/info "Queue URL:" queue-url)
            
            ;; Get queue ARN for SNS subscription
            (let [queue-attrs (sqs/get-queue-attributes queue-url)
                  queue-arn (or (get-in queue-attrs [:Attributes "QueueArn"])
                                ;; Construct ARN manually if not provided (LocalStack sometimes doesn't return it)
                                (let [queue-name (last (clojure.string/split queue-url #"/"))]
                                  (format "arn:aws:sqs:%s:000000000000:%s" "us-east-1" queue-name)))]
              (log/info "Queue ARN:" queue-arn)
              
              ;; Set queue policy to allow SNS to send messages
              (log/info "Setting queue policy for SNS")
              (sqs/set-queue-policy-for-sns queue-url queue-arn topic-arn)
              
              ;; Subscribe SQS queue to SNS topic
              (log/info "Subscribing SQS queue to SNS topic")
              (let [subscription-arn (sns/subscribe topic-arn "sqs" queue-arn)]
                (log/info "Subscription ARN:" subscription-arn)
                
                ;; Publish message to SNS
                (log/info "Publishing message to SNS topic")
                (sns/publish-message topic-arn
                                      (format "File uploaded to S3: s3://%s/test-file.txt" bucket-name)
                                      :subject "S3 Upload Notification")
                
                ;; Wait a bit for message propagation
                (Thread/sleep 1000)
                
                ;; Receive message from SQS
                (log/info "Receiving message from SQS queue")
                (let [messages (sqs/receive-messages queue-url :max-messages 1 :wait-time-seconds 2)]
                  (if (seq messages)
                    (let [message (first messages)]
                      (log/info "Received message:" (pr-str message))
                      ;; Delete the message after processing
                      (sqs/delete-message queue-url (:ReceiptHandle message))
                      (log/info "Message deleted from queue"))
                    (log/warn "No messages received"))
                  
                  {:bucket-name     bucket-name
                   :topic-arn       topic-arn
                   :queue-url       queue-url
                   :subscription-arn subscription-arn
                   :messages        messages})))))))))

(defn cleanup-integration-example
  "Cleans up resources created in the integration example."
  []
  (log/info "Cleaning up integration example...")
  
  (try
    ;; Delete S3 objects and bucket
    (s3/delete-object "test-bucket-integration" "test-file.txt")
    (s3/delete-bucket "test-bucket-integration")
    (catch Exception e (log/warn "Error cleaning up S3:" (.getMessage e))))
  
  (try
    ;; Delete SNS topic
    (sns/delete-topic "arn:aws:sns:us-east-1:000000000000:test-topic-integration")
    (catch Exception e (log/warn "Error cleaning up SNS:" (.getMessage e))))
  
  (try
    ;; Delete SQS queue
    (sqs/delete-queue "http://localhost:4566/000000000000/test-queue-integration")
    (catch Exception e (log/warn "Error cleaning up SQS:" (.getMessage e))))
  
  (log/info "Cleanup complete"))
