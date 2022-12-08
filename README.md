# PI bootstrap

```
> sudo apt-get update
> sudo apt-get install ansible git python3-pip
> ansible-pull -U https://github.com/monkey-codes/hydroponics.git -i bootstrap-pi/hosts bootstrap-pi/local.yml
```

Copy the certificate files for AWS IOT to the device under /home/hydro/

To find the endpoint url:

```
> aws iot describe-endpoint --endpoint-type iot:Data-ATS
```

## Useful commands

* `npm run build`   compile typescript to js
* `npm run watch`   watch for changes and compile
* `npm run test`    perform the jest unit tests
* `cdk deploy`      deploy this stack to your default AWS account/region
* `cdk diff`        compare deployed stack with current state
* `cdk synth`       emits the synthesized CloudFormation template
