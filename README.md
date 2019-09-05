# Stoat Prototype
Stoat (STochastic model App Tester) is a guided approach to perform stochastic model-based testing on Android Apps. The idea is to thoroughly test the functionalities of an app from its GUI model, and validate the app’s behavior by enforcing various user/system interactions. 

# Update

We have made all the Stoat's source code publicly available. We hope this project can benefit other researchers or practiontiners in the field of automated GUI testing of Android apps. Feel free to contact us if you have any questions and issues. We will continue to maintain this project. Thanks for your feedback.

Next step: We will integrate Jacoco with Stoat to handle gradle-based Android projects.

# Publication
[1] Guided, Stochastic Model-based GUI Testing of Android Apps (FSE'2017)

```
@inproceedings{DBLP:conf/sigsoft/SuMCWYYPLS17,
  author    = {Ting Su and
               Guozhu Meng and
               Yuting Chen and
               Ke Wu and
               Weiming Yang and
               Yao Yao and
               Geguang Pu and
               Yang Liu and
               Zhendong Su},
  title     = {Guided, stochastic model-based {GUI} testing of Android apps},
  booktitle = {Proceedings of the 2017 11th Joint Meeting on Foundations of Software
               Engineering, {ESEC/FSE} 2017, Paderborn, Germany, September 4-8, 2017},
  pages     = {245--256}
}
```

[2] FSMdroid: Guided GUI Testing of Android Apps (ICSE'16, ACM SRC)

```
@inproceedings{FSMdroid16,
  author    = {Ting Su},
  title     = {FSMdroid: guided {GUI} testing of android apps},
  booktitle = {Proceedings of the 38th International Conference on Software Engineering,
               {ICSE} 2016, Austin, TX, USA, May 14-22, 2016 - Companion Volume},
  pages     = {689--691},
  year      = {2016}
}
```

[3] Large-Scale Analysis of Framework-Specific Exceptions in Android Apps (ICSE'18)

```
@inproceedings{FanSCMLXPS18,
  author    = {Lingling Fan and
               Ting Su and
               Sen Chen and
               Guozhu Meng and
               Yang Liu and
               Lihua Xu and
               Geguang Pu and
               Zhendong Su},
  title     = {Large-scale analysis of framework-specific exceptions in Android apps},
  booktitle = {Proceedings of the 40th International Conference on Software Engineering,
               {ICSE} 2018, Gothenburg, Sweden, May 27 - June 03, 2018},
  pages     = {408--419},
  year      = {2018}
}
```

# Quick Review

Click this [link](https://tingsu.github.io/files/stoat.html) for quick review on the approach workflow, evaluation results, demo and etc.

# Setup 

You can checkout Stoat from this repo.

### Environment Configration
* Ruby: 2.1 

* [Nokogiri](http://www.nokogiri.org/tutorials/installing_nokogiri.html)

If your default ruby version is lower than 2.1, the installation of Nokogiri will fail. In this case, please [upgrade ruby to 2.1 or higher](https://stackoverflow.com/questions/26595620/how-to-install-ruby-2-1-4-on-ubuntu-14-04). If the installation still fails due to [this issue](https://github.com/github/pages-gem/issues/133), you need to execute "sudo apt-get install ruby2.1-dev".

* Python: 2.7

* Android SDK: API 18+ 

Android SDK recently makes an update about emulators. If you cannot start the emulator (like "Cannot launch AVD in emulator:QT library not found"), please check [this post](https://stackoverflow.com/questions/42554337/cannot-launch-avd-in-emulatorqt-library-not-found) for solution. You can create an emulator before running Stoat. See [this link](https://stackoverflow.com/questions/43275238/how-to-set-system-images-path-when-creating-an-android-avd) for how to create avd using [avdmanager](https://developer.android.com/studio/command-line/avdmanager).

E.g. 

1. sdkmanager  "system-images;android-18;google_apis;x86"

2. avdmanager create avd --force --name testAVD_1 --package 'system-images;android-18;google_apis;x86' --abi google_apis/x86 --sdcard 512M --device 'Nexus 7'

* Ubuntu 14.04/Linux

* uiautomator (A python wrapper of [UIAutomator](https://github.com/xiaocong/uiautomator))


We strongly recommend to run Stoat on a physical machine to ensure the performance instead of ruuning on virtual machines (e.g., VirtualBox or Docker). In addition, please choose x86 image if you use Android emulators, and [setup hardware acceleration](https://developer.android.com/studio/run/emulator-acceleration.html) for [Ubuntu](https://help.ubuntu.com/community/KVM/Installation).

Please export ANDROID_HOME (for android sdk), PYTHON_PATH (for uiautomator), CLASSPATH (for soot)

 Example:
  ```
  export ANDROID_HOME="/home/XX/Android/Sdk"
  export PYTHONPATH="/home/XX/uiautomator"
  export CLASSPATH="/home/XX/fsmdroid/soot-github/lib/soot-develop.jar
  export PATH=$PATH:${ANDROID_HOME}/build-tools/25.0.0:${ANDROID_HOME}/emulator:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:
  ```
You may also need to modify "Stoat/CONF.txt" to set the tool path.
  
# Usage

Stoat provides several ways to test android apps by command lines. Note before running Stoat, please disable keyboard (for emulator, add "hw.keyboard=yes" in its config.ini; for real device, please install the "com.wparam.nullkeyboard_1.apk" at "Stoat/bin/sdcard", and configure it to be the default input method)
Please also add "hw.mainKeys=yes" in the config.ini to disable the soft "main" keys if you use emulators, which may affect Stoat's exploration.

1. Ant opens-soruce projects

 	ruby run_stoat_testing.rb --app_dir /home/XX/caldwell.ben.bites_4_src --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 --project_type ant 
 	
2. apk without instrumentation

	**Note this may mitigate Stoat's power due to lack of coverage info for test optimization. Otherwise, you need to instrument apk with Ella**
    
	ruby run_stoat_testing.rb --app_dir /home/XX/Bites.apk --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 (the output will be under "/home/XX/Bites-output")
 	 
3. Use real device, ant projects

	Please open wifi, and disable keyboard before do testing on real device.
    
 	ruby run_stoat_testing.rb --app_dir /home/XX/caldwell.ben.bites_4_src/ --real_device_serial cf00b9e6 --stoat_port 2000 --project_type ant 
	
4. A list of apps (If they are apks, append the option "--project_type apk")

	ruby run_stoat_testing.rb --apps_dir /home/XX/test_apps/ --apps_list /home/XX/test_apps/apps_list.txt --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 --force_restart 


### Subject Requirement:
* instrumented apps (use [Emma](http://emma.sourceforge.net/index.html) for open-source apps and [Ella](https://github.com/saswatanand/ella) for closed-source apps) should end with "-debug.apk"
* closed-source/non-instrumented apk can also be tested, and its name should end with ".apk" 


# Output

stoat_fsm_building_output: the outputs of model construction. 

	
	 crashes/ -- crash report (include crash stack, event trace, screen shots); 
	 ui/ -- ui xml files; 
	 coverage/ -- coverage files during model construction; 
	 FSM.txt/app.gv -- xdot model graph; 
	 fsm_building_process.txt/fsm_states_edges.txt -- the model building process, mainly the increasing coverage/#states/#edges 
	 CONF.txt -- configuration file 
         
	 
stoat_mcmc_sampling_output: the outputs of mcmc sampling. 
    
    	
	 crashes/ -- crash report (include crash stack, event trace, screen shots); 
	 MCMC_coverage/ -- the coverage data during mcmc sampling; 
	 mcmc_sampling_progress.txt/mcmc_data.txt -- mcmc sampling progress data; 
	 initial_markov_model.txt/optimal_markov_model.txt/mcmc_models.txt -- the initial/optimal/all mcmc sampling models; 
	 mcmc_all_history_testsuites.txt -- all executed test suites for mcmc sampling; 
	 test_suite_to_execute.txt -- the current test suite under execution;
	 CONF.txt -- configuration file. 
	 
     
coverage: the all coverage data during two phases

## Benchmark

Some benchmark apps used in our paper.

##  Notes
* This implementation has been tested with Android 4.4, running on Ubuntu 14.04. 
* If measure statement coverage for open-sourced apps, the subjects need to be processed to support EMMA instrumentation:
(Please refer to [Dynodroid](https://code.google.com/archive/p/dyno-droid/) for details.) You can also refer to the apps in the benchmark.
* This version only supports testing ant projects.

## Contact
[Ting Su](http://tingsu.github.io/)
All Copyright Reserved.

## TODO
1. use monkey to bypass welcome page in google play apps

2. directly use monkey to start the app instead of using "am", the monkey way is more robust.

## Papers that uses, extends or compares with Stoat

1. Yifei Lu, Minxue Pan, Juan Zhai, Tian Zhang, Xuandong Li; Preference-wise testing for Android applications. ESEC/SIGSOFT FSE 2019: 268-278

2. Tianxiao Gu, Chengnian Sun, Xiaoxing Ma, Chun Cao, Chang Xu, Yuan Yao, Qirun Zhang, Jian Lu, Zhendong Su; Practical GUI testing of Android applications via model abstraction and refinement. ICSE 2019: 269-280

3. Wenyu Wang, Dengfeng Li, Wei Yang, Yurui Cao, Zhenwen Zhang, Yuetang Deng, Tao Xie; An empirical study of Android test generation tools in industrial cases. ASE 2018: 738-748

4. Yuanchun Li, Ziyue Yang, Yao Guo, Xiangqun Chen; A Deep Learning based Approach to Automated Android App Testing. CoRR abs/1901.02633 (2019)

5. Yu Zhao, Tingting Yu, Ting Su, Yang Liu, Wei Zheng, Jingzhi Zhang, William G. J. Halfond; ReCDroid: automatically reproducing Android application crashes from bug reports. ICSE 2019: 128-139

6. Sen Chen, Lingling Fan, Chunyang Chen, Ting Su, Wenhe Li, Yang Liu, Lihua Xu; StoryDroid: automated generation of storyboard for Android apps. ICSE 2019: 596-607

7. Chunyang Chen, Ting Su, Guozhu Meng, Zhenchang Xing, Yang Liu; From UI design image to GUI skeleton: a neural machine translator to bootstrap mobile GUI implementation. ICSE 2018: 665-676

8. Lingling Fan, Ting Su, Sen Chen, Guozhu Meng, Yang Liu, Lihua Xu, Geguang Pu; Efficiently manifesting asynchronous programming errors in Android apps. ASE 2018: 486-497



