import {
  APIGatewayProxyEventV2,
  APIGatewayProxyResultV2,
  Context,
} from "aws-lambda";
import { DocumentClient } from "aws-sdk/clients/dynamodb";
import { query } from "./util";
const documentClient = new DocumentClient();
const tableName = process.env.TABLE_NAME;

export const handler = async (
  event: APIGatewayProxyEventV2,
  context: Context
): Promise<APIGatewayProxyResultV2> => {
  console.log("event", event);
  const deviceId = event.pathParameters?.["deviceId"];
  return await query(
    documentClient,
    `devices#${deviceId}`,
    `metadata#${deviceId}`,
    true
  );
};
