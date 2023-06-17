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
            enabled = true;
            sfxToPlay = NONE;
            sfxToStop = NONE;
            this.sfx = sfx;
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

    Audio() {
        sfx = new Sound[NUM_SFX];

        audioEnabled = true;
        musicEnabled = true;
        sfxEnabled = true;
        sfxToPlay = NONE;
        sfxToStop = NONE;

        sfxThread = new SfxThread(sfx);
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
        loadSound(SFX_COIN, "coin.wav");
        loadSound(SFX_CRATE, "crate.wav");
        loadSound(SFX_DIALOG_SELECT, "dialog-select.wav");
        loadSound(SFX_ERROR, "error.wav");
        loadSound(SFX_FALL, "fall.wav");
        loadSound(SFX_HIT, "hit.wav");
        loadSound(SFX_HOLE, "hole.wav");
        loadSound(SFX_RESPAWN, "respawn.wav");
        loadSound(SFX_SCORE, "score.wav");
        loadSound(SFX_SLIP, "slip.wav");
        loadSound(SFX_SPRING, "spring.wav");
        loadSound(SFX_TIME, "time.wav");

        sfxThread.start();
    }

    void enableAudio(boolean en) {
        if (audioEnabled != en) {
            audioEnabled = en;

            if (bgm != null) {
                if (audioEnabled && musicEnabled) {
                    bgm.play();
                } else {
                    bgm.stop();
                }
            }

            sfxThread.enable(audioEnabled && sfxEnabled);
        }
    }

    void enableMusic(boolean en) {
        if (musicEnabled != en) {
            musicEnabled = en;

            if (bgm != null) {
                if (audioEnabled && musicEnabled) {
                    bgm.play();
                } else {
                    bgm.stop();
                }
            }
        }
    }

    void enableSfx(boolean en) {
        if (sfxEnabled != en) {
            sfxEnabled = en;
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
        String file;
        switch (id) {
            case BGMTITLE: file = "bgmtitle.ogg"; break;
            case BGM1:     file = "bgm1.ogg";     break;
            case BGM2:     file = "bgm2.ogg";     break;
            case BGM3:     file = "bgm3.ogg";     break;
            default:       return;
        }

        if (bgm != null) bgm.dispose();

        try {
            bgm = Gdx.audio.newMusic(Gdx.files.internal(file));
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

    //--------------------------------------------------------------------------

    void loadSound(int id, String path) {
        try {
            sfx[id] = Gdx.audio.newSound(Gdx.files.internal(path));
        } catch (Exception e) {
            sfx[id] = null;
        }
    }
}

