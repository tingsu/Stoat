# monkeyrunner script to install current klaxon, and run its tests.
# Author: Marc Dougherty <muncus@gmail.com>

import os

from com.android.monkeyrunner import MonkeyDevice, MonkeyRunner

INSTRUMENTATION_RUNNER = 'android.test.InstrumentationTestRunner'

device = MonkeyRunner.waitForConnection()

device.installPackage('bin/Klaxon-debug.apk')
device.installPackage('tests/bin/KlaxonTests-debug.apk')
package = 'org.nerdcircus.android.klaxon'
activity = package + ".KlaxonList"

test_dict = device.instrument("%s/%s" % (package+'.tests', INSTRUMENTATION_RUNNER))

assert 'OK' in test_dict['stream']
print test_dict['stream']

# try to send some smses through adb.
def sendSms(sender="11111", text="blah"):
  os.system("adb emu 'sms send %(sender)s %(text)s'" % {
      'sender': sender,
      'text': text,})

sendSms(text="blah blah blah")

#import socket
#sock = socket.socket()
#sock.connect(('localhost', 5554))
#sock.recv(300) # eat the banner.

#pattern = "sms send 424242 %(msg)s\n"

#for m in ['foo', 'Foo', 'FOO']:
#  sock.send(pattern % {'msg': m})
#  assert 'OK' in sock.recv(10)
