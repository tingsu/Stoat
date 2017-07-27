## Copyright (c) 2011-2012,
##  Jinseong Jeon <jsjeon@cs.umd.edu>
##  Jeff Foster   <jfoster@cs.umd.edu>
## All rights reserved.
##
## Redistribution and use in source and binary forms, with or without
## modification, are permitted provided that the following conditions are met:
##
## 1. Redistributions of source code must retain the above copyright notice,
## this list of conditions and the following disclaimer.
##
## 2. Redistributions in binary form must reproduce the above copyright notice,
## this list of conditions and the following disclaimer in the documentation
## and/or other materials provided with the distribution.
##
## 3. The names of the contributors may not be used to endorse or promote
## products derived from this software without specific prior written
## permission.
##
## THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
## AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
## IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
## ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
## LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
## CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
## SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
## INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
## CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
## ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
## POSSIBILITY OF SUCH DAMAGE.

class AVD
    
  def initialize(avd, opt="")
    @avd = avd
    @opt = opt
  end

  ADR = "android"
  AL = ADR + " list"

  def exists?
    `#{AL} avd`.include? @avd
  end

  def delete
    system("#{ADR} delete avd -n #{@avd}")
  end

    #A10 = ADR + "-10"
    #AOPT = "-s WQVGA432" # resolution option
    
    # Modified by Ting, 01/30/15
    A10 = ADR + "-16"  # android 4.1.2
    # Note: make sure you use the exact same device type, "Nexus 7".
    # On Mac OS, the "-d" option could be specified
    # On Ubuntu, the "-d" option is missing? So you have to manually create the exact device type
    AOPT = "-s HVGA -c 128M --abi armeabi-v7a -d \"Nexus 7\"" # resolution option and ABI

  def create
    delete
    system("echo | #{ADR} create avd -n #{@avd} -t #{A10} #{AOPT}")
  end

  EM = "emulator"
  OPT = "-cpu-delay 0 -netfast -no-snapshot-save"

  def start
    system("#{EM} -avd #{@avd} #{OPT} #{@opt} &")
  end

  ARM = EM + "-arm"

  def stop
    p = RUBY_PLATFORM.downcase
    system("pkill #{ARM}")            if p.include? "linux"
    system("killall #{ARM}")          if p.include? "darwin"
    system("taskkill /IM #{ARM}.exe") if p.include? "mswin"
  end
  
  def get_avd_name ()
    @avd
  end
  
  # check whether the avd is online and get its serial
  # @return the serial if it is online, otherwise "nil"
  def query_avd_serial ()
      
      target_avd_serial = ""
      
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
                      
                      # try 10 times
                      for try in 1..10 do
                          
                          # wait 2 seconds
                          sleep 2
                          
                          # Note the wait time is set as long enough so that we cat get the avd name
                          output = `(sleep 3.0; echo 'avd name') | telnet #{localhost} #{avd_terminal_port}`
                          
                          name_contained = output=~ /OK\s+.+\s+OK/
                          if name_contained != nil then
                              output.gsub(/OK\s+.+\s+OK/) {|match_name|
                                  # puts match_name
                                  arr_2 = match_name.split(/\s+/)
                                  avd_name = arr_2[1]
                                  
                                  if avd_name.eql?(@avd) then
                                      target_avd_serial = avd_serial
                                      puts "[AVD-#{@avd}] I: we find the target emulator, name: #{@avd}, serial: #{target_avd_serial}"
                                      break
                                  else
                                      # it is not the target avd
                                      break
                                  end
                              }
                              break
                          else
                              # try again until we get the avd name
                              puts "[AVD-#{@avd}] Warning: failed to get the avd name by telnet! Let's try again! "
                              sleep 2
                          end
                          
                      end # the end of the for loop
                      
                      puts "[AVD-#{@avd}] I: break out of the loop"
                    }
               end # the end of if "em!=nil"
                      
            if not target_avd_serial.eql?("") then
               # we already found the target emulator :)
               puts "[AVD-#{@avd}] I: we found the emulator serial, stop querying."
               break
            end

        end # the end of do

        return target_avd_serial
                    
    end # the end of if "os_version"
      
    end
    
    
    def query_avd_state (avd_serial)
        devices = `adb devices`
        avd_state = ""
        devices.each_line do |line|
            em = line=~ /emulator-\d+\s+.+$/
            if em!= nil then
                arr_2 = line.split(/\s+/)
                if arr_2[0].strip.eql?(avd_serial) then
                    avd_state = arr_2[1].strip
                    puts "[AVD-#{@avd}] state: #{avd_state}"
                    break
                end
            end
        end
        avd_state
    end


end
