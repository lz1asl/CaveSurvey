CaveSurvey
==========

Cave surveying app for Android devices


About
=====

Preparing a cave map is a tough activity. For example if you want to map a gallery you usually pick reference points for the main polyline going trough that gallery. Between every two poins distance, angle and inclination is measured and on each one (and on any other more specific place) distance to the top, bottom and both sides is measured. Usually this is written to a paper sheet, later transformed in to Excel spreadsheet and processed with a variety of existing tools.
Well, in real life this process is much harder and very error prone. CaveSurvey targets the process of collecting measures and tries to improve it.
Contact us at cave.survey.project@gmail.com


Goal
====

CaveSurvey is tool for mapping caves having Android device and laser distance meter:
 - Having the measures sheet in digital format from the beginning (eliminating typo errors)
 - Easy to enter measures (not all cave parts are pleasant place to stay)
 - Able to export the sheet in Excel format for further processing (not being primary tool for the final map artifact, such tools exist are awesome)
 - Being able to draw simple map of the main line (detect big erros on place) or create simple sketch from a point.
 - Aid measures collection (reduce number of measure instruments needed and/or need to type the value):
  - using build-in sensors if available (as compass, clinometer) and tools (take a picture, take GPS location of the entrance, type a note, etc)
  - using Bluetooth protocol to integrate external laser distance meter - distance and in some cases clinometer measures

Features
========

 Currently CaveSurvey can :
  - Supports multiple projects
  - Can add points to the sheet and manually enter measures
  - Can export to Excel using Apache POI
  - For each point you can add note, take picture, draw simple sketch
  - Display 2D map of the current measures
  

  In progress:
  - Take GPS coordinates of a point (can take the first point outside and will geo reference the rest)
  - Read compass from the build-in compass
  - Read inclination from the build-in compass
  - Read measures from Bluetooth laser distance meter
  - 3D engine to better display the main direction (aid the 2D for vertical caves)
  
Notes
=====

  Test devices:
  - Testing with both Android 2.1(cheap and small) and 4.3(better hardware)
  - iLDM-150 laser distance meter (IP54) with build in clinometer and Bluetooth transfer from CEM
  - Leica and Bosh have awesome distance meters with Bluetooth but are much more expensive
  
  Both waterproof Android 4.x + iLDM-150 are about $250!
  Target configuration is to read the distance and inclination from Bluetooth and use the build-in compass from the device


Precision
=========

Having precise instruments is important to do a proper work. Anyway in most caves centimeter precision is not possible.

For iLDM-150 CEM have specified precision of 1.5mm for the distance and 0.5' from the clinometer.
For the Android build-in compass - you have to consult your device manifacturer, but if small and you use short legs there should be no problem (error will distribute and compensate anyway)

If you are paranoic you can still use CaveSurvey in manual mode - type the proper values from the existing tools you use. It will save you the Excel work later.


Links
====

Another Android app is Abris (https://play.google.com/store/apps/details?id=com.shturmsoft.abris&hl=en). They are already alive and have focus on creating the whole map underground. I have to admit they have done a great job, but I'm not confident in drawing stuff with muddy fingers.

