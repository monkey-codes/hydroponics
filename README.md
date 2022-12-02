# PI bootstrap

```
$ sudo apt-get update
$ sudo apt-get install ansible git python3-pip
$ ansible-pull -U https://github.com/monkey-codes/hydroponics.git -i bootstrap-pi/hosts bootstrap-pi/local.yml
```

Copy the certificate files for AWS IOT to the device under /home/hydro/

