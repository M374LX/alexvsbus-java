/*
 * Alex vs Bus
 * Copyright (C) 2021 M-374 LX
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
            quitted = false;
            enabled = true;
            sfxToPlay = NONE;
            sfxToStop = NONE;
            shouldStopAllSfx = false;
            this.sfx = sfx;
        }

        synchronized void quit() {
            quitted = true;
        }

        synchronized void enable(boolean b) {
            if (enabled != b) {
                enabled = b;
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
    boolean enabled;
    int sfxToPlay;
    int sfxToStop;
    int bgmToPlay;
    boolean shouldStopAllSfx;
    boolean shouldStopBgm;

    SfxThread sfxThread;

    //--------------------------------------------------------------------------

    Audio() {
        sfx = new Sound[NUM_SFX];
        for (int i = 0; i < NUM_SFX; i++) sfx[i] = null;

        bgm = null;

        quit = false;
        enabled = true;
        sfxToPlay = NONE;
        sfxToStop = NONE;
        bgmToPlay = NONE;
        shouldStopAllSfx = false;
        shouldStopBgm = false;

        sfxThread = new SfxThread(sfx);
    }

    void dispose() {
        if (bgm != null) {
            bgm.dispose();
            bgm = null;
        }

        sfxThread.quit();

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
        loadSound(SFX_DIALOG_CONFIRM, "dialog-confirm.wav");
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

    void enable(boolean b) {
        if (enabled == b) return;

        enabled = b;

        if (bgm != null) {
            if (enabled) bgm.play(); else bgm.stop();
        }

        sfxThread.enable(enabled);
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
            if (enabled) bgm.play();
        } catch (Exception e) {
            bgm = null;
        }
    }

    void stopBgm() {
        if (bgm != null) bgm.stop();
    }

    void unloadBgm() {
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

