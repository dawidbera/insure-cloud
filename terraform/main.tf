# SNS Topic for policy issued events
resource "aws_sns_topic" "policy_issued_topic" {
  name = "policy-issued-topic"
}

# SQS Queues for microservices
resource "aws_sqs_queue" "notification_queue" {
  name = "notification-queue"
}

resource "aws_sqs_queue" "document_queue" {
  name = "document-queue"
}

resource "aws_sqs_queue" "search_queue" {
  name = "search-queue"
}

# SNS Subscriptions for SQS queues
resource "aws_sns_topic_subscription" "notif_subscription" {
  topic_arn = aws_sns_topic.policy_issued_topic.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.notification_queue.arn
}

resource "aws_sns_topic_subscription" "doc_subscription" {
  topic_arn = aws_sns_topic.policy_issued_topic.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.document_queue.arn
}

resource "aws_sns_topic_subscription" "search_subscription" {
  topic_arn = aws_sns_topic.policy_issued_topic.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.search_queue.arn
}

# S3 Bucket for policy documents
resource "aws_s3_bucket" "policy_documents" {
  bucket = "policy-documents"
}

# Basic S3 Bucket Configuration (formerly ACL)
resource "aws_s3_bucket_ownership_controls" "example" {
  bucket = aws_s3_bucket.policy_documents.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}
