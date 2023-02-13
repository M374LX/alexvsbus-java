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


package org.alexvsbus;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.alexvsbus.Defs.Config;
import org.alexvsbus.Defs.PlatDep;

import static org.alexvsbus.Defs.NONE;
import static org.alexvsbus.Defs.DIFFICULTY_NORMAL;
import static org.alexvsbus.Defs.DIFFICULTY_HARD;
import static org.alexvsbus.Defs.DIFFICULTY_SUPER;
import static org.alexvsbus.Defs.DIFFICULTY_MAX;
import static org.alexvsbus.Defs.WM_UNSUPPORTED;
import static org.alexvsbus.Defs.difficultyNumLevels;
import static org.alexvsbus.Defs.vscreenWidths;
import static org.alexvsbus.Defs.vscreenHeights;

class AndroidPlatDep implements PlatDep {
    Config config;

    SharedPreferences prefs;
    Editor editor;

    AndroidPlatDep(SharedPreferences prefs) {
        this.prefs = prefs;
        editor = null;
        config = new Config();
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void postInit() {
    }

    @Override
    public void setMinWindowSize(int width, int height) {
    }

    public void loadConfig() {
        int numLevels;

        config.touchEnabled = true;
        config.windowMode = WM_UNSUPPORTED;
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
        config.useBackKey = true;

        try {
            config.audioEnabled = prefs.getBoolean("audio-enabled", true);
        } catch (Exception e) {
            config.audioEnabled = true;
        }

        try {
            config.scanlinesEnabled = prefs.getBoolean("scanlines-enabled", false);
        } catch (Exception e) {
            config.scanlinesEnabled = false;
        }

        try {
            config.touchButtonsEnabled =
                prefs.getBoolean("touch-buttons-enabled", true);
        } catch (Exception e) {
            config.touchButtonsEnabled = true;
        }

        try {
            config.vscreenAutoSize = prefs.getBoolean("vscreen-auto-size", true);
        } catch (Exception e) {
            config.vscreenAutoSize = true;
        }

        if (!config.vscreenAutoSize) {
            try {
                int val = prefs.getInt("vscreen-width", 480);
                if (val >= 1 && val <= 999) {
                    int i;
                    for (i = 0; i < vscreenWidths.length; i++) {
                        if (val == vscreenWidths[i]) {
                            config.vscreenWidth = val;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                config.vscreenAutoSize = true;
            }

            try {
                int val = prefs.getInt("vscreen-height", 270);
                if (val >= 1 && val <= 999) {
                    int i;
                    for (i = 0; i < vscreenHeights.length; i++) {
                        if (val == vscreenHeights[i]) {
                            config.vscreenHeight = val;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                config.vscreenAutoSize = true;
            }
        }

        try {
            int val = prefs.getInt("progress-difficulty", DIFFICULTY_NORMAL);
            if (val < DIFFICULTY_NORMAL || val > DIFFICULTY_MAX) {
                val = DIFFICULTY_NORMAL;
            }

            config.progressDifficulty = val;
        } catch (Exception e) {
            config.progressDifficulty = DIFFICULTY_NORMAL;
        }

        try {
            int val = prefs.getInt("progress-level", 1);
            if (val < 1 || val > 9) {
                val = 1;
            }

            config.progressLevel = val;
        } catch (Exception e) {
            config.progressLevel = 1;
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
        editor = prefs.edit();
        editor.putBoolean("audio-enabled", config.audioEnabled);
        editor.putBoolean("scanlines-enabled", config.scanlinesEnabled);
        editor.putBoolean("touch-buttons-enabled", config.touchButtonsEnabled);
        editor.putBoolean("vscreen-auto-size", config.vscreenAutoSize);
        editor.putInt("vscreen-width",  config.vscreenWidth);
        editor.putInt("vscreen-height", config.vscreenHeight);
        editor.putInt("progress-level", config.progressLevel);
        editor.putInt("progress-difficulty", config.progressDifficulty);
        editor.apply();
        editor = null;
    }
}

