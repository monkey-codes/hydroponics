import { HttpApi, HttpMethod } from "@aws-cdk/aws-apigatewayv2-alpha";
import { HttpJwtAuthorizer } from "@aws-cdk/aws-apigatewayv2-authorizers-alpha";
import { HttpLambdaIntegration } from "@aws-cdk/aws-apigatewayv2-integrations-alpha";
import * as cdk from "aws-cdk-lib";
import { UserPool } from "aws-cdk-lib/aws-cognito";
import { Table } from "aws-cdk-lib/aws-dynamodb";
import { Effect, PolicyStatement } from "aws-cdk-lib/aws-iam";
import * as lambda from "aws-cdk-lib/aws-lambda";
import { RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";

interface APIStackProps extends cdk.StackProps {
  dynamoDBTable: Table;
  uploadBucketName: string;
  uploadBucketArn: string;
  timestreamTableArn: string;
  timestreamDatabaseName: string;
  timestreamTableName: string;
}

// import * as sqs from 'aws-cdk-lib/aws-sqs';
export class APIStack extends cdk.Stack {
  httpApi: HttpApi;
  jwtAuthorizer: HttpJwtAuthorizer;
  // tableName: string;
  table: cdk.aws_dynamodb.Table;
  constructor(scope: Construct, id: string, props: APIStackProps) {
    super(scope, id, props);
    const userPool = new UserPool(this, "api-users", {
      userPoolName: "api-users",
      selfSignUpEnabled: false,
      signInCaseSensitive: true,
    });

    const appClient = userPool.addClient("app-client", {
      userPoolClientName: "app-client",
      authFlows: {
        userPassword: true,
      },
      oAuth: {
        flows: {
          implicitCodeGrant: true,
        },
      },
    });

    const httpApi = new HttpApi(this, "HttpApi");
    const jwtAuthorizer = new HttpJwtAuthorizer(
      "ApiAuthorizer",
      userPool.userPoolProviderUrl,
      {
        jwtAudience: [appClient.userPoolClientId],
      }
    );

    this.httpApi = httpApi;
    this.jwtAuthorizer = jwtAuthorizer;
    this.table = props.dynamoDBTable;
    this.addGetRoute(this, "/hello", "hello.handler");
    this.addGetRoute(this, "/devices", "get-devices.handler");
    this.addGetRoute(this, "/devices/{deviceId}", "get-device.handler");
    this.addGetRoute(
      this,
      "/devices/{deviceId}/cameras",
      "get-cameras.handler"
    );
    this.addGetRoute(
      this,
      "/devices/{deviceId}/sensors/{aggregateFn}/{measureName}",
      "get-sensor-data.handler",
      this.configureSensorApiLambda(props)
    );
    this.addTimelapseDownloadRoute(this, props);
  }



  addGetRoute(
    scope: Construct,
    path: string,
    handler: string,
    callback: (fn: lambda.Function) => void = (fn) => {}
  ) {
    const id = handler.replace(".handler", "");
    const routeLambda = new lambda.Function(scope, `api-${id}`, {
      code: new lambda.AssetCode("lambdas/api"),
      handler: handler,
      runtime: lambda.Runtime.NODEJS_14_X,
      logRetention: RetentionDays.ONE_DAY,
      environment: {
        TABLE_NAME: this.table.tableName,
      },
    });
    const routeIntegration = new HttpLambdaIntegration(
      `${id}-integration`,
      routeLambda
    );
    this.httpApi.addRoutes({
      path: path,
      methods: [HttpMethod.GET],
      integration: routeIntegration,
      authorizer: this.jwtAuthorizer,
      authorizationScopes: ["aws.cognito.signin.user.admin"],
    });
    this.table.grantReadData(routeLambda);
    callback(routeLambda);
  }

  addTimelapseDownloadRoute(scope: Construct, props: APIStackProps) {
    const id = "timelapse-download";
    const routeLambda = new lambda.Function(scope, `api-id`, {
      code: new lambda.AssetCode("lambdas/api"),
      handler: "post-latest-timelapse-download-request.handler",
      runtime: lambda.Runtime.NODEJS_14_X,
      logRetention: RetentionDays.ONE_DAY,
      environment: {
        BUCKET_NAME: props.uploadBucketName,
      },
    });
    routeLambda.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: ["s3:GetObject"],
        resources: [`${props.uploadBucketArn}/*`],
      })
    );
    const routeIntegration = new HttpLambdaIntegration(
      `${id}-integration`,
      routeLambda
    );
    this.httpApi.addRoutes({
      path: "/devices/{deviceId}/cameras/{cameraId}/latest-timelapse-download-request",
      methods: [HttpMethod.POST],
      integration: routeIntegration,
      authorizer: this.jwtAuthorizer,
      authorizationScopes: ["aws.cognito.signin.user.admin"],
    });
  }

  private configureSensorApiLambda(props: APIStackProps): ((fn: lambda.Function) => void) | undefined {
    return (fn) => {
      fn.addToRolePolicy(
        new PolicyStatement({
          effect: Effect.ALLOW,
          actions: ["timestream:Select"],
          resources: [props.timestreamTableArn],
        })
      );
      fn.addToRolePolicy(
        new PolicyStatement({
          effect: Effect.ALLOW,
          actions: ["timestream:DescribeEndpoints"],
          resources: ["*"],
        })
      );
      fn.addEnvironment("TS_DATABASE_NAME", props.timestreamDatabaseName);
      fn.addEnvironment("TS_TABLE_NAME", props.timestreamTableName);
    };
  }
}
