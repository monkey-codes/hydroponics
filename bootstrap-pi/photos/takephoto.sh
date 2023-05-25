#!/bin/bash

DEVICE_ID=`cat /sys/class/net/eth0/address`
MIN_FILE_SIZE=-500k

for CAMERA in /dev/video*; do
	CAMERA_ID="${CAMERA/\/dev\//}"
	mkdir -p $CAMERA_ID
	FILE_NAME=$(date +"%Y-%m-%dT%H:%M:%S%z").jpg
	FILE_DIR=`pwd`/$CAMERA_ID
	FILE_PATH=$FILE_DIR/$FILE_NAME
	fswebcam -r 1920x1080 -d $CAMERA --flip v --skip 20 --set "Gamma"=100 --set "Hue"=0 --set "Sharpness"=2 --set "Saturation"=64 --set "White Balance Temperature"=6500 --set "Focus, Auto"=False --set "Focus (absolute)"=0 --set "Brightness"=-60 $FILE_PATH
	#fswebcam -r 1920x1080 -d $CAMERA --skip 20 --set "Focus, Auto"=False --set "Focus (absolute)"=10 --set "Brightness"=0 $FILE_PATH

	find $FILE_DIR -name "*.jpg" -type f -size $MIN_FILE_SIZE -delete
	if test -f "$FILE_PATH"; then
        UPLOAD_PATH=$DEVICE_ID/photos/$CAMERA_ID/$FILE_NAME
	    echo "uploading $FILE_PATH to $UPLOAD_PATH"
   	    echo "$FILE_PATH|$UPLOAD_PATH" | socat - UNIX-CONNECT:/home/hydro/upload.sock
		sleep 15
	fi
done