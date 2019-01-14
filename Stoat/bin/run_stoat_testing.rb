#!/usr/bin/env ruby

# this script runs stoat to test android apps
# Preparation before testing: 1. open network, 2. disable keyborad in android (e.g., using nullkeyboard) 

require 'optparse'

# only execute the cmd
def execute_shell_cmd(cmd)
	puts "$ #{cmd}"
	`#{cmd}`
end

# execute the cmd and outputs the running info
def execute_shell_cmd_output(cmd)
	puts "$ #{cmd}"
	IO.popen(cmd).each do |line|  # outputs the running info
		puts line
	end
end

# create the avd 
def create_avd(avd_name="testAVD_1", sdk_version="android-18")

	# check whether #avd_name already exist?
	res = execute_shell_cmd("avdmanager list avd | grep #{avd_name}")

	if (not res.strip.eql?("")) then # if exist and forced to create a new one, delete it
		kill_avd() # make sure the avd is stopped
		execute_shell_cmd_output("avdmanager delete avd -n #{avd_name}")
		sleep 1 # wait a while
	end
	
	# create the avd
	#avdmanager create avd -f -n testAVD_1 -k 'system-images;android-18;google_apis;x86' -b google_apis/x86 -c 512M -d 'Nexus 7'
	execute_shell_cmd_output("echo no | avdmanager create avd --force --name #{avd_name} --package 'system-images;#{sdk_version};google_apis;x86' --abi google_apis/x86 --sdcard 512M --device 'Nexus 7'")
	sleep 2

end


# start the avd
def start_avd(avd_name="testAVD_1", avd_port="5554", force_restart=false)

	res = execute_shell_cmd("ps | grep qemu-system")
	if (not res.strip.eql?("")) && (not force_restart) then
		return
	end

	if (not res.strip.eql?("")) && force_restart then
		kill_avd() # make sure the avd is stopped
	end
	
	# start the adb server
  	execute_shell_cmd("adb start-server")
	sleep 1

	# start the emulator, http://stackoverflow.com/questions/2504445/spawn-a-background-process-in-ruby
	job1 = fork do 
		# -swipe-data: clean up the emulator
  		exec "emulator -port #{avd_port} -avd #{avd_name} -wipe-data &"
	end
	Process.detach(job1)

	puts "spawn the emulator, wait for it to boot ..."
	sleep 5
	wait_avd("emulator-" + avd_port)

end

def wait_avd(avd_serial="emulator-5554")

	Dir.chdir($STOAT_TOOL_DIR+"/bin/") do
		execute_shell_cmd_output("./waitForEmu.sh #{avd_serial}")
	end

end

# prepare the avd by pushing data files into the sdcard
def prepare_avd(avd_serial="emulator-5554")

	Dir.chdir($STOAT_TOOL_DIR+"/bin/") do
		execute_shell_cmd_output("./setupEmu.sh #{avd_serial}")
	end

    # naturalize the screen to avoid side-effect from previous testing
	puts "naturalize the screen to avoid side-effect from previous testing..."
	execute_shell_cmd("python #{$STOAT_TOOL_DIR}/a3e/bin/events/rotation_natural.py #{avd_serial}")

end

# kill the avd
def kill_avd()

	# kill the existing emulator
  	execute_shell_cmd("kill -9 `ps | grep qemu-system | awk '{print $1}'`")
 
end

def cleanup()

	execute_shell_cmd("for pid in $(ps a | grep 'Server.jar' | awk '{print $1}'); do kill -9 $pid; done")
  	execute_shell_cmd("for pid in $(ps | grep adb | awk '{print $1}'); do kill -9 $pid; done")
  	execute_shell_cmd("for pid in $(ps | grep sleep | awk '{print $1}'); do kill -9 $pid; done")
end

# install the app
def install_app(avd_serial, app_dir, apk_path)

	if app_dir.end_with?(".apk") then
		# this is a closed-source app
		execute_shell_cmd("adb -s #{avd_serial} install -g -r #{app_dir}")
	else
		apk =""
		if $project_type.eql?("ant") then # ant project
			apk=`ls #{app_dir}/bin/*-debug.apk`.strip
	    else # gradle project
		  	apk=apk_path
		end
		execute_shell_cmd("adb -s #{avd_serial} install -g -r #{apk}")
		
	end
	
end

