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

class Dialogs {
    DialogCtx ctx;
    Audio audio;
    Config config;
    int difficulty;
    boolean waitInputUp;
    boolean useCursor; //Whether or not to highlight a dialog item in green
    int cursorDirection;
    int prevCursorDirection;
    float cursorDelay;
    int levelNum;
    float levelStartDelay;
    int cheatPos;

    Dialogs(Audio audio, Config config) {
        this.audio = audio;
        this.config = config;
        difficulty = DIFFICULTY_NORMAL;
        waitInputUp = false;
        useCursor = !config.touchEnabled;
        cursorDirection = NONE;
        prevCursorDirection = NONE;
        cheatPos = 0;
    }

    DialogCtx newCtx() {
        int i;

        ctx = new DialogCtx();
        ctx.stack = new DialogStackEntry[DIALOG_MAX_STACK_SIZE];
        ctx.items = new DialogItem[DIALOG_MAX_ITEMS];

        for (i = 0; i < DIALOG_MAX_STACK_SIZE; i++) {
            ctx.stack[i] = new DialogStackEntry();
        }
        for (i = 0; i < DIALOG_MAX_ITEMS; i++) {
            ctx.items[i] = new DialogItem();
            ctx.items[i].targets = new int[4];
        }

        ctx.action = NONE;
        ctx.levelSelected = false;
        ctx.selectedVisible = true;
        ctx.showFrame = false;
        ctx.text = "";

        return ctx;
    }

    //Returns the absolute X position of an item on the screen in pixels
    static int itemX(DialogItem item) {
        int x = item.offsetX;

        if (item.align == ALIGN_TOPRIGHT) {
            x += (SCREEN_WIDTH / TILE_SIZE) - item.width;
        } else if (item.align == ALIGN_CENTER) {
            x += ((SCREEN_WIDTH / TILE_SIZE) - item.width) / 2;
        }

        return x * TILE_SIZE;
    }

    //Returns the absolute Y position of an item on the screen in pixels
    static int itemY(DialogItem item, int projectionHeight) {
        int y = item.offsetY;

        if (item.align == ALIGN_CENTER) {
            y += ((projectionHeight / TILE_SIZE) - item.height) / 2;
        }

        return y * TILE_SIZE;
    }

    //Called when the touchscreen is tapped
    void onTap(int x, int y, int projectionHeight) {
        //Nothing to do if no dialog is open
        if (ctx.stackSize <= 0) return;

        int dialogType = ctx.stack[ctx.stackSize - 1].type;
        boolean changeItem = true;

        for (int i = 0; i < ctx.numItems; i++) {
            int ix = itemX(ctx.items[i]);
            int iy = itemY(ctx.items[i], projectionHeight);
            int w = ctx.items[i].width;
            int h = ctx.items[i].height;

            if (x < ix) continue;
            if (x > ix + (w * TILE_SIZE)) continue;
            if (y < iy) continue;
            if (y > iy + (h * TILE_SIZE)) continue;

            if (ctx.items[i].disabled) continue;

            //Do not change the selection to the audio toggle item when
            //touching it
            if ((dialogType == DLG_MAIN  && i == 3) ||
                (dialogType == DLG_PAUSE && i == 2)) {

                changeItem = false;
            }

            //Tapping normally disables the cursor, except when selecting a
            //BGM track (not the "return" item) on the jukebox
            useCursor = false;
            if (dialogType == DLG_JUKEBOX && i < 4) {
                useCursor = true;
            }

            confirm(i, changeItem);

            return;
        }
    }

    //Handles player's input from keyboard or game controller, but not screen
    //touches
    void handleKeys(int inputHeld, int inputHit) {
        //Nothing to do if no dialog is open
        if (ctx.stackSize <= 0) return;

        if (waitInputUp) {
            if (inputHeld == 0) waitInputUp = false;

            cursorDirection = NONE;
            prevCursorDirection = NONE;

            return;
        }

        if ((inputHeld & INPUT_UP) > 0) {
            cursorDirection = DLGDIR_UP;
        } else if ((inputHeld & INPUT_DOWN) > 0) {
            cursorDirection = DLGDIR_DOWN;
        } else if ((inputHeld & INPUT_LEFT) > 0) {
            cursorDirection = DLGDIR_LEFT;
        } else if ((inputHeld & INPUT_RIGHT) > 0) {
            cursorDirection = DLGDIR_RIGHT;
        } else {
            cursorDirection = NONE;
        }

        if ((inputHit & INPUT_DIALOG_CONFIRM) > 0) {
            int item = ctx.stack[ctx.stackSize - 1].selectedItem;
            confirm(item, false);
        }
        if ((inputHit & INPUT_DIALOG_RETURN) > 0) {
            int type = ctx.stack[ctx.stackSize - 1].type;

            if (type == DLG_MAIN) {
                open(DLG_QUIT);
            } else if (type == DLG_TRYAGAIN) {
                //Do nothing
            } else {
                close();
            }
        }
    }

