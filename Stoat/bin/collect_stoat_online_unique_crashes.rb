#! /usr/bin/env ruby

def remove_message_head(message_line)

	loc = message_line.index(":")
	if loc == nil then
		puts "message line: #{message_line}"
	end
	length = message_line.length
	return message_line[(loc+1)..(length-1)]

end

# extract the crash stack from the bug report
def extract_crash_stack(crash_sub_dir, package_name)

	# Only one logcat.txt, no worries
	bug_report = `ls #{crash_sub_dir}/*_logcat.txt`.strip
	if not File.exist?(bug_report) then
		return
	end

 	puts "bug_report: #{bug_report}"
	puts "package_name: #{package_name}"

	# before extracting the crash stack, make sure the crash report is not a false positive (i.e., it should at least contain the package name)
	res=`grep #{package_name} #{bug_report}`.strip()
	if res.eql?("") then
		return
	end

	
	# the crash stack file name: xxx_logcat_stack_trace.txt
	stack_trace_file_name = File.dirname(bug_report) + "/" + File.basename(bug_report, ".txt") + "_stack_trace.txt"
	puts "processing stack trace file: #{stack_trace_file_name}"
	open(stack_trace_file_name, 'a'){ |f|
	  File.readlines(bug_report).each do |line|
		# we only focus on these exceptions
		if line.start_with?("E/AndroidRuntime") || line.start_with?("E/SQLiteDatabase") then
			f.puts remove_message_head(line)
		end
	  end
	}
	

end

# collect app crashes from the fsm and mcmc dirs to the crashes dir
def collect_app_crashes(app_dir)

	puts "app_dir: #{app_dir}"

	stoat_fsm_crash_dir = app_dir + "/stoat_fsm_output/crashes"
	stoat_mcmc_crash_dir = app_dir + "/stoat_mcmc_sampling_output/crashes"
	
	`rm -rf #{app_dir}/crashes`
	`mkdir -p #{app_dir}/crashes`

	package_name = get_app_package_name(app_dir)
	
	if package_name == nil  or package_name.eql?("") then
		puts "the package name is *empty*, give up!"
		sleep 1
		`echo #{app_dir} >> /tmp/stoat_new_gradle_projects_untested.txt`
		return
	end
	
	if File.exist?(stoat_fsm_crash_dir) then
		crash_dirs = Dir.entries(stoat_fsm_crash_dir)
		for dir in crash_dirs do
			if (not dir.eql?(".")) && (not dir.eql?("..")) then
				dir_name = dir + ".fsm"
				`cp -r #{stoat_fsm_crash_dir}/#{dir} #{app_dir}/crashes/#{dir_name}`
				extract_crash_stack(app_dir + "/crashes/" + dir_name, package_name)
			end
		end
	end

	if File.exist?(stoat_mcmc_crash_dir) then
		crash_dirs = Dir.entries(stoat_mcmc_crash_dir)
		for dir in crash_dirs do
			if (not dir.eql?(".")) && (not dir.eql?("..")) then
				bug_trace_file = `ls #{stoat_mcmc_crash_dir}/#{dir}/bug_event_trace.txt`.strip
				dir_name = ""
				# the bug trace file should always exist, but we still check its existence here
				if File.exist?(bug_trace_file) then
					res = `grep adb #{bug_trace_file}`
					if res.eql?("") then
						dir_name = dir + ".mcmc_ui"
					else
						dir_name = dir + ".mcmc_sys"
					end
				else
					dir_name = ".mcmc_ui"
				end
				`cp -r #{stoat_mcmc_crash_dir}/#{dir} #{app_dir}/crashes/#{dir_name}`
				extract_crash_stack(app_dir + "/crashes/" + dir_name, package_name)
			end
		end
	end

	total_crashes = `ls #{app_dir}/crashes | wc -l`.strip
	puts "there are total #{total_crashes} crashes for #{app_dir}."
end




# get the app package name from the apk file
def get_app_package_name(app_path)

	apk_path = ""
	if $closed_source == false then
	  if $project_type == "ant" then
	      	apk_path = `ls #{app_path}/bin/*-debug.apk`.strip
	  else # "gradle"
		apk_path = `find #{app_path} -name "*-debug.apk" | head -1`.strip
	  end
	else # closed-source apks
		apk_path = app_path.sub("-output",".apk")
	end
	package_name = `aapt dump badging #{apk_path} | grep package | awk '{print $2}' | sed s/name=//g | sed s/\\'//g`.strip
	
	if package_name.end_with?(".debug") then

		package_name = package_name.chomp(".debug")
	
		`echo #{package_name} > /tmp/stoat.debug.txt`
	end

	return package_name
end

def get_app_version_name(app_path)
	
	apk_path = ""
	if $closed_source == false then
	  if $project_type == "ant" then
		apk_path = `ls #{app_path}/bin/*-debug.apk`.strip
	  else# "gradle"
		apk_path = `find #{app_path} -name "*-debug.apk" | head -1`.strip
	  end
	else
		apk_path = app_path.sub("-output",".apk")
	end
	code_version = `aapt dump badging #{apk_path} | grep package | awk '{print $4}' | sed s/versionName=//g | sed s/\\'//g`.strip
	return code_version
	
end

def get_app_version_code(app_path)

        apk_path = ""
        if $closed_source == false then
          if $project_type.eql?("ant") then
                apk_path = `ls #{app_path}/bin/*-debug.apk`.strip
          else
                 # for gradle projects, we only get the first line in case there are multiple apks
                apk_path = `find #{app_path} -name '*-debug.apk' | head -1`.strip
          end
        else
		apk_path = app_path.sub("-output",".apk")
        end
        version_code = `aapt dump badging #{apk_path} | grep package | awk '{print $3}' | sed s/versionCode=//g | sed s/\\'//g`.strip
        return version_code
end


# check whether the two bug reports are different
def get_diff_lines(file_path_1, file_path_2, package_name) 

	different_lines = `diff #{file_path_1} #{file_path_2} | grep ^"<"`
	count = 0
	different_lines.each_line do | l |
		line = l.strip
		# exclude newlines
		if not line.eql?("<") then
			line2 = line[1..(line.length-1)].strip
			# exclude the differences in the description of the exception, 
			# we only care about the exact stack difference w.r.t the app under test by package name
			if line2.start_with?("at") then
				count = count + 1
			end
		end
	end
	return count
end

$duplicate_bug_reports = Hash.new
# the happening frequency of the same bug reports
$duplicate_bug_reports_frequency = Hash.new
# the first happening time of the bug
$duplicate_bug_reports_time = Hash.new

# get the original bug report path, 
# @bugreport: the dir name of the newly created bug report
def get_original_bugreport_path(app_dir, bugreport_path)

	bugreport = File.basename(File.dirname(bugreport_path))
	puts "bugreport: #{bugreport}"
	
	num = bugreport.split(".")[0].strip() # "*.fsm", "*.mcmc_sys", "*.mcmc_ui"
	puts "num: #{num}"
	
	original_bugreport_file_path = ""
	if bugreport.include?(".fsm") then
	        original_bugreport_file_path = `ls #{app_dir}/stoat_fsm_output/crashes/#{num}/*_logcat.txt`.strip()
        else
		original_bugreport_file_path = `ls #{app_dir}/stoat_mcmc_sampling_output/crashes/#{num}/*_logcat.txt`.strip()
	end
	return original_bugreport_file_path
end

