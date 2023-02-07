import * as cdk from "aws-cdk-lib";
import { Table } from "aws-cdk-lib/aws-dynamodb";
import { Topic } from "aws-cdk-lib/aws-sns";
import { Construct } from "constructs";

import { MetadataProcessingStack } from "./metadata-processing-stack";
import { TimelapseGeneratorStack } from "./timelapse-generator-stack";

interface UploadProcessingStackProps extends cdk.StackProps {
  uploadBucketTopic: Topic;
  dynamoDBTable: Table;
  uploadBucketName: string;
  uploadBucketArn: string;
  // uploadBucket: Bucket
}

export class UploadProcessingStack extends cdk.Stack {
  // s3SnsFanoutStack: S3SnsFanoutStack;
  constructor(scope: Construct, id: string, props: UploadProcessingStackProps) {
    super(scope, id, props);
    new MetadataProcessingStack(this, "MetadataProcessingStack", props);
    new TimelapseGeneratorStack(this, "TimelapseGeneratorStack", props);
  }
}
