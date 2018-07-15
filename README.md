Prism
=====

Prism is a framework to synchronize incarnations of a model shared in different technological spaces.

<a href="https://raw.githubusercontent.com/fcoulon/prism/master/demo.webm">
  <img src="link.png"">
</a> 

This repo contains :
 - The Prism framework
 - A Finite State Machine language implemented in Rascal, EMF and Java fluent API
 - Implementations of Patch producer/consumer for Rascal, EMF and Java


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

