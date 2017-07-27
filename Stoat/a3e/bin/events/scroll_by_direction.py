#!/usr/bin/python

from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# press the menu
if sys.argv[2] == 'up':
	d(scrollable=True).scroll.toBeginning()
elif sys.argv[2] == 'down':
	d(scrollable=True).scroll.toEnd()
else:
	print "error direction?"

