import subprocess

def install_apk(apk_path, device):
	subprocess.check_output(['adb', '-s', device.serial, 'install', apk_path])

def uninstall_apk(package_name, device):
	subprocess.check_output(['adb', '-s', device.serial, 'uninstall', package_name])