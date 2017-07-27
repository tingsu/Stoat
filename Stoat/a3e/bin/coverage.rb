## Copyright (c) 2015-2017
## Ting Su <tsuletgo@gmail.com>
## All rights reserved.

# the coverage reporter

class Coverage
    
    @total_packages
    @total_classes
    @total_methods
    @total_blocks
    @total_executable_files
    @total_executable_lines
    
    @covered_classes
    @covered_methods
    @covered_blocks
    @covered_lines
    
    # TODO the coverage weight is set uniformly as 0.25
    @@coverage_weight
    
    # the debug flag
    @@debug_flag = "[Coverage] "
    
    def initialize ()
        # set the coverage weight
        @@coverage_weight = 0.25
    end
    
    # set class coverage
    def setClassCoverage (cclasses, tclasses)
        @covered_classes =cclasses
        @total_classes = tclasses
    end
    # set method coverage
    def setMethodCoverage (ccmethods, tmethods)
        @covered_methods = ccmethods
        @total_methods = tmethods
    end
    # set block coverage
    def setBlockCoverage (cblocks, tblocks)
        @covered_blocks = cblocks
        @total_blocks = tblocks
    end
    
    # set line coverage
    def setLineCoverage (clines, tlines)
        @covered_lines = clines
        @total_executable_lines = tlines
    end
    
    # get line coverage
    def getLineCoverage
        @covered_lines.to_i-30.7
    end
    
    def getTotalExecutableLine
        @total_executable_lines.to_i-95
    end
    
    # remove the additional lines from Emma Instrumentation
    def getLineCoveragePercentage
        (@covered_lines.to_i-30.7)*100.0/(@total_executable_lines.to_i-95)
    end
    
    def getMethodCoverage
        @covered_methods.to_i-9
    end
    
    def getTotalMethods
        @total_methods.to_i-22
    end
    
    def getMethodCoveragePercentage
        (@covered_methods.to_i-9)*100.0/(@total_methods.to_i-22)
    end
    
    def getTotalClasses
        @total_classes.to_i-4
    end
    
    # set total packages
    def setTotalpackages (packages)
        @total_packages = packages
    end
    # set total executable files
    def setTotalFiles (files)
        @total_executable_files = files
    end
    
    # get the final coverage which will be used in MCMC sampling
    # It currently returns the line coverage
    def getCoverage
        puts "#{@@debug_flag} I: the final line coverage: #{@covered_lines} "
        @covered_lines
    end

    
    # output coverage
    def outputCoverage
        puts "----------------------------"
        puts "class coverage: #{@covered_classes}/#{@total_classes}"
        puts "method coverage: #{@covered_methods}/#{@total_methods}"
        puts "block coverage: #{@covered_blocks}/#{@total_blocks}"
        puts "line coverage: #{@covered_lines}/#{@total_executable_lines}"
        puts "total packages: #{@total_packages}"
        puts "total executable files: #{@total_executable_files}"
        puts "----------------------------"
    end
    
    # parse the emma coverage report with the "txt" format
    # we currently focus on the overall coverage summary
    # An example file :
    #    [EMMA v2.0.5312 report, generated Mon Feb 09 10:38:28 PST 2015]
    #    -------------------------------------------------------------------------------
    #    OVERALL COVERAGE SUMMARY:
    #
    #    [class, %]	[method, %]	[block, %]	[line, %]	[name]
    #    100% (1/1)	50%  (3/6)!	36%  (27/76)!	38%  (9/24)!	all classes
    #
    #    OVERALL STATS SUMMARY:
    #
    #    total packages:	1
    #    total classes:	1
    #    total methods:	6
    #    total executable files:	1
    #    total executable lines:	24
    #
    #    COVERAGE BREAKDOWN BY PACKAGE:
    #
    #    [class, %]	[method, %]	[block, %]	[line, %]	[name]
    #    100% (1/1)	50%  (3/6)!	36%  (27/76)!	38%  (9/24)!	course.examples.UI.MenuExample
    #    -------------------------------------------------------------------------------
    def parse_emma_coverage_report (report_file)
        
        puts "#{@@debug_flag} I: start to parse the coverage report file: #{report_file}. "
        
        # the coverage info type: "1" - coverage summary, "2" - stats summary, "3" - detail summary
        coverage_info_type = 0
        # is the data here?
        data = 0
        IO.foreach(report_file) { |line|
            
            if line.include? ("COVERAGE SUMMARY") then
                #puts "the coverage [summary] line ... "
                coverage_info_type = 1;
                elsif line.include? ("STATS SUMMARY") then
                #puts "the coverage [stats] line ... "
                coverage_info_type = 2;
            end
            
            ########### extract coverage summary
            if (coverage_info_type == 1) && (line.include? ("[class, %]")) then
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
                        setClassCoverage arr[0], arr[1]
                        puts "#{arr[0]}, #{arr[1]}"
                        elsif (i==2) then
                        setMethodCoverage arr[0], arr[1]
                        elsif (i==3) then
                        setBlockCoverage arr[0], arr[1]
                        elsif (i==4) then
                        setLineCoverage arr[0], arr[1]
                        puts "#{arr[0]}, #{arr[1]}"
                    end
                    i += 1
                }
            end
            
            ######## extract stats summary
            if (coverage_info_type == 2) && (line.include? ("total")) then
                #puts "the stats line ... "
                # get the desciption_statement/number
                arr = line.split(":")
                #puts "#{arr[0]}, #{arr[1].strip}"
                if line.include? ("total packages") then
                    setTotalpackages arr[1].strip
                    elsif line.include? ("total executable files") then
                    setTotalFiles arr[1].strip
                end
            end
        }
        
    end

    
end
