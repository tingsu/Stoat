# Copyright
# tsuletgo@gmail.com


# avd info
class AVD_INFO
    @avd_name #e.g., testAVD_1
    @avd_serial #e.g., emulator-5554
    @avd_terminal_port #e.g., 5554
    @avd_state #e.g., offline, device
    
    def initialize(avd_name, avd_serial, avd_terminal_port)
        @avd_name = avd_name
        @avd_serial = avd_serial
        @avd_terminal_port = avd_terminal_port
    end
    
    # get avd name
    def get_avd_name ()
        @avd_name
    end
    
    # get avd serial
    def get_avd_serial ()
        @avd_serial
    end
    
    def get_avd_terminal_port ()
        @avd_terminal_port
    end
    
end


# This module manages the status of android emulators

module EMULATOR
    
    @@DEBUG_STRING = "[Emulator] "
    
    # the avd mapping table of avd's serial and avd's name
    @@avd_mapping_table = Hash.new
    
    
    # set up the mapping table: [avd'serial --- avd's name]
    # the avd serial is used to direct command to the specified avd
    # the avd name is used to start the specified avd
    # How to get the avd name:
    # http://stackoverflow.com/questions/8381998/how-to-get-android-avd-name-from-serial-number#new-answer
    def EMULATOR.setup_device_map ()
        
        os_version = `uname`
	#puts "os_version:"+os_version
        if os_version.include?("Darwin") then
        
            # telnet: connect to address ::1: Connection refused
            # http://serverfault.com/questions/260765/telnet-connect-to-address-1-connection-refused
            localhost = "127.0.0.1"
            
            devices = `adb devices`
            devices.each_line do |line|
                # puts line
                em = line=~ /emulator-\d+\s+device$/
                if em!=nil then
                    line.gsub(/emulator-\d+/) { |match|
                        # emulator-5554
                        arr = match.split("-")
                        avd_serial = match
                        avd_terminal_port = arr[1]
                        
                        
                        emulator_name = nil
                        
                        while emulator_name == nil do
                            
                        # Note the wait time is set as long enough so that we cat get the avd name
                        output = `(sleep 6.0; echo 'avd name') | telnet #{localhost} #{avd_terminal_port}`
                        
                        name_contained = output=~ /OK\s+.+\s+OK/
                        if name_contained != nil then
                            output.gsub(/OK\s+.+\s+OK/) {|match_name|
                                # puts match_name
                                arr_2 = match_name.split(/\s+/)
                                avd_name = arr_2[1]
                                
                                # create an avd info object
                                avd_info = AVD_INFO.new(avd_name, avd_serial, avd_terminal_port)
                                # add this object into the mapping table
                                @@avd_mapping_table.store(avd_name, avd_info)
                                puts "#{@@DEBUG_STRING} I: we got the emulator name: #{avd_name}"
                            }
                            break
                        else
                            # try again until we get the avd name
                            puts "#{@@DEBUG_STRING} Warning: failed to get the avd name by telnet! Let's try again! "
                            sleep 2
                        end
                        
                      end
                    }
                end
            end
            
        elsif os_version.include?("Linux") then #Linux
            #puts "I'm Linux"
            avd_name = "testAVD_1"
            avd_serial = "emulator-5554"
            avd_terminal_port = "5554"
            avd_info = AVD_INFO.new(avd_name, avd_serial,avd_terminal_port)
            @@avd_mapping_table.store(avd_name, avd_info)
            avd_info2 = AVD_INFO.new("testAVD_2", "emulator-5556","5556")
            @@avd_mapping_table.store("testAVD_2", avd_info2)
        else
            #puts "I'm else"
        end
        
        # output the avd mapping table
        puts "------------"
        puts "avd_name  avd_serial      avd_terminal_port"
        @@avd_mapping_table.each { |avd_name, avd_info|
            puts "#{avd_name}  #{avd_info.get_avd_serial}     #{avd_info.get_avd_terminal_port}"
        }
        puts "------------"
    end

    # query the avd serial by the avd name
    def EMULATOR.query_avd_serial (avd_name)
        puts "#{@@DEBUG_STRING} the mapping table size: #{@@avd_mapping_table.size}"
        res = @@avd_mapping_table.has_key?(avd_name)
        if res == false then
            puts "#{@@DEBUG_STRING} #{avd_name} is not in the mapping table. "
            nil
        else
            puts "#{@@DEBUG_STRING} #{avd_name} is online, found it. "
            avd_info = @@avd_mapping_table.fetch (avd_name)
            avd_info.get_avd_serial
        end
    end
    
    # query the avd state by the avd serial
    def EMULATOR.query_avd_state (avd_serial)
        devices = `adb devices`
        avd_state = ""
        devices.each_line do |line|
            em = line=~ /emulator-\d+\s+.+$/
            if em!= nil then
                arr_2 = line.split(/\s+/)
                if arr_2[0].strip.eql?(avd_serial) then
                    avd_state = arr_2[1].strip
                    puts "#{@@DEBUG_STRING} #{avd_serial}, state: #{avd_state}"
                    break
                end
            end
        end
        avd_state
    end

end
