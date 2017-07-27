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

module Commands
  CTRL = File.dirname(__FILE__)

  require "#{CTRL}/adb"

  def getViews
    no_arg(mtd)
  end
  #tanzir 0=img button, 1 =img view, 2= text view
  def getImgBtnCount
  	no_arg(mtd)
  end
  
  def getImgViewCount
  	no_arg(mtd)
  end
  
  def getTextViewCount
  	no_arg(mtd)
  end
  
  def getImgBtnCnt	
  	no_arg(mtd)
  end
  
  def getImgViewCnt	
  	no_arg(mtd)
  end

  def getActivities
    no_arg(mtd)
  end
  
  def back
    no_arg(mtd)
  end

  def down
    no_arg(mtd)
  end

  def up
    no_arg(mtd)
  end

  def menu
    no_arg(mtd)
  end

  def finish
    no_arg(mtd)
  end

  def edit(idx, what)
    w_idx_what(mtd, idx, what)
  end

  def clear(what)
    w_what(mtd, what)
  end

  def search(what)
    w_what(mtd, what)
  end

  def checked(what)
    w_what(mtd, what)
  end

  def click(what)
    w_what(mtd, what)
  end

  def clickLong(what)
    w_what(mtd, what)
  end

  def clickOn(xy)
    w_at(mtd, xy)
  end

  def clickIdx(idx)
    w_idx(mtd, idx)
  end

  def clickImg(idx)
    w_idx(mtd, idx)
  end
  
  #tanzir
  def clickTxtView(idx)
  	w_idx(mtd, idx)
  end
  
  #####
  
  # Ting
  def getCurrentActivity
      no_arg(mtd)
  end
  
  def dumpCoverage
      no_arg(mtd)
  end
  
  #Added by Ting, 2015/3/24
  def key_event(idx)
    w_idx(mtd, idx)
  end
  
  # click on a menu item, 2015/4/14
  def clickMenuItem(what)
    w_what(mtd, what)
  end
  
  # click on a check box, 2015/4/15
  def clickCheckBox(idx)
    w_idx(mtd, idx)
  end
  
  # click on a radio button, 2015/4/21
  def clickRadioButton(idx)
    w_idx(mtd, idx)
  end
  
  # click on a toggle button
  def clickToggleButton(what)
    w_what(mtd, what)
  end
  
  # invoke keyevent back
  def keyevent_back
    ADB.keyevent_back(mtd)
  end
  
  def clickTextViewByIndex(idx)
    w_idx(mtd, idx)
  end
  
  def clickLongTextViewByIndex(idx)
    w_idx(mtd, idx)
  end
  
  ######
  
  def clickImgBtn(idx)
  	w_idx(mtd, idx)
  end

  def clickImgView(idx)
  	w_idx(mtd, idx)
  end


  def clickItem(idx, item)
    w_idx_item(mtd, idx, item)
  end

  def drag(xy1, xy2)
    w_src_dst(mtd, xy1, xy2)
  end

private

  def mtd
    caller[0] =~ /`([^']*)'/ and $1
  end

  def no_arg(cmd)
    ADB.cmd(cmd, [])
  end

  def w_idx_what(cmd, idx, what)
    ADB.cmd(cmd, [["idx", idx], ["what", what]])
  end

  def w_what(cmd, what)
    ADB.cmd(cmd, [["what", what]])
  end

  def w_idx(cmd, idx)
    ADB.cmd(cmd, [["idx", idx]])
  end

  def w_idx_item(cmd, idx, item)
    ADB.cmd(cmd, [["idx", idx], ["item", item]])
  end

  def w_at(cmd, xy)
    ADB.cmd(cmd, [["at", xy]])
  end

  def w_src_dst(cmd, xy1, xy2)
    ADB.cmd(cmd, [["from", xy1], ["to", xy2]])
  end

end
