import {
  APIGatewayProxyEventV2,
  APIGatewayProxyResultV2,
  Context,
} from "aws-lambda";
import { TimestreamQuery } from "aws-sdk";
import { QueryRequest, QueryResponse } from "aws-sdk/clients/timestreamquery";

const queryClient = new TimestreamQuery();

const databaseName = process.env.TS_DATABASE_NAME;
const tableName = process.env.TS_TABLE_NAME;
const measureValueLookup: { [key: string]: string } = {
  disk_usage_free_mb: "measure_value::bigint",
  disk_usage_used_mb: "measure_value::bigint",
  webcam_count: "measure_value::bigint",
};
export const handler = async (
  event: APIGatewayProxyEventV2,
  context: Context
): Promise<APIGatewayProxyResultV2> => {
  console.log("event", event);
  const deviceId = event.pathParameters?.["deviceId"];
  const aggFn = event.pathParameters?.["aggregateFn"];
  const measureName = event.pathParameters?.["measureName"]?.replace(
    /\-/g,
    "_"
  );
  const binTime = event.queryStringParameters?.["bin"] || "10m";
  const since = event.queryStringParameters?.["since"] || "60m";
  const measureValue =
    measureValueLookup[measureName!] || "measure_value::double";
  //TODO this is probably vulnerable to sql injection, find a way to properly parameterize these values instead
  // of using string interpolation
  const query = `
    SELECT BIN(time, ${binTime}) AS ts, ROUND(${aggFn?.toUpperCase()}(${measureValue}), 2) AS v FROM "${databaseName}"."${tableName}" 
    WHERE measure_name = '${measureName}'
    AND device_id = '${deviceId}'
    AND time between ago(${since}) and now() 
    GROUP BY BIN(time, ${binTime})
    ORDER BY ts ASC
    `;
  try {
    const result = await getAllRows(query);

    return {
      body: JSON.stringify({
        deviceId,
        aggFn,
        measureName,
        binTime,
        since,
        data: [...result],
      }),
      statusCode: 200,
    };
  } catch (err) {
    return {
      body: JSON.stringify({ err, query }),
      statusCode: 500,
    };
  }
};

interface Record {
  ts: number; //timestamp
  v: number; // value
}

async function getAllRows(
  query: string,
  nextToken: string | undefined = undefined
) {
  const result = new Array<Record>();
  let response;
  try {
    response = await queryClient
      .query(<QueryRequest>{
        QueryString: query,
        NextToken: nextToken,
      })
      .promise();
  } catch (err) {
    console.error("Error while querying:", err);
    throw err;
  }

  result.push(...parseQueryResult(response));
  if (response.NextToken) {
    result.push(...(await getAllRows(query, response.NextToken)));
  }
  return result;
}

function parseQueryResult(response: QueryResponse): Array<Record> {
  const queryStatus = response.QueryStatus;
  console.log("Current query status: " + JSON.stringify(queryStatus));

  const columnInfo = response.ColumnInfo;
  const rows = response.Rows;

  console.log("Metadata: " + JSON.stringify(columnInfo));
  console.log("Data: ");

  return rows.map((row) => parseRow(columnInfo, row));
}

function parseRow(
  columnInfo: TimestreamQuery.ColumnInfoList,
  row: TimestreamQuery.Row
): Record {
  const data = row.Data;
  return {
    ts: new Date(parseDatum(columnInfo[0], data[0])).getTime(),
    v: +parseDatum(columnInfo[1], data[1]),
  };
}

function parseDatum(
  info: TimestreamQuery.ColumnInfo,
  datum: TimestreamQuery.Datum
): any {
  if (datum.NullValue != null && datum.NullValue === true) {
    return `${info.Name}=NULL`;
  }

  const columnType = info.Type;

  // If the column is of TimeSeries Type
  if (columnType.TimeSeriesMeasureValueColumnInfo != null) {
    return parseTimeSeries(info, datum);
  }
  // If the column is of Array Type
  else if (columnType.ArrayColumnInfo != null) {
    const arrayValues = datum.ArrayValue;
    return `${info.Name}=${parseArray(info.Type.ArrayColumnInfo, arrayValues)}`;
  }
  // If the column is of Row Type
  else if (columnType.RowColumnInfo != null) {
    const rowColumnInfo = info.Type.RowColumnInfo;
    const rowValues = datum.RowValue;
    return parseRow(rowColumnInfo!, rowValues!);
  }
  // If the column is of Scalar Type
  else {
    return parseScalarType(info, datum);
  }
}

function parseTimeSeries(
  info: TimestreamQuery.ColumnInfo,
  datum: TimestreamQuery.Datum
): any {
  const timeSeriesOutput: string[] = [];
  datum.TimeSeriesValue?.forEach(function (dataPoint) {
    timeSeriesOutput.push(
      `{time=${dataPoint.Time}, value=${parseDatum(
        info.Type.TimeSeriesMeasureValueColumnInfo!,
        dataPoint.Value
      )}}`
    );
  });

  return `[${timeSeriesOutput.join(", ")}]`;
}

function parseArray(
  arrayColumnInfo: TimestreamQuery.ColumnInfo | undefined,
  arrayValues: TimestreamQuery.DatumList | undefined
) {
  const arrayOutput: any[] = [];
  arrayValues?.forEach(function (datum) {
    arrayOutput.push(parseDatum(arrayColumnInfo!, datum));
  });
  return `[${arrayOutput.join(", ")}]`;
}

function parseScalarType(
  info: TimestreamQuery.ColumnInfo,
  datum: TimestreamQuery.Datum
): any {
  // return parseColumnName(info) + datum.ScalarValue;
  return datum.ScalarValue;
}

function parseColumnName(info: TimestreamQuery.ColumnInfo) {
  return info.Name == null ? "" : `${info.Name}=`;
}