    void update(float dt) {
        //Nothing to do if no dialog is open
        if (ctx.stackSize <= 0) return;

        boolean selectionChanged = false;

        if (ctx.levelSelected) {
            levelStartDelay -= dt;

            //Flash selected item
            ctx.selectedVisible = !ctx.selectedVisible;

            if (levelStartDelay <= 0) {
                ctx.action = DLGACT_PLAY;
                ctx.actionParam = levelNum | (difficulty << 4);
                ctx.levelSelected = false;
                ctx.selectedVisible = true;
            }

            return;
        }

        //Handle selected item change
        if (cursorDirection != NONE) {
            cursorDelay -= dt;

            if (prevCursorDirection != cursorDirection) {
                cursorDelay = 0.5f;
                selectionChanged = true;
            } else if (cursorDelay <= 0) {
                cursorDelay = 0.1f;
                selectionChanged = true;
            }
        }

        if (selectionChanged) {
            int sel = ctx.stack[ctx.stackSize - 1].selectedItem;
            int prevSel = sel;

            if (sel != NONE) {
                do {
                    sel = ctx.items[sel].targets[cursorDirection];
                } while (ctx.items[sel].disabled);
            } else {
                sel = 0;
                useCursor = true;
            }

            ctx.stack[ctx.stackSize - 1].selectedItem = sel;

            if (sel != prevSel) {
                audio.playSfx(SFX_DIALOG_SELECT);
            }
        }

        prevCursorDirection = cursorDirection;

        updateAudioIcon();
    }

    //Decides whether to show the "enable audio" or "disable audio" icon
    void updateAudioIcon() {
        int spr = config.audioEnabled ? SPR_DIALOG_AUDIO_ON : SPR_DIALOG_AUDIO_OFF;
        int type = ctx.stack[ctx.stackSize - 1].type;

        if (type == DLG_MAIN) {
            ctx.items[4].iconSprite = spr;
        } else if (type == DLG_PAUSE) {
            ctx.items[2].iconSprite = spr;
        }
    }

    //Confirms the selection of an item
    void confirm(int item, boolean changeItem) {
        waitInputUp = true;

        if (changeItem) {
            ctx.stack[ctx.stackSize - 1].selectedItem = item;
        }

        switch (ctx.stack[ctx.stackSize - 1].type) {
            case DLG_MAIN:
                switch (item) {
                    case 0:
                        open(DLG_DIFFICULTY);
                        break;

                    case 1:
                        open(DLG_JUKEBOX);
                        break;

                    case 2:
                        open(DLG_ABOUT);
                        break;

                    case 3:
                        open(DLG_QUIT);
                        break;

                    case 4:
                        config.audioEnabled = !config.audioEnabled;
                        break;
                }
                break;

            case DLG_DIFFICULTY:
                switch (item) {
                    case 0:
                        difficulty = DIFFICULTY_NORMAL;
                        open(DLG_LEVEL);
                        break;

                    case 1:
                        difficulty = DIFFICULTY_HARD;
                        open(DLG_LEVEL);
                        break;

                    case 2:
                        difficulty = DIFFICULTY_SUPER;
                        open(DLG_LEVEL);
                        break;

                    case 3:
                        close();
                        break;
                }
                break;

            case DLG_LEVEL:
                if (item == ctx.numItems - 1) {
                    close();
                } else {
                    ctx.levelSelected = true;
                    levelNum = item + 1;
                    levelStartDelay = 0.75f;
                }
                break;

            case DLG_JUKEBOX:
                ctx.stack[ctx.stackSize - 1].selectedItem = item;
                switch (item) {
                    case 0:
                        audio.playBgm(BGM1);
                        break;

                    case 1:
                        audio.playBgm(BGM2);
                        break;

                    case 2:
                        audio.playBgm(BGM3);
                        break;

                    case 3:
                        audio.playBgm(BGMTITLE);
                        break;

                    case 4:
                        close();
                        break;
                }

                if (!config.progressCheat) {
                    if (item == cheatSequence[cheatPos]) {
                        cheatPos++;

                        if (cheatSequence[cheatPos] == -1) {
                            config.progressCheat = true;
                            audio.playSfx(SFX_COIN);
                        }
                    } else {
                        cheatPos = 0;
                    }
                }

                break;

            case DLG_ABOUT:
                switch (item) {
                    case 0:
                        open(DLG_CREDITS);
                        break;

                    case 1:
                        close();
                        break;
                }
                break;

            case DLG_CREDITS:
                close();
                break;

            case DLG_PAUSE:
                switch (item) {
                    case 0:
                        closeAll();
                        break;

                    case 1:
                        open(DLG_QUIT);
                        break;

                    case 2:
                        config.audioEnabled = !config.audioEnabled;
                        break;
                }
                break;

            case DLG_TRYAGAIN:
                switch (item){
                    case 0:
                        ctx.action = DLGACT_TRYAGAIN;
                        break;

                    case 1:
                        ctx.action = DLGACT_TITLE;
                        break;
                }
                break;

            case DLG_QUIT:
                switch (item){
                    case 0:
                        ctx.action = DLGACT_QUIT;
                        break;

                    case 1:
                        close();
                        break;

                    case 2:
                        close();
                        break;
                }
                break;

            case DLG_ERROR:
                ctx.action = DLGACT_QUIT;
                break;
        }
    }

