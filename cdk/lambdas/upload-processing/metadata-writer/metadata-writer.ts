import {
  Context,
  S3Event,
  S3EventRecord,
  SNSMessage,
  SQSEvent,
  SQSRecord,
} from "aws-lambda";
import { DynamoDB } from "aws-sdk";
import {
  PutItemInput,
  PutItemInputAttributeMap,
} from "aws-sdk/clients/dynamodb";

const dynamoDB = new DynamoDB();
const tableName = process.env.TABLE_NAME;

export const handler = async (event: SQSEvent, context: Context) => {
  console.log("event", JSON.stringify(event));
  const keys: Set<string> = event?.Records?.map(
    (record: SQSRecord): SNSMessage => JSON.parse(record.body) as SNSMessage
  )
    ?.map(
      (snsMessage: SNSMessage): S3Event =>
        JSON.parse(snsMessage.Message) as S3Event
    )
    ?.flatMap((s3Event: S3Event): S3EventRecord[] => {
      return s3Event.Records;
    })
    ?.filter((record: S3EventRecord) =>
      record.s3.object.key.includes("/photos/video")
    )
    ?.reduce((keySet: Set<string>, record: S3EventRecord) => {
      const key = decodeURIComponent(record.s3.object.key);
      const newKey = key.split("/").slice(0, -1).join("/");
      keySet.add(newKey);
      return keySet;
    }, new Set<string>());
  await writeDevicesCollection(keys);
};

async function writeDevicesCollection(keys: Set<string>) {
    for (const key of keys) {
        console.log(key);
        const [deviceId, _, cameraId] = key.split("/")
        const created = new Date().getTime().toString();
        const basicDeviceAttributes = <PutItemInputAttributeMap> {
            id: { S: deviceId },
            created: { N: created }
        }
        await writeItem({
            pk: { S: "devices" },
            sk: { S: `devices#${deviceId}` },
            ...basicDeviceAttributes
        });
        await writeItem({
            pk: { S: `devices#${deviceId}` },
            sk: { S: `metadata#${deviceId}` },
            ...basicDeviceAttributes
        })
        await writeItem({
            pk: { S: `devices#${deviceId}` },
            sk: { S: `cameras#${cameraId}` },
            id: { S: cameraId },
            deviceId: { S: deviceId },
            created: { N: created }
        })
        console.log("done writing to dynamodb");
    }
}

async function writeItem(item: PutItemInputAttributeMap) {
  try {
    const response = await dynamoDB
      .putItem(<PutItemInput>{
        TableName: tableName,
        Item: item,
        ConditionExpression: "attribute_not_exists(pk) AND attribute_not_exists(sk)"
      })
      .promise();
    console.log(response);
  } catch (error) {
    console.log("failed to write to dynamodb", error);
  }
}
