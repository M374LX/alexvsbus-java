/*
 * Alex vs Bus
 * Copyright (C) 2021-2023 M374LX
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */


package org.alexvsbus.desktop;

import static org.alexvsbus.Defs.*;

import static org.alexvsbus.Data.difficultyNumLevels;
import static org.alexvsbus.Data.vscreenWidths;
import static org.alexvsbus.Data.vscreenHeights;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import org.alexvsbus.Defs.Config;
import org.alexvsbus.Defs.PlatDep;
import org.alexvsbus.LineRead;
import org.alexvsbus.Main;

class DesktopPlatDep implements PlatDep {
    Path configFilePath;
    Config config;

    Lwjgl3Window window;
    int minWindowWidth;
    int minWindowHeight;

    boolean cliHelp;
    boolean cliVersion;
    String cliConfig;
    boolean cliResizable;
    boolean cliTouchEnabled;
    boolean cliFullscreen;
    boolean cliWindowed;
    boolean cliFixedWindowMode;
    int cliWindowScale;         //0 = unset
    int cliScanlinesEnabled;    //0 = unset; -1 = disable; 1 = enable
    int cliAudioEnabled;        //0 = unset; -1 = disable; 1 = enable
    int cliMusicEnabled;        //0 = unset; -1 = disable; 1 = enable
    int cliSfxEnabled;          //0 = unset; -1 = disable; 1 = enable
    int cliTouchButtonsEnabled; //0 = unset; -1 = disable; 1 = enable
    int cliVscreenWidth;        //0 = unset; -1 = auto
    int cliVscreenHeight;       //0 = unset; -1 = auto

    DesktopPlatDep() {
        config = new Config();
    }

    @Override
    public Config getConfig() {
        return config;
    }

    void run(String[] args) {
        Lwjgl3ApplicationConfiguration appConfig = new Lwjgl3ApplicationConfiguration();

        parseCli(args);
        if (cliHelp) {
            showHelp();
            return;
        } else if (cliVersion) {
            showVersion();
            return;
        }

        loadConfig();

        appConfig.setTitle("Alex vs Bus: The Race");
        appConfig.setWindowIcon("icon16.png", "icon32.png", "icon48.png", "icon128.png");
        appConfig.setResizable(config.resizableWindow);
        appConfig.setInitialVisible(false);
        appConfig.setForegroundFPS(60);
        appConfig.useVsync(true);

        new Lwjgl3Application(new Main(this), appConfig);
    }

    @Override
    public void postInit() {
        window = ((Lwjgl3Graphics)Gdx.graphics).getWindow();
        window.setVisible(true);
    }

    @Override
    public void setMinWindowSize(int width, int height) {
        if (width != minWindowWidth || height != minWindowHeight) {
            minWindowWidth  = width;
            minWindowHeight = height;
            window.setSizeLimits(width, height, -1, -1);
        }
    }

    static void showHelp() {
        int i;

        System.out.println(
        "Alex vs Bus: The Race\n" +
        "\n" +
        "-h, --help             Show this usage information and exit\n" +
        "-v, --version          Show version and license information and exit\n" +
        "-c, --config <file>    Set the config file to use\n" +
        "-f, --fullscreen       Run in fullscreen mode\n" +
        "-w, --windowed         Run in windowed mode\n" +
        "--window-scale <scale> Set the window scale (1 to 3)\n" +
        "--resizable            Make the window resizable\n" +
        "--vscreen-size <size>  Set the size of the virtual screen (vscreen)\n" +
        "--fixed-window-mode    Simulate the mobile version's inability to toggle\n" +
        "                       between fullscreen and windowed mode and to change\n" +
        "                       the window scale\n" +
        "--scanlines-on         Enable scanlines visual effect\n" +
        "--scanlines-off        Disable scanlines visual effect\n" +
        "--audio-on             Enable audio output\n" +
        "--audio-off            Disable audio output\n" +
        "--music-on             Enable music\n" +
        "--music-off            Disable music\n" +
        "--sfx-on               Enable sound effects\n" +
        "--sfx-off              Disable sound effects\n" +
        "--touch                Enable touchscreen controls, which can also be\n" +
        "                       simulated by using the mouse\n" +
        "--touch-buttons-on     Enable left, right, and jump buttons on\n" +
        "                       touchscreen (visible only if --touch is also used)\n" +
        "--touch-buttons-off    Disable left, right, and jump buttons on\n" +
        "                       touchscreen\n" +
        "--mobile               As a shorthand for --fixed-window-mode and --touch,\n" +
        "                       simulate the mobile version\n" +
        "\n" +
        "For --vscreen-size, the size can be either \"auto\" or a width and a height\n" +
        "separated by an \"x\" (example: 480x270), with the supported values listed\n" +
        "below.\n"
        );

        System.out.println("Supported width values:");
        for (i = 0; i < vscreenWidths.length; i++) {
            System.out.println(vscreenWidths[i]);
        }
        System.out.println();

        System.out.println("Supported height values:");
        for (i = 0; i < vscreenHeights.length; i++) {
            System.out.println(vscreenHeights[i]);
        }
    }

