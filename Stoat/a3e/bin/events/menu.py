#!/usr/bin/python

from uiautomator import Device
import sys


# get the target device
d = Device(sys.argv[1])

# press the menu
d.press.menu()









