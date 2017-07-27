INTENT_ACTIONS = '''android.intent.action.AIRPLANE_MODE_CHANGED
android.intent.action.ALL_APPS
android.intent.action.ANSWER
android.intent.action.APPLICATION_RESTRICTIONS_CHANGED
android.intent.action.APP_ERROR
android.intent.action.ASSIST
android.intent.action.ATTACH_DATA
android.intent.action.BATTERY_CHANGED
android.intent.action.BATTERY_LOW
android.intent.action.BATTERY_OKAY
android.intent.action.BOOT_COMPLETED
android.intent.action.BUG_REPORT
android.intent.action.CALL
android.intent.action.CALL_BUTTON
android.intent.action.CAMERA_BUTTON
android.intent.action.CHOOSER
android.intent.action.CLOSE_SYSTEM_DIALOGS
android.intent.action.CONFIGURATION_CHANGED
android.intent.action.CREATE_DOCUMENT
android.intent.action.CREATE_SHORTCUT
android.intent.action.DATE_CHANGED
android.intent.action.DEFAULT
android.intent.action.DELETE
android.intent.action.DEVICE_STORAGE_LOW
android.intent.action.DEVICE_STORAGE_OK
android.intent.action.DIAL
android.intent.action.DOCK_EVENT
android.intent.action.DREAMING_STARTED
android.intent.action.DREAMING_STOPPED
android.intent.action.EDIT
android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE
android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE
android.intent.action.FACTORY_TEST
android.intent.action.GET_CONTENT
android.intent.action.GET_RESTRICTION_ENTRIES
android.intent.action.GTALK_SERVICE_CONNECTED
android.intent.action.GTALK_SERVICE_DISCONNECTED
android.intent.action.HEADSET_PLUG
android.intent.action.INPUT_METHOD_CHANGED
android.intent.action.INSERT
android.intent.action.INSERT_OR_EDIT
android.intent.action.INSTALL_PACKAGE
android.intent.action.LOCALE_CHANGED
android.intent.action.MAIN
android.intent.action.MANAGED_PROFILE_ADDED
android.intent.action.MANAGED_PROFILE_REMOVED
android.intent.action.MANAGE_NETWORK_USAGE
android.intent.action.MANAGE_PACKAGE_STORAGE
android.intent.action.MEDIA_BAD_REMOVAL
android.intent.action.MEDIA_BUTTON
android.intent.action.MEDIA_CHECKING
android.intent.action.MEDIA_EJECT
android.intent.action.MEDIA_MOUNTED
android.intent.action.MEDIA_NOFS
android.intent.action.MEDIA_REMOVED
android.intent.action.MEDIA_SCANNER_FINISHED
android.intent.action.MEDIA_SCANNER_SCAN_FILE
android.intent.action.MEDIA_SCANNER_STARTED
android.intent.action.MEDIA_SHARED
android.intent.action.MEDIA_UNMOUNTABLE
android.intent.action.MEDIA_UNMOUNTED
android.intent.action.MY_PACKAGE_REPLACED
android.intent.action.NEW_OUTGOING_CALL
android.intent.action.OPEN_DOCUMENT
android.intent.action.OPEN_DOCUMENT_TREE
android.intent.action.PACKAGE_ADDED
android.intent.action.PACKAGE_CHANGED
android.intent.action.PACKAGE_DATA_CLEARED
android.intent.action.PACKAGE_FIRST_LAUNCH
android.intent.action.PACKAGE_FULLY_REMOVED
android.intent.action.PACKAGE_INSTALL
android.intent.action.PACKAGE_NEEDS_VERIFICATION
android.intent.action.PACKAGE_REMOVED
android.intent.action.PACKAGE_REPLACED
android.intent.action.PACKAGE_RESTARTED
android.intent.action.PACKAGE_VERIFIED
android.intent.action.PASTE
android.intent.action.PICK
android.intent.action.PICK_ACTIVITY
android.intent.action.POWER_CONNECTED
android.intent.action.POWER_DISCONNECTED
android.intent.action.POWER_USAGE_SUMMARY
android.intent.action.PROVIDER_CHANGED
android.intent.action.QUICK_CLOCK
android.intent.action.REBOOT
android.intent.action.RUN
android.intent.action.SCREEN_OFF
android.intent.action.SCREEN_ON
android.intent.action.SEARCH
android.intent.action.SEARCH_LONG_PRESS
android.intent.action.SEND
android.intent.action.SENDTO
android.intent.action.SEND_MULTIPLE
android.intent.action.SET_WALLPAPER
android.intent.action.SHUTDOWN
android.intent.action.SYNC
android.intent.action.SYSTEM_TUTORIAL
android.intent.action.TIMEZONE_CHANGED
android.intent.action.TIME_CHANGED
android.intent.action.TIME_TICK
android.intent.action.UID_REMOVED
android.intent.action.UMS_CONNECTED
android.intent.action.UMS_DISCONNECTED
android.intent.action.UNINSTALL_PACKAGE
android.intent.action.USER_BACKGROUND
android.intent.action.USER_FOREGROUND
android.intent.action.USER_INITIALIZE
android.intent.action.USER_PRESENT
android.intent.action.VIEW
android.intent.action.VOICE_COMMAND
android.intent.action.WALLPAPER_CHANGED
android.intent.action.WEB_SEARCH
android.provider.Telephony.SMS_RECEIVED
'''.splitlines()

