CaveSurvey
==========

Cave surveying application for Android devices


The Story
=========

Preparing a cave map is a tough activity. For example if you want to map a gallery you usually pick reference points for the main polyline going trough that gallery. Between every two stations distance, angle and inclination are measured. On each station (and at any other specific gallery place) the distances to the top, bottom and both sides are measured. Usually this is written to a sheet of paper, later transferred into Excel spreadsheet and processed with a variety of existing tools.
Well, in real life this process is much harder and error prone than it sounds. CaveSurvey targets the process of collecting measurements and tries to improve it. See examples in the [User Guide](https://github.com/lz1asl/CaveSurvey/wiki/User-Guide).


Goals
=====

CaveSurvey is a tool for mapping caves using (at least) an Android device and (optionally) a laser distance meter and/or other measuring devices by:
 - keeping the measurements sheet in digital format during the survey (thus eliminating typo errors)
 - making it easy to enter measurements (because not all caves are a pleasant place to stay)
 - allowing you to export the data in Excel format for further processing (and not being a tool for the creation of a final map, such tools already exist and are awesome)
 - drawing a simple map of the main line (allowing the detection of big measurement erros on site)
 - allowing the creation of simple sketches
 - aiding measurement collection (reducing the number of measuring instruments needed and/or the need to manually type the value):
  - by using built-in sensors if available (as compass and clinometer) and tools (take pictures, save the GPS locations of the entrances, type notes, etc)
  - by using Bluetooth to integrate external laser distance meters (to read distance and in some cases compass and clinometer measurements)


Features
========

 Currently with CaveSurvey you can:
  - make multiple cave surveys
  - split a survey into galleries
  - add stations and interim points
  - save notes, pictures, drawings, GPS coordinates and vectors at any station
  - input measurements:
   - manually
   - from the built-in sensors
   - from Bluetooth
  - preview a 2D plan/section map of the current survey
  - export to Excel

  See the current [issues](https://github.com/lz1asl/CaveSurvey/issues) to see what we are working on.
  The supported languages so far are English and Bulgarian.
  

Notes
=====

One of the possible target configurations is to read the distance and inclination from Bluetooth and use the built-in compass of the device. In this case both a waterproof Android 4.x device and CEM iLDM-150 cost about $250.

Test devices:
  - We have tested from Android 2.1(cheap and small) up to 5.0 (better hardware and protection). Smartphones are recommended to have water protection (to survive), a compass (to read from), SPP Bluetooth profile (to connect instruments).
  - [CEM iLDM-150](https://github.com/lz1asl/CaveSurvey/wiki/iLDM-150) laser distance meter (IP54) with built-in clinometer and Bluetooth
  - [Trimble LaserAce 1000](https://github.com/lz1asl/CaveSurvey/wiki/Laser-Ace-100) (very accurate) that provides distance, clinometer and azimuth
  - [LTI TruPulse 360B](https://github.com/lz1asl/CaveSurvey/wiki/LTI-TruPulse-360B)
  - Feel free to join our team and help adding other devices/functionalities
  


Precision
=========

Having precise instruments is important to do a proper work. Anyway in most caves centimeter precision is never possible.

For iLDM-150 CEM have specified precision of 1.5mm for the distance and 0.5' from the clinometer.
For LaserAce [this](https://www.trimble.com/mappingGIS/laserace1000.aspx) is the manifacturer info provided.
For the Android build-in compass - you have to consult your device manifacturer, but if small enough and you use short legs there should be no problem (error will distribute and compensate anyway).

If you are paranoic you can still use CaveSurvey in manual mode only - type the proper values from the existing tools you use. It will save you the Excel work later.


Links
====

Another Android app is [Abris](https://play.google.com/store/apps/details?id=com.shturmsoft.abris&hl=en). They are already alive and have focus on creating the whole map underground. I have to admit they have done a great job, but I'm not confident in drawing stuff with muddy fingers.


About
=====

Developed by members of caving club [Paldin](http://sk-paldin.eu/) Plovdiv, Bulgaria.

![Picture](res/drawable-mdpi/paldin.jpg)

Use the official version at [Google Play](https://play.google.com/store/apps/details?id=com.astoev.cave.survey) or the [latest build](https://razhodki.ci.cloudbees.com/job/CaveSurvey/lastSuccessfulBuild/artifact/build/apk/CaveSurvey-defaultFlavor-release.apk) for devces without Google Play (tested to work on BlackBerry Q5).

![CloudBees](http://www.cloudbees.com/sites/default/files/Button-Built-on-CB-1.png)


This software is free to use and modify. Well, we provide no guarantee in any kind but are open for ideas and collaborations. Contact us at cave.survey.project@gmail.com.
