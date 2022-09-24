This document briefly describes the basic concepts of the game engine.


## Screen types and dialogs

On the ``Main`` class (file: ``Main.java``), the member variable ``screenType``
keeps track of the current screen type (blank, logo, play, or final score). The
different screen types are identified by constants defined in ``Defs.java``:
``SCR_BLANK``, ``SCR_LOGO``, ``SCR_PLAY``, ``SCR_PLAY_FREEZE``, and
``SCR_FINALSCORE``.

The dialogs, which enable the player to select the difficulty, the level to
play and so on, are handled by the file ``Dialogs.java`` independently of the
screen type. This enables the main dialog to be show over the logo screen and
the pause dialog to be shown over the play screen, for instance.


## Game objects

Objects that appear during the game include the player character, the bus,
banana peels, crates, and so on.

The file that handles the play session, including the game objects, is
``Play.java``.

The ``PlayCtx`` class, defined in the ``Defs.java`` file, keeps track of the
current play session. Its member ``objs[]`` stores the type and position of most
but not all objects. Certain object types require additional data and reference
an index within ``objs[]``. This is why classes like ``Gush`` and ``MovingPeel``
exist in ``Defs.java``. Each type of object that uses ``objs[]`` is identified
by one of the ``OBJ_*`` constants defined also in ``Defs.java``.

Objects that do not use ``objs[]`` include the player character, the bus, and
crate blocks, among others.


## Solids

The solids are what prevents the player character from moving through the floor
or objects like crate blocks and parked cars and trucks. These are handled
separately from the objects themselves. The class that stores information about
solids is ``Solid``, which is defined in the file ``Defs.java``.


## Triggers

A trigger is what causes a car that throws a banana peel or a hen to appear when
the player character reaches a certain X position. The ``Trigger`` class is
defined in the file ``Defs.java``.


## Respawn points

A respawn point is a position at which the player character reappears when he
falls into a deep hole. After falling, the character reappears at the closest
respawn point with an X position lower than that of the hole.


## Sequences

The sequences seen during the game, like the player character automatically
moving and jumping into the bus, or the ending sequence, are handled by the
method ``updateSequence()``, found in the ``Play`` class, itself found in the
``Play.java`` file.


## Sprites

The graphics are contained in a single image file (gfx.png). Each element in
the file is referred to as a sprite.

A sprite can have more than one animation frame.

