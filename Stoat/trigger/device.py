import subprocess
from sys_event import IntentEvent, LauncherEvent, ListenerEvent, ServiceEvent, ReceiverEvent
from telnet import TelnetConsole
from uiautomator import Device as Dev
import random

class Device:

	def __init__(self, serial=None):
		self.adb = 'adb'
		self.serial = serial
		#self.connect()

	def connect(self):
		self.proc = subprocess.Popen(['adb', 'shell'])
		try:
			if self.serial is None:
				self.proc = subprocess.Popen(['adb', 'shell'])
			else:
				self.proc = subprocess.Popen(['adb', '-s', self.serial, 'shell'])
		except:
			raise Exception('Cannot connect to the emulator')

	def execute(self, sys_event):
		#self.proc.communicate(CmdInterpreter.parse_event(sys_event))
		#args = ['adb', 'shell']
		args = self.parse_event(sys_event)
		filtered_args = [arg.strip() for arg in args]
		if len(filtered_args) > 0:
			print filtered_args
			subprocess.check_output(filtered_args)

	def parse_event(self, sys_event):
		if isinstance(sys_event, IntentEvent):
			args = ['adb', '-s', self.serial, 'shell', 'am', 'broadcast']
			if sys_event.get_action() is not None:
				args.extend(['-a', sys_event.get_action()])
			if sys_event.get_category() is not None:
				args.extend(['-c', sys_event.get_category()])
			if sys_event.get_package_name() is not None:
				args.extend(['-n', sys_event.get_package_name()])
			return args
		elif isinstance(sys_event, LauncherEvent):
			args = ['adb', '-s', self.serial, 'shell', 'am', 'start']
			args.extend(['-n', sys_event.get_package_name().strip()+'/'+sys_event.get_activity_name().strip()])
			return args
		elif isinstance(sys_event, ListenerEvent):

			#print 'listener - ', sys_event.get_listener_type()

			if sys_event.get_listener_type() == 'volume':
				d = Dev(self.serial)
				t = random.randint(0, 2)
				if t == 0:
					d.press.volume_up()
				elif t == 1:
					d.press.volume_down()
				else:
					d.press.volume_mute()
			elif sys_event.get_listener_type() == 'rotate':
				d = Dev(self.serial)
				d.orientation = ['l', 'r', 'n'][random.randint(0,2)]
			else:
				connector = TelnetConsole(self)
				connector.verify_auth()
				connector.check_connectivity()
				args = sys_event.feed_cmd()
				print args
				connector.run_cmd(args)
				post_args = sys_event.post_cmd()
				if len(post_args) > 0:
					import time
					#print 'waiting 3 seconds for the post command'
					time.sleep(3)
					print args
					connector.run_cmd(post_args)
				connector.disconnect()
			return []

		elif isinstance(sys_event, ServiceEvent):
			if sys_event.get_stop():
				args = ['adb', '-s', self.serial, 'shell', 'am', 'stopservice']
			else:
				args = ['adb', '-s', self.serial, 'shell', 'am', 'startservice']
			args.extend([sys_event.get_package_name().strip()+'/'+sys_event.get_service_name().strip()])
			return args
		elif isinstance(sys_event, ReceiverEvent):
			args = ['adb', '-s', self.serial, 'shell', 'am', 'broadcast']
			args.extend(['-n', sys_event.get_package_name().strip()+'/'+sys_event.get_receiver_name().strip()])
			return args


