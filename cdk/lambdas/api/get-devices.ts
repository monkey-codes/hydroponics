import {
  APIGatewayProxyEventV2,
  APIGatewayProxyResultV2,
  Context,
} from "aws-lambda";
import { DynamoDB } from "aws-sdk";
import { query } from './util';
const dynamoDB = new DynamoDB();
const tableName = process.env.TABLE_NAME;

export const handler = async (
  event: APIGatewayProxyEventV2,
  context: Context
): Promise<APIGatewayProxyResultV2> => {
  console.log("event", event);
  return await query(dynamoDB, "devices", "devices#")

};


