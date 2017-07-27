#! /usr/bin/env ruby

# The script to test apps with pure system events
# a wrapper of trigger

require_relative 'aapt'
require_relative 'adb'

def parse_emma_coverage_report (report_file, app_name)
        
       #puts "I: the coverage report file: #{report_file}. "


       # the coverage info type: "1" - coverage summary, "2" - stats summary, "3" - detail summary
       coverage_info_type = 0
       # is the data here?
       data = 0
       IO.foreach(report_file) { |line|
           
           if line.include?("COVERAGE SUMMARY") then
               #puts "the coverage [summary] line ... "
               coverage_info_type = 1;
           elsif line.include?("STATS SUMMARY") then
               #puts "the coverage [stats] line ... "
               coverage_info_type = 2;
           end
           
           ########### extract coverage summary
           if (coverage_info_type == 1) && (line.include?("[class, %]")) then
               # puts "the coverage label line ... "
               data = 1
           end
           if coverage_info_type == 1 && data == 1 then
               
               # puts "the coverage data line ..."
               i = 1
               
               # match the pattern "(digits/digits)", note "digits" could be "float"
               line.gsub(/(\d|\.)+\/(\d)+/){ |match|
                   #puts "#{match}"
                   # get the covered/total entities
                   arr = match.split("/")
                   #puts "#{arr[0]}, #{arr[1]}"
                   if (i==1) then
                       #@coverager.setClassCoverage arr[0], arr[1]
                   elsif (i==2) then
                       #@coverager.setMethodCoverage arr[0], arr[1]
                   elsif (i==3) then
                       #@coverager.setBlockCoverage arr[0], arr[1]
                   elsif (i==4) then
                       #@coverager.setLineCoverage arr[0], arr[1]
			   covered_lines = arr[0].to_i 
			   total_lines = arr[1].to_i 
			   #puts "covered lines: #{covered_lines}, total lines: #{total_lines}"
			   coverage_percentage = covered_lines*1.0/total_lines*100.0
			   #puts "coverage percentage: #{coverage_percentage}"
			   open("#{$output_dir}/system_events_#{$policy}.csv", 'a') { |f|
			    
			    output = "#{app_name},#{total_lines},#{covered_lines},#{coverage_percentage}"
			    f.puts output
                    
                	  }
                   end
                   i += 1
               }
           end
           
           ######## extract stats summary
           if (coverage_info_type == 2) && (line.include?("total")) then
               #puts "the stats line ... "
               # get the desciption_statement/number
               arr = line.split(":")
               #puts "#{arr[0]}, #{arr[1].strip}"
               if line.include?("total packages") then
                    #@coverager.setTotalpackages arr[1].strip
               elsif line.include?("total executable files") then
                    #@coverager.setTotalFiles arr[1].strip
               end
           end
       }
       
end

def run_all_system_events(apk, events)
   cmd = "python /home/suting/proj/fsmdroid/trigger/tester.py -f #{apk} -s #{$dev} -p random"
   dump = "timeout 2s adb -s #{$dev} shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE"

   
   
   for i in 0..events.to_i
	puts `#{cmd}`
	`#{dump}`
	`adb -s #{$dev} pull /mnt/sdcard/coverage.ec #{$output_dir}/coverage_#{i}.ec`

	 coverage_files = `find #{$output_dir} -name "*.ec"`
          str_coverage_files = ""
          coverage_files.each_line do |line|
              str_coverage_files += line.strip + ","
          end

	cmd1 = "java -cp /home/suting/Android/Sdk/tools/lib/emma.jar emma report -r txt -in " + str_coverage_files + $em_file
	`#{cmd1}`

	parse_emma_coverage_report("coverage.txt", `basename #{apk}`)
	`rm -rf coverage.txt`
   end
end


