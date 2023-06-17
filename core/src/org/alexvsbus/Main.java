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

import static org.alexvsbus.Data.vscreenWidths;
import static org.alexvsbus.Data.vscreenHeights;
import static org.alexvsbus.Data.difficultyNumLevels;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;

public class Main extends ApplicationAdapter implements Thread.UncaughtExceptionHandler {
    //Interface to platform-dependent methods
    PlatDep platDep;

    //Default uncaught exception handler
    Thread.UncaughtExceptionHandler defHandler;

    //Seconds since the previous frame
    float deltaTime;

    //Display parameters
    DisplayParams displayParams;

    //Configuration
    Config config;
    Config oldConfig;

    //Game progress
    boolean progressChecked;

    //Screen type (playing game, final score, ...)
    int screenType;

    //Gameplay
    PlayCtx playCtx;
    Play play;
    LevelLoad levelLoad;

    //Delayed action
    int delayedActionType;
    float actionDelay;

    //Dialogs
    DialogCtx dialogCtx;
    Dialogs dialogs;

    //Graphical rendering and audio
    Renderer renderer;
    Audio audio;

    //Input
    Input input;
    int inputHit;
    int inputHeld;
    int oldInputHeld;
    boolean waitInputUp;
    boolean wasTouching;

    //Screen wiping effects
    int wipeCmd;
    int wipeValue;
    int wipeDelta;
    float wipeDelay;

    // -------------------------------------------------------------------------

    public Main(PlatDep pd) {
        platDep = pd;
        config = platDep.getConfig();
    }

    @Override
    public void create() {
        displayParams = new DisplayParams();
        oldConfig = new Config();
        input = new Input(displayParams, config);
        audio = new Audio();
        play = new Play(displayParams, audio);
        playCtx = play.newCtx();
        dialogs = new Dialogs(displayParams, config, audio);
        dialogCtx = dialogs.newCtx();
        levelLoad = new LevelLoad(playCtx);
        renderer = new Renderer(displayParams, config, playCtx, dialogCtx);

        defHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.currentThread().setDefaultUncaughtExceptionHandler(this);

        delayedActionType = NONE;
        wipeCmd = NONE;

        audio.loadSfx();
        play.clear();
        renderer.load();

        audio.enableAudio(config.audioEnabled);
        audio.enableMusic(config.musicEnabled);
        audio.enableSfx(config.sfxEnabled);
        changeWindowMode();

        config.fullscreen = Gdx.graphics.isFullscreen();

        oldConfig.fullscreen = config.fullscreen;
        oldConfig.windowScale = config.windowScale;
        oldConfig.vscreenAutoSize = config.vscreenAutoSize;
        oldConfig.audioEnabled = config.audioEnabled;
        oldConfig.musicEnabled = config.musicEnabled;
        oldConfig.sfxEnabled = config.sfxEnabled;

        platDep.postInit();

        showTitle();
    }

    @Override
    public void render() {
        getDeltaTime();
        handleInput();

        if (dialogCtx.stackSize > 0) { //If a dialog is open
            dialogs.handleKeys(inputHeld, inputHit);
            dialogs.update(deltaTime);
            handleDialogAction();

            //Dialog just closed
            if (dialogCtx.stackSize == 0) {
                waitInputUp = true;
            }
        } else if (screenType == SCR_PLAY) {
            play.setInput(inputHeld);
            play.update(deltaTime);
            handlePause();
            checkGameProgress();
            handleLevelEnd();
        }

        handleConfigChange();
        handleDelayedAction();
        updateScreenWipe();
        renderer.draw(screenType, inputHeld, wipeValue);
    }

    //Unlike the similarly named method handlePause(), this one is called by
    //libGDX when the app loses focus on Android
    @Override
    public void pause() {
        boolean dialogOpen = dialogCtx.stackSize > 0;

        //Show the pause dialog when losing focus while playing the game
        //(Android only, as when pressing the home button)
        if (screenType == SCR_PLAY && playCtx.canPause && !dialogOpen) {
            dialogs.open(DLG_PAUSE);
        }
    }

