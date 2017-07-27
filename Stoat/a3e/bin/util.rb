module UTIL
require 'fileutils'

	def UTIL.compare(file_name1, file_name2)
		is_same=FileUtils.compare_file(file_name1, file_name2);
	end
	
	def UTIL.extract_num(file_name)
		num=IO.readlines(file_name)
	end
	
	def UTIL.compare_output(file1, file2, file3)
		
        #add by ting
        file1_arr = IO.readlines(file1)
        file2_arr = IO.readlines(file2)
        print "[A3E] file name: " + file1 + "\n"
        i = 0
        while i < file1_arr.size do
            print file1_arr[i] + "\n"
            i +=1
        end
        print "[A3E] file name: " + file2 + "\n"
        i = 0
        while i < file2_arr.size do
            print file2_arr[i] + "\n"
            i +=1
        end
        
		f1 = IO.readlines(file1).map(&:chomp)
		f2 = IO.readlines(file2).map(&:chomp)
		f3= (f1-f2)
		print "*******\n"
		print f3
		print "\n*******\n"
		File.open(file3,"w"){ |f| f.write((f1-f2).join("\n")) }
	
	end
	
	# execute shell command
	def UTIL.execute_shell_cmd(cmd)
	  puts "$ #{cmd}"
	  `#{cmd}`
	end

 	def UTIL.execute_shell_cmd_with_output(cmd)
	  puts "$ #{cmd}"
	  IO.popen(cmd).each do |line|  # outputs the running info
	  	puts line
	  end
	end
	
	def UTIL.get_package_name(apk)
    execute_shell_cmd("aapt dump badging #{apk} | grep package | awk '{print $2}' | sed s/name=//g | sed s/\\'//g").strip()
	end
	
	def UTIL.get_app_version(apk)
	  execute_shell_cmd("aapt dump badging #{apk} | grep package | awk '{print $4}' | sed s/versionName=//g | sed s/\\'//g").strip()
	end


	# check whether we need input login info
	def UTIL.need_login(avd_serial, screen_layout_file, app_name)
		if app_name.eql?("wechat") then
			puts "this is wechat!"
			res1 = execute_shell_cmd("grep 'wxid_wuel5b5ivo7812' #{screen_layout_file}").strip()
			res2 = execute_shell_cmd("grep 'Log In' #{screen_layout_file}").strip()
			if (not res1.eql?("")) && (not res2.eql?("")) then
				return true
			else
				puts "we are fine, not in the login page ..."
				return false
			end
		else
				return false
		end
	end

	def UTIL.login(avd_serial, app_name)
		if app_name.eql?("wechat") then
				puts "this is wechat, enter accound info ..."
				execute_shell_cmd("timeout 2s python ./bin/events/edit_by_resource_id_with_specified_content.py #{avd_serial} 'com.tencent.mm:id/bd3' 'hezuotest@123'")
				execute_shell_cmd("timeout 2s python ./bin/events/click_by_resource_id.py #{avd_serial} 'com.tencent.mm:id/bd4'")
				sleep 5
		end
	end
	
end
