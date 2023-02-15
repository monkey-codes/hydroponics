#!/usr/bin/env node
import "source-map-support/register";
import * as cdk from "aws-cdk-lib";
import { IOTCoreStack } from "../lib/cdk-stack";
import { IOTUploadStack } from "../lib/iot-upload-stack";
import { APIStack } from "../lib/api-stack";
import { UploadProcessingStack } from "../lib/upload-processing/upload-processing-stack";
import { DynamoDBStack } from "../lib/dynamodb-stack";
import { SensorDataStack } from "../lib/sensor-data/sensor-data-stack";

const app = new cdk.App();
new IOTCoreStack(app, "CdkStack", {
  /* If you don't specify 'env', this stack will be environment-agnostic.
   * Account/Region-dependent features and context lookups will not work,
   * but a single synthesized template can be deployed anywhere. */
  /* Uncomment the next line to specialize this stack for the AWS Account
   * and Region that are implied by the current CLI configuration. */
  // env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION },
  /* Uncomment the next line if you know exactly what Account and Region you
   * want to deploy the stack to. */
  // env: { account: '123456789012', region: 'us-east-1' },
  /* For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html */
});
const uploadStack = new IOTUploadStack(app, "IOTUploadStack", {});
const dynamodbStack = new DynamoDBStack(app, "DynamoDBStack", {});
const uploadProcessingStack = new UploadProcessingStack(
  app,
  "UploadProcessingStack",
  {
    uploadBucketTopic: uploadStack.s3Topic,
    dynamoDBTable: dynamodbStack.dynamoDBTable,
    uploadBucketName: uploadStack.uploadBucketName,
    uploadBucketArn: uploadStack.uploadBucketArn,
  }
);

new APIStack(app, "APIStack", {
  dynamoDBTable: dynamodbStack.dynamoDBTable,
  uploadBucketName: uploadStack.uploadBucketName,
  uploadBucketArn: uploadStack.uploadBucketArn,
});

new SensorDataStack(app, "SensorDataStack", {
  databaseName: "HydroponicsSensorDB",
  tableName: "HydroponicsSensorData"
});
