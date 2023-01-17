# PI bootstrap

```
> sudo apt-get update
> sudo apt-get install ansible git python3-pip
> ansible-pull -U https://github.com/monkey-codes/hydroponics.git -i bootstrap-pi/hosts bootstrap-pi/local.yml
```

After bootstrap, sudo to allow members of sudo group to sudo without
password.
```
> EDITOR=vi visudo
```
change `%sudo   ALL=(ALL:ALL) ALL` to `%sudo   ALL=(ALL:ALL) NOPASSWD: ALL`

# View service logs
```
> journalctl -u github-webhook-receiver -f
# OR
> journalctl -u upload -f
```

# Restart service

```
> systemctl restart upload.service
```

Copy the certificate files (hydropi.cert.pem, hydropi.private.key, root-CA.crt) for AWS IOT to the device under /home/hydro/

To find the endpoint url:

```
> aws iot describe-endpoint --endpoint-type iot:Data-ATS
```

## Trigger upload
```
> echo "/tmp/test:logs/blah" | socat - UNIX-CONNECT:/home/hydro/upload.sock
```
# Testing using VirtualBox/Vagrant

## Create a virtual webcam
```
> sudo apt-get -y install v4l2loopback-dkms
> sudo modprobe v4l2loopback devices=1 video_nr=1 card_label='MyWebCam'
#/dev/video1
```

## Stream static image to virtual webcam using ffmpeg
```
> sudo apt-get -y install ffmpeg
> ffmpeg -loop 1 -re -i ./test.jpg -f v4l2 -pix_fmt yuv420p /dev/video1
```


## Useful commands

* `npm run build`   compile typescript to js
* `npm run watch`   watch for changes and compile
* `npm run test`    perform the jest unit tests
* `cdk deploy`      deploy this stack to your default AWS account/region
* `cdk diff`        compare deployed stack with current state
* `cdk synth`       emits the synthesized CloudFormation template