    //Opens a specific dialog
    void open(int dialogType) {
        int sel = useCursor ? 0 : NONE;

        ctx.stack[ctx.stackSize].type = dialogType;
        ctx.stackSize++;

        loadItemsAndText(dialogType);

        switch (dialogType) {
            case DLG_JUKEBOX:
                audio.stopBgm();
                break;

            case DLG_PAUSE:
                ctx.showFrame = true;
                break;

            case DLG_TRYAGAIN:
                audio.stopBgm();
                break;

            case DLG_ERROR:
                audio.stopBgm();
                audio.playSfx(SFX_ERROR);
                break;
        }

        //By default, select the highest unlocked difficulty or the last level
        //within the selected difficulty
        if (useCursor) {
            if (dialogType == DLG_DIFFICULTY) {
                sel = config.progressDifficulty;
            } else if (dialogType == DLG_LEVEL) {
                sel = config.progressLevel - 1;

                if (difficulty < config.progressDifficulty) {
                    sel = ctx.numItems - 2;
                }
                if (config.progressCheat) {
                    sel = ctx.numItems - 2;
                }
                if (config.progressLevel > ctx.numItems - 2) {
                    sel = ctx.numItems - 2;
                }
            }
        }

        updateAudioIcon();
        waitInputUp = true;

        ctx.stack[ctx.stackSize - 1].selectedItem = sel;
    }

    //Closes the current dialog and returns to the previous one
    void close() {
        int dialogType = ctx.stack[ctx.stackSize - 1].type;

        waitInputUp = true;

        if (dialogType == DLG_JUKEBOX) {
            audio.playBgm(BGMTITLE);
        } else if (dialogType == DLG_PAUSE) {
            ctx.showFrame = false;
        } else if (dialogType == DLG_ERROR) {
            ctx.action = DLGACT_QUIT;
        }

        ctx.text = "";

        ctx.stackSize--;
        if (ctx.stackSize > 0) {
            loadItemsAndText(ctx.stack[ctx.stackSize - 1].type);

            if (!useCursor) {
                ctx.stack[ctx.stackSize - 1].selectedItem = NONE;
            }
        }

        if (config.progressCheat) {
            cheatPos = 0;
        }
    }

    //Closes all dialogs
    void closeAll() {
        ctx.stackSize = 0;
        ctx.numItems = 0;
        ctx.showFrame = false;
        ctx.text = "";
    }

    //Shows an error message
    void showError(String msg) {
        closeAll();
        open(DLG_ERROR);

        ctx.text = "ERROR\n\n" + msg;
        ctx.textOffsetX = -40;
        ctx.textOffsetY = -6;
        ctx.textWidth = 40;
        ctx.textHeight = 4;
    }

