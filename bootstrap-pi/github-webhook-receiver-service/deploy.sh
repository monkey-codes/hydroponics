#!/bin/bash
ansible-pull -U https://github.com/monkey-codes/hydroponics.git -i bootstrap-pi/hosts bootstrap-pi/local.yml 2>&1 | tee /tmp/last-deploy