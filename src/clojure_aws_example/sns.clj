(ns clojure-aws-example.sns
  "SNS operations using LocalStack."
  (:require [clojure-aws-example.config :as config]
            [cognitect.aws.client.api :as aws]
            [clojure.tools.logging :as log]))

(defn create-topic
  "Creates an SNS topic and returns the topic ARN."
  [topic-name]
  (log/info "Creating topic:" topic-name)
  (let [response (aws/invoke @config/sns-client
                              {:op      :CreateTopic
                               :request {:Name topic-name}})]
    (get-in response [:TopicArn])))

(defn list-topics
  "Lists all SNS topics."
  []
  (log/info "Listing topics")
  (aws/invoke @config/sns-client {:op :ListTopics}))

(defn get-topic-attributes
  "Gets attributes of an SNS topic."
  [topic-arn]
  (log/info "Getting topic attributes for:" topic-arn)
  (aws/invoke @config/sns-client
              {:op      :GetTopicAttributes
               :request {:TopicArn topic-arn}}))

(defn publish-message
  "Publishes a message to an SNS topic."
  [topic-arn message & {:keys [subject] :or {subject nil}}]
  (log/info "Publishing message to topic:" topic-arn)
  (aws/invoke @config/sns-client
              {:op      :Publish
               :request (cond-> {:TopicArn topic-arn
                                 :Message  message}
                          subject (assoc :Subject subject))}))

(defn subscribe
  "Subscribes an endpoint to an SNS topic.
   Supported protocols: sqs, email, email-json, sms, http, https, lambda"
  [topic-arn protocol endpoint]
  (log/info "Subscribing" protocol "endpoint" endpoint "to topic:" topic-arn)
  (let [response (aws/invoke @config/sns-client
                              {:op      :Subscribe
                               :request {:TopicArn topic-arn
                                         :Protocol protocol
                                         :Endpoint endpoint}})]
    (get-in response [:SubscriptionArn])))

(defn list-subscriptions
  "Lists all subscriptions for a topic."
  [topic-arn]
  (log/info "Listing subscriptions for topic:" topic-arn)
  (aws/invoke @config/sns-client
              {:op      :ListSubscriptionsByTopic
               :request {:TopicArn topic-arn}}))

(defn list-all-subscriptions
  "Lists all subscriptions."
  []
  (log/info "Listing all subscriptions")
  (aws/invoke @config/sns-client {:op :ListSubscriptions}))

(defn confirm-subscription
  "Confirms a subscription (for http/https endpoints)."
  [topic-arn token]
  (log/info "Confirming subscription for topic:" topic-arn)
  (aws/invoke @config/sns-client
              {:op      :ConfirmSubscription
               :request {:TopicArn topic-arn
                         :Token    token}}))

(defn delete-topic
  "Deletes an SNS topic."
  [topic-arn]
  (log/info "Deleting topic:" topic-arn)
  (aws/invoke @config/sns-client
              {:op      :DeleteTopic
               :request {:TopicArn topic-arn}}))
