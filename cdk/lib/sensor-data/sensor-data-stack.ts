import * as cdk from "aws-cdk-lib";
import {
  Role,
  ServicePrincipal,
  Effect,
  PolicyStatement,
} from "aws-cdk-lib/aws-iam";
import { CfnTopicRule } from "aws-cdk-lib/aws-iot";
import { CfnDatabase, CfnTable } from "aws-cdk-lib/aws-timestream";
import { Construct } from "constructs";

interface UploadProcessingStackProps extends cdk.StackProps {
  databaseName: string;
  tableName: string;
}

export class SensorDataStack extends cdk.Stack {
  public readonly database: CfnDatabase;
  public readonly table: CfnTable;
  // s3SnsFanoutStack: S3SnsFanoutStack;
  constructor(scope: Construct, id: string, props: UploadProcessingStackProps) {
    super(scope, id, props);
    this.database = new CfnDatabase(this, "Database", {
      databaseName: props.databaseName,
    });
    // this.database.applyRemovalPolicy(RemovalPolicy.RETAIN);

    this.table = new CfnTable(this, "Table", {
      tableName: props.tableName,
      databaseName: props.databaseName,
      retentionProperties: {
        memoryStoreRetentionPeriodInHours: (24 * 7).toString(10),
        magneticStoreRetentionPeriodInDays: (365 * 2).toString(10),
      },
    });
    this.table.node.addDependency(this.database);
    const role = new Role(this, "IotTopicToTimestreamRole", {
      roleName: "IotTopicToTimestreamRole",
      assumedBy: new ServicePrincipal("iot.amazonaws.com"),
    });
    role.addToPolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["timestream:WriteRecords"],
        resources: [this.table.attrArn],
      })
    );
    role.addToPolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["timestream:DescribeEndpoints"],
        resources: ["*"],
      })
    );

    const topicRule = new CfnTopicRule(this, "IotSensorTopicRule", {
      topicRulePayload: {
        ruleDisabled: false,
        actions: [
          {
            timestream: {
              databaseName: this.database.databaseName!,
              tableName: this.table.tableName!,
              dimensions: [
                {
                  name: "device_id",
                  value: "${device_id}",
                },
                {
                  name: "rpi_model",
                  value: "${rpi_model}",
                },
              ],
              roleArn: role.roleArn,
            },
          },
        ],
        sql: "select cpu_percent, cpu_temp, load_avg_1, load_avg_5, load_avg_15, mem_percent, disk_usage_percent, disk_usage_used_mb, disk_usage_free_mb, webcam_count FROM 'device/sensordata'",
      },
    });
    // this.table.applyRemovalPolicy(RemovalPolicy.RETAIN);
  }
}
