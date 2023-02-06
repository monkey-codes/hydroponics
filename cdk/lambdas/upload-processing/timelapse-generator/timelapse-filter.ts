import { Context, S3Event, S3EventRecord, SNSMessage } from "aws-lambda";
import { SQS } from "aws-sdk";
import { Queue } from "aws-cdk-lib/aws-sqs";
import {
  DeleteMessageRequest,
  Message,
  MessageList,
  ReceiveMessageRequest,
  SendMessageRequest,
  SendMessageResult,
} from "aws-sdk/clients/sqs";

const sourceSqsQueueUrl = process.env.SOURCE_SQS_QUEUE_URL;
const targetSqsQueueUrl = process.env.TARGET_SQS_QUEUE_URL;
const sqs = new SQS();

export const handler = async (event: any, context: Context) => {
  console.log("event", JSON.stringify(event));
  const allMessages = await readAllMessageFromSourceQueue();
  const timelapsePrefixes = generateTimelapseS3KeyPrefixes(allMessages);
  await writeToTargetQueue(timelapsePrefixes);
  await deleteMessagesFromSourceQueue(allMessages);
};
async function readAllMessageFromSourceQueue(): Promise<SQS.Message[]> {
  let allMessages = new Array<Message>();
  let messages: MessageList | undefined;
  do {
    console.log("executing do while");
    messages = (
      await sqs
        .receiveMessage(<ReceiveMessageRequest>{
          QueueUrl: sourceSqsQueueUrl,
          MaxNumberOfMessages: 10,
          VisibilityTimeout: 120,
          WaitTimeSeconds: 5,
        })
        .promise()
    ).Messages;
    if (messages) {
      allMessages.push(...messages);
    }
  } while (messages && messages?.length > 0);
  console.log("finished do while");
  return allMessages;
}

function generateTimelapseS3KeyPrefixes(
  allMessages: SQS.Message[]
): Set<string> {
  return allMessages
    .filter((msg) => msg.Body)
    .map((msg) => JSON.parse(msg.Body!) as SNSMessage)
    .map((msg): S3Event => JSON.parse(msg.Message) as S3Event)
    .flatMap((msg) => msg.Records)
    .filter(
      //destructuring s3Event argument
      ({
        s3: {
          object: { key },
        },
      }) => key.includes("/photos/video") && key.includes(".jpg")
    )
    .reduce((keySet: Set<string>, record: S3EventRecord) => {
      const key = decodeURIComponent(record.s3.object.key);
      const newKey = key.split("/").slice(0, -1).join("/");
      keySet.add(newKey);
      return keySet;
    }, new Set<string>());
}

async function writeToTargetQueue(
  timelapsePrefixes: Set<string>
): Promise<SendMessageResult[]> {
  const results = new Array<SendMessageResult>();
  for (const prefix of timelapsePrefixes) {
    const response = await sqs
      .sendMessage(<SendMessageRequest>{
        QueueUrl: targetSqsQueueUrl,
        MessageBody: JSON.stringify({ prefix }),
      })
      .promise();
    console.log(
      `Wrote ${prefix} to target queue messageId: ${response.MessageId}`
    );
    results.push(response);
  }
  return results;
}

async function deleteMessagesFromSourceQueue(allMessages: SQS.Message[]) {
  for (const message of allMessages) {
    const response = await sqs
      .deleteMessage(<DeleteMessageRequest>{
        QueueUrl: sourceSqsQueueUrl,
        ReceiptHandle: message.ReceiptHandle,
      })
      .promise();
    console.log("Deleted message from sourceQueue", message.MessageId);
  }
}
