(ns clojure-aws-example.s3
  "S3 operations using LocalStack."
  (:require [clojure-aws-example.config :as config]
            [cognitect.aws.client.api :as aws]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(defn create-bucket
  "Creates an S3 bucket."
  [bucket-name]
  (log/info "Creating bucket:" bucket-name)
  (aws/invoke @config/s3-client
              {:op      :CreateBucket
               :request {:Bucket bucket-name}}))

(defn list-buckets
  "Lists all S3 buckets."
  []
  (log/info "Listing buckets")
  (aws/invoke @config/s3-client {:op :ListBuckets}))

(defn upload-object
  "Uploads a file or string content to S3."
  [bucket-name key content]
  (log/info "Uploading object:" key "to bucket:" bucket-name)
  (let [body (if (string? content)
               (.getBytes content "UTF-8")
               (if (instance? java.io.File content)
                 (io/input-stream content)
                 content))]
    (aws/invoke @config/s3-client
                {:op      :PutObject
                 :request {:Bucket bucket-name
                           :Key    key
                           :Body   body}})))

(defn download-object
  "Downloads an object from S3."
  [bucket-name key]
  (log/info "Downloading object:" key "from bucket:" bucket-name)
  (aws/invoke @config/s3-client
              {:op      :GetObject
               :request {:Bucket bucket-name
                         :Key    key}}))

(defn list-objects
  "Lists objects in a bucket."
  [bucket-name & {:keys [prefix] :or {prefix ""}}]
  (log/info "Listing objects in bucket:" bucket-name "with prefix:" prefix)
  (aws/invoke @config/s3-client
              {:op      :ListObjectsV2
               :request {:Bucket bucket-name
                         :Prefix prefix}}))

(defn delete-object
  "Deletes an object from S3."
  [bucket-name key]
  (log/info "Deleting object:" key "from bucket:" bucket-name)
  (aws/invoke @config/s3-client
              {:op      :DeleteObject
               :request {:Bucket bucket-name
                         :Key    key}}))

(defn delete-bucket
  "Deletes an S3 bucket (must be empty)."
  [bucket-name]
  (log/info "Deleting bucket:" bucket-name)
  (aws/invoke @config/s3-client
              {:op      :DeleteBucket
               :request {:Bucket bucket-name}}))

(defn object-exists?
  "Checks if an object exists in S3."
  [bucket-name key]
  (try
    (aws/invoke @config/s3-client
                {:op      :HeadObject
                 :request {:Bucket bucket-name
                           :Key    key}})
    true
    (catch Exception _ false)))
