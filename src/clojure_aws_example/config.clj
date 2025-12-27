(ns clojure-aws-example.config
  "Configuration for AWS services using LocalStack."
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]))

(def localstack-endpoint "http://localhost:4566")
(def region "us-east-1")

(defn create-client
  "Creates an AWS client configured for LocalStack.
   LocalStack accepts any dummy credentials."
  [service]
  (aws/client {:api                  service
               :region               region
               :credentials-provider (credentials/basic-credentials-provider
                                      {:access-key-id     "test"
                                       :secret-access-key "test"})
               :endpoint-override    {:protocol :http
                                      :hostname "localhost"
                                      :port     4566}}))

(def s3-client (delay (create-client :s3)))
(def sns-client (delay (create-client :sns)))
(def sqs-client (delay (create-client :sqs)))
