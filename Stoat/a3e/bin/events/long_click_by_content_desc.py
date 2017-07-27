#!/usr/bin/python

from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# check whether the description exists
if d(description=sys.argv[2]).exists:
	d(description=sys.argv[2]).long_click()







