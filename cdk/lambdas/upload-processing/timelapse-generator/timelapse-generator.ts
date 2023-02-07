import { Context, SQSEvent } from "aws-lambda";
import * as Ffmpeg from "fluent-ffmpeg";
import { S3 } from "aws-sdk";
import {
  GetObjectRequest,
  ListObjectsRequest,
  PutObjectRequest,
} from "aws-sdk/clients/s3";
import { promisify } from "util";
import { writeFile, readdirSync, createReadStream } from "fs";

export const handler = async (event: SQSEvent, context: Context) => {
  console.log(event);
  const bucketName = process.env.BUCKET_NAME;
  console.log("Lambda triggered", bucketName);
  const s3 = new S3();
  for (const record of event.Records) {
    const body: { prefix: string } = JSON.parse(record.body);
    console.log(`Handling ${body.prefix}`);
    await downloadImages(s3, <ListObjectsRequest>{
      Bucket: bucketName,
      Prefix: body.prefix,
    });

    await ffmpegSync();
    // readdirSync("/tmp").forEach((file) => {
    //   if (file.endsWith(".mp4")) {
    //     console.log(file);
    //   }
    // });
    const stream = createReadStream(`/tmp/video.mp4`);
    await s3
      .putObject(<PutObjectRequest>{
        Bucket: bucketName,
        Key: `${body.prefix}/video.mp4`,
        Body: createReadStream("/tmp/video.mp4"),
      })
      .promise();
  }

  console.log("Lambda done");
};

function ffmpegSync(): Promise<{}> {
  //TODO: Test this -> ffmpeg -y -pattern_type glob -i './video0/*.jpg' -vcodec libx264 -b:v 8500k -c:v h264_videotoolbox  -vf "setpts=2.0*PTS" /tmp/video.mp4
  return new Promise((resolve, reject) => {
    Ffmpeg("/tmp/*.jpg")
      .addInputOption("-y", "-pattern_type", "glob")
      .videoCodec("libx264")
      .videoBitrate("1024k")
      .addOutputOption("-pix_fmt", "yuv420p")
      //.addOutputOption("-vf", "setpts=2.0*PTS")
      .addOutputOption("-vf", "scale=640:360")
      .addOutputOption("-r", "30")
      .on("progress", (progress) =>
        console.log(
          `Processing frames: ${progress.frames} currentFps: ${progress.currentFps}` +
            ` currentKbps: ${progress.currentKbps} targetSize: ${progress.targetSize} timemark: ${progress.timemark}`
        )
      )
      .on("error", (err) => reject(err))
      .on("end", () => resolve({}))
      .save("/tmp/video.mp4");
  });
}

async function downloadImages(s3: S3, request: ListObjectsRequest) {
  let isTruncated = true;
  let marker;
  const wf = promisify(writeFile);
  while (isTruncated) {
    let currentRequest: ListObjectsRequest = { ...request, Marker: marker };
    const response = await s3.listObjects(currentRequest).promise();
    const batch = response.Contents?.filter((i) =>
      i.Key?.endsWith(".jpg")
    )?.map((i) =>
      s3
        .getObject(<GetObjectRequest>{ Bucket: request.Bucket, Key: i.Key })
        .promise()
        .then((f) => {
          const imageName = i.Key?.split("/").pop();
          wf(`/tmp/${imageName}`, f.Body);
        })
    );
    if (batch) {
      await Promise.all(batch);
    }
    isTruncated = !!response.IsTruncated;
    if (isTruncated) {
      marker = response.Contents?.slice(-1)[0].Key;
    }
  }
}
// handler({ key: "key", file: "file" }, {} as Context);
