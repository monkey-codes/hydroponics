#!/bin/bash

DEVICE_ID=`cat /sys/class/net/eth0/address`
MIN_FILE_SIZE=100k

for CAMERA in /dev/video*; do
	CAMERA_ID="${CAMERA/\/dev\//}"
	mkdir -p $CAMERA_ID
	FILE_NAME=$(date +"%Y-%m-%dT%H:%M:%S%z").jpg
	FILE_PATH=`pwd`/$CAMERA_ID/$FILE_NAME
	fswebcam -r 1920x1080 -d $CAMERA --skip 20 --set "Focus, Auto"=False --set "Focus (absolute)"=10 --set "Brightness"=0 $FILE_PATH

	find ./$CAMERA_ID -name "*.jpg" -type f -size $MIN_FILE_SIZE -delete
	if test -f "$FILE_PATH"; then
        UPLOAD_PATH=$DEVICE_ID/photos/$CAMERA_ID/$FILE_NAME
	    echo "uploading $FILE_PATH to $UPLOAD_PATH"
   	    echo "$FILE_PATH|$UPLOAD_PATH" | socat - UNIX-CONNECT:/home/hydro/upload.sock
	fi
done