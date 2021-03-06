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

* progress-difficulty

  The highest difficulty the player has unlocked. The allowed values are
  ``normal`` and ``hard``.

* progress-level

  The highest level number the player has unlocked within the highest unlocked
  difficulty. The allowed values range from 1 to 5.

## Example file

```
window-mode fullscreen
audio-enabled true
progress-difficulty normal
progress-level 1
```

