CaveSurvey
==========

Cave surveying application for Android devices.

CaveSurvey tries to help you collect measurements data underground. See our [User Guide](https://github.com/lz1asl/CaveSurvey/wiki/User-Guide) for details or read further.


Why
===

There are several approaches for digital surveying and most of them rely on the famous DistoX. There are much less choises if you don't have one. We have started CaveSurvey as a classic digitalized sheet. Later we started to leverage the Android build-in features and Bluetooth connected measurement devices. CaveSurvey will help you by:
 - keeping the measurements sheet in digital format during the survey process (thus eliminating typo errors)
 - making it easy to enter measurements (because not all caves are a pleasant place to stay)
 - allowing you to export the collected data for further processing (and not being a tool for the creation of the final map, such tools already exist and are awesome)
 - drawing a simple map of the main line (allowing the detection of big measurement erros on site)
 - allowing the creation of simple sketches
 - aiding measurement collection (reducing the number of measuring instruments needed and/or the need to manually type the value):
  - by using built-in sensors if available (as compass and clinometer) and tools (take pictures, save the GPS locations of the entrances, type notes, etc)
  - by using Bluetooth to integrate external laser distance meters (to read distance and in some cases compass and clinometer measurements), see the list of supported devices below


Where are we now
================

We have started with an old Android 2.1 phone (cheap and small) up tested to 7.0 (better hardware and protection). Smartphones are recommended to have water protection (to survive), a compass (to read from) and a SPP or LE Bluetooth profile (to connect instruments). The APK should also work on BlackBerry Q5 and probably others.

Currently with CaveSurvey you can:
  - make multiple cave surveys
  - split a survey into galleries
  - add stations and interim points
  - save notes, pictures, drawings, GPS coordinates and vectors at any station
  - input measurements:
    - manually
    - from the built-in sensors
    - from several Bluetooth enabled [devices](https://github.com/lz1asl/CaveSurvey/wiki/Measurement-Devices)
  - preview a 2D plan/section map of the current survey
  - export to Excel, AutoCad, Therion, KML and more

See the current list of [issues](https://github.com/lz1asl/CaveSurvey/issues) we are working on. Feel free to [join](https://github.com/lz1asl/CaveSurvey/wiki/CaveSurvey-Development) our team and help adding other devices/functionalities.


About
=====

Developed by members of caving club [Paldin](http://sk-paldin.eu/) Plovdiv, Bulgaria with cavers help around the world.

![Picture](src/main/res/drawable-mdpi/paldin.jpg)

You can use the official version at [Google Play](https://play.google.com/store/apps/details?id=com.astoev.cave.survey) or the [latest test builds](https://github.com/lz1asl/CaveSurvey/actions?query=is%3Asuccess%2C+branch%3Amaster). [Releases history](https://github.com/lz1asl/CaveSurvey/wiki/Releases).


This software is free to use and modify.
We do not provide warranty of any kind.

Contact us at cave.survey.project@gmail.com.


Related Projects
================

[CaveSurveyBTEmulator](https://github.com/lz1asl/CaveSurveyBTEmulator) can emulate Bluetooth measurement devices.

[CaveSurveyReports](https://github.com/lz1asl/CaveSurveyReports) collects error reports for CaveSurvey.

[OpensTopo](http://www.openspeleo.org/openspeleo/openstopo.en.html) export is used. 