def uninstall_app(avd_serial, apk_path)

	puts "uninstall the app "
 	res = execute_shell_cmd("aapt dump badging #{apk_path} | grep package | awk '{print $2}' | sed s/name=//g | sed s/\\'//g")
	package_name = res.strip
  	execute_shell_cmd("#{$timeout_cmd} 10s adb -s #{avd_serial} uninstall #{package_name}")

end

# construct fsm for open source app, given app's dir, emma-instrumented
def construct_fsm(app_dir, apk_path, avd_serial="emulator-5554", stoat_port="2000")

	
	if app_dir.end_with?(".apk") then
	# clsed-source projects
	
		# start the stoat server
		puts "** RUNNING STOAT FOR #{app_dir}\n"  
		Dir.chdir($STOAT_TOOL_DIR) do
			job2 = fork do 
				cmd = "bash -x ./bin/analyzeAndroidApk.sh fsm_apk #{app_dir} apk #{apk_path} &> /dev/null &"
				puts "$ #{cmd}"
		  		exec cmd
			end
			Process.detach(job2)
		end

		if $only_gui_exploration then
			# start the stoat client for only gui exploration
			Dir.chdir($STOAT_TOOL_DIR + "/a3e") do
			  	execute_shell_cmd_output("#{$timeout_cmd} #{$model_construction_time} ruby ./bin/rec.rb --app #{app_dir} --apk #{apk_path} --dev #{avd_serial} --port #{stoat_port} --no-rec -loop --search weighted --events #{$max_event_number} --event_delay #{$event_delay} --project_type #{$project_type} --enable_dump_screenshot --disable_coverage_report") # --disable_crash_report
			end
		else
			
			# start the stoat client for testing
			Dir.chdir($STOAT_TOOL_DIR + "/a3e") do
			  	execute_shell_cmd_output("#{$timeout_cmd} #{$model_construction_time} ruby ./bin/rec.rb --app #{app_dir} --apk #{apk_path} --dev #{avd_serial} --port #{stoat_port} --no-rec -loop --search weighted --events #{$max_event_number} --event_delay #{$event_delay} --project_type #{$project_type} --disable_coverage_report") # disable coverage report because we assume .apk are not instrumented
			end
			
		end

		puts "** FINISH STOAT FOR FSM BUILDING"

		cleanup()

	else
	# open-source projects

		Dir.chdir(app_dir) do
	
			app_dir_path=`pwd`.strip # get the absolute path
			# remove old dir
			execute_shell_cmd("rm -rf stoat_fsm_output")

			# get the apk file
		    	apk = ""
			if $project_type.eql?("ant") then
		      		apk=`ls bin/*-debug.apk`.strip
		   	else
		      		apk=apk_path
		    	end
			if not File.exist?(apk) then
				puts "apk file not found."
				exit
			end
			# get the apk file name
	  		apk_name=`basename #{apk}`.strip
			puts "the apk file: #{apk_name}"

			# create "coverage" dir to store the dumped coverage files
			dumped_coverage_dir = app_dir_path + "/coverage"
			execute_shell_cmd("mkdir -p #{dumped_coverage_dir}")

			# start the coverage dumper
			puts "** DUMPING INTERMEDIATE COVERAGE "
			Dir.chdir($STOAT_TOOL_DIR+"/bin/") do
				job1 = fork do 
					cmd = "./dumpCoverage.sh #{dumped_coverage_dir} #{avd_serial} &> #{dumped_coverage_dir}/icoverage.log &"
					puts "$ #{cmd}"
			  		exec cmd
				end
				Process.detach(job1)
			end

			# start the stoat server
			puts "** RUNNING STOAT FOR #{app_dir}\n"  
			Dir.chdir($STOAT_TOOL_DIR) do
				job2 = fork do 
					cmd = "bash -x ./bin/analyzeAndroidApk.sh fsm #{app_dir_path} #{$project_type} #{apk_path} &> /dev/null &"
					puts "$ #{cmd}"
			  		exec cmd
				end
				Process.detach(job2)
			end
	  		
			# start the stoat client
			Dir.chdir($STOAT_TOOL_DIR + "/a3e") do
			  	execute_shell_cmd_output("#{$timeout_cmd} #{$model_construction_time} ruby ./bin/rec.rb --app #{app_dir_path} --apk #{apk_path} --dev #{avd_serial} --port #{stoat_port} --no-rec -loop --search weighted --events #{$max_event_number} --event_delay #{$event_delay} --project_type #{$project_type}")
			end

			puts "** FINISH STOAT FOR FSM BUILDING"

			# clean up
			execute_shell_cmd("kill -9 `ps | grep dumpCoverage | awk '{print $1}'`")
			cleanup()
	  		
		end	
	end
