# Stoat
Stoat (STochastic model App Tester) is a guided approach to perform stochastic model-based testing on Android Apps. The idea is to thoroughly test the functionalities of an app from its GUI model, and validate the app’s behavior by enforcing various user/system interactions. 

# Publication
[1] Guided, Stochastic Model-based GUI Testing of Android Apps (FSE'2017)

```
@InProceedings{tingsu:stoat:17,
    author = {Ting Su, Guozhu Meng, Yuting Chen, Ke Wu, Weiming Yang, Yao Yao, Geguang Pu, Yang Liu, Zhendong Su},
    title = {Guided, Stochastic Model-based GUI Testing of Android Apps},
    booktitle = {The 11th joint meeting of the European Software Engineering Conference and the ACM SIGSOFT Symposium on the Foundations of Software Engineering},
    year = {2017}
} 
```

[2] FSMdroid: Guided GUI Testing of Android Apps (ICSE'16, ACM SRC)

```
@inproceedings{Su16,
  author    = {Ting Su},
  title     = {FSMdroid: guided {GUI} testing of android apps},
  booktitle = {Proceedings of the 38th International Conference on Software Engineering,
               {ICSE} 2016, Austin, TX, USA, May 14-22, 2016 - Companion Volume},
  pages     = {689--691},
  year      = {2016}
}
```

# Quick Review

Click this [link](https://sites.google.com/site/stoat2017/) for quick review on the approach workflow, evaluation results, demo and etc.

# Setup 

You can checkout Stoat from this repo.

### Environment Configration
* Ruby: 2.2

* Python: 2.7

* Android SDK: API 18/19

* Ubuntu 14.04/Linux 

We strongly recommend to run Stoat on a physical machine to ensure the performance instead of ruuning on virtual machines (e.g., VirtualBox or Docker). In addition, please choose x86 image if you use Android emulators, and [setup hardware acceleration](https://developer.android.com/studio/run/emulator-acceleration.html) for [Ubuntu](https://help.ubuntu.com/community/KVM/Installation).

# Usage

Stoat provides several ways to test android apps by command lines.

### Subject Requirement:
* to fully open-source apps should be instrumented 
* closed-source/non-instrumented apk name should end with ".apk" 
