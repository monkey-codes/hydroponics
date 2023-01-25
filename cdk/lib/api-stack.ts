import { HttpApi, HttpMethod } from "@aws-cdk/aws-apigatewayv2-alpha";
import { HttpJwtAuthorizer } from "@aws-cdk/aws-apigatewayv2-authorizers-alpha";
import { HttpLambdaIntegration } from "@aws-cdk/aws-apigatewayv2-integrations-alpha";
import * as cdk from "aws-cdk-lib";
import { UserPool } from "aws-cdk-lib/aws-cognito";
import * as lambda from "aws-cdk-lib/aws-lambda";
import { RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";

// import * as sqs from 'aws-cdk-lib/aws-sqs';
export class APIStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
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
  }
}