    @Override
    public void resize(int newWidth, int newHeight) {
        int minWindowWidth;
        int minWindowHeight;

        if (config.vscreenAutoSize) {
            minWindowWidth  = VSCREEN_MIN_WIDTH;
            minWindowHeight = VSCREEN_MIN_HEIGHT;
        } else {
            minWindowWidth  = displayParams.vscreenWidth;
            minWindowHeight = displayParams.vscreenHeight;
        }

        platDep.setMinWindowSize(minWindowWidth, minWindowHeight);

        displayParams.physWidth  = newWidth;
        displayParams.physHeight = newHeight;

        //Ensure the window is not smaller than the minimum size
        if (!Gdx.graphics.isFullscreen()) {
            int width  = Gdx.graphics.getWidth();
            int height = Gdx.graphics.getHeight();

            if (width  < minWindowWidth)  width  = minWindowWidth;
            if (height < minWindowHeight) height = minWindowHeight;

            displayParams.physWidth  = width;
            displayParams.physHeight = height;

            Gdx.graphics.setWindowedMode(width, height);
        }

        if (config.vscreenAutoSize) {
            autoSizeVscreen();
        } else {
            scaleManualVscreen();
        }

        if (screenType == SCR_PLAY || screenType == SCR_PLAY_FREEZE) {
            play.onScreenResize();
        }

        dialogs.adaptToScreenSize();
        renderer.onScreenResize();
    }

