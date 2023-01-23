import * as cdk from "aws-cdk-lib";
import { Construct } from "constructs";
import * as lambda from "aws-cdk-lib/aws-lambda";
import { IotToLambda } from "@aws-solutions-constructs/aws-iot-lambda";
import { RetentionDays } from "aws-cdk-lib/aws-logs";
import {
  BlockPublicAccess,
  Bucket,
  BucketEncryption,
} from "aws-cdk-lib/aws-s3";
import { StorageClass } from "aws-cdk-lib/aws-s3/lib/rule";
import { Effect, PolicyStatement } from "aws-cdk-lib/aws-iam";
import { Duration, Size } from "aws-cdk-lib";

// import * as sqs from 'aws-cdk-lib/aws-sqs';

export class IOTUploadStack extends cdk.Stack {
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
    
    const ffmegLayer = new lambda.LayerVersion(this, 'ffmpeg-layer', {
      layerVersionName: 'ffmpeg',
      compatibleRuntimes: [lambda.Runtime.NODEJS_14_X],
      code: lambda.AssetCode.fromAsset('lambda-layers/ffmpeg')
    });

    const timelapseLambda = new lambda.Function(this, "timelapse_generator", {
      code: new lambda.AssetCode("lambdas/timelapse-video-generator"),
      handler: "index.handler",
      runtime: lambda.Runtime.NODEJS_14_X,
      logRetention: RetentionDays.ONE_DAY,
      timeout: Duration.seconds(240),
      memorySize: 2048,
      ephemeralStorageSize: Size.gibibytes(10),
      layers: [ffmegLayer],
      environment: {
        BUCKET_NAME: uploadBucket.bucketName,
      },
    });
    timelapseLambda.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["s3:PutObject", "s3:GetObject"],
        resources: [`${uploadBucket.bucketArn}/*`],
      })
    );
    timelapseLambda.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["s3:ListBucket"],
        resources: [uploadBucket.bucketArn],
      })
    );
  }
}
