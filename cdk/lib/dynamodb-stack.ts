import * as cdk from "aws-cdk-lib";
import { AttributeType, BillingMode, Table } from "aws-cdk-lib/aws-dynamodb";
import { Topic } from "aws-cdk-lib/aws-sns";
import { Construct } from "constructs";

// import * as sqs from 'aws-cdk-lib/aws-sqs';

export class DynamoDBStack extends cdk.Stack {
  public readonly dynamoDBTable: Table;
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);
    const table = new Table(this, 'Hydroponics', {
        partitionKey: { name: 'pk', type: AttributeType.STRING },
        sortKey: { name: 'sk', type: AttributeType.STRING },
        billingMode: BillingMode.PAY_PER_REQUEST,
      });
      this.dynamoDBTable = table
  }
}
