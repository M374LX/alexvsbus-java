/*
 * Alex vs Bus
 * Copyright (C) 2021-2022 M374LX
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

    //Delay on final score screen and when the player selects "Try again" on
    //the pause dialog
    float screenDelay;

    //True if the player has selected "Try again"
    boolean tryAgain;

    //Dialogs
    DialogCtx dialogCtx;
    Dialogs dialogs;

    //Graphical rendering and audio
    Renderer renderer;
    Audio audio;

    //Input
    Input input;
    int oldInputHeld;
    boolean waitInputUp;
    boolean wasTouching;

    //Screen wiping effects
    int wipeValue;
    int wipeDelta;
    float wipeDelay;

    //Viewport and projection within the screen
    int viewportX;
    int viewportWidth;
    int viewportHeight;
    int projectionHeight;

    // -------------------------------------------------------------------------

    public Main(PlatDep platDep) {
        this.platDep = platDep;
        config = platDep.getConfig();
    }

    @Override
    public void create() {
        input = new Input(config);
        audio = new Audio();
        play = new Play(audio);
        playCtx = play.newCtx();
        dialogs = new Dialogs(audio, config);
        dialogCtx = dialogs.newCtx();
        levelLoad = new LevelLoad(playCtx);
        renderer = new Renderer(playCtx, dialogCtx);

        defHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.currentThread().setDefaultUncaughtExceptionHandler(this);

        oldInputHeld = 0;
        waitInputUp = false;
        wasTouching = false;
        config.hideTouchControls = true;

        tryAgain = false;

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
        int inputHeld, inputHit;
        boolean playing = (screenType == SCR_PLAY);
        boolean dialogOpen = (dialogCtx.stackSize > 0);
        float dt = Gdx.graphics.getDeltaTime();

        //Limit delta time to prevent problems with collision detection
        if (dt > MAX_DT) dt = MAX_DT;

        //Decide whether to show or hide touchscreen game controls
        config.hideTouchControls = false;
        if (!config.touchEnabled || dialogOpen || !playing) {
            config.hideTouchControls = true;
        }

        //Handle user input
        handleTouch();
        inputHeld = input.read();
        if (waitInputUp) {
            if (inputHeld == 0) {
                waitInputUp = false;
            } else {
                inputHeld = 0;
            }
        }

        inputHit = inputHeld & (~oldInputHeld);

        //Handle keys that change configuration
        if (config.windowMode != WM_UNSUPPORTED) {
            if ((inputHit & INPUT_CFG_WINDOW_MODE) > 0) {
                config.windowMode++;
                if (config.windowMode > WM_FULLSCREEN) {
                    config.windowMode = WM_1X;
                }
            }
        }
        if ((inputHit & INPUT_CFG_AUDIO_TOGGLE) > 0) {
            config.audioEnabled = !config.audioEnabled;
        }

        //Handle dialogs
        if (dialogOpen) {
            dialogs.handleKeys(inputHeld, inputHit);
            dialogs.update(dt);

            int param = dialogCtx.actionParam;
            int levelNum = (param & 0xF);
            int difficulty = (param >> 4);

            switch (dialogCtx.action) {
                case DLGACT_QUIT:
                    if (playing) {
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
                    playLevel(levelNum, difficulty, false);
                    break;

                case DLGACT_TRYAGAIN_WIPE:
                    dialogs.closeAll();
                    wipeToBlack();
                    tryAgain = true;
                    screenDelay = 1.0f;
                    break;

                case DLGACT_TRYAGAIN_IMMEDIATE:
                    tryAgain = true;
                    screenDelay = 0;
                    break;
            }

            dialogCtx.action = NONE;

            //Dialog just closed
            if (dialogCtx.stackSize == 0) {
                waitInputUp = true;
                inputHeld = 0;
                inputHit = 0;
            }
        }

        handleConfigChange();

        if (playing && !dialogOpen && !tryAgain) {
            play.setInput(inputHeld);
            play.update(dt);

            //Handle pause
            boolean pause = (inputHit & INPUT_PAUSE) > 0;
            boolean pauseTouch = (inputHit & INPUT_PAUSE_TOUCH) > 0;
            if (playCtx.canPause && (pause || pauseTouch)) {
                dialogCtx.useCursor = !pauseTouch;
                dialogs.open(DLG_PAUSE);
                waitInputUp = true;
            }

            //Check start of screen wipe effect
            if (playCtx.wipeToBlack) {
                wipeToBlack();
                playCtx.wipeToBlack = false;
            }
            if (playCtx.wipeFromBlack) {
                wipeFromBlack();
                playCtx.wipeFromBlack = false;
            }

            //Check for advances in game progress
            if (!progressChecked && playCtx.goalReached && !config.progressCheat) {
                if (playCtx.levelNum   == config.progressLevel &&
                    playCtx.difficulty == config.progressDifficulty) {

                    int numLevels = difficultyNumLevels[playCtx.difficulty];

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

                progressChecked = true;
            }

            //Handle end of level
            if (playCtx.sequenceStep == SEQ_FINISHED) {
                if (playCtx.levelNum == LVLNUM_ENDING) {
                    showFinalScore();
                } else if (playCtx.timeUp) {
                    screenType = SCR_BLANK;
                    removeWipe();
                    audio.stopBgm();
                    dialogs.open(DLG_TRYAGAIN_TIMEUP);
                } else if (playCtx.goalReached) {
                    if (playCtx.lastLevel) {
                        if (playCtx.difficulty == DIFFICULTY_MAX) {
                            showFinalScore();
                        } else {
                            startEndingSequence();
                        }
                    } else {
                        playLevel(playCtx.levelNum + 1, playCtx.difficulty, false);
                    }
                }
            }
        }

        //Handle final score screen
        if (screenType == SCR_FINALSCORE) {
            screenDelay -= dt;

            if (screenDelay <= 0) {
                if (playCtx.difficulty == DIFFICULTY_MAX) {
                    showTitle();
                } else {
                    playLevel(1, playCtx.difficulty + 1, false);
                }
            }
        }

        //Act if the player has selected "Try again"
        if (tryAgain) {
            screenDelay -= dt;

            if (screenDelay <= 0) {
                tryAgain = false;
                playCtx.score = 0;
                dialogs.closeAll();
                playLevel(playCtx.levelNum, playCtx.difficulty, true);
            }
        }

        //Update screen wipe effects
        wipeDelay -= dt;
        if (wipeDelay <= 0) {
            wipeDelay = WIPE_DELAY;
            wipeValue += wipeDelta;

            if (wipeValue <= 0) {
                wipeValue = 0;
                wipeDelta = 0;
            } else if (wipeValue >= WIPE_MAX_VALUE) {
                wipeValue = WIPE_MAX_VALUE;
                wipeDelta = 0;
            }
        }

        //Draw graphics
        renderer.draw(screenType, config.touchEnabled, inputHeld, wipeValue);

        oldInputHeld = inputHeld;
    }

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
        float ratio = (float)newWidth / (float)newHeight;
        if (ratio > DEFAULT_ASPECT_RATIO) {
            viewportWidth = (int)(newHeight * DEFAULT_ASPECT_RATIO);
            viewportX = (newWidth - viewportWidth) / 2;
            projectionHeight = SCREEN_MIN_HEIGHT;
        } else {
            viewportX = 0;
            viewportWidth = (int)newWidth;
            projectionHeight = (int)((float)SCREEN_WIDTH / ratio);
        }

        viewportHeight = newHeight;

        renderer.setViewport(viewportX, 0, viewportWidth, viewportHeight);
        renderer.setProjection(SCREEN_WIDTH, projectionHeight);
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
            //Just in case an uncaught exception happens, stop the audio thread
            audio.dispose();
        } finally {
            defHandler.uncaughtException(t, e);
        }
    }


    //--------------------------------------------------------------------------

    void handleTouch() {
        boolean touching = false;
        int i;

        if (!config.touchEnabled) return;

        for (i = 0; i < 10; i++) {
            float x, y;

            if (!Gdx.input.isTouched(i)) continue;

            touching = true;

            //Convert coordinates from physical screen to projection
            x = Gdx.input.getX(i);
            x -= viewportX;
            x /= viewportWidth;
            x *= SCREEN_WIDTH;

            y = Gdx.input.getY(i);
            y /= viewportHeight;
            y *= projectionHeight;

            input.onTouch(x, y, projectionHeight);
            if (!wasTouching) {
                dialogs.onTap((int)x, (int)y, projectionHeight);
            }
        }

        wasTouching = touching;
    }

    void showTitle() {
        screenType = SCR_LOGO;

        playCtx.score = 0;
        playCtx.levelNum = -1;

        audio.stopAllSfx();
        audio.playBgm(BGMTITLE);

        dialogs.closeAll();
        dialogs.open(DLG_MAIN);

        removeWipe();
    }

    void showFinalScore() {
        screenType = SCR_FINALSCORE;
        screenDelay = 4.0f;
        removeWipe();
    }

    void playLevel(int levelNum, int difficulty, boolean skipInitialSequence) {
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

            removeWipe();
            screenType = SCR_BLANK;
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

        switch (levelNum) {
            case 1:
                playCtx.bus.routeSign = NONE;
                playCtx.bus.numCharacters = 0;
                break;

            case 2:
                playCtx.bus.routeSign = 0;
                playCtx.bus.numCharacters = 0;
                break;

            case 3:
                playCtx.bus.routeSign = 1;
                playCtx.bus.numCharacters = 1;
                break;

            case 4:
                playCtx.bus.routeSign = 2;
                playCtx.bus.numCharacters = 2;
                break;

            case 5:
                playCtx.bus.routeSign = 3;
                playCtx.bus.numCharacters = 3;
                break;
        }

        if (playCtx.lastLevel) {
            playCtx.bus.numCharacters = 3;
        }

        audio.playBgm(playCtx.bgm);
        wipeFromBlack();
    }

    void startEndingSequence() {
        screenType = SCR_PLAY;

        playCtx.levelNum = LVLNUM_ENDING;
        playCtx.lastLevel = false;

        playCtx.levelSize = 8 * SCREEN_WIDTH;
        playCtx.bgColor = SPR_BG_SKY3;
        playCtx.bgm = BGM3;

        playCtx.sequenceStep = SEQ_ENDING;
        playCtx.sequenceDelay = 0;

        play.clear();

        audio.playBgm(playCtx.bgm);
        wipeFromBlack();
    }

    void wipeToBlack() {
        wipeDelay = WIPE_DELAY;
        wipeValue = 0;
        wipeDelta = WIPE_DELTA;
    }

    void wipeFromBlack() {
        wipeDelay = WIPE_DELAY;
        wipeValue = WIPE_MAX_VALUE;
        wipeDelta = -WIPE_DELTA;
    }

    void removeWipe() {
        wipeValue = 0;
        wipeDelta = 0;
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
                int width  = SCREEN_WIDTH;
                int height = SCREEN_MIN_HEIGHT;

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
}

