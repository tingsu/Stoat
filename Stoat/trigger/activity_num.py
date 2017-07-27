from sys_event import IntentEvent, LauncherEvent, ListenerEvent, ServiceEvent, ReceiverEvent
from device import Device
import sys
import stevedore
from optparse import OptionParser
import random
import os
import hashlib

#sys.path.append('/home/pillar/android/AndroidToolChain/profiling/python')
from androguard.core.bytecodes.apk import APK

FILTERED_BROADCAST = ['com.apposcopy.ella.runtime.BroadcastReceiver', 'EmmaInstrument.SMSInstrumentedReceiver']
FILTERED_ACTIVITY = ['EmmaInstrument.InstrumentedActivity']
LOGS = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'logs')

def filtered(item, filter_list):
	for f in filter_list:
		if item.endswith(f):
			return True
	return False

def main(apk_path):
	try:
		apk = APK(apk_path)
	except Exception as ex:
		return False, ex
	if not apk.is_valid_APK():
		return False, 'It is not a valid apk'


	activities = []
	for act in apk.get_activities():
		if not filtered(act, FILTERED_ACTIVITY):
			activities.append(act)
	#print len(activities)

if __name__ == '__main__':

	main(sys.argv[1])
