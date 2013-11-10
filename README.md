Pipe Game Solver
================

This is a simple solver for a certain type of "connect the pipes" game.

**THIS CODE IS PROVIDED SOLELY FOR ENTERTAINMENT PURPOSES**

Game Rules
----------

* There is a grid of m*n fields.
* Each field can either be empty or have a pipe with 1,2,3 or 4 neighbours.
* Each pipe must have exactly as many pipes in the 4 adjacent fields (top, left, bottom, right)
  as it's number.
* Two pipes with the same number cannot be neighbours.
* In the end there can be only one interconnected system of pipes (ie. it must be possible to
  reach every pipe on the board from every other pipe without having to go over an empty field).

Usage
-----
The game can be run by executing the `com.github.users.dmoagx.pipes.Game` Java class.
The `examples` folder contains some boards (of varying difficulty) which can be used to test the solver.

Credits
-------
This program is based on a game in "Professor Layton and the Miracle Mask".  
(C) 2012 LEVEL-5 Inc.  
Distributed by Nintendo  