    static void showVersion() {
        System.out.println(
        "Alex vs Bus: The Race\n" +
        "Version " + VERSION + "\n" +
        "\n" +
        "Copyright (C) 2021-2023 M374LX\n" +
        "\n" +
        "This program is free software: you can redistribute it and/or modify\n" +
        "it under the terms of the GNU General Public License as published by\n" +
        "the Free Software Foundation, either version 3 of the License, or\n" +
        "(at your option) any later version.\n" +
        "\n" +
        "This program is distributed in the hope that it will be useful,\n" +
        "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
        "GNU General Public License for more details.\n" +
        "\n" +
        "You should have received a copy of the GNU General Public License\n" +
        "along with this program.  If not, see <https://www.gnu.org/licenses/>."
        );
    }

    void parseCli(String[] args) {
        int argc = args.length;
        int i;

        cliHelp = false;
        cliVersion = false;
        cliConfig = "";
        cliResizable = false;
        cliFullscreen = false;
        cliWindowed = false;
        cliFixedWindowMode = false;
        cliWindowScale = 0;
        cliVscreenWidth = 0;
        cliVscreenHeight = 0;
        cliScanlinesEnabled = 0;
        cliAudioEnabled = 0;
        cliMusicEnabled = 0;
        cliSfxEnabled = 0;
        cliTouchEnabled = false;
        cliTouchButtonsEnabled = 0;

        for (i = 0; i < argc; i++) {
            String a = args[i];

            if (a.equals("-h") || a.equals("--help")) {
                cliHelp = true;
                return;
            } else if (a.equals("-v") || a.equals("--version")) {
                cliVersion = true;
                return;
            } else if (a.equals("-f") || a.equals("--fullscreen")) {
                cliFullscreen = true;
                cliWindowed = false;
            } else if (a.equals("-w") || a.equals("--windowed")) {
                cliWindowed = true;
                cliFullscreen = false;
            } else if (a.equals("-c") || a.equals("--config")) {
                i++;
                if (i >= argc) {
                    cliHelp = true;
                    return;
                }

                cliConfig = args[i];
            } else if (a.equals("--vscreen-size")) {
                i++;
                if (i >= argc) {
                    cliHelp = true;
                    return;
                }

                if (!parseVscreenSizeArg(args[i])) {
                    cliHelp = true;
                    return;
                }
            } else if (a.equals("--window-scale")) {
                i++;
                if (i >= argc) {
                    cliHelp = true;
                    return;
                }

                a = args[i];
                if (a.equals("1")) {
                    cliWindowScale = 1;
                } else if (a.equals("2")) {
                    cliWindowScale = 2;
                } else if (a.equals("3")) {
                    cliWindowScale = 3;
                }
            } else if (a.equals("--fixed-window-mode")) {
                cliFixedWindowMode = true;
            } else if (a.equals("--scanlines-on")) {
                cliScanlinesEnabled = 1;
            } else if (a.equals("--scanlines-off")) {
                cliScanlinesEnabled = -1;
            } else if (a.equals("--resizable")) {
                cliResizable = true;
            } else if (a.equals("--audio-on")) {
                cliAudioEnabled = 1;
            } else if (a.equals("--audio-off")) {
                cliAudioEnabled = -1;
            } else if (a.equals("--music-on")) {
                cliMusicEnabled = 1;
            } else if (a.equals("--music-off")) {
                cliMusicEnabled = -1;
            } else if (a.equals("--sfx-on")) {
                cliSfxEnabled = 1;
            } else if (a.equals("--sfx-off")) {
                cliSfxEnabled = -1;
            } else if (a.equals("--touch")) {
                cliTouchEnabled = true;
            } else if (a.equals("--touch-buttons-on")) {
                cliTouchButtonsEnabled = 1;
            } else if (a.equals("--touch-buttons-off")) {
                cliTouchButtonsEnabled = -1;
            } else if (a.equals("--mobile")) {
                //Shorthand for --fixed-window-mode and --touch
                cliFixedWindowMode = true;
                cliTouchEnabled = true;
            } else {
                cliHelp = true;
                return;
            }
        }
    }

