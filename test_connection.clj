#!/usr/bin/env clj
;; Quick test script to verify LocalStack connection
(ns test-connection
  (:require [clojure-aws-example.config :as config]
            [clojure-aws-example.s3 :as s3]
            [clojure-aws-example.sns :as sns]
            [clojure-aws-example.sqs :as sqs]))

(defn test-connection []
  (println "Testing LocalStack connection...")
  (try
    (println "\n1. Testing S3...")
    (let [buckets (s3/list-buckets)]
      (println "   ✓ S3 connection successful")
      (println "   Buckets:" (count (get buckets :Buckets []))))
    
    (println "\n2. Testing SNS...")
    (let [topics (sns/list-topics)]
      (println "   ✓ SNS connection successful")
      (println "   Topics:" (count (get topics :Topics []))))
    
    (println "\n3. Testing SQS...")
    (let [queues (sqs/list-queues)]
      (println "   ✓ SQS connection successful")
      (println "   Queues:" (count (get queues :QueueUrls []))))
    
    (println "\n✓ All services connected successfully!")
    (println "\nYou can now run: clj -M -m clojure-aws-example.core")
    (catch Exception e
      (println "\n✗ Connection failed!")
      (println "Error:" (.getMessage e))
      (println "\nMake sure LocalStack is running:")
      (println "  cd /home/dario/Desktop/home/Hobby/localstack")
      (println "  docker-compose up -d")
      (System/exit 1))))

(test-connection)
