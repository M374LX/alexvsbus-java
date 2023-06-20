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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

class Audio {
    //We handle the sound effects on a separate thread because not doing so
    //results in undesired delays on Android
    class SfxThread extends Thread {
        volatile boolean quitted;
        boolean enabled;
        int sfxToPlay;
        int sfxToStop;
        boolean shouldStopAllSfx;
        Sound sfx[];

        SfxThread(Sound sfx[]) {
            super("alexvsbus-sfx");
            this.sfx = sfx;
            sfxToPlay = NONE;
            sfxToStop = NONE;
        }

        synchronized void quit() {
            quitted = true;
        }

        synchronized void enable(boolean en) {
            if (enabled != en) {
                enabled = en;
                if (!enabled) stopAllSfxInternal();
            }
        }

        synchronized void playSfx(int id) {
            sfxToPlay = id;
        }

        synchronized void stopSfx(int id) {
            sfxToStop = id;
        }

        synchronized void stopAllSfx() {
            shouldStopAllSfx = true;
        }

        @Override
        public void run() {
            while (!quitted) {
                synchronized(this) {
                    if (enabled && sfxToPlay != NONE && sfx[sfxToPlay] != null) {
                        sfx[sfxToPlay].stop();
                        sfx[sfxToPlay].play();
                    }

                    if (sfxToStop != NONE && sfx[sfxToStop] != null) {
                        sfx[sfxToStop].stop();
                    }

                    if (shouldStopAllSfx) {
                        stopAllSfxInternal();
                        shouldStopAllSfx = false;
                    }

                    sfxToPlay = NONE;
                    sfxToStop = NONE;
                    shouldStopAllSfx = false;
                }
            }
        }

        void stopAllSfxInternal() {
            for (int i = 0; i < NUM_SFX; i++) {
                if (sfx[i] != null) sfx[i].stop();
            }
        }
    }

    Config config;

    Sound sfx[];
    Music bgm;

    volatile boolean quit;
    boolean audioEnabled;
    boolean musicEnabled;
    boolean sfxEnabled;
    int sfxToPlay;
    int sfxToStop;
    boolean shouldStopAllSfx;
    boolean shouldStopBgm;

    SfxThread sfxThread;

    //--------------------------------------------------------------------------

    Audio(Config cfg) {
        config = cfg;
        sfx = new Sound[NUM_SFX];
        sfxThread = new SfxThread(sfx);

        sfxToPlay = NONE;
        sfxToStop = NONE;
    }

    void stopSfxThread() {
        sfxThread.quit();
    }

    void dispose() {
        stopSfxThread();

        if (bgm != null) {
            bgm.dispose();
            bgm = null;
        }

        for (int i = 0; i < NUM_SFX; i++) {
            if (sfx[i] != null) {
                sfx[i].dispose();
                sfx[i] = null;
            }
        }
    }

    void loadSfx() {
        int i;

        for (i = 0; i < NUM_SFX; i++) {
            try {
                sfx[i] = Gdx.audio.newSound(Gdx.files.internal(Data.sfxFiles[i]));
            } catch (Exception e) {
                sfx[i] = null;
            }
        }

        sfxThread.start();
    }

    void handleToggling() {
        boolean audioToggled = false;
        boolean musicToggled = false;
        boolean sfxToggled   = false;

        if (audioEnabled != config.audioEnabled) {
            audioEnabled = config.audioEnabled;
            audioToggled = true;
        }
        if (musicEnabled != config.musicEnabled) {
            musicEnabled = config.musicEnabled;
            musicToggled = true;
        }
        if (sfxEnabled != config.sfxEnabled) {
            sfxEnabled = config.sfxEnabled;
            sfxToggled = true;
        }

        if (audioToggled || musicToggled) {
            if (bgm != null) {
                if (audioEnabled && musicEnabled) {
                    bgm.play();
                } else {
                    bgm.stop();
                }
            }
        }
        if (audioToggled || sfxToggled) {
            sfxThread.enable(audioEnabled && sfxEnabled);
        }
    }

    void playSfx(int id) {
        sfxThread.playSfx(id);
    }

    void stopSfx(int id) {
        sfxThread.stopSfx(id);
    }

    void stopAllSfx() {
        sfxThread.stopAllSfx();
    }

    void playBgm(int id) {
        if (bgm != null) bgm.dispose();

        try {
            bgm = Gdx.audio.newMusic(Gdx.files.internal(Data.bgmFiles[id]));
            bgm.setLooping(true);
            if (audioEnabled && musicEnabled) bgm.play();
        } catch (Exception e) {
            bgm = null;
        }
    }

    void stopBgm() {
        if (bgm != null) {
            bgm.stop();
            bgm.dispose();
            bgm = null;
        }
    }
}