class SysEvent:

	def __init__(self, tag):
		self.tag = 'sys_event'

	def to_string(self):
		return 'system event'


class IntentEvent(SysEvent):

	def __init__(self, action=None, category=None, package_name=None):
		self.tag = 'intent_event'
		self.action = action
		self.category = category
		self.package_name = package_name

	def get_action(self):
		return self.action

	def get_category(self):
		return self.category

	def get_package_name(self):
		return self.package_name

	def execute(self, device):
		device.execute(self)

	@staticmethod
	def get_intent_actions():
		return INTENT_ACTIONS

	def to_string(self):
		return 'intent event'

class LauncherEvent(SysEvent):

	def __init__(self, package_name=None, activity_name=None):
		self.tag = 'launcher event'
		self.package_name = package_name
		self.activity_name = activity_name

	def get_package_name(self):
		return self.package_name

	def get_activity_name(self):
		return self.activity_name

	def execute(self, device):
		device.execute(self)

class ListenerEvent(SysEvent):

	@staticmethod
	def get_possible_cmd():
		cmds = dict()
		cmds['power'] = ['display', 'ac', 'status', 'present', 'health', 'capacity']
		cmds['avd'] = ['stop', 'start', 'status', 'name', 'snapshot']
		cmds['finger'] = ['touch', 'remove']
		cmds['geo'] = ['nmea', 'fix']
		cmds['sms'] = ['send', 'pdu']
		cmds['cdma'] = ['ssource', 'prl_version']
		cmds['gsm'] = ['list', 'call', 'busy', 'hold', 'accept', 'cancel', 'data', 'voice', 'status', 'signal', 'signal-profile']
		cmds['rotate'] = []
		return cmds   

	def __init__(self, listener_type):
		self.listener_type = listener_type

	def get_listener_type(self):
		return self.listener_type

	def feed_cmd(self):
		if self.listener_type == 'sms':
			return ['sms', 'send', '+6510000000', '9527']
		elif self.listener_type == 'gsm':
			return ['gsm', 'call', '+6510000000']
		else:
			return []

	def post_cmd(self):
		if self.listener_type == 'gsm':
			return ['gsm', 'cancel', '+6510000000']
		else:
			return []

	def execute(self, device):
		device.execute(self)

class ServiceEvent(SysEvent):

	def __init__(self, package_name=None, service_name=None, stop=False):
		self.package_name = package_name
		self.service_name = service_name
		self.stop = stop

	def get_package_name(self):
		return self.package_name

	def get_service_name(self):
		return self.service_name

	def get_stop(self):
		return self.stop

	def execute(self, device):
		device.execute(self)

class ReceiverEvent(SysEvent):

	def __init__(self, package_name=None, receiver_name=None):
		self.package_name = package_name
		self.receiver_name = receiver_name

	def get_package_name(self):
		return self.package_name

	def get_receiver_name(self):
		return self.receiver_name

	def execute(self, device):
		device.execute(self)