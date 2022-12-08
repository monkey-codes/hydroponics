import { Context } from "aws-lambda";
import { S3 } from "aws-sdk";
import { IotData } from "aws-sdk";
export const handler = async (
  event: {key: string, file: string},
  context: Context
) => {
  console.log("Lambda triggered from IOT Rule event", event);
  const bucketName = process.env.BUCKET_NAME;
  const s3 = new S3()
  const url = s3.getSignedUrl('putObject', {
    'Bucket': bucketName,
    'Key': event.key,
    'Expires': 60*15
  })
  console.log("Got presigned url", url.substr(0, 30))
  const iotData = new IotData({
    endpoint: "a1jt3uh16ux115-ats.iot.ap-southeast-2.amazonaws.com",
  });
  const response = await iotData
    .publish({
      topic: "upload/requests",
      payload: JSON.stringify({...event, type: 'response', url }),
    })
    .promise();
  console.log("Publish response", response)
};
