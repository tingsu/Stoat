#!/usr/bin/python

from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# check whether the resourceId exists
if d(resourceId=sys.argv[2]).exists:
	d(resourceId=sys.argv[2]).click()