end




def mcmc_sampling_fuzzing_open_source_projects(app_dir, apk_path, avd_serial, stoat_port)
	
	apk_loc = ""

	Dir.chdir(app_dir) do

		app_dir_path=`pwd`.strip # get the absolute path
		
	
		# check the existence of FSM.txt
		fsm_file=app_dir_path + "/stoat_fsm_output/FSM.txt"
		if not File.exist?(fsm_file) then
			puts "fsm file not found"
			# do nothing
		else

			res=execute_shell_cmd("cat #{fsm_file} | sed -n '2p' | cut -d ';' -f 1")
			fsm_states = res.strip.to_i
			puts "the app model has #{fsm_states} states."
			if not (fsm_states >= 2) then
				puts "the constructed fsm is too small, give up mcmc sampling."
				# do nothing
			else

				# running mcmc sampling
				puts "*** Start MCMC"
				# remove old dir
				execute_shell_cmd("rm -rf stoat_mcmc_sampling_output")	

				# start the coverage dumper
				puts "** DUMPING INTERMEDIATE COVERAGE "
				dumped_coverage_dir = app_dir_path + "/coverage"
				Dir.chdir($STOAT_TOOL_DIR+"/bin/") do
					job1 = fork do 
				  		exec "./dumpCoverage_mcmc.sh #{dumped_coverage_dir} #{avd_serial} &> #{dumped_coverage_dir}/icoverage.log &"
					end
					Process.detach(job1)
				end

				apk = ""
		    	if $project_type.eql?("ant") then
		  			apk=`ls bin/*-debug.apk`.strip
					apk_loc = app_dir + "/" + apk
	       		else
		  			apk=apk_path
					apk_loc = apk_path
				end
				if not File.exist?(apk) then
					puts "apk file not found."
					exit
				end
				# get the apk file name
		  		apk_name=`basename #{apk}`.strip
				puts "the apk file: #{apk_name}"

				# start the stoat server
				puts "** RUNNING STOAT FOR #{app_dir}"  
				Dir.chdir($STOAT_TOOL_DIR) do
					job2 = fork do 
				  		exec "./bin/analyzeAndroidApk.sh mcmc #{app_dir_path} #{$project_type} #{apk_path}&> /dev/null &"
					end
					Process.detach(job2)
				end
		  		
				# start the stoat client
				Dir.chdir($STOAT_TOOL_DIR + "/a3e") do
				  	execute_shell_cmd_output("#{$timeout_cmd} #{$mcmc_sampling_time} ruby ./bin/agentManager.rb --app #{app_dir_path} --apk #{apk_path} --dev #{avd_serial} --port #{stoat_port} --devnum 1 --max_iteration 500 --event_delay #{$event_delay} --project_type #{$project_type}")
				end

				puts "** FINISH STOAT FOR MCMC SAMPLING"
			end
		end
		
	end

	# clean up
	execute_shell_cmd("kill -9 `ps | grep dumpCoverage | awk '{print $1}'`")
	cleanup()

	# uninstall the app after all remainings are cleaned up
	uninstall_app(avd_serial, apk_loc)

end

