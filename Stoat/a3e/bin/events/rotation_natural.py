#!/usr/bin/python

from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# dump the orientation
#print d.orientation
if d.orientation != "n":
	d.orientation = "n" 










