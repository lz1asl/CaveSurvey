About
===

Connection with the [Bosch measurement devices](https://github.com/lz1asl/CaveSurvey/wiki/Bosch-Devices) uses proprietary library provided by Bosch Power Tools that cannot be published freely.
For automated builds the support of Bosch devices can be removed.

Patch file
===

* Make the project compile:

```shell script
git apply --ignore-space-change --ignore-whitespace no_bosch_library.patch
```

* Update the patch file:

```shell script
# create branch
git checkout -b fix_patch

# 4 manual steps below
# remove everything in com.astoev.cave.survey.service.bluetooth.device.comm.bosch
# remove references to that classes from BluetoothService
# remove the reference to the library in build.gradle
# commit the above changes

# generate new patch
git format-patch master --stdout > no_bosch_library.patch

```