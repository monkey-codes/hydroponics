import * as cdk from "aws-cdk-lib";
import { Construct } from "constructs";
import * as lambda from "aws-cdk-lib/aws-lambda";
import { IotToLambda } from "@aws-solutions-constructs/aws-iot-lambda";
import { RetentionDays } from "aws-cdk-lib/aws-logs";
import {
  BlockPublicAccess,
  Bucket,
  BucketEncryption,
  EventType,
} from "aws-cdk-lib/aws-s3";
import { Effect, PolicyStatement } from "aws-cdk-lib/aws-iam";
import { Duration, Size } from "aws-cdk-lib";
import { Topic } from "aws-cdk-lib/aws-sns";
import { SnsDestination } from "aws-cdk-lib/aws-s3-notifications";

// import * as sqs from 'aws-cdk-lib/aws-sqs';

export class IOTUploadStack extends cdk.Stack {
  public readonly s3Topic: Topic;
  public readonly uploadBucketName: string;
  public readonly uploadBucketArn: string;
  // public readonly uploadBucket: Bucket;
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const uploadBucket = new Bucket(this, "UploadBucket", {
      encryption: BucketEncryption.UNENCRYPTED,
      blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
      bucketName: "hydroponics-iot-upload-bucket",
      versioned: false,
      // lifecycleRules: [
      //   {
      //     enabled: true,
      //     transitions: [
      //       {
      //         storageClass: StorageClass.ONE_ZONE_INFREQUENT_ACCESS,
      //         transitionAfter: cdk.Duration.days(1),
      //       },
      //     ],
      //   },
      // ],
    });
    const topic = new Topic(this, "S3EventTopic", {
      topicName: "S3EventTopic",
      displayName: "S3EventTopic",
    });
    uploadBucket.addEventNotification(
      EventType.OBJECT_CREATED,
      new SnsDestination(topic)
    );
    this.s3Topic = topic;
    this.uploadBucketName = uploadBucket.bucketName;
    this.uploadBucketArn = uploadBucket.bucketArn;
    // this.uploadBucket = uploadBucket;
    const iotToLambda = new IotToLambda(this, "upload_generator", {
      lambdaFunctionProps: {
        code: lambda.Code.fromAsset(`lambdas`),
        runtime: lambda.Runtime.NODEJS_14_X,
        handler: "upload-url-generator.handler",
        logRetention: RetentionDays.ONE_DAY,
        environment: {
          BUCKET_NAME: uploadBucket.bucketName,
        },
      },
      iotTopicRuleProps: {
        topicRulePayload: {
          ruleDisabled: false,
          description: "Processing file upload requests",
          sql: "SELECT * FROM 'upload/requests' WHERE type = 'request'",
          actions: [],
        },
      },
    });
    iotToLambda.lambdaFunction.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["s3:PutObject", "s3:GetObject"],
        resources: [`${uploadBucket.bucketArn}/*`],
      })
    );
    iotToLambda.lambdaFunction.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["iot:Publish"],
        resources: [
          `arn:aws:iot:ap-southeast-2:${this.account}:topic/upload/requests`,
        ],
      })
    );
  }
}
