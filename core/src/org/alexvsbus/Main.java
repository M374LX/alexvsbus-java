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

    //Delta time (seconds since the previous frame)
    float dt;

    //Display parameters
    DisplayParams displayParams;

    //Configuration
    Config config;
    int oldWindowMode;
    boolean oldAudioEnabled;

    //Game progress
    boolean progressChecked;

    //Screen type
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

    public Main(PlatDep platDep) {
        this.platDep = platDep;
        config = platDep.getConfig();
    }

    @Override
    public void create() {
        displayParams = new DisplayParams();
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

        oldInputHeld = 0;
        waitInputUp = false;
        wasTouching = false;
        config.showTouchControls = false;

        delayedActionType = NONE;

        wipeCmd = NONE;
        wipeValue = 0;
        wipeDelta = 0;
        wipeDelay = 0;

        progressChecked = false;
        config.progressCheat = false;

        audio.loadSfx();
        play.clear();
        renderer.load();

        oldWindowMode = -1;
        oldAudioEnabled = true;
        handleConfigChange();

        showTitle();
    }

    @Override
    public void render() {
        getDeltaTime();
        handleInput();

        if (dialogCtx.stackSize > 0) { //If a dialog is open
            dialogs.handleKeys(inputHeld, inputHit);
            dialogs.update(dt);
            handleDialogAction();

            //Dialog just closed
            if (dialogCtx.stackSize == 0) {
                waitInputUp = true;
            }
        } else if (screenType == SCR_PLAY) {
            play.setInput(inputHeld);
            play.update(dt);
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
        int physWidth  = newWidth;
        int physHeight = newHeight;

        if (config.vscreenAutoSize) {
            autoSizeVscreen(physWidth, physHeight);
        } else {
            scaleManualVscreen(physWidth, physHeight);
        }

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
        dt = Gdx.graphics.getDeltaTime();

        //Limit delta time to prevent problems with collision detection
        if (dt > MAX_DT) dt = MAX_DT;
    }

    void handleInput() {
        config.showTouchControls = false;

        if (config.touchEnabled) {
            boolean touching = false;
            boolean playing = (screenType == SCR_PLAY);
            boolean ending = (playCtx.levelNum == LVLNUM_ENDING);
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
        if ((inputHit & INPUT_CFG_WINDOW_MODE) > 0) {
            if (config.windowMode != WM_UNSUPPORTED) {
                config.windowMode++;
                if (config.windowMode > WM_FULLSCREEN) {
                    config.windowMode = WM_1X;
                }
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

        config.progressLevel++;
        if (config.progressLevel > numLevels) {
            if (config.progressDifficulty < DIFFICULTY_MAX) {
                config.progressLevel = 1;
                config.progressDifficulty++;
            } else {
                config.progressLevel = numLevels;
            }
        }

        platDep.saveConfig();
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
        //Window mode change
        if (config.windowMode != WM_UNSUPPORTED && config.windowMode != oldWindowMode) {
            boolean modeChanged = false;

            if (config.windowMode == WM_FULLSCREEN) {
                Monitor m = Gdx.graphics.getMonitor();
                DisplayMode dm = Gdx.graphics.getDisplayMode(m);

                modeChanged = Gdx.graphics.setFullscreenMode(dm);
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

                if (config.windowMode == WM_2X) {
                    width  *= 2;
                    height *= 2;
                } else if (config.windowMode == WM_3X) {
                    width  *= 3;
                    height *= 3;
                }

                modeChanged = Gdx.graphics.setWindowedMode(width, height);
            }

            if (modeChanged) {
                oldWindowMode = config.windowMode;
            } else {
                config.windowMode = oldWindowMode;
            }

            if (!config.touchEnabled && Gdx.graphics.isFullscreen()) {
                Gdx.input.setCursorCatched(true);
            } else {
                Gdx.input.setCursorCatched(false);
            }
        }

        //Audio toggle
        if (config.audioEnabled != oldAudioEnabled) {
            audio.enable(config.audioEnabled);
            oldAudioEnabled = config.audioEnabled;
        }
    }

    void handleDelayedAction() {
        if (delayedActionType == NONE) return;

        actionDelay -= dt;
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

        wipeDelay -= dt;
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

        play.clear();

        err = levelLoad.load(filename);
        if (err != LVLERR_NONE) {
            String msg = "";

            switch (err) {
                case LVLERR_CANNOT_OPEN:
                    msg = "Cannot load level file";
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

        audio.playBgm(playCtx.bgm);
        wipeCmd = WIPECMD_IN;
    }

    void startEndingSequence() {
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

    void autoSizeVscreen(int physWidth, int physHeight) {
        int vscreenWidth  = 0;
        int vscreenHeight = 0;
        int bestWidthDiff = 99999;
        int bestHeightDiff = 99999;
        int viewportWidth;
        int viewportHeight;
        int scale = 1;
        int i, j;

        //Determine the virtual screen size that best fits the physical screen
        for (i = 0; i < vscreenWidths.length; i++) {
            for (j = 0; j < vscreenWidths.length; j++) {
                int scaledWidth  = 0;
                int scaledHeight = 0;
                int widthDiff;
                int heightDiff;
                int w = vscreenWidths[i];
                int h = vscreenHeights[j];

                for (scale = 1; scale <= 8; scale++) {
                    scaledWidth  = w * scale;
                    scaledHeight = h * scale;

                    if (scaledWidth > physWidth || scaledHeight > physHeight) {
                        //With the scale being zero, nothing would appear on
                        //the screen
                        if (scale > 1) {
                            scale -= 1;
                        }

                        scaledWidth  = w * scale;
                        scaledHeight = h * scale;

                        break;
                    }
                }

                widthDiff  = physWidth  - scaledWidth;
                heightDiff = physHeight - scaledHeight;

                if (widthDiff < bestWidthDiff && heightDiff < bestHeightDiff) {
                    bestWidthDiff  = widthDiff;
                    bestHeightDiff = heightDiff;
                    vscreenWidth   = w;
                    vscreenHeight  = h;
                }
            }
        }

        applyDisplayParams(physWidth, physHeight, vscreenWidth, vscreenHeight, scale);
    }

    void scaleManualVscreen(int physWidth, int physHeight) {
        int vscreenWidth  = config.vscreenWidth;
        int vscreenHeight = config.vscreenHeight;
        int scale;

        for (scale = 1; scale <= 8; scale++) {
            int scaledWidth  = vscreenWidth  * scale;
            int scaledHeight = vscreenHeight * scale;

            if (scaledWidth > physWidth || scaledHeight > physHeight) {
                //With the scale being zero, nothing would appear on the screen
                if (scale > 1) {
                    scale -= 1;
                }

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

