    # get the current focused package name
    def get_current_package_name()
        
	puts "in get_current_package_name"
        dump_package_and_activity_cmd = "python /home/suting/proj/fsmdroid/a3e/bin/events/device.py emulator-5554"
	puts "in get_current_package_name ---- "
        package_name_re = /('currentPackageName':\su')(.*)'/
        lines = `#{dump_package_and_activity_cmd}`
	puts "in get_current_package_name *** "

        pkg = lines.match(package_name_re)
        puts "after match: pkg[0]:#{pkg[0]},pkg[1]:#{pkg[1]},pkg[2]:#{pkg[2]},pkg[3]:#{pkg[3]}"
        current_package = pkg[2] # get the current focused package
	puts "after current package "
        puts "[D] the current package name: #{current_package}#"
        return current_package
    end


get_current_package_name()
