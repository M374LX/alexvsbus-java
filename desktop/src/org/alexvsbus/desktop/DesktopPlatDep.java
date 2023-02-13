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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import org.alexvsbus.Defs.Config;
import org.alexvsbus.Defs.PlatDep;
import org.alexvsbus.LineRead;

import static org.alexvsbus.Defs.NONE;
import static org.alexvsbus.Defs.DIFFICULTY_NORMAL;
import static org.alexvsbus.Defs.DIFFICULTY_HARD;
import static org.alexvsbus.Defs.DIFFICULTY_SUPER;
import static org.alexvsbus.Defs.WM_UNSET;
import static org.alexvsbus.Defs.WM_1X;
import static org.alexvsbus.Defs.WM_2X;
import static org.alexvsbus.Defs.WM_3X;
import static org.alexvsbus.Defs.WM_FULLSCREEN;
import static org.alexvsbus.Defs.difficultyNumLevels;
import static org.alexvsbus.Defs.vscreenWidths;
import static org.alexvsbus.Defs.vscreenHeights;

class DesktopPlatDep implements PlatDep {
    Path configDirPath;
    Path configFilePath;
    Config config;

    boolean help;
    boolean version;
    boolean resizable;
    boolean touchEnabled;
    int cliWindowMode;
    int cliAudioEnabled; //0 = unset; -1 = disable; 1 = enable
    int cliScanlinesEnabled; //0 = unset; -1 = disable; 1 = enable
    int cliVscreenWidth;  //0 = unset; -1 = auto
    int cliVscreenHeight; //0 = unset; -1 = auto

    Lwjgl3Window window;
    int minWindowWidth;
    int minWindowHeight;

    DesktopPlatDep() {
        config = new Config();
    }

    @Override
    public Config getConfig() {
        return config;
    }