# Count the unique bug reports
def get_unique_bug_reports(path, package_name)
	
	count = 0  # the number of unique bugs
	fsm_count = 0 # the number of unique bugs found in fsm
	mcmc_count = 0 # the number of unique bugs found in mcmc
	both_count = 0 # the number of unique bugs found in the both stage

	unique_bug_reports = []

	# the bug reports path
	bug_reports_path = path + "/crashes"
	puts "bug_reports_path: #{bug_reports_path}"

	if not File.exists?(bug_reports_path)  then
		return 0,0,0,0,""
	end

	# if not bug reports exists 
	if Dir.entries(bug_reports_path).size() == 2 then
		return 0,0,0,0,""
	end

	# the unique bug reports path
	unique_bug_reports_path = bug_reports_path + "/unique"

	if File.exists?(unique_bug_reports_path) then
		`rm -rf #{unique_bug_reports_path}`
	end
	`mkdir -p #{unique_bug_reports_path}`

	# get base time (use the modification time of 'CONF.txt' in the 'stoat_fsm_output' dir as the base time)
	conf_file = path + "/" + "stoat_fsm_output/CONF.txt"
	base_time = File.mtime(conf_file)

	bugreports = Dir.entries(bug_reports_path)
	for bugreport in bugreports do

		# make sure we are dealing with a bugreport file		
		if (not bugreport.include?(".fsm")) && (not bugreport.include?(".mcmc"))  then
			next
		end
		
		if (bugreport.start_with?(".fsm")) || bugreport.start_with?(".mcmc") then
			next
		end

		bugreport_file_path = `ls #{bug_reports_path}/#{bugreport}/*_stack_trace.txt`.strip 

		# if the bug report does not exist or is empty, skip it
		if (not File.exist?(bugreport_file_path)) || (File.zero?(bugreport_file_path)) then
			next 
		end
		
		puts "analyze bug report: #{bugreport_file_path}"
		is_unique = true

	
		for existing_file_path in unique_bug_reports do
			# if one bug report has different exception lines (Note these lines are only related to the app) from the existing reports,
			# this bug report is a new (unique) crash. Otherwise, it is duplicate
			if get_diff_lines(bugreport_file_path, existing_file_path, package_name) < 1 then
				is_unique = false

				$duplicate_bug_reports_frequency[existing_file_path]=$duplicate_bug_reports_frequency[existing_file_path]+1				
				# check whether we have found an earlier bug report
				original_bugreport_file_path = get_original_bugreport_path(path, bugreport_file_path)
				bugreport_file_time = File.mtime(original_bugreport_file_path)
				time_span = ((bugreport_file_time - base_time)/60.0).round() # in mins
				if time_span < $duplicate_bug_reports_time[existing_file_path] then
					$duplicate_bug_reports_time[existing_file_path] = time_span
				end

				if bugreport.include?("fsm") then
					if $duplicate_bug_reports[existing_file_path].include?("mcmc") then
						$duplicate_bug_reports[existing_file_path]="both"
						fsm_count = fsm_count + 1
						both_count = both_count + 1
					end
				else
					if $duplicate_bug_reports[existing_file_path].include?("fsm") then
						$duplicate_bug_reports[existing_file_path]="both"
						mcmc_count = mcmc_count + 1
						both_count = both_count + 1
					end
				end		
	

				break
			end
		end

		if is_unique then
			unique_bug_reports.push(bugreport_file_path)
			count = count + 1

			original_bugreport_file_path = get_original_bugreport_path(path, bugreport_file_path)
			bugreport_file_time = File.mtime(original_bugreport_file_path)
			time_span = ((bugreport_file_time - base_time)/60.0).round() # in mins
			$duplicate_bug_reports_time[bugreport_file_path] = time_span.to_i

			# copy unique bug reports to ./unique
			`cp -r #{bug_reports_path}/#{bugreport} #{unique_bug_reports_path}`  

			# check the crash was triggered in the model construction phase or the Gibbs sampling phase
			if bugreport.include?("fsm") then
				$duplicate_bug_reports[bugreport_file_path]="fsm"
				$duplicate_bug_reports_frequency[bugreport_file_path]=1
				fsm_count = fsm_count + 1
			elsif bugreport.include?("mcmc_ui")
				$duplicate_bug_reports[bugreport_file_path]="mcmc_ui"
				$duplicate_bug_reports_frequency[bugreport_file_path]=1
				mcmc_count = mcmc_count + 1
			else
				$duplicate_bug_reports[bugreport_file_path]="mcmc_sys"
				$duplicate_bug_reports_frequency[bugreport_file_path]=1
				mcmc_count = mcmc_count + 1
			end
			
		end		
	end

	puts "For the app: #{path}, found unique bugs: #{count}, fsm (#{fsm_count}), mcmc (#{mcmc_count}), both (#{both_count})"
	
	return count, fsm_count, mcmc_count, both_count, unique_bug_reports_path, bug_reports_path

end


def remove_leading_substring(original_string, prefix)
        tmp_string = original_string.strip()
        if tmp_string.start_with?(prefix) then
          return tmp_string[(prefix.length())..-1].strip()
        else
          return tmp_string
        end
end


# get the bug exception type by identifying the "Caused by" term
def get_exception_type(bugreport_path, package_name)

        puts "analyze bug report: #{bugreport_path}"

        exception_type = ""
        exception_desc = ""
        stack_top_line = ""
        exception_msg = ""

        lines = `grep -n 'Caused by' #{bugreport_path}`.strip()
        if lines == "" then
          # there are no 'Caused by', get the exception message
          exception_desc = `sed -n 2p #{bugreport_path}`.strip() # get the exception description (should include exception type and exception message) at line 5
          exception_type = exception_desc.split(":")[0].strip()

          # get the top stack line
          tmp_line = `sed -n 3p #{bugreport_path}`.strip() # at line 6
          stack_top_line = remove_leading_substring(tmp_line, "at")

          # get the whole exception msg
          exception_msg = exception_desc

        else

          # get the last 'Caused by'
          exception_desc = lines.split("\n")[-1]
          exception_type = exception_desc.split(":")[2].strip()

          # get the top stack line
          stack_top_line_number = exception_desc.split(":")[0].strip().to_i + 1
          tmp_line = `sed -n #{stack_top_line_number}p #{bugreport_path}`.strip()
          stack_top_line = remove_leading_substring(tmp_line, "at")

	  # get the whole exception msg, split exception_desc and only return 3 elements
          exception_msg = exception_desc.split(":", 3)[-1].strip()

        end

        exception_source = ""

        # check exception signaler
        if stack_top_line.include?(package_name) then
          exception_source = "app"
        elsif stack_top_line.start_with?("android.") or stack_top_line.start_with?("java.") or stack_top_line.start_with?("javax.") or stack_top_line.start_with?("com.android.") or stack_top_line.start_with?("dalvik.") then
          exception_source = "framework"
        else
          exception_source = "libcore/lib" # e.g., org.apache.*, org.w3c.*, org.xml.*, org.jason.*, junit.* (see Android API doc)
        end

        #puts "exception_desc: #{exception_desc}"
        #puts "exception_type: #{exception_type}"
        #puts "exception_msg: #{exception_msg}"
        #puts "stack_top_line: #{stack_top_line}"
        #puts "exception_source: #{exception_source}"
        #sleep 1

        return exception_type, exception_msg, exception_source, stack_top_line
end



# get the bug exceptin crash stack by using package name
def get_exception_crash_stack(bugreport_path, package_name)

	crash_stack = ""
	# make sure the package name is not empty
	if package_name != nil then
		lines= `grep '#{package_name}' #{bugreport_path}`
		lines.each_line do |line|
			crash_stack = crash_stack + line.strip
		end	
		return crash_stack
	else
		return ""
	end
end

# get the found stage of the bug
def get_found_stage(bugreport_path)

	# remove "/unique/", and replace it by "/"
	original_bugreport_path = bugreport_path.sub("/unique/", "/")
	return $duplicate_bug_reports[original_bugreport_path]	

end


def get_bug_frequency(bugreport_path)

	# remove "/unique/", and replace it by "/"
	original_bugreport_path = bugreport_path.sub("/unique/", "/")
	return $duplicate_bug_reports_frequency[original_bugreport_path]	
end

def get_bug_happening_time(bugreport_path)

        # remove "/unique/", and replace it by "/"
        original_bugreport_path = bugreport_path.sub("/unique/", "/")
        return $duplicate_bug_reports_time[original_bugreport_path]
