#!/usr/bin/python

from uiautomator import Device
import sys
import random

# get the target device
d = Device(sys.argv[1])

inputs = ["test", "12"] 

# edit text
if d(resourceId=sys.argv[2]).exists:
	d(resourceId=sys.argv[2]).set_text(inputs[random.randint(0,1)])









