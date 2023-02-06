import * as cdk from "aws-cdk-lib";
import { Rule, RuleTargetInput, Schedule } from "aws-cdk-lib/aws-events";
import {
  addLambdaPermission,
  LambdaFunction,
} from "aws-cdk-lib/aws-events-targets";
import * as lambda from "aws-cdk-lib/aws-lambda";
import { RetentionDays } from "aws-cdk-lib/aws-logs";
import { Topic } from "aws-cdk-lib/aws-sns";
import { SqsSubscription } from "aws-cdk-lib/aws-sns-subscriptions";
import { Queue } from "aws-cdk-lib/aws-sqs";
import { Construct } from "constructs";

interface TimelapseGeneratorStackProps extends cdk.NestedStackProps {
  uploadBucketTopic: Topic;
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
    const timelapseQueue = new Queue(this, "TimelapseQueue", {});

    const filterLambda = new lambda.Function(this, "TimelapseFilterLambda", {
      code: new lambda.AssetCode(
        "lambdas/upload-processing/timelapse-generator"
      ),
      handler: "timelapse-filter.handler",
      runtime: lambda.Runtime.NODEJS_14_X,
      logRetention: RetentionDays.ONE_DAY,
      timeout: cdk.Duration.seconds(60),
      environment: {
        SOURCE_SQS_QUEUE_URL: timelapsePreFilterQueue.queueUrl,
        TARGET_SQS_QUEUE_URL: timelapseQueue.queueUrl
      },
    });
    timelapsePreFilterQueue.grantConsumeMessages(filterLambda);
    timelapseQueue.grantSendMessages(filterLambda);

    // const filterCronRule = new Rule(this, "TimelapseFilterTrigger", {
    //   schedule: Schedule.cron({ minute: "0/1" }),
    // });
    // filterCronRule.addTarget(
    //   new LambdaFunction(filterLambda, {
    //     event: RuleTargetInput.fromObject({ message: "Filter cron trigger" }),
    //   })
    // );
    // addLambdaPermission(filterCronRule, filterLambda);

    //   const eventSource = new SqsEventSource(timelapsePreFilterQueue);

    //   metadataWriterLambda.addEventSource(eventSource);
    //   props.dynamoDBTable.grantWriteData(metadataWriterLambda)
  }
}