    void parseCli(String[] args) {
        int argc = args.length;
        int i;

        help = false;
        version = false;
        resizable = false;
        touchEnabled = false;

        cliWindowMode = WM_UNSET;
        cliAudioEnabled = 0;
        cliScanlinesEnabled = 0;
        cliVscreenWidth = 0;
        cliVscreenHeight = 0;

        for (i = 0; i < argc; i++) {
            String a = args[i];

            if (a.equals("-h") || a.equals("--help")) {
                help = true;
                return;
            } else if (a.equals("-v") || a.equals("--version")) {
                version = true;
                return;
            } else if (a.equals("-f") || a.equals("--fullscreen")) {
                cliWindowMode = WM_FULLSCREEN;
            } else if (a.equals("-w") || a.equals("--window")) {
                i++;
                if (i >= argc) {
                    help = true;
                    return;
                }

                a = args[i];
                if (a.equals("1")) {
                    cliWindowMode = WM_1X;
                } else if (a.equals("2")) {
                    cliWindowMode = WM_2X;
                } else if (a.equals("3")) {
                    cliWindowMode = WM_3X;
                } else {
                    help = true;
                    return;
                }
            } else if (a.equals("--vscreen-size")) {
                i++;
                if (i >= argc) {
                    help = true;
                    return;
                }

                if (!parseVscreenSizeArg(args[i])) {
                    help = true;
                    return;
                }
            } else if (a.equals("--audio-on")) {
                cliAudioEnabled = 1;
            } else if (a.equals("--audio-off")) {
                cliAudioEnabled = -1;
            } else if (a.equals("--scanlines-on")) {
                cliScanlinesEnabled = 1;
            } else if (a.equals("--scanlines-off")) {
                cliScanlinesEnabled = -1;
            } else if (a.equals("--resizable")) {
                resizable = true;
            } else if (a.equals("--touch")) {
                touchEnabled = true;
            } else {
                help = true;
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

    @Override
    public void postInit() {
        window = ((Lwjgl3Graphics)Gdx.graphics).getWindow();
    }

    @Override
    public void setMinWindowSize(int width, int height) {
        if (width != minWindowWidth || height != minWindowHeight) {
            minWindowWidth  = width;
            minWindowHeight = height;
            window.setSizeLimits(width, height, -1, -1);
        }
    }

    void loadConfig() {
        LineRead lineRead = new LineRead();
        String os = System.getProperty("os.name").toLowerCase();
        int numLevels;
        int i;

        //Defaults
        config.touchEnabled = false;
        config.touchButtonsEnabled = true;
        config.windowMode = WM_FULLSCREEN;
        config.resizableWindow = false;
        config.audioEnabled = true;
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
        if (cliWindowMode != WM_UNSET) {
            config.windowMode = cliWindowMode;
        }
        if (cliAudioEnabled != 0) {
            config.audioEnabled = (cliAudioEnabled == -1) ? false : true;
        }
        if (cliScanlinesEnabled != 0) {
            config.scanlinesEnabled = (cliScanlinesEnabled == -1) ? false : true;
        }
        if (touchEnabled) {
            config.touchEnabled = true;
        }
        if (cliVscreenWidth > 0 && cliVscreenHeight > 0) {
            config.vscreenAutoSize = false;
            config.vscreenWidth = cliVscreenWidth;
            config.vscreenHeight = cliVscreenHeight;
        }
        if (resizable) {
            config.resizableWindow = true;
        }

        //Find configuration directory
        String configDir;
        if (os.startsWith("windows")) {
            String appdata = System.getenv("LOCALAPPDATA");

            configDir = appdata + "\\alexvsbus-java\\";
        } else {
            //Use the XDG Base Directory specification
            String home = System.getenv("HOME");
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");

            if (xdgConfig == null) {
                xdgConfig = home + "/.config";
            }

            configDir = xdgConfig + "/alexvsbus-java/";
        }

        configDirPath = Paths.get(configDir);

        //Try to open the config file
        try {
            configFilePath = Paths.get(configDir + "alexvsbus.cfg");

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

            if (tokens[0].equals("window-mode")) {
                //Do not load window mode from config file if set from CLI
                if (cliWindowMode != WM_UNSET) {
                    continue;
                }

                if (tokens[1].equals("1")) {
                    config.windowMode = WM_1X;
                } else if (tokens[1].equals("2")) {
                    config.windowMode = WM_2X;
                } else if (tokens[1].equals("3")) {
                    config.windowMode = WM_3X;
                } else if (tokens[1].equals("fullscreen")) {
                    config.windowMode = WM_FULLSCREEN;
                }
            } else if (tokens[0].equals("audio-enabled")) {
                //If the audio was enabled or disabled from CLI, do not load
                //the configuration from the config file
                if (cliAudioEnabled != 0) {
                    continue;
                }

                if (tokens[1].equals("true")) {
                    config.audioEnabled = true;
                } else if (tokens[1].equals("false")) {
                    config.audioEnabled = false;
                }
             } else if (tokens[0].equals("scanlines-enabled")) {
                //If the scanlines were enabled or disabled from CLI, do not
                //load the configuration from the config file
                if (cliScanlinesEnabled != 0) {
                    continue;
                }

                if (tokens[1].equals("true")) {
                    config.scanlinesEnabled = true;
                } else if (tokens[1].equals("false")) {
                    config.scanlinesEnabled = false;
                }
            } else if (tokens[0].equals("touch-buttons-enabled")) {
                if (tokens[1].equals("true")) {
                    config.touchButtonsEnabled = true;
                } else if (tokens[1].equals("false")) {
                    config.touchButtonsEnabled = false;
                }
            } else if (tokens[0].equals("vscreen-auto-size")) {
                //If the virtual screen (vscreen) size was set from CLI, do not
                //load the configuration from the config file
                if (cliVscreenWidth != 0 && cliVscreenHeight != 0) {
                    continue;
                }

                if (tokens[1].equals("true")) {
                    config.vscreenAutoSize = true;
                } else if (tokens[1].equals("false")) {
                    config.vscreenAutoSize = false;
                }
            } else if (tokens[0].equals("vscreen-width")) {
                //If the virtual screen (vscreen) size was set from CLI, do not
                //load the configuration from the config file
                if (cliVscreenWidth != 0 && cliVscreenHeight != 0) {
                    continue;
                }

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
                //If the virtual screen (vscreen) size was set from CLI, do not
                //load the configuration from the config file
                if (cliVscreenWidth != 0 && cliVscreenHeight != 0) {
                    continue;
                }

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
    public void saveConfig() {
        String data = "";

        //Window mode
        data += "window-mode ";
        switch (config.windowMode) {
            case WM_1X: data += "1"; break;
            case WM_2X: data += "2"; break;
            case WM_3X: data += "3"; break;
            case WM_FULLSCREEN: data += "fullscreen"; break;
        }
        data += "\n";

        //Audio enabled
        data += "audio-enabled " +
            (config.audioEnabled ? "true" : "false") + "\n";

        //Scanlines enabled
        data += "scanlines-enabled " +
            (config.scanlinesEnabled ? "true" : "false") + "\n";

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
            Files.createDirectories(configDirPath);
            Files.write(configFilePath, data.getBytes());
        } catch (Exception e) {
            Gdx.app.log("Warning", "Unable to save configuration.");
        }
    }
}

