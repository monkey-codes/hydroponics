import { APIGatewayProxyResultV2 } from "aws-lambda";
import { DynamoDB } from "aws-sdk";
import { DocumentClient } from "aws-sdk/clients/dynamodb";
const tableName = process.env.TABLE_NAME;
export const query = async (
  documentClient: DocumentClient,
  pk: string,
  sk: string,
  expectOne: boolean = false
): Promise<APIGatewayProxyResultV2> => {
  try {
    const result = await documentClient
      .query(<DynamoDB.Types.QueryInput>{
        TableName: tableName,
        KeyConditionExpression: `#pk = :pk and begins_with(#sk, :sk)`,
        ExpressionAttributeNames: {
          "#pk": "pk",
          "#sk": "sk",
        },
        ExpressionAttributeValues: {
          ":pk": pk,
          ":sk": sk,
        },
      })
      .promise();

    if (expectOne) {
      if (result.Count == 0) {
        return {
          body: "Not found",
          statusCode: 404,
        };
      }
      return {
        body: JSON.stringify(result.Items?.slice(0, 1).map(mapToApiOutput)[0]),
        statusCode: 200,
      };
    }
    return {
      body: JSON.stringify(result.Items?.map(mapToApiOutput)),
      statusCode: 200,
    };
  } catch (error) {
    return {
      body: JSON.stringify({ message: error }),
      statusCode: 500,
    };
  }
};

function mapToApiOutput(item: DynamoDB.AttributeMap): any {
  return item;
}
