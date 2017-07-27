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

module Uid
  require 'nokogiri'

  UID = File.dirname(__FILE__)
  AT = UID + "/../tools/apktool.jar"

  def Uid.change_uid(bef, aft)
    q = " > /dev/null 2>&1"
    dir = rand(36**8).to_s(36) # random string with size 8     
    system("java -Djava.awt.headless=true -jar #{AT} d -f --no-src --keep-broken-res #{bef} -o #{dir} #{q}")
	  # system("java -Djava.awt.headless=true -jar #{AT} d -f --no-src --keep-broken-res #{bef} -o #{dir} ")
    meta = dir + "/AndroidManifest.xml"
    f = File.open(meta, 'r')
    doc = Nokogiri::XML(f)
    f.close

    pref = "android"
    roots = doc.xpath("/manifest")
    roots.each do |root|
      root["#{pref}:sharedUserId"] = "umd.troyd"
    end
    
    apps = doc.xpath("/manifest/application")
	apps.each do |app|
      app["#{pref}:process"] = "a3e.process"
    end
    
    debuggs = doc.xpath("/manifest/application")
	debuggs.each do |debugg|
      debugg["#{pref}:debuggable"] = "true"
    end
    
    f = File.open(meta, 'w')
    doc.write_xml_to(f)
    f.close
    system("java -jar #{AT} b -f #{dir} -o #{aft} #{q}")
    # system("java -jar #{AT} b -f #{dir} -o #{aft} ")
    system("rm -rf #{dir}")
  end
end
