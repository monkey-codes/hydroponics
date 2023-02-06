import * as cdk from 'aws-cdk-lib';
import { Table } from 'aws-cdk-lib/aws-dynamodb';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import { SqsEventSource } from 'aws-cdk-lib/aws-lambda-event-sources';
import { RetentionDays } from 'aws-cdk-lib/aws-logs';
import { Topic } from 'aws-cdk-lib/aws-sns';
import { SqsSubscription } from 'aws-cdk-lib/aws-sns-subscriptions';
import { Queue } from 'aws-cdk-lib/aws-sqs';
import { Construct } from 'constructs';

interface MetadataProcessingStackProps extends cdk.NestedStackProps {
    uploadBucketTopic: Topic;
    dynamoDBTable: Table;
}

export class MetadataProcessingStack extends cdk.NestedStack {
  public readonly s3EventTopic: Topic;
  constructor(scope: Construct, id: string, props: MetadataProcessingStackProps) {
    super(scope, id, props);
    const metadataProcessingQueue = new Queue(this, 'MetadataProcessingQueue', {})
    props.uploadBucketTopic.addSubscription(new SqsSubscription(metadataProcessingQueue))

    const metadataWriterLambda = new lambda.Function(this, "MetadataWriter", {
        code: new lambda.AssetCode("lambdas/upload-processing/metadata-writer"),
        handler: "metadata-writer.handler",
        runtime: lambda.Runtime.NODEJS_14_X,
        logRetention: RetentionDays.ONE_DAY,
        timeout: cdk.Duration.seconds(10),
        environment: {
            TABLE_NAME: props.dynamoDBTable.tableName,
          },
      });

      const eventSource = new SqsEventSource(metadataProcessingQueue);

      metadataWriterLambda.addEventSource(eventSource);
      props.dynamoDBTable.grantWriteData(metadataWriterLambda)
      
  }
}
