CaveSurvey
==========

Free and open-source cave surveying application for Android devices.

CaveSurvey tries to help you collect measurements data underground. See our [User Guide](https://github.com/lz1asl/CaveSurvey/wiki/User-Guide) for details or read further.


Why
===

There are several approaches for digital surveying and most of them rely on the famous DistoX. There are not many choices if you don't have it. We started the CaveSurvey project as a classic digital sheet. Later we started to leverage the Android build-in features and Bluetooth connected measurement devices. CaveSurvey will help you:
 - keeping the measurements sheet in digital format during the survey process (thus reducing typo errors)
 - aid in measurement collection (reducing the number of measurement instruments needed and/or the need to manually type the value) by:
   - using built-in sensors (such as compass and clinometer) and tools (camera, GPS locations of the entrances, text notes, etc) if available
   - using Bluetooth enabled devices (to read distance and depending on the device also compass and clinometer)
 - allow you to export the collected data for further processing (like final map creation) with other apps
 - show a simple map of the main line (allowing the detection of big measurement errors on site)
 - allow the creation of simple sketches


Current state
================

We have started with an old Android 2.1 phone (cheap and small) and have tested up to Android 9.0 (better hardware and protection). Smartphones are recommended to have water protection, a good compass and a Bluetooth (SPP or LE profile depending on the measurement device). It should also work on BlackBerry Q5 and probably other devices.

Currently with CaveSurvey you can:
  - make multiple surveys
  - split a survey into galleries
  - add stations with classic LRUD and middle points
  - create notes, pictures, drawings, GPS coordinates and vectors at any station
  - input measurements:
    - manually
    - from the built-in sensors
    - from several Bluetooth enabled [devices](https://github.com/lz1asl/CaveSurvey/wiki/Measurement-Devices)
  - preview a 2D plan/section map of the current survey
  - export to Excel, AutoCad, Therion, KML, and more

See the current list of [issues](https://github.com/lz1asl/CaveSurvey/issues) we are working on. Feel free to [join](https://github.com/lz1asl/CaveSurvey/wiki/CaveSurvey-Development) our team and help adding other devices or improve CaveSurvey.


About
=====

Developed by members of caving club [Paldin](http://sk-paldin.eu/) Plovdiv, Bulgaria with cavers' help from around the world.

![Picture](src/main/res/drawable/paldin_logo.jpg)

You can use ~the official version at [Google Play](https://play.google.com/store/apps/details?id=com.astoev.cave.survey) or~ the [latest test builds](https://github.com/lz1asl/CaveSurvey/actions?query=is%3Asuccess%2C+branch%3Amaster). [Releases history](https://github.com/lz1asl/CaveSurvey/wiki/Releases).


This software is free to use and modify.
We do not provide warranty of any kind.

Contact us at cave.survey.project@gmail.com.


Related Projects
================

[CaveSurveyBTEmulator](https://github.com/lz1asl/CaveSurveyBTEmulator) can emulate Bluetooth measurement devices.

[~CaveSurveyReports~](https://github.com/lz1asl/CaveSurveyReports) collects error reports for CaveSurvey until 1.51.

[OpensTopo](http://www.openspeleo.org/openspeleo/openstopo.en.html)'s export is used.

[Underwater survey](https://github.com/f0xdude/cave-mapper/) app by fellow diver