    //Loads the items and text corresponding to a specific dialog
    void loadItemsAndText(int dialogType) {
        final int TL = ALIGN_TOPLEFT;
        final int TR = ALIGN_TOPRIGHT;
        final int CT = ALIGN_CENTER;

        //Load text
        if (dialogType == DLG_ABOUT) {
            ctx.text =
                "Alex vs Bus: The Race\n" +
                "\177 2021 M-374 LX\n" + // \177 = copyright symbol
                "\n" +
                "Version\n" +
                VERSION + "\n" +
                "\n" +
                "Repository\n" +
                REPOSITORY + "\n" +
                "\n" +
                "Licenses\n" +
                "The code is under GNU GPLv3, while the\n" +
                "assets are under CC BY-SA 4.0.";

            ctx.textOffsetX = -47;
            ctx.textOffsetY = -14;
            ctx.textWidth = 47;
            ctx.textHeight = 14;
        } else if (dialogType == DLG_CREDITS) {
            ctx.text =
                "M-374 LX (http://m374lx.users.sourceforge.net)\n" +
                " Game design, programming, music, SFX, graphics\n" +
                "\n" +
                "Hoton Bastos\n" +
                " Additional game design\n" +
                "\n" +
                "Harim Pires\n" +
                " Testing\n" +
                "\n" +
                "Codeman38 (https://www.zone38.net)\n" +
                " \"Press Start 2P\" font\n" +
                "\n" +
                "YoWorks (https://www.yoworks.com)\n" +
                " \"Telegrama\" font";

            ctx.textOffsetX = -47;
            ctx.textOffsetY = -14;
            ctx.textWidth = 47;
            ctx.textHeight = 14;
        } else if (dialogType == DLG_TRYAGAIN) {
            ctx.text = "TRY AGAIN?";
            ctx.textOffsetX = -9;
            ctx.textOffsetY = -2;
            ctx.textWidth = 10;
            ctx.textHeight = 1;
        } else if (dialogType == DLG_QUIT) {
            ctx.text = "QUIT?";
            ctx.textOffsetX = -9;
            ctx.textOffsetY = -2;
            ctx.textWidth = 10;
            ctx.textHeight = 1;
        }

        //Load items
        switch (dialogType) {
            case DLG_MAIN:
                di(0, CT,  0, 0, 14, 6, 4, 1, 4, 1, SPR_DIALOG_PLAY);
                di(1, CT, -8, 8, 6, 6, 0, 4, 0, 2, SPR_DIALOG_JUKEBOX);
                di(2, CT,  0, 8, 6, 6, 0, 4, 1, 3, SPR_DIALOG_ABOUT);
                di(3, CT,  8, 8, 6, 6, 0, 4, 2, 4, SPR_DIALOG_QUIT);
                di(4, TR, -1, 1, 5, 5, 3, 0, 3, 0, SPR_DIALOG_AUDIO_ON);
                ctx.numItems = 5;
                break;

            case DLG_DIFFICULTY:
                di(0, CT, -12, 0, 10, 5, 3, 3, 3, 1, SPR_DIALOG_NORMAL);
                di(1, CT,   0, 0, 10, 5, 3, 3, 0, 2, SPR_DIALOG_HARD);
                di(2, CT,  12, 0, 10, 5, 3, 3, 1, 3, SPR_DIALOG_SUPER);
                di(3, TL,   1, 1, 5, 5, 0, 0, 2, 0, SPR_DIALOG_RETURN);
                ctx.numItems = 4;
                break;

            case DLG_LEVEL:
                if (difficulty == DIFFICULTY_SUPER) { //3 levels
                    di(0, CT, -8, 0, 6, 6, 3, 3, 3, 1, SPR_DIALOG_1);
                    di(1, CT,  0, 0, 6, 6, 3, 3, 0, 2, SPR_DIALOG_2);
                    di(2, CT,  8, 0, 6, 6, 3, 3, 1, 3, SPR_DIALOG_3);
                    di(3, TL,  1, 1, 5, 5, 0, 0, 2, 0, SPR_DIALOG_RETURN);
                    ctx.numItems = 4;
                } else { //5 levels
                    di(0, CT, -16, 0, 6, 6, 5, 5, 5, 1, SPR_DIALOG_1);
                    di(1, CT, -8, 0, 6, 6, 5, 5, 0, 2, SPR_DIALOG_2);
                    di(2, CT,  0, 0, 6, 6, 5, 5, 1, 3, SPR_DIALOG_3);
                    di(3, CT,  8, 0, 6, 6, 5, 5, 2, 4, SPR_DIALOG_4);
                    di(4, CT,  16, 0, 6, 6, 5, 5, 3, 5, SPR_DIALOG_5);
                    di(5, TL,  1, 1, 5, 5, 0, 0, 4, 0, SPR_DIALOG_RETURN);
                    ctx.numItems = 6;
                }
                break;

            case DLG_JUKEBOX:
                di(0, CT, -12, 0, 6, 6, 4, 4, 4, 1, SPR_DIALOG_1);
                di(1, CT, -4, 0, 6, 6, 4, 4, 0, 2, SPR_DIALOG_2);
                di(2, CT,  4, 0, 6, 6, 4, 4, 1, 3, SPR_DIALOG_3);
                di(3, CT,  12, 0, 6, 6, 4, 4, 2, 4, SPR_DIALOG_4);
                di(4, TL,  1, 1, 5, 5, 0, 0, 3, 0, SPR_DIALOG_RETURN);
                ctx.numItems = 5;
                break;

            case DLG_ABOUT:
                di(0, CT,  0, 13, 10, 5, 1, 1, 1, 1, SPR_DIALOG_CREDITS);
                di(1, TL,  1, 1, 5, 5, 0, 0, 0, 0, SPR_DIALOG_RETURN);
                ctx.numItems = 2;
                break;

            case DLG_CREDITS:
                di(0, TL,  1, 1, 5, 5, 0, 0, 0, 0, SPR_DIALOG_RETURN);
                ctx.numItems = 1;
                break;

            case DLG_PAUSE:
                di(0, CT,  0, 0, 14, 6, 2, 1, 2, 2, SPR_DIALOG_PLAY);
                di(1, CT,  0, 8, 6, 6, 0, 2, 2, 2, SPR_DIALOG_QUIT);
                di(2, TR, -1, 1, 5, 5, 1, 0, 0, 0, SPR_DIALOG_AUDIO_ON);
                ctx.numItems = 3;
                break;

            case DLG_TRYAGAIN:
                di(0, CT, -4, 8, 6, 6, 1, 1, 1, 1, SPR_DIALOG_CONFIRM);
                di(1, CT,  4, 8, 6, 6, 0, 0, 0, 0, SPR_DIALOG_CANCEL);
                ctx.numItems = 2;
                break;

            case DLG_QUIT:
                di(0, CT, -4, 8, 6, 6, 2, 2, 2, 1, SPR_DIALOG_CONFIRM);
                di(1, CT,  4, 8, 6, 6, 2, 2, 0, 2, SPR_DIALOG_CANCEL);
                di(2, TL,  1, 1, 5, 5, 1, 0, 1, 0, SPR_DIALOG_RETURN);
                ctx.numItems = 3;
                break;

            case DLG_ERROR:
                di(0, CT,  0, 8, 6, 6, 0, 0, 0, 0, SPR_DIALOG_CONFIRM);
                ctx.numItems = 1;
        }

        if (dialogType == DLG_DIFFICULTY) {
            if (!config.progressCheat) {
                if (config.progressDifficulty < DIFFICULTY_HARD) {
                    //Disable "hard" if it has not been unlocked
                    ctx.items[1].disabled = true;
                    ctx.items[1].iconSprite = SPR_DIALOG_HARD_DISABLED;
                }
                if (config.progressDifficulty < DIFFICULTY_SUPER) {
                    //Disable "super" if it has not been unlocked
                    ctx.items[2].disabled = true;
                    ctx.items[2].iconSprite = SPR_DIALOG_SUPER_DISABLED;
                }
            }
        } else if (dialogType == DLG_LEVEL) {
            if (difficulty == config.progressDifficulty && !config.progressCheat) {
                int numLevels = difficultyNumLevels[difficulty];

                //Disable selection of locked levels
                for (int i = config.progressLevel; i <= numLevels - 1; i++) {
                    ctx.items[i].iconSprite = SPR_DIALOG_LOCKED;
                    ctx.items[i].disabled = true;
                }
            }
        }
    }

    //Sets the attributes of a dialog item
    void di(int item, int align, int xoffs, int yoffs, int width, int height,
            int targetUp, int targetDown, int targetLeft, int targetRight,
            int iconSprite) {

        DialogItem it = ctx.items[item];
        it.align = align;
        it.offsetX = xoffs;
        it.offsetY = yoffs;
        it.width = width;
        it.height = height;
        it.targets[DLGDIR_UP] = targetUp;
        it.targets[DLGDIR_DOWN] = targetDown;
        it.targets[DLGDIR_LEFT] = targetLeft;
        it.targets[DLGDIR_RIGHT] = targetRight;
        it.iconSprite = iconSprite;
        it.disabled = false;
    }
}

