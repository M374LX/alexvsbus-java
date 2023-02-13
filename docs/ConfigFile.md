This document describes the format of the configuration file used on a PC. On
Android, the game uses the ``SharedPreferences`` class.

The file saves both user preferences and the game progress.


## File location

On Windows, the file is
``%LOCALAPPDATA%\alexvsbus-java\alexvsbus.cfg``

``%LOCALAPPDATA%`` defaults to ``C:\Users\<username>\AppData\Local``.

On other operating systems, like Linux, the file is
``$XDG_CONFIG_HOME/alexvsbus-java/alexvsbus.cfg``

If unset, ``$XDG_CONFIG_HOME`` defaults to ``$HOME/.config``, in accordance to
the XDG Base Directory Specification
(https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html),
summarized in https://wiki.archlinux.org/index.php/XDG_Base_Directory.


## File format

It is a simple plain text file in which each line defines a property. The line
starts with the property name, which is followed by a space and then the
property value. The value can be either a string or a number.


## Properties

* window-mode

  Defines the window mode. The allowed values are ``1`` (small window),`` 2``
  (medium-sized window), ``3`` (large window), and ``fullscreen`` (fullscreen
  mode).

* audio-enabled

  The value ``true`` enables audio output, while the value ``false`` disables
  audio output.

* scanlines-enabled

  The value ``true`` enables a scanlines visual effect, while the value
  ``false`` disables the effect.

* vscreen-auto-size

  The value ``true`` causes the virtual screen (vscreen) to be automatically
  set to the size that best fits in the physical screen or window, while the
  value ``false`` is used when the size is set manually.

* vscreen-width

  The width of the virtual screen (vscreen) if its size is set manually. It is
  required if the property ``vscreen-auto-size`` is set to ``false``, but
  ignored otherwise.

* vscreen-height

  The height of the virtual screen (vscreen) if its size is set manually. It is
  required if the property ``vscreen-auto-size`` is set to ``false``, but
  ignored otherwise.

* touch-buttons-enabled

  The value ``true`` causes the left, right, and jump buttons to be displayed
  if the touchscreen is enabled. The player might want to disable the buttons
  when using a phone with a physical game controller.

* progress-difficulty

  The highest difficulty the player has unlocked. The allowed values are
  ``normal``, ``hard``, and ``super``.

* progress-level

  The highest level number the player has unlocked within the highest unlocked
  difficulty. The allowed values range from 1 to 5 (if ``progress-difficulty``
  is set to ``normal`` or ``hard``) or 1 to 3 (if ``progress-difficulty`` is
  set to ``super``).

## Example file

```
window-mode fullscreen
audio-enabled true
progress-difficulty normal
progress-level 1
```

