TeamCity.DepsOrder
==================

Plugin for TeamCity 8.0.x+ that allows to specify specific order
to build listed snapshot dependencies for the build configuration

Use of the plugin will make you minimize number of extra build configurations


License
=======
Apache 2.0


Installation
============

Download the latest build from TBD and put in to ```<TeamCity Data Directory>/plugins``` folder. Restart the server


Builds
======


Initial setup was made with https://github.com/jonnyzzz/TeamCity.PluginTemplate

In this sample you will find
-----------------------------
- TeamCity server-side only plugin
- Plugin version will be patched if building with IDEA build runner in TeamCity
- Run configuration to run/debug plugin under TeamCity (use `http://localhost:8111/bs`)
- pre-configured IDEA settings to support references to TeamCity
- Uses `$TeamCityDistribution$` IDEA path variable as path to TeamCity home (unpacked .tar.gz or .exe distribution)
- Bunch of libraries for most recent needed TeamCity APIs
- Module with TestNG tests that uses TeamCity Tests API

