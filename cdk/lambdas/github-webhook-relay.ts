import {
  APIGatewayProxyEventV2,
  APIGatewayProxyResultV2,
  Context,
} from "aws-lambda";
import { IotData } from "aws-sdk";

export const handler = async (
  event: APIGatewayProxyEventV2,
  context: Context
): Promise<APIGatewayProxyResultV2> => {
  console.log("event", event);
  const iotData = new IotData({
    endpoint: "a1jt3uh16ux115-ats.iot.ap-southeast-2.amazonaws.com",
  });
  const response = await iotData
    .publish({
      topic: "webhooks/github/events",
      payload: event.body,
    })
    .promise();

  console.log("response", response);
  return {
    body: JSON.stringify({ message: "Successful lambda invocation" }),
    statusCode: 200,
  };
};
