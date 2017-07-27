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

def main(options, args):

	if options.emulator is None or options.file is None or options.policy is None:
		sys.exit()#please confirm the parameters are correct')

	emulator = options.emulator
	apk_path = options.file
	policy = options.policy

	device = Device(emulator)

	if options.install:
		stevedore.install_apk(apk_path, device)

	if not os.path.isdir(LOGS):
		os.mkdir(LOGS)

	sha256 = hashlib.sha256()
	sha256.update(open(apk_path).read())
	sha256sum = sha256.hexdigest()

	log_file = os.path.join(LOGS, sha256sum+'.log')

	if not os.path.isfile(log_file):
		#print 'writing log file into %s' % log_file
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
		services = apk.get_services()
		receivers = []
		for rec in apk.get_receivers():
			if not filtered(rec, FILTERED_BROADCAST):
				receivers.append(rec)
		
		package_name = apk.get_package()

		#write to log file
		with open(log_file, 'w') as fout:
			fout.write('package_name='+package_name+'\n')
			fout.write('activity='+' '.join(activities)+'\n')
			fout.write('service='+ ' '.join(services)+'\n')
			fout.write('receiver='+' '.join(receivers)+'\n')
	else:
		#print 'reading log file from %s' % log_file
		with open(log_file, 'r') as fin:
			for line in fin.readlines():
				if line.startswith('package_name='):
					package_name = line.replace('package_name=', '')
				elif line.startswith('activity='):
					activities = line.replace('activity=', '').split()
				elif line.startswith('service='):
					services = line.replace('service=', '').split()
				elif line.startswith('receiver='):
					receivers = line.replace('receiver=', '').split()
		

	if policy == 'random':
		policy = ['broadcast', 'listener', 'launcher', 'service', 'receiver'][random.randint(0, 4)]

	if policy == 'broadcast':
		actions = IntentEvent.get_intent_actions()
		if len(actions) > 0:
			selected = actions[random.randint(0, len(actions)-1)]
			intent = IntentEvent(action=selected, package_name=package_name)
			intent.execute(device)
	elif policy == 'listener':
		if emulator.startswith('emulator'):
			listener_type = ['sms', 'gsm', 'rotate', 'volume'][random.randint(0, 3)]
			listener = ListenerEvent(listener_type)
			listener.execute(device)
		else:
			listener_type = ['sms', 'rotate', 'volume'][random.randint(0, 2)]
			if listener_type == 'sms':
				intent = IntentEvent(action='android.provider.Telephony.SMS_RECEIVED') #, package_name=package_name)
				intent.execute(device)
			else:
				listener = ListenerEvent(listener_type)
				listener.execute(device)
	elif policy == 'launcher':
		if len(activities) > 0:
			selected = activities[random.randint(0, len(activities)-1)]
			launcher = LauncherEvent(package_name, selected)
			launcher.execute(device)
	elif policy == 'service':
		
		if len(services) > 0:
			selected = services[random.randint(0, len(services)-1)]
			service = ServiceEvent(package_name, selected)
			service.execute(device)
		
			if random.randint(0, 1) == 1:
				import time
				time.sleep(3)
				service = ServiceEvent(package_name=package_name, service_name=selected, stop=True)
				service.execute(device)

	elif policy == 'receiver':
		if len(receivers) > 0:
			selected = receivers[random.randint(0, len(receivers)-1)]
			receiver = ReceiverEvent(package_name, selected)
			receiver.execute(device)

	#actions = IntentEvent.get_intent_actions()
	#for action in actions:
	#	print 'Action: ', action
	#	intent = IntentEvent(action=action)
	#	intent.execute(device)


	#try:
	#	apk = APK(apk_path)
	#except Exception as ex:
	#	return False, ex

	#if not apk.is_valid_APK():
	#	return False, 'it is not a valid apk'

	#stevedore.install_apk(apk_path, device)

	#package_name = apk.get_package()

	#for act in apk.get_activities():
	#	launcher = LauncherEvent(package_name, act)
	#	launcher.execute(device)


if __name__ == '__main__':

	parser = OptionParser()
	parser.add_option('-i', '--install', action='store_true', dest='install', help='If install the apk', default=False)
	parser.add_option('-s', '--emulator', dest='emulator', help='Specify the device to simulate')
	parser.add_option('-f', '--file', dest='file', help='Specify the file path of the apk')
	parser.add_option('-p', '--policy', dest='policy', choices=['broadcast', 'listener', 'launcher', 'service', 'receiver', 'random'], help='Specify the policy for generating sys events (broadcast, listener, launcher, service, receiver or random)\t\n\tbroadcast - general system broadcast\n\tlistener - events of incoming sms and call\n\tlauncher - start activities in the app\n\tservice - start service or stop service in the app\n\treceiver - send broadcast message defined in the app\n\trandom - select a random one')

	options, args = parser.parse_args()

	main(options, args)