def mcmc_sampling_fuzzing_closed_source_projects(app_dir, apk_path, avd_serial, stoat_port)
	
	loc = app_dir.rindex(".apk")
	if loc==nil then
		puts "error, not an apk file?"
		exit
	end

	# get the apk output dir loc
	app_dir_loc = app_dir[0..loc-1] + "-output" # xx.apk -> xx-output

	Dir.chdir(app_dir_loc) do

		app_dir_path=`pwd`.strip # get the absolute path
		
		# check the existence of FSM.txt
		fsm_file=app_dir_path + "/stoat_fsm_output/FSM.txt"
		if not File.exist?(fsm_file) then
			puts "fsm file not found"
			# do nothing
		else

			res=execute_shell_cmd("cat #{fsm_file} | sed -n '2p' | cut -d ';' -f 1")
			fsm_states = res.strip.to_i
			puts "the app model has #{fsm_states} states."
			if not (fsm_states >= 2) then
				puts "the constructed fsm is too small, give up mcmc sampling."
				# do nothing
			else

				# running mcmc sampling
				puts "*** Start MCMC"
				# remove old dir
				execute_shell_cmd("rm -rf stoat_mcmc_sampling_output")	

				# start the stoat server
				puts "** RUNNING STOAT FOR #{app_dir}"  
				Dir.chdir($STOAT_TOOL_DIR) do
					job2 = fork do 
				  		exec "./bin/analyzeAndroidApk.sh mcmc_apk #{app_dir} #{$project_type} #{apk_path}&> /dev/null &"
					end
					Process.detach(job2)
				end
		  		
				# start the stoat client
				Dir.chdir($STOAT_TOOL_DIR + "/a3e") do
				  	execute_shell_cmd_output("#{$timeout_cmd} #{$mcmc_sampling_time} ruby ./bin/agentManager.rb --app #{app_dir} --apk #{apk_path} --dev #{avd_serial} --port #{stoat_port} --devnum 1 --max_iteration 500 --event_delay #{$event_delay} --project_type #{$project_type} --restart_delay 1000 --disable_coverage_report") # disable coverage report
				end

				puts "** FINISH STOAT FOR MCMC SAMPLING"
			end
		end
		
	end

	# clean up
	cleanup()

	# uninstall the app after all remainings are cleaned up
	#uninstall_app(avd_serial, app_dir)

end

def mcmc_sampling_fuzzing(app_dir, apk_path, avd_serial="emulator-5554", stoat_port="2000")


	if app_dir.end_with?(".apk") then
		mcmc_sampling_fuzzing_closed_source_projects(app_dir, apk_path, avd_serial, stoat_port)
	else
		mcmc_sampling_fuzzing_open_source_projects(app_dir, apk_path, avd_serial, stoat_port)
	end
	

end

$STOAT_TOOL_DIR = File.expand_path(File.dirname(__FILE__)) + "/../"
puts "stoat tool dir: #{$STOAT_TOOL_DIR}"

# the default configuration when using emulators
avd_name="testAVD_1"
avd_serial="emulator-5554"
avd_port="5554"
avd_sdk_version="android-18"
auto_install=true

# timeout command
$timeout_cmd="timeout"

# the default configuration of sever side
stoat_port="2000"

# the default configuration for Stoat
force_to_create=false 
force_to_restart=false
$model_construction_time="1.2h"
$mcmc_sampling_time="2h"

# only construct the app model by gui exploration without mcmc sampling
$only_gui_exploration=false 

# the maximum number of events for model construction
$max_event_number=3000

# the delay time between events (in milliseconds)
$event_delay=0

# project types can be open-source projects, either "ant" or "gradle"; or closed-source projects, "apk"
$project_type="ant" 

# not used now, for future extension
$enable_xdot=false

# use the real device to test (currently Stoat is compatible with Android-18/19)
is_real_device=false # when use real devices, this flag is set as true

# the dir of the open-source app under test 
app_dir=""
apps_dir=""
apps_list=""
# the path of the apk under test 
# for ant projects, the apk is default located under bin/*-debug.apk; but for gradle projects, the apk location may not be easy to infer, so we require the user to provide the path
apk_path="unmeaningful-apk-path"

# clean up for ctrl+c
trap("INT") { 
	puts "Shutting down."
	execute_shell_cmd("kill -9 `ps | grep dumpCoverage | awk '{print $1}'`")
	cleanup()
	execute_shell_cmd("for pid in $(ps | grep ruby | awk '{print $1}'); do kill -9 $pid; done")
	exit
}