    @Override
    public void dispose() {
        platDep.saveConfig();
        renderer.dispose();
        audio.dispose();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            //Just in case an uncaught exception happens, stop the sound
            //effects thread
            audio.stopSfxThread();
        } finally {
            defHandler.uncaughtException(t, e);
        }
    }


    //--------------------------------------------------------------------------

    void getDeltaTime() {
        deltaTime = Gdx.graphics.getDeltaTime();

        //Limit delta time to prevent problems with collision detection
        if (deltaTime > MAX_DT) deltaTime = MAX_DT;
    }

    void handleInput() {
        config.showTouchControls = false;

        if (config.touchEnabled) {
            boolean touching = false;
            boolean playing = (screenType == SCR_PLAY);
            boolean ending = (playing && playCtx.levelNum == LVLNUM_ENDING);
            boolean dialogOpen = (dialogCtx.stackSize > 0);
            int i;

            if (!dialogOpen && playing && !ending) {
                config.showTouchControls = true;
            }

            for (i = 0; i < 10; i++) {
                float x, y;

                if (!Gdx.input.isTouched(i)) continue;

                touching = true;

                //Convert coordinates from physical screen to virtual screen
                x = Gdx.input.getX(i);
                x -= displayParams.viewportOffsetX;
                x /= displayParams.viewportWidth;
                x *= displayParams.vscreenWidth;

                y = Gdx.input.getY(i);
                y -= displayParams.viewportOffsetY;
                y /= displayParams.viewportHeight;
                y *= displayParams.vscreenHeight;

                input.onTouch(x, y);
                if (!wasTouching) {
                    dialogs.onTap((int)x, (int)y);
                }
            }

            wasTouching = touching;
        }

        inputHeld = input.read();
        if (waitInputUp) {
            if (inputHeld == 0) {
                waitInputUp = false;
            } else {
                inputHeld = 0;
            }
        }

        inputHit = inputHeld & (~oldInputHeld);
        oldInputHeld = inputHeld;

        //Handle keys that change configuration
        if ((inputHit & INPUT_CFG_FULLSCREEN_TOGGLE) > 0) {
            if (!config.fixedWindowMode) {
                config.fullscreen = !config.fullscreen;
            }
        }
        if ((inputHit & INPUT_CFG_AUDIO_TOGGLE) > 0) {
            config.audioEnabled = !config.audioEnabled;
        }
        if ((inputHit & INPUT_CFG_SCANLINES_TOGGLE) > 0) {
            config.scanlinesEnabled = !config.scanlinesEnabled;
        }
    }

    void handleDialogAction() {
        int param = dialogCtx.actionParam;
        int levelNum = (param & 0xF);
        int difficulty = (param >> 4);

        switch (dialogCtx.action) {
            case DLGACT_QUIT:
                if (screenType == SCR_PLAY) {
                    showTitle();
                } else {
                    dialogs.closeAll();
                    Gdx.app.exit();
                }
                break;

            case DLGACT_TITLE:
                showTitle();
                break;

            case DLGACT_PLAY:
                dialogs.closeAll();
                startLevel(levelNum, difficulty, false);
                break;

            case DLGACT_TRYAGAIN_WIPE:
                dialogs.closeAll();
                playCtx.canPause = false;
                screenType = SCR_PLAY_FREEZE;
                wipeCmd = WIPECMD_OUT;
                delayedActionType = DELACT_TRY_AGAIN;
                actionDelay = 1.0f;
                break;

            case DLGACT_TRYAGAIN_IMMEDIATE:
                dialogs.closeAll();
                delayedActionType = DELACT_TRY_AGAIN;
                actionDelay = 0;
                break;
        }

        dialogCtx.action = NONE;
    }

    //Unlike the similarly named method pause(), this one checks if the user
    //has paused the game and acts accordingly
    void handlePause() {
        boolean pause = (inputHit & INPUT_PAUSE) > 0;
        boolean pauseTouch = (inputHit & INPUT_PAUSE_TOUCH) > 0;

        if (playCtx.canPause && (pause || pauseTouch)) {
            dialogCtx.useCursor = !pauseTouch;
            dialogs.open(DLG_PAUSE);
            audio.stopAllSfx();
            waitInputUp = true;
        }
    }

    void checkGameProgress() {
        int numLevels = difficultyNumLevels[playCtx.difficulty];

        if (progressChecked) return;
        if (!playCtx.goalReached) return;

        progressChecked = true;

        if (config.progressCheat) return;
        if (playCtx.levelNum != config.progressLevel) return;
        if (playCtx.difficulty != config.progressDifficulty) return;

        //Unlock next level
        config.progressLevel++;
        if (config.progressLevel > numLevels) {
            if (config.progressDifficulty < DIFFICULTY_MAX) {
                config.progressLevel = 1;
                config.progressDifficulty++;
            } else {
                config.progressLevel = numLevels;
            }
        }

        //Try to save the configuration, which includes the game progress
        if (!platDep.saveConfig()) {
            //Display a message on the screen if it fails
            renderer.saveFailed = true;
        }
    }

    void handleLevelEnd() {
        if (playCtx.sequenceStep != SEQ_FINISHED) return;

        if (playCtx.levelNum == LVLNUM_ENDING) {
            showFinalScore();
        } else if (playCtx.timeUp) {
            screenType = SCR_BLANK;
            wipeCmd = WIPECMD_CLEAR;
            audio.stopBgm();
            dialogs.open(DLG_TRYAGAIN_TIMEUP);
        } else if (playCtx.goalReached) {
            if (!playCtx.lastLevel) {
                startLevel(playCtx.levelNum + 1, playCtx.difficulty, false);
            } else if (playCtx.difficulty == DIFFICULTY_MAX) {
                showFinalScore();
            } else {
                startEndingSequence();
            }
        }
    }

    void handleConfigChange() {
        //Manually set virtual screen (vscreen) size change
        if (!config.vscreenAutoSize) {
            boolean sizeChanged = false;

            if (config.vscreenWidth != displayParams.vscreenWidth) {
                sizeChanged = true;
            }
            if (config.vscreenHeight != displayParams.vscreenHeight) {
                sizeChanged = true;
            }

            if (sizeChanged) {
                displayParams.vscreenWidth  = config.vscreenWidth;
                displayParams.vscreenHeight = config.vscreenHeight;

                if (!Gdx.graphics.isFullscreen() && !config.resizableWindow) {
                    int width  = config.vscreenWidth  * config.windowScale;
                    int height = config.vscreenHeight * config.windowScale;

                    Gdx.graphics.setWindowedMode(width, height);
                } else {
                    resize(displayParams.physWidth, displayParams.physHeight);
                }
            }
        }

        //Virtual screen (vscreen) sizing mode change
        if (config.vscreenAutoSize != oldConfig.vscreenAutoSize) {
            resize(displayParams.physWidth, displayParams.physHeight);
            oldConfig.vscreenAutoSize = config.vscreenAutoSize;
        }

        if (!config.fixedWindowMode) {
            //Fullscreen toggle
            if (config.fullscreen != oldConfig.fullscreen) {
                changeWindowMode();
                config.fullscreen = Gdx.graphics.isFullscreen();
                oldConfig.fullscreen = config.fullscreen;
            }

            //Window scale change
            if (config.windowScale != oldConfig.windowScale) {
                if (!Gdx.graphics.isFullscreen() && !config.resizableWindow) {
                    changeWindowMode();
                }

                oldConfig.windowScale = config.windowScale;
            }
        }

        //Audio toggle
        if (config.audioEnabled != oldConfig.audioEnabled) {
            audio.enableAudio(config.audioEnabled);
            oldConfig.audioEnabled = config.audioEnabled;
        }

        //Music toggle
        if (config.musicEnabled != oldConfig.musicEnabled) {
            audio.enableMusic(config.musicEnabled);
            oldConfig.musicEnabled = config.musicEnabled;
        }

        //Sound effects toggle
        if (config.sfxEnabled != oldConfig.sfxEnabled) {
            audio.enableSfx(config.sfxEnabled);
            oldConfig.sfxEnabled = config.sfxEnabled;
        }

    }

    void handleDelayedAction() {
        if (delayedActionType == NONE) return;

        actionDelay -= deltaTime;
        if (actionDelay > 0) return;

        switch (delayedActionType) {
            case DELACT_TITLE:
                showTitle();
                break;

            case DELACT_NEXT_DIFFICULTY:
                startLevel(1, playCtx.difficulty + 1, false);
                break;

            case DELACT_TRY_AGAIN:
                playCtx.score = 0;
                startLevel(playCtx.levelNum, playCtx.difficulty, true);
                break;
        }

        delayedActionType = NONE;
    }

    void updateScreenWipe() {
        if (playCtx.wipeIn) {
            wipeCmd = WIPECMD_IN;
            playCtx.wipeIn = false;
        }
        if (playCtx.wipeOut) {
            wipeCmd = WIPECMD_OUT;
            playCtx.wipeOut = false;
        }

        switch (wipeCmd) {
            case WIPECMD_IN:
                wipeDelay = WIPE_MAX_DELAY;
                wipeValue = WIPE_MAX_VALUE;
                wipeDelta = -WIPE_DELTA;
                break;

            case WIPECMD_OUT:
                wipeDelay = WIPE_MAX_DELAY;
                wipeValue = 0;
                wipeDelta = WIPE_DELTA;
                break;

            case WIPECMD_CLEAR:
                wipeValue = 0;
                wipeDelta = 0;
                break;
        }
        wipeCmd = NONE;

        wipeDelay -= deltaTime;
        if (wipeDelay > 0) return;

        wipeDelay = WIPE_MAX_DELAY;
        wipeValue += wipeDelta;

        if (wipeValue <= 0) {
            wipeValue = 0;
            wipeDelta = 0;
        } else if (wipeValue >= WIPE_MAX_VALUE) {
            wipeValue = WIPE_MAX_VALUE;
            wipeDelta = 0;
        }
    }

    //--------------------------------------------------------------------------

    void showTitle() {
        screenType = SCR_BLANK;

        playCtx.score = 0;
        playCtx.levelNum = -1;

        audio.stopAllSfx();
        audio.playBgm(BGMTITLE);

        dialogs.closeAll();
        dialogs.open(DLG_MAIN);

        wipeCmd = WIPECMD_CLEAR;
    }

    void showFinalScore() {
        screenType = SCR_FINALSCORE;

        if (playCtx.difficulty == DIFFICULTY_MAX) {
            delayedActionType = DELACT_TITLE;
        } else {
            delayedActionType = DELACT_NEXT_DIFFICULTY;
        }
        actionDelay = 4.0f;

        wipeCmd = WIPECMD_CLEAR;
    }

    void startLevel(int levelNum, int difficulty, boolean skipInitialSequence) {
        int err;
        String filename = "level" + levelNum;

        switch (difficulty) {
            case DIFFICULTY_NORMAL: filename += 'n'; break;
            case DIFFICULTY_HARD:   filename += 'h'; break;
            case DIFFICULTY_SUPER:  filename += 's'; break;
        }

        renderer.saveFailed = false;
        play.clear();

        err = levelLoad.load(filename);
        if (err != LVLERR_NONE) {
            String msg = "";

            switch (err) {
                case LVLERR_CANNOT_OPEN:
                    msg = "Cannot open level file";
                    break;

                case LVLERR_TOO_LARGE:
                    msg = "Level file too large (over 4 kB)";
                    break;

                case LVLERR_INVALID:
                    msg = "Invalid level file";
                    break;
            }

            msg += ":\n" + filename;

            screenType = SCR_BLANK;
            wipeCmd = WIPECMD_CLEAR;
            dialogs.showError(msg);

            return;
        }

        progressChecked = false;
        screenType = SCR_PLAY;

        playCtx.difficulty = difficulty;
        playCtx.levelNum = levelNum;
        playCtx.lastLevel = (levelNum == difficultyNumLevels[difficulty]);
        playCtx.sequenceStep = SEQ_INITIAL;
        playCtx.skipInitialSequence = skipInitialSequence;

        if (playCtx.lastLevel) {
            playCtx.bus.numCharacters = 3;
        } else {
            switch (levelNum) {
                case 1: playCtx.bus.numCharacters = 0; break;
                case 2: playCtx.bus.numCharacters = 0; break;
                case 3: playCtx.bus.numCharacters = 1; break;
                case 4: playCtx.bus.numCharacters = 2; break;
                case 5: playCtx.bus.numCharacters = 3; break;
            }
        }

        playCtx.bus.routeSign = levelNum;
        playCtx.cam.fixedAtLeftmost = true;

        audio.playBgm(playCtx.bgm);
        wipeCmd = WIPECMD_IN;

        play.onScreenResize();
    }

    void startEndingSequence() {
        renderer.saveFailed = false;
        play.clear();

        progressChecked = false;
        screenType = SCR_PLAY;

        playCtx.levelNum = LVLNUM_ENDING;
        playCtx.lastLevel = false;

        playCtx.levelSize = 8 * VSCREEN_MAX_WIDTH;
        playCtx.bgColor = SPR_BG_SKY3;
        playCtx.bgm = BGM3;

        playCtx.sequenceStep = SEQ_ENDING;
        playCtx.sequenceDelay = 0;

        audio.playBgm(playCtx.bgm);
        wipeCmd = WIPECMD_IN;
    }

    //--------------------------------------------------------------------------

    void changeWindowMode() {
        if (config.fullscreen) {
            Monitor m = Gdx.graphics.getMonitor();
            DisplayMode dm = Gdx.graphics.getDisplayMode(m);

            Gdx.graphics.setFullscreenMode(dm);
        } else {
            int width;
            int height;

            if (config.vscreenAutoSize) {
                width  = VSCREEN_MAX_WIDTH;
                height = VSCREEN_MAX_HEIGHT;
            } else {
                width  = config.vscreenWidth;
                height = config.vscreenHeight;
            }

            width  *= config.windowScale;
            height *= config.windowScale;

            Gdx.graphics.setWindowedMode(width, height);
        }

        if (!config.touchEnabled && Gdx.graphics.isFullscreen()) {
            Gdx.input.setCursorCatched(true);
        } else {
            Gdx.input.setCursorCatched(false);
        }
    }

    void autoSizeVscreen() {
        boolean smallScreen = false;
        int physWidth  = displayParams.physWidth;
        int physHeight = displayParams.physHeight;
        int vscreenWidth  = 0;
        int vscreenHeight = 0;
        int bestScaledWidth  = 0;
        int bestScaledHeight = 0;
        int scale = 1;
        int i, j;

        if (physWidth < VSCREEN_AUTO_MIN_WIDTH) {
            smallScreen = true;
        }
        if (physHeight < VSCREEN_AUTO_MIN_HEIGHT) {
            smallScreen = true;
        }

        //Determine the size for the virtual screen (vscreen) so that it best
        //fits in the physical screen or window
        for (i = 0; i < vscreenWidths.length; i++) {
            for (j = 0; j < vscreenHeights.length; j++) {
                int scaledWidth  = 0;
                int scaledHeight = 0;
                int w = vscreenWidths[i];
                int h = vscreenHeights[j];

                if (!smallScreen && w < VSCREEN_AUTO_MIN_WIDTH)  continue;
                if (!smallScreen && h < VSCREEN_AUTO_MIN_HEIGHT) continue;

                for (scale = 8; scale > 1; scale--) {
                    if ((w * scale) <= physWidth && (h * scale) <= physHeight) {
                        break;
                    }
                }

                scaledWidth  = w * scale;
                scaledHeight = h * scale;

                if (scaledWidth > physWidth || scaledHeight > physHeight) {
                    continue;
                }

                if (scaledWidth > bestScaledWidth && scaledHeight > bestScaledHeight) {
                    bestScaledWidth  = scaledWidth;
                    bestScaledHeight = scaledHeight;
                    vscreenWidth  = w;
                    vscreenHeight = h;
                }
            }
        }

        applyDisplayParams(physWidth, physHeight, vscreenWidth, vscreenHeight, scale);
    }

    void scaleManualVscreen() {
        int physWidth  = displayParams.physWidth;
        int physHeight = displayParams.physHeight;
        int vscreenWidth  = config.vscreenWidth;
        int vscreenHeight = config.vscreenHeight;
        int scale;

        for (scale = 8; scale > 1; scale--) {
            int w = vscreenWidth;
            int h = vscreenHeight;

            if ((w * scale) <= physWidth && (h * scale) <= physHeight) {
                break;
            }
        }

        applyDisplayParams(physWidth, physHeight, vscreenWidth, vscreenHeight, scale);
    }

    void applyDisplayParams(int physWidth, int physHeight,
                            int vscreenWidth, int vscreenHeight, int scale) {

        int viewportWidth  = vscreenWidth  * scale;
        int viewportHeight = vscreenHeight * scale;

        displayParams.vscreenWidth    = vscreenWidth;
        displayParams.vscreenHeight   = vscreenHeight;
        displayParams.viewportWidth   = viewportWidth;
        displayParams.viewportHeight  = viewportHeight;
        displayParams.viewportOffsetX = (physWidth  - viewportWidth)  / 2;
        displayParams.viewportOffsetY = (physHeight - viewportHeight) / 2;
        displayParams.scale = scale;
    }
}

