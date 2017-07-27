#!/usr/bin/python

from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# check whether the text exists
if d(text=sys.argv[2]).exists:
	d(text=sys.argv[2]).click()







