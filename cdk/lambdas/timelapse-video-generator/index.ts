import { Context } from "aws-lambda";
import * as Ffmpeg from "fluent-ffmpeg";
import { S3 } from "aws-sdk";
import {
  GetObjectRequest,
  ListObjectsRequest,
  PutObjectRequest,
} from "aws-sdk/clients/s3";
import { promisify } from "util";
import { writeFile, readdirSync, createReadStream } from "fs";

export const handler = async (
  event: { key: string; file: string },
  context: Context
) => {
  const bucketName = process.env.BUCKET_NAME;
  console.log("Lambda triggered", bucketName);
  const s3 = new S3();
  await downloadImages(s3, <ListObjectsRequest>{
    Bucket: bucketName,
    Prefix: "b8:27:eb:66:03:0b/photos/video0/",
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
      Key: `b8:27:eb:66:03:0b/photos/video0/video.mp4`,
      Body: createReadStream("/tmp/video.mp4"),
    })
    .promise();
  console.log("Lambda done");
};

function ffmpegSync(): Promise<{}> {
  return new Promise((resolve, reject) => {
    Ffmpeg("/tmp/*.jpg")
      .addInputOption("-y", "-pattern_type", "glob")
      .videoCodec("libx264")
      .videoBitrate("1024k")
      .size("1920x1080")
      .addOutputOption("-pix_fmt", "yuv420p")
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
