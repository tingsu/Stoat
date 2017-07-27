#!/usr/bin/python

from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# dump ui xml
d.dump(sys.argv[2],False)









