#!/bin/sh
sed 's/^$/xxxxxx/' ../COPYING|tr '\n' ' '|sed 's/xxxxxx/\n\n/g'|sed 's/ \+/ /g' >../res/raw/license

