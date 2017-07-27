# ApkTrack

ApkTrack is a simple Android application which periodically checks if your installed apps can be updated.

It was created for users who don't use the Google Play Store, but still need to know when new APKs are available for their apps. ApkTrack performs simple website scraping to grab the latest versions of packages present on the device.

This application is distributed under the terms of the [GPL v3 License](https://www.gnu.org/licenses/gpl.html).

-------------------------------

## Usage

![ApkTrack screenshot](http://img4.hostingpics.net/pics/168848apktracksmall.png)

* Click on an application to perform a manual version check.
* The buttons at the top are used to respectively refresh the installed application list and perform a version check for all applications.
* Additional buttons allow you to change the way applications are sorted, and to access ApkTrack's settings.

That's it!

## Things to keep in mind

* Applications are *not* updated automatically. You still have to find a way to download the latest APKs and sideload them yourself. ApkTrack is simply here to tell you that the update is available.
* Updates, installations and uninstallations are detected automatically by the application in most cases. When it fails, you can press the ![](http://img4.hostingpics.net/pics/230860icmenufind.png) button to refresh the installed apps.
* ApkTrack uses regular expressions to scrape webpages, so it may cease to work without notice if the target websites are modified.
* Although there is a background service checking for updates every day, it may get killed by the OS. Remember to check for updates manually in the application from time to time.
* I am by no means an Android developper. This is a project I hacked quickly because I was tired of checking updates manually. If you are learning Android development, what you see in the code should definitely not be considered best practice. You're welcome to point out what I did wrong, though!

-------------------------------

### Download
A precompiled version of the application can be found here: [ApkTrack 1.1](http://kwiatkowski.fr/apktrack/ApkTrack.apk).  
If you want to help me test ApkTrack, feel free to use the [beta version](http://kwiatkowski.fr/apktrack/ApkTrack_beta.apk). More features are implemented, but bugs may occur! Be sure to report them!

### Donations
ApkTrack is completely free, and I don't expect any kind of compensation for using this application. I do like Bitcoins though, so if you want to send some my way, here's an address you can use: ```19wFVDUWhrjRe3rPCsokhcf1w9Stj3Sr6K```  
Feel free to drop me a line if you donate to the project, so I can thank you personally!

### Contact
[![](http://img11.hostingpics.net/pics/871895mailbutton.png)](mailto:justicerage *at* manalyzer.org)
[![](http://img11.hostingpics.net/pics/637656twitterbutton.png)](https://twitter.com/JusticeRage)