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

import static org.alexvsbus.Defs.*;

import static org.alexvsbus.Data.difficultyNumLevels;
import static org.alexvsbus.Data.vscreenWidths;
import static org.alexvsbus.Data.vscreenHeights;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.alexvsbus.Defs.Config;
import org.alexvsbus.Defs.PlatDep;

class AndroidPlatDep implements PlatDep {
    Config config;

    SharedPreferences prefs;
    Editor editor;

    AndroidPlatDep(SharedPreferences prefs) {
        this.prefs = prefs;
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

        //Use the back key only on Android, as the value of the libGDX constant
        //Keys.BACK conflicts with that of Keys.META_SYM_ON (the Windows logo
        //key on desktop)
        config.useBackKey = true;

        //Display settings
        config.fullscreen = true;
        config.fixedWindowMode = true;
        config.resizableWindow = false;
        config.scanlinesEnabled = getPrefsBoolean("scanlines-enabled", false);
        config.vscreenAutoSize = getPrefsBoolean("vscreen-auto-size", true);
        config.vscreenWidth  = -1;
        config.vscreenHeight = -1;

        //Audio settings
        config.audioEnabled = getPrefsBoolean("audio-enabled", true);
        config.musicEnabled = getPrefsBoolean("music-enabled", true);
        config.sfxEnabled = getPrefsBoolean("sfx-enabled", true);

        //Touchscreen settings
        config.touchEnabled = true;
        config.touchButtonsEnabled = getPrefsBoolean("touch-buttons-enabled", true);

        //Game progress
        config.progressDifficulty = getPrefsInt("progress-difficulty",
                        DIFFICULTY_NORMAL, DIFFICULTY_NORMAL, DIFFICULTY_MAX);
        config.progressLevel = getPrefsInt("progress-level", 1, 1, 9);

        if (!config.vscreenAutoSize) {
            int val;
            int i;

            val = getPrefsInt("vscreen-width", 480, 1, 999);
            for (i = 0; i < vscreenWidths.length; i++) {
                if (val == vscreenWidths[i]) {
                    config.vscreenWidth = val;
                    break;
                }
            }

            val = getPrefsInt("vscreen-height", 270, 1, 999);
            for (i = 0; i < vscreenHeights.length; i++) {
                if (val == vscreenHeights[i]) {
                    config.vscreenHeight = val;
                    break;
                }
            }

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
        editor = prefs.edit();
        editor.putBoolean("scanlines-enabled", config.scanlinesEnabled);
        editor.putBoolean("touch-buttons-enabled", config.touchButtonsEnabled);
        editor.putBoolean("vscreen-auto-size", config.vscreenAutoSize);
        editor.putInt("vscreen-width",  config.vscreenWidth);
        editor.putInt("vscreen-height", config.vscreenHeight);
        editor.putBoolean("audio-enabled", config.audioEnabled);
        editor.putBoolean("music-enabled", config.musicEnabled);
        editor.putBoolean("sfx-enabled", config.sfxEnabled);
        editor.putInt("progress-level", config.progressLevel);
        editor.putInt("progress-difficulty", config.progressDifficulty);
        editor.apply();
        editor = null;

        return true;
    }

    boolean getPrefsBoolean(String key, boolean def) {
        boolean ret = def;

        try {
            ret = prefs.getBoolean(key, def);
        } catch (Exception e) {
            ret = def;
        }

        return ret;
    }

    int getPrefsInt(String key, int def, int min, int max) {
        int ret = def;

        try {
            ret = prefs.getInt(key, def);

            if (ret < min || ret > max) {
                ret = def;
            }
        } catch (Exception e) {
            ret = def;
        }

        return ret;
    }
}

