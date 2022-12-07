#!/bin/bash
flock --verbose /home/hydro/build.lock ansible-pull -U https://github.com/monkey-codes/hydroponics.git -i bootstrap-pi/hosts bootstrap-pi/local.yml
