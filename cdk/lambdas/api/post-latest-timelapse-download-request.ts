import {
  APIGatewayProxyEventV2,
  APIGatewayProxyResultV2,
  Context,
} from "aws-lambda";
import { DynamoDB, S3 } from "aws-sdk";
const dynamoDB = new DynamoDB();
const tableName = process.env.TABLE_NAME;

const bucketName = process.env.BUCKET_NAME;
const s3 = new S3();

export const handler = async (
  event: APIGatewayProxyEventV2,
  context: Context
): Promise<APIGatewayProxyResultV2> => {
  const deviceId = event.pathParameters?.["deviceId"];
  const cameraId = event.pathParameters?.["cameraId"];
  const timelapseDownloadUrl = s3.getSignedUrl("getObject", {
    Bucket: bucketName,
    Key: `${deviceId}/photos/${cameraId}/video.mp4`,
    Expires: 60 * 15,
  });

  return {
    body: JSON.stringify({ deviceId, cameraId, timelapseDownloadUrl }),
    statusCode: 200,
  };
};