    boolean parseVscreenSizeArg(String arg) {
        if (arg.equals("auto")) {
            cliVscreenWidth  = -1;
            cliVscreenHeight = -1;
        } else {
            int sepPos = arg.indexOf('x'); //Separator position

            if (sepPos < 1 || sepPos > 3 || arg.length() > 7) {
                return false;
            } else {
                try {
                    int width  = Integer.parseInt(arg.substring(0, sepPos));
                    int height = Integer.parseInt(arg.substring(sepPos + 1));
                    boolean supportedWidth  = false;
                    boolean supportedHeight = false;
                    int i;

                    for (i = 0; i < vscreenWidths.length; i++) {
                        if (width == vscreenWidths[i]) {
                            supportedWidth = true;
                            break;
                        }
                    }
                    for (i = 0; i < vscreenHeights.length; i++) {
                        if (height == vscreenHeights[i]) {
                            supportedHeight = true;
                            break;
                        }
                    }

                    if (!supportedWidth || !supportedHeight) {
                        return false;
                    }

                    cliVscreenWidth  = width;
                    cliVscreenHeight = height;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        return true;
    }

    Path findConfigFile() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        String file;

        if (cliConfig.length() > 0) {
            file = cliConfig;
        } else if (os.startsWith("windows")) {
            String appdata = System.getenv("LOCALAPPDATA");

            file = appdata + "\\alexvsbus-java\\alexvsbus.cfg";
        } else if (os.startsWith("mac")) {
            file = home + "/Library/Preferences/alexvsbus-java/alexvsbus.cfg";
        } else {
            //Use the XDG Base Directory specification
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");

            if (xdgConfig == null) {
                xdgConfig = home + "/.config";
            }

            file = xdgConfig + "/alexvsbus-java/alexvsbus.cfg";
        }

        if (file.startsWith("~")) {
            file = home + file.substring(1);
        }

        return Paths.get(file);
    }

    void loadConfig() {
        LineRead lineRead = new LineRead();
        int numLevels;
        int i;

        //Defaults
        config.touchEnabled = false;
        config.touchButtonsEnabled = true;
        config.fullscreen = true;
        config.fixedWindowMode = false;
        config.windowScale = 2;
        config.resizableWindow = false;
        config.audioEnabled = true;
        config.musicEnabled = true;
        config.sfxEnabled = true;
        config.scanlinesEnabled = false;
        config.vscreenAutoSize = true;
        config.vscreenWidth  = -1;
        config.vscreenHeight = -1;
        config.progressLevel = 1;
        config.progressDifficulty = DIFFICULTY_NORMAL;

        //Use the back key only on Android, as the value of the libGDX constant
        //Keys.BACK conflicts with that of Keys.META_SYM_ON (the Windows logo
        //key on desktop)
        config.useBackKey = false;

        //Apply configuration set from CLI
        if (cliFullscreen) {
            config.fullscreen = true;
        }
        if (cliWindowed) {
            config.fullscreen = false;
        }
        if (cliWindowScale > 0) {
            config.windowScale = cliWindowScale;
        }
        if (cliScanlinesEnabled != 0) {
            config.scanlinesEnabled = (cliScanlinesEnabled == -1) ? false : true;
        }
        if (cliVscreenWidth > 0 && cliVscreenHeight > 0) {
            config.vscreenAutoSize = false;
            config.vscreenWidth  = cliVscreenWidth;
            config.vscreenHeight = cliVscreenHeight;
        }
        if (cliResizable) {
            config.resizableWindow = true;
        }
        if (cliFixedWindowMode) {
            config.fixedWindowMode = true;
        }
        if (cliAudioEnabled != 0) {
            config.audioEnabled = (cliAudioEnabled == -1) ? false : true;
        }
        if (cliMusicEnabled != 0) {
            config.musicEnabled = (cliMusicEnabled == -1) ? false : true;
        }
        if (cliSfxEnabled != 0) {
            config.sfxEnabled = (cliSfxEnabled == -1) ? false : true;
        }
        if (cliTouchEnabled) {
            config.touchEnabled = true;
        }
        if (cliTouchButtonsEnabled != 0) {
            config.touchButtonsEnabled = (cliTouchButtonsEnabled == -1) ? false : true;
        }

        //Try to open the config file
        try {
            configFilePath = findConfigFile();

            if (Files.size(configFilePath) > 4096) {
                //Too large (over 4 kB)
                return;
            }

            lineRead.setData(new String(Files.readAllBytes(configFilePath)));
        } catch (Exception e) {
            return;
        }

        while (!lineRead.endOfData()) {
            String line = lineRead.getLine();
            String tokens[] = line.split(" ");

            if (tokens.length < 2) {
                continue;
            }

            if (tokens[0].equals("fullscreen")) {
                //If set from CLI, skip loading from config file
                if (cliFullscreen || cliWindowed) continue;

                //Check for the non-default boolean value
                if (tokens[1].equals("false")) config.fullscreen = false;
            } else if (tokens[0].equals("window-scale")) {
                //If set from CLI, skip loading from config file
                if (cliWindowScale > 0) continue;

                if (tokens[1].equals("1")) {
                    config.windowScale = 1;
                } else if (tokens[1].equals("2")) {
                    config.windowScale = 2;
                } else if (tokens[1].equals("3")) {
                    config.windowScale = 3;
                }
             } else if (tokens[0].equals("scanlines-enabled")) {
                //If set from CLI, skip loading from config file
                if (cliScanlinesEnabled != 0) continue;

                //Check for the non-default boolean value
                if (tokens[1].equals("true")) config.scanlinesEnabled = true;
            } else if (tokens[0].equals("audio-enabled")) {
                //If set from CLI, skip loading from config file
                if (cliAudioEnabled != 0) continue;

                //Check for the non-default boolean value
                if (tokens[1].equals("false")) config.audioEnabled = false;
            } else if (tokens[0].equals("music-enabled")) {
                //If set from CLI, skip loading from config file
                if (cliMusicEnabled != 0) continue;

                //Check for the non-default boolean value
                if (tokens[1].equals("false")) config.musicEnabled = false;
            } else if (tokens[0].equals("sfx-enabled")) {
                //If set from CLI, skip loading from config file
                if (cliSfxEnabled != 0) continue;

                //Check for the non-default boolean value
                if (tokens[1].equals("false")) config.sfxEnabled = false;
            } else if (tokens[0].equals("touch-buttons-enabled")) {
                //If set from CLI, skip loading from config file
                if (cliTouchButtonsEnabled != 0) continue;

                //Check for the non-default boolean value
                if (tokens[1].equals("false")) config.touchButtonsEnabled = false;
            } else if (tokens[0].equals("vscreen-auto-size")) {
                //If set from CLI, skip loading from config file
                if (cliVscreenWidth != 0 && cliVscreenHeight != 0) continue;

                //Check for the non-default boolean value
                if (tokens[1].equals("false")) config.vscreenAutoSize = false;
            } else if (tokens[0].equals("vscreen-width")) {
                //If set from CLI, skip loading from config file
                if (cliVscreenWidth != 0 && cliVscreenHeight != 0) continue;

                try {
                    int val = Integer.parseInt(tokens[1]);

                    if (val >= 1 && val <= 999) {
                        for (i = 0; i < vscreenWidths.length; i++) {
                            if (vscreenWidths[i] == val) {
                                config.vscreenWidth = val;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    //Do nothing
                }
            } else if (tokens[0].equals("vscreen-height")) {
                //If set from CLI, skip loading from config file
                if (cliVscreenWidth != 0 && cliVscreenHeight != 0) continue;

                try {
                    int val = Integer.parseInt(tokens[1]);

                    if (val >= 1 && val <= 999) {
                        for (i = 0; i < vscreenHeights.length; i++) {
                            if (vscreenHeights[i] == val) {
                                config.vscreenHeight = val;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    //Do nothing
                }
            } else if (tokens[0].equals("progress-difficulty")) {
                if (tokens[1].equals("normal")) {
                    config.progressDifficulty = DIFFICULTY_NORMAL;
                } else if (tokens[1].equals("hard")) {
                    config.progressDifficulty = DIFFICULTY_HARD;
                } else if (tokens[1].equals("super")) {
                    config.progressDifficulty = DIFFICULTY_SUPER;
                }
            } else if (tokens[0].equals("progress-level")) {
                try {
                    int val = Integer.parseInt(tokens[1]);

                    if (val < 1 || val > 9) {
                        val = 1;
                    }

                    config.progressLevel = val;
                } catch (Exception e) {
                    //Do nothing
                }
            }
        }

        if (!config.vscreenAutoSize) {
            if (config.vscreenWidth == -1 || config.vscreenHeight == -1) {
                config.vscreenAutoSize = true;
            }
        }

        numLevels = difficultyNumLevels[config.progressDifficulty];
        if (config.progressLevel > numLevels) {
            config.progressLevel = 1;
        }
    }

    @Override
    public boolean saveConfig() {
        String data = "";

        //Fullscreen
        data += "fullscreen " +
            (config.fullscreen ? "true" : "false") + "\n";

        //Window scale
        data += "window-scale " + config.windowScale + "\n";

        //Scanlines enabled
        data += "scanlines-enabled " +
            (config.scanlinesEnabled ? "true" : "false") + "\n";

        //Audio enabled
        data += "audio-enabled " +
            (config.audioEnabled ? "true" : "false") + "\n";

        //Music enabled
        data += "music-enabled " +
            (config.musicEnabled ? "true" : "false") + "\n";

        //Sound effects enabled
        data += "sfx-enabled " +
            (config.sfxEnabled ? "true" : "false") + "\n";

        //Touchscreen buttons enabled
        data += "touch-buttons-enabled " +
            (config.touchButtonsEnabled ? "true" : "false") + "\n";

        //Virtual screen (vscreen) size
        data += "vscreen-auto-size " +
            (config.vscreenAutoSize ? "true" : "false") + "\n";

        //Manual vscreen size if set
        if (!config.vscreenAutoSize) {
            data += "vscreen-width "  + config.vscreenWidth  + "\n";
            data += "vscreen-height " + config.vscreenHeight + "\n";
        }

        //Game progress
        data += "progress-difficulty ";
        switch (config.progressDifficulty) {
            case DIFFICULTY_NORMAL: data += "normal"; break;
            case DIFFICULTY_HARD:   data += "hard";   break;
            case DIFFICULTY_SUPER:  data += "super";  break;
        }
        data += "\n";
        data += "progress-level " + config.progressLevel + "\n";

        try {
            Files.createDirectories(configFilePath.toAbsolutePath().getParent());
            Files.write(configFilePath, data.getBytes());

            return true;
        } catch (Exception e) {
            Gdx.app.log("Warning", "Unable to save configuration (" + e.toString() + ")");

            return false;
        }
    }
}

