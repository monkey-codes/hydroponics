import * as cdk from "aws-cdk-lib";
import { Duration, Size } from "aws-cdk-lib";
import { Rule, RuleTargetInput, Schedule } from "aws-cdk-lib/aws-events";
import {
  LambdaFunction,
  addLambdaPermission,
} from "aws-cdk-lib/aws-events-targets";
import { Effect, PolicyStatement } from "aws-cdk-lib/aws-iam";
import * as lambda from "aws-cdk-lib/aws-lambda";
import { SqsEventSource } from "aws-cdk-lib/aws-lambda-event-sources";
import { RetentionDays } from "aws-cdk-lib/aws-logs";
import { Topic } from "aws-cdk-lib/aws-sns";
import { SqsSubscription } from "aws-cdk-lib/aws-sns-subscriptions";
import { Queue } from "aws-cdk-lib/aws-sqs";
import { Construct } from "constructs";

interface TimelapseGeneratorStackProps extends cdk.NestedStackProps {
  uploadBucketTopic: Topic;
  uploadBucketName: string;
  uploadBucketArn: string;
}

export class TimelapseGeneratorStack extends cdk.NestedStack {
  public readonly s3EventTopic: Topic;
  constructor(
    scope: Construct,
    id: string,
    props: TimelapseGeneratorStackProps
  ) {
    super(scope, id, props);
    const timelapsePreFilterQueue = new Queue(
      this,
      "TimelapsePreFilterQueue",
      {}
    );
    props.uploadBucketTopic.addSubscription(
      new SqsSubscription(timelapsePreFilterQueue)
    );
    const timelapseQueue = new Queue(this, "TimelapseQueue", {
      visibilityTimeout: Duration.minutes(16),
    });

    const lambdaCode = new lambda.AssetCode(
      "lambdas/upload-processing/timelapse-generator"
    );
    const filterLambda = new lambda.Function(this, "TimelapseFilterLambda", {
      code: lambdaCode,
      handler: "timelapse-filter.handler",
      runtime: lambda.Runtime.NODEJS_14_X,
      logRetention: RetentionDays.ONE_DAY,
      timeout: cdk.Duration.seconds(60),
      environment: {
        SOURCE_SQS_QUEUE_URL: timelapsePreFilterQueue.queueUrl,
        TARGET_SQS_QUEUE_URL: timelapseQueue.queueUrl,
      },
    });
    timelapsePreFilterQueue.grantConsumeMessages(filterLambda);
    timelapseQueue.grantSendMessages(filterLambda);

    const filterCronRule = new Rule(this, "TimelapseFilterTrigger", {
      // 6am AEST
      schedule: Schedule.cron({
        minute: "0",
        hour: "20",
        day: "*",
        month: "*"
      }),
    });
    filterCronRule.addTarget(
      new LambdaFunction(filterLambda, {
        event: RuleTargetInput.fromObject({ message: "Filter cron trigger" }),
      })
    );
    addLambdaPermission(filterCronRule, filterLambda);

    const ffmegLayer = new lambda.LayerVersion(this, "ffmpeg-layer", {
      layerVersionName: "ffmpeg",
      compatibleRuntimes: [lambda.Runtime.NODEJS_14_X],
      code: lambda.AssetCode.fromAsset("lambda-layers/ffmpeg"),
    });

    const timelapseLambda = new lambda.Function(this, "Timelapse", {
      code: lambdaCode,
      handler: "timelapse-generator.handler",
      runtime: lambda.Runtime.NODEJS_14_X,
      logRetention: RetentionDays.ONE_DAY,
      timeout: Duration.seconds(900),
      memorySize: 2048,
      ephemeralStorageSize: Size.gibibytes(10),
      layers: [ffmegLayer],
      environment: {
        BUCKET_NAME: props.uploadBucketName,
      },
    });
    const timelapseLambdaEventSource = new SqsEventSource(timelapseQueue);
    timelapseLambda.addEventSource(timelapseLambdaEventSource);
    timelapseLambda.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["s3:PutObject", "s3:GetObject"],
        resources: [`${props.uploadBucketArn}/*`],
      })
    );
    timelapseLambda.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["s3:ListBucket"],
        resources: [props.uploadBucketArn],
      })
    );
  }
}
