import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { ThingWithCert } from 'cdk-iot-core-certificates';
// import * as sqs from 'aws-cdk-lib/aws-sqs';

export class IOTCoreStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const thingWithCert = new ThingWithCert(this, 'ThingWithCert', {
      thingName: 'hydrorpi',
      saveToParamStore: true,
      paramPrefix: '/devices',
    });
  }
}
