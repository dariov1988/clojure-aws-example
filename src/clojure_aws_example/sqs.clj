(ns clojure-aws-example.sqs
  "SQS operations using LocalStack."
  (:require [clojure-aws-example.config :as config]
            [cognitect.aws.client.api :as aws]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]))

(defn create-queue
  "Creates an SQS queue and returns the queue URL.
   Attributes can include: DelaySeconds, MaximumMessageSize, MessageRetentionPeriod, etc."
  [queue-name & {:keys [attributes] :or {attributes {}}}]
  (log/info "Creating queue:" queue-name)
  (let [response (aws/invoke @config/sqs-client
                              {:op      :CreateQueue
                               :request (cond-> {:QueueName queue-name}
                                          (seq attributes) (assoc :Attributes attributes))})]
    (get-in response [:QueueUrl])))

(defn list-queues
  "Lists all SQS queues."
  [& {:keys [prefix] :or {prefix ""}}]
  (log/info "Listing queues with prefix:" prefix)
  (aws/invoke @config/sqs-client
              {:op      :ListQueues
               :request (when (seq prefix) {:QueueNamePrefix prefix})}))

(defn get-queue-url
  "Gets the URL for a queue by name."
  [queue-name]
  (log/info "Getting queue URL for:" queue-name)
  (let [response (aws/invoke @config/sqs-client
                             {:op      :GetQueueUrl
                              :request {:QueueName queue-name}})]
    (get-in response [:QueueUrl])))

(defn get-queue-attributes
  "Gets attributes of an SQS queue."
  [queue-url & {:keys [attribute-names] :or {attribute-names ["All"]}}]
  (log/info "Getting queue attributes for:" queue-url)
  (aws/invoke @config/sqs-client
              {:op      :GetQueueAttributes
               :request {:QueueUrl       queue-url
                         :AttributeNames attribute-names}}))

(defn send-message
  "Sends a message to an SQS queue."
  [queue-url message-body & {:keys [delay-seconds message-attributes] :or {delay-seconds nil message-attributes {}}}]
  (log/info "Sending message to queue:" queue-url)
  (aws/invoke @config/sqs-client
              {:op      :SendMessage
               :request (cond-> {:QueueUrl    queue-url
                                 :MessageBody message-body}
                          delay-seconds (assoc :DelaySeconds delay-seconds)
                          (seq message-attributes) (assoc :MessageAttributes message-attributes))}))

(defn receive-messages
  "Receives messages from an SQS queue.
   Returns up to max-messages (default 1, max 10).
   wait-time-seconds: 0-20, enables long polling if > 0."
  [queue-url & {:keys [max-messages wait-time-seconds visibility-timeout] :or {max-messages 1 wait-time-seconds 0 visibility-timeout nil}}]
  (log/info "Receiving messages from queue:" queue-url)
  (aws/invoke @config/sqs-client
              {:op      :ReceiveMessage
               :request (cond-> {:QueueUrl                queue-url
                                 :MaxNumberOfMessages     max-messages}
                          (pos? wait-time-seconds) (assoc :WaitTimeSeconds wait-time-seconds)
                          visibility-timeout (assoc :VisibilityTimeout visibility-timeout))}))

(defn delete-message
  "Deletes a message from an SQS queue."
  [queue-url receipt-handle]
  (log/info "Deleting message from queue:" queue-url)
  (aws/invoke @config/sqs-client
              {:op      :DeleteMessage
               :request {:QueueUrl      queue-url
                         :ReceiptHandle receipt-handle}}))

(defn purge-queue
  "Purges all messages from an SQS queue."
  [queue-url]
  (log/info "Purging queue:" queue-url)
  (aws/invoke @config/sqs-client
              {:op      :PurgeQueue
               :request {:QueueUrl queue-url}}))

(defn delete-queue
  "Deletes an SQS queue."
  [queue-url]
  (log/info "Deleting queue:" queue-url)
  (aws/invoke @config/sqs-client
              {:op      :DeleteQueue
               :request {:QueueUrl queue-url}}))

(defn set-queue-attributes
  "Sets attributes for an SQS queue."
  [queue-url attributes]
  (log/info "Setting queue attributes for:" queue-url)
  (aws/invoke @config/sqs-client
              {:op      :SetQueueAttributes
               :request {:QueueUrl   queue-url
                         :Attributes attributes}}))

(defn set-queue-policy-for-sns
  "Sets a queue policy to allow SNS to send messages to this queue.
   This is required for SNS-SQS integration."
  [queue-url queue-arn topic-arn]
  (log/info "Setting queue policy for SNS integration")
  (let [policy {:Version   "2012-10-17"
                :Statement [{:Effect    "Allow"
                             :Principal {:Service "sns.amazonaws.com"}
                             :Action    "sqs:SendMessage"
                             :Resource  queue-arn
                             :Condition {:ArnEquals {:aws:SourceArn topic-arn}}}]}
        policy-json (json/write-str policy)]
    (set-queue-attributes queue-url {"Policy" policy-json})))
