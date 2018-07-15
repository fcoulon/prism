POC of the SLEBus
=================

This repo contains :
 - The Bus implementation
 - An example FSM as EMF model, Java fluent API & Rascal grammar
 - The implementation of Patch production/consomation for EMF, Java FSM & Rascal

 <video width="320" height="240" controls>
  <source src="https://raw.githubusercontent.com/fcoulon/prism/master/demo.ogg" type="video/ogg">
Your browser does not support the video tag.
</video> 

![Screenshot](Screenshot.png)

Setup
-----
 1. Have an Eclipse with EMF & Sirius & Rascal
 2. Import projects from 'languageWB' in your workspace
 3. Launch new Eclipse instance
 4. Import projects from 'modelingWB' in your new workspace

Play
----
Once you are in the second Eclipse instance

 0. Check your are in the 'Modeling' perspective (needed for Sirius)
 1. Open 'example/representation.aird' (Machine Diagram)
 2. Open 'foo/Main.java'
 3. Open 'TestIt/doors.mf'
 4. Now editing one representation should update the others (after a save)


TODO:
 - Clean up the code
 - Remove hard coded stuff -> need a way to config (through extension point?)
 - Some tests :)