OptionParser.new do |opts|
  opts.banner = "Usage: ruby #{__FILE__} [options]\n \tStoat can test open-source projects (Ant projects -- Emma, Gradle projects -- Jacoco) and closed-source projects (Ant/Gradle projects -- Ella), Developers: Ting Su (tsuletgo@gmail.com), Guozhu Meng, copyright reserved, 2015-2017"
  opts.on("--avd_name avd", "your own Android Virtual Device") do |n|
    avd_name = n
  end
  opts.on("--avd_port port", "the serial port number of Android Virtual Device, e.g., 5554") do |p|
    avd_port = p
	avd_serial = "emulator-" + p
  end
  opts.on("--real_device_serial serial_number", "the serial number of a real Android device") do |r|
    avd_serial = r
	is_real_device = true
  end
  opts.on("--avd_sdk_version version", "the sdk version of Android Virtual Device, e.g., android-18") do |v|
    avd_sdk_version = v
  end

  opts.on("--stoat_port port", "the communication port between Stoat's server and client side, e.g., 2000") do |c|
    stoat_port = c
  end

  opts.on("--app_dir dir", "the directory of the open-source app to test (relative path or absolute path) ") do |d|
    app_dir = d
  end
  opts.on("--apk_path path", "the path of the apk to test (relative path or absolute path) ") do |a|
    apk_path = a
  end
  opts.on("--apps_dir dir", "the dir containing all the apps to test (absolute path). Note this option is used with --apps_list.") do |l1|
    apps_dir = l1
  end
  opts.on("--apps_list file", "the file containing all the apps to test (absolute path). Note the file is at the same location with these apps.") do |l2|
    apps_list = l2
  end
  
  opts.on("--model_time time", "the time allocated for construct model (in hours), default: 1.2h") do |t1|
    $model_construction_time = t1
  end
  opts.on("--mcmc_time time", "the time allocated for mcmc sampling (in hours), default: 2h") do |t2|
    $mcmc_sampling_time = t2
  end
  
  opts.on("--max_event number", "the max number of events for model construction, default: 3000") do |m|
    $max_event_number = m
  end
  opts.on("--event_delay time", "the delay time between the events in milliseconds, e.g., 1000, wait util the event takes effect, especially useful for long-running tasks (e.g., network accessing, processing bar, data processing) or slow-reacting emulators.") do |y|
    $event_delay = y
  end
  opts.on("--project_type type", "how is the project compiled? \"ant\" or \"gradle\", default is \"ant\".") do |t|
    $project_type = t
	if not ($project_type.eql?("ant") || $project_type.eql?("gradle") || $project_type.eql?("apk") ) then
		puts "error project type: ant or gradle?"
		exit
	end
  end
 
  opts.on("--only_gui_exploration", "only construct the app model by gui exploration") do
	$only_gui_exploration = true
  end
  opts.on("--force_create", "force to create a fresh/clean Android Virtual Device") do 
	force_to_create = true	
  end
  opts.on("--force_restart", "force to restart the current Android Virtual Device") do 
	force_to_restart = true	
  end
  opts.on("--disable_auto_install", "require install app manually, especially used when testing apps that needs pre-configurations, auto-install will remove these pre-configurations, e.g., login account info") do 
	auto_install = false
  end
  opts.on("--xdot", "enable xdot to view the app model at runtime (make sure xdot is installed)") do 
	$enable_xdot = true	
  end

  opts.on_tail("-h", "--help", "show this message. Note before testing an app, please set \"hw.keyboard=yes\" in the emulator's config file \"~/.android/avd/testAVD_1.avd/config.ini\"  and open the wifi network. \n\n Examples: \n \t
	<Ant opens-soruce projects>\n \t ruby run_stoat_testing.rb --app_dir /home/suting/proj/mobile_app_benchmarks/test_apps/caldwell.ben.bites_4_src --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 --project_type ant \n \t
	<Gradle open-source projects>\n \t ruby run_stoat_testing.rb --app_dir /home/suting/proj/mobile_app_benchmarks/test_apps/tests/com.linuxcounter.lico_update_003_5_src.tar.gz --apk /home/suting/proj/mobile_app_benchmarks/test_apps/tests/com.linuxcounter.lico_update_003_5_src.tar.gz/app/build/outputs/apk/app-debug.apk --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 --project_type gradle \n \t 
	<apk without instrumentation> \n\t Note this may mitigate Stoat's power due to lack of coverage info for test optimization. \n\t ruby run_stoat_testing.rb --app_dir /home/suting/proj/mobile_app_benchmarks/test_apps/Bites.apk --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 (the output will be under \"Bites-output\")\n \t 
	<Use real device, ant projects>\n\t Please open wifi, and disable keyboard before do testing on real device! \n \t ruby run_stoat_testing.rb --app_dir /home/suting/proj/mobile_app_benchmarks/dyno-droid-fse13-apps/caldwell.ben.bites_4_src/ --real_device_serial cf00b9e6 --stoat_port 2000 --project_type ant \n\t
	<a set of apps> \n\t ruby run_stoat_testing.rb --apps_dir /home/suting/proj/mobile_app_benchmarks/test_apps/ --apps_list /home/suting/proj/mobile_app_benchmarks/test_apps/apps_list.txt --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 --force_restart \n\n Outputs: \n \t<stoat_fsm_building_output>: the outputs of model construction. \n\t\t crashes/ -- crash report (include crash stack, event trace, screen shots); \n\t\t ui/ -- ui xml files; \n\t\t coverage/ -- coverage files during model construction; \n\t\t FSM.txt/app.gv -- xdot model graph; \n\t\t fsm_building_process.txt/fsm_states_edges.txt -- the model building process, mainly the increasing coverage/#states/#edges \n\t\t CONF.txt -- configuration file \n \t<stoat_mcmc_sampling_output>: the outputs of mcmc sampling. \n\t\t crashes/ -- crash report (include crash stack, event trace, screen shots); \n\t\t MCMC_coverage/ -- the coverage data during mcmc sampling; \n\t\t mcmc_sampling_progress.txt/mcmc_data.txt -- mcmc sampling progress data; \n\t\t initial_markov_model.txt/optimal_markov_model.txt/mcmc_models.txt -- the initial/optimal/all mcmc sampling models; \n\t\t mcmc_all_history_testsuites.txt -- all executed test suites for mcmc sampling; \n\t\t test_suite_to_execute.txt -- the current test suite under execution;\n\t\t CONF.txt -- configuration file. \n\t <coverage>: the all coverage data during two phases") do
    puts opts
    exit
  end
end.parse!

# santisize the options
if $project_type.eql?("gradle") && apk_path.eql?("unmeaningful-apk-path") then
	if $project_type.eql?("gradle") && apps_list.eql?("") then
		puts "for gradle projects, please specify both app dir and apk path OR apps list."
		exit
	end
end


# clean up before do anything
puts "clean up .... before do anything ...."
cleanup()

if (not app_dir.eql?("")) && File.exist?(app_dir) then  # for testing one app at one time

	if force_to_create then
		create_avd(avd_name, avd_sdk_version)
	end

	if not is_real_device then # if no real device, use emulators
		start_avd(avd_name, avd_port, force_to_restart)
	end

	prepare_avd(avd_serial)
	if auto_install then
		install_app(avd_serial, app_dir, apk_path)
	end

	if $only_gui_exploration then
		# only construct the gui model for the app
		construct_fsm(app_dir, apk_path, avd_serial, stoat_port)
		uninstall_app(avd_serial, app_dir)
	else
		construct_fsm(app_dir, apk_path, avd_serial, stoat_port)
		mcmc_sampling_fuzzing(app_dir, apk_path, avd_serial, stoat_port)
	end

elsif (not apps_list.eql?("")) && File.exist?(apps_list) then # for testing multiple apps listed in a file at one time

	if force_to_create then
		create_avd(avd_name, avd_sdk_version)
	end

	File.readlines(apps_list).each do |line|

		if line.start_with?("#") then # if "#" is added before an app, this app will be omitted
			next
		end

		app_dir_from_file="xx"
		apk_path_from_file="xx"
		if $project_type.eql?("ant") then
			# for ant projects, each line is the app dir
			app_dir_from_file = apps_dir + line.strip
		elsif $project_type.eql?("gradle") then
			# for gradle projects, each line is "the app dir,the apk path"
			arr = line.split(',')
			app_dir_from_file = apps_dir + arr[0].strip
			apk_path_from_file = apps_dir + arr[1].strip
		else
			# for closed-source projects
			app_dir_from_file = apps_dir + line.strip
		end
	
		if not is_real_device then # if no real device, use emulators
			start_avd(avd_name, avd_port, force_to_restart)
		end

		prepare_avd(avd_serial)
		if auto_install then
			install_app(avd_serial, app_dir, app_dir_from_file)
		end

		# get the app path (absolute path)
		puts "***"
		puts "to test: #{app_dir_from_file}"
		puts "***"
		sleep 2
		if $only_gui_exploration then
			# only construct gui models for apps
			construct_fsm(app_dir_from_file, apk_path_from_file, avd_serial, stoat_port)
			uninstall_app(avd_serial, app_dir_from_file)
		else
			construct_fsm(app_dir_from_file, apk_path_from_file, avd_serial, stoat_port)
			mcmc_sampling_fuzzing(app_dir_from_file, apk_path_from_file, avd_serial, stoat_port)
		end
	
		execute_shell_cmd("echo #{app_dir_from_file} > #{avd_name}.done.txt")
                
	end
else
	puts "please specify the app to test, or check the app path"
	exit
end



