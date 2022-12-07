import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import {ThingWithCert} from 'cdk-iot-core-certificates';
import {ApiGatewayToLambda} from '@aws-solutions-constructs/aws-apigateway-lambda';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import {AuthorizationType, MethodLoggingLevel} from "aws-cdk-lib/aws-apigateway";
import {AnyPrincipal, Effect, PolicyDocument, PolicyStatement} from "aws-cdk-lib/aws-iam";
import {RetentionDays} from "aws-cdk-lib/aws-logs";

// import * as sqs from 'aws-cdk-lib/aws-sqs';

export class IOTCoreStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const thingWithCert = new ThingWithCert(this, 'ThingWithCert', {
      thingName: 'hydrorpi',
      saveToParamStore: true,
      paramPrefix: '/devices',
    });

    const relay = new ApiGatewayToLambda(this, 'GithubWebhookRelay', {
      apiGatewayProps: {
        restApiName: 'GithubWebhookRelay',
        defaultMethodOptions: {
          apiKeyRequired: false,
          authorizationType: AuthorizationType.NONE
        },
        policy: new PolicyDocument({
          statements: [
            new PolicyStatement({
              effect: Effect.ALLOW,
              principals: [new AnyPrincipal()],
              actions: ['execute-api:Invoke'],
              resources: ['execute-api:/*/*/*']
            }),
            new PolicyStatement({
              effect: Effect.DENY,
              principals: [new AnyPrincipal()],
              actions: ['execute-api:Invoke'],
              resources: ['execute-api:/*/*/*'],
              conditions: {
                'NotIpAddress': {
                  'aws:SourceIp': [
                    '210.185.118.77/32',
                    //github webhook ips
                    '192.30.252.0/22',
                    '185.199.108.0/22',
                    '140.82.112.0/20',
                    '143.55.64.0/20',
                    '2a0a:a440::/29',
                    '2606:50c0::/32']
                }
              }
            })
          ]
        }),
        deployOptions: {
          tracingEnabled: false,
          dataTraceEnabled: false,
          loggingLevel: MethodLoggingLevel.OFF

        }
      },
      lambdaFunctionProps: {
        runtime: lambda.Runtime.NODEJS_14_X,
        handler: 'github-webhook-relay.handler',
        code: lambda.Code.fromAsset(`lambdas`),
        logRetention: RetentionDays.ONE_DAY
      },
      logGroupProps: {
        retention: RetentionDays.ONE_DAY
      }
    });
    relay.lambdaFunction.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ['iot:Publish'],
        resources: ['*']
      })
    )
  }
}
