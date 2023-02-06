import { HttpApi, HttpMethod } from "@aws-cdk/aws-apigatewayv2-alpha";
import { HttpJwtAuthorizer } from "@aws-cdk/aws-apigatewayv2-authorizers-alpha";
import { HttpLambdaIntegration } from "@aws-cdk/aws-apigatewayv2-integrations-alpha";
import * as cdk from "aws-cdk-lib";
import { UserPool } from "aws-cdk-lib/aws-cognito";
import { Table } from "aws-cdk-lib/aws-dynamodb";
import * as lambda from "aws-cdk-lib/aws-lambda";
import { RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";

interface APIStackProps extends cdk.StackProps {
  dynamoDBTable: Table;
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

    const helloLambda = new lambda.Function(this, "api-hello", {
      code: new lambda.AssetCode("lambdas/api"),
      handler: "hello.handler",
      runtime: lambda.Runtime.NODEJS_14_X,
      logRetention: RetentionDays.ONE_DAY,
    });

    const helloIntegration = new HttpLambdaIntegration(
      "HelloIntegration",
      helloLambda
    );
    const httpApi = new HttpApi(this, "HttpApi");
    const jwtAuthorizer = new HttpJwtAuthorizer(
      "ApiAuthorizer",
      userPool.userPoolProviderUrl,
      {
        jwtAudience: [appClient.userPoolClientId],
      }
    );
    httpApi.addRoutes({
      path: "/hello",
      methods: [HttpMethod.GET],
      integration: helloIntegration,
      authorizer: jwtAuthorizer,
      authorizationScopes: ["aws.cognito.signin.user.admin"],
    });
    this.httpApi = httpApi;
    this.jwtAuthorizer = jwtAuthorizer;
    this.table = props.dynamoDBTable;
    // this.tableName = props.dynamoDBTable.tableName;
    this.addGetRoute(this, "/devices", "get-devices.handler");
    this.addGetRoute(this, "/devices/{deviceId}", "get-device.handler");
    this.addGetRoute(this, "/devices/{deviceId}/cameras", "get-cameras.handler");
  }

  addGetRoute(
    scope: Construct,
    path: string,
    handler: string
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
  }
}