end

# app_path: the abosolute path of the app
# summarize the bug info: app name, exception type, crash stack (with app package name), found in the fsm or mcmc stage
def summarize_unique_bug_reports(app_path, unique_bug_reports_path, package_name, version_name, version_code)


	if File.exists?(unique_bug_reports_path) then
		
		# list the unique bug reports
		bugreports = Dir.entries(unique_bug_reports_path)
		count = 0
		for bugreport in bugreports do

			# make sure we are dealing with a bugreport file, skip "." and ".."		
			if (not bugreport.include?(".fsm")) && (not bugreport.include?(".mcmc")) then
				next
			end

			bugreport_path = `ls #{unique_bug_reports_path}/#{bugreport}/*_stack_trace.txt`.strip

			exception_type, exception_desc, exception_source, stack_top_line = get_exception_type(bugreport_path, package_name)
			happening_time = get_bug_happening_time(bugreport_path)
			found_stage = get_found_stage(bugreport_path)
			bug_frequency = get_bug_frequency(bugreport_path)
			bugreport_file_name = bugreport
			
			open($bug_summary_report, 'a') { |f|
		 	  f.puts "#{app_path}, #{package_name},#{version_name}, #{version_code}, Stoat, #{bugreport_file_name}, #{exception_type}, #{bug_frequency}, #{found_stage}, #{happening_time}, #{exception_source}, \"#{stack_top_line}\", \"#{exception_desc}\""
	  		}

		end
		
	end

end


# path: the path of all apps
def get_all_unique_bug_reports(path, apps_list_file)

	unique_bug_reports = 0
	unique_fsm_bug_reports = 0
	unique_mcmc_bug_reports = 0
	unique_both_bug_reports = 0

	`rm -rf #{path}/unique_bugs`
	`rm -rf #{$bug_reports_loc}`
	`mkdir -p #{$bug_reports_loc}`

	apps = ""
	if apps_list_file.eql?("") then
		apps = `ls #{path}`
	else
		if not File.exist?(apps_list_file)
			puts "the apps list file not exist?"
			exit
		end
		apps = `cat #{apps_list_file}`
	end

	apps.each_line do |app|
		app_name = app.strip
		app_dir = path + "/" + app_name
		puts "handling app dir: #{app_dir}"
		
		if Dir.exists?(app_dir) then

			# collect all crashes
			collect_app_crashes(app_dir)

			# get package name of the app
			package_name = get_app_package_name(app_dir)
			version_name = get_app_version_name(app_dir)
			version_code = get_app_version_code(app_dir)
			
			count, fsm_count, mcmc_count, both_count, unique_bug_reports_path, bug_reports_path = get_unique_bug_reports(app_dir, package_name)
			unique_bug_reports = unique_bug_reports + count
			unique_fsm_bug_reports = unique_fsm_bug_reports + fsm_count
			unique_mcmc_bug_reports = unique_mcmc_bug_reports + mcmc_count
			unique_both_bug_reports = unique_both_bug_reports + both_count
			
			# summarize the bug info
			summarize_unique_bug_reports(app_name, unique_bug_reports_path, package_name, version_name, version_code)

			if count > 0 then
				`mkdir -p #{$bug_reports_loc}/#{app_name}`
  				`cp -r #{unique_bug_reports_path} #{$bug_reports_loc}/#{app_name}`
				`rm -rf #{bug_reports_path}`
			end
			
		end
	end
	
	
end


$project_type="ant"
$closed_source=false


# for 'ant projects'
$bug_reports_loc="/home/XX/stoat_ant_projects_unique_app_bugs/"
$bug_summary_report= $bug_reports_loc + "/stoat_ant_projects_bug_summary.csv"
$apps_dir="/home/XX/ant_projects/"
$apps_list_file = "/home/XX/ant_apps_list.txt"

#o When the apps list file is given, the script will scan all the dirs which is generated by concatenating the path arg and the app name in the apps list file
get_all_unique_bug_reports($apps_dir, $apps_list_file)

