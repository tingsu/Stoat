## Copyright (c) 2014-2015
##  Ting Su <tsuletgo@gmail.com>
## All rights reserved.

#! /usr/bin/env ruby

class Device
    
    @task_id # i.e., process id of the testing task
    @task_start_time # the start time of the task
    @is_available # is the device available?
    
    @communication_port # the port between a3e & java server
    
    def initialize(device_name, emulator_serial, device_state)
        # e.g., testAVD_1
        @device_name = device_name
        # e.g., emulator-5554
        @emulator_serial = emulator_serial
        # e.g., device/offline
        @device_state = device_state
        
        @task_id = 0
        @task_start_time = 0
        @is_available = 1
    end
    
    def is_available ()
        if @is_available == 1
            return true
            else
            return false
        end
    end
    
    def get_device_name()
        @device_name
    end
    
    def get_device_serial()
        @emulator_serial
    end
    
    def set_not_available()
        @is_available = 0
    end
    
    def set_available()
        @is_available = 1
    end
    
    def set_communication_port(port)
        @communication_port = port
    end
    
    def get_communication_port()
        @communication_port
    end
    
end


#def detectRealDevices()
#    dev = "adb devices | grep -w \"device\" "
#    out = `#{dev}`
#    puts out
#    
#    file_name = "real_devices.txt"
#    if File.exist?(file_name)
#        File.delete(file_name)
#    end
#    
#    open(file_name, "a") { |f|
#        out.each_line do |line|
#            f.puts line
#            arr = line.split()
#            puts arr[0]
#            puts arr[1]
#        end
#    }
#        
#end
#
#detectRealDevices()