def run_broadcast_system_events(apk, events)
   cmd = "python /home/suting/proj/fsmdroid/trigger/tester.py -f #{apk} -s $dev -p broadcast"
   dump = "timeout 2s adb -s #{$dev} shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE"
   for i in 0..events.to_i
	puts `#{cmd}`
	`#{dump}`
	`adb -s #{$dev} pull /mnt/sdcard/coverage.ec #{$output_dir}/coverage_#{i}.ec`

	coverage_files = `find #{$output_dir} -name "*.ec"`
          puts "#{coverage_files}"
          str_coverage_files = ""
          coverage_files.each_line do |line|
              str_coverage_files += line.strip + ","
          end

	cmd1 = "java -cp /home/suting/Android/Sdk/tools/lib/emma.jar emma report -r txt -in " + str_coverage_files + $em_file
	`#{cmd1}`

	parse_emma_coverage_report("coverage.txt", `basename #{apk}`)
	`rm -rf coverage.txt`
   end
end


def run_listener_system_events(apk, events)
   cmd = "python /home/suting/proj/fsmdroid/trigger/tester.py -f #{apk} -s $dev -p listener"
   dump = "timeout 2s adb -s #{$dev} shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE"
   for i in 0..events.to_i
	puts `#{cmd}`
	`#{dump}`
	`adb -s #{$dev} pull /mnt/sdcard/coverage.ec #{$output_dir}/coverage_#{i}.ec`
	
	coverage_files = `find #{$output_dir} -name "*.ec"`
          puts "#{coverage_files}"
          str_coverage_files = ""
          coverage_files.each_line do |line|
              str_coverage_files += line.strip + ","
          end

	cmd1 = "java -cp /home/suting/Android/Sdk/tools/lib/emma.jar emma report -r txt -in " + str_coverage_files + $em_file
	`#{cmd1}`

	parse_emma_coverage_report("coverage.txt", `basename #{apk}`)
	`rm -rf coverage.txt`
   end
end

def run_launcher_system_events(apk, events)
   cmd = "python /home/suting/proj/fsmdroid/trigger/tester.py -f #{apk} -s $dev -p launcher"
   dump = "timeout 2s adb -s #{$dev} shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE"
   for i in 0..events.to_i
	puts `#{cmd}`
	`#{dump}`
	`adb -s #{$dev} pull /mnt/sdcard/coverage.ec #{$output_dir}/coverage_#{i}.ec`
	
	coverage_files = `find #{$output_dir} -name "*.ec"`
          puts "#{coverage_files}"
          str_coverage_files = ""
          coverage_files.each_line do |line|
              str_coverage_files += line.strip + ","
          end

	cmd1 = "java -cp /home/suting/Android/Sdk/tools/lib/emma.jar emma report -r txt -in " + str_coverage_files + $em_file

	parse_emma_coverage_report("coverage.txt", `basename #{apk}`)
	`rm -rf coverage.txt`
   end
end

# install the app under test
def install_app (apk)
  $adb.install apk
  sleep 1
end

# uninstall the app under test
def uninstall_app (apk)
  pkg = $aapt.pkg apk
  uninstall_cmd = "adb -s #{$dev} uninstall #{pkg}"
  puts "$ #{uninstall_cmd}"
  `#{uninstall_cmd}`
  sleep 1
end

apk=ARGV[0]
dev=ARGV[1]
$policy=ARGV[2]
events=ARGV[3]
$output_dir=ARGV[4]
$em_file = ARGV[5]

$aapt = AAPT.new()

$dev = dev #device serial number

# create "adb" instance
$adb = ADB.new()
# init. the emulator serial
$adb.device $dev

uninstall_app(apk)
install_app(apk)

pkg = $aapt.pkg apk
act = $aapt.launcher apk

cmd = "adb -s " + $dev + " shell am start -n " + pkg + "/" + act
`#{cmd}`

`rm -rf #{$output_dir}/system_events_#{$policy}.csv`
`rm -rf #{$output_dir}/*.ec`

if $policy.eql?("random") then
  run_all_system_events(apk, events)
elsif $policy.eql?("broadcast") then
  run_broadcast_system_events(apk, events)
elsif $policy.eql?("listener") then
  run_listener_system_events(apk, events)
elsif $policy.eql?("launcher") then
  run_launcher_system_events(apk, events)
end

# clean 
uninstall_app(apk)


