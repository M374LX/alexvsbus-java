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

class Dialogs {
    DialogCtx ctx;
    static DisplayParams displayParams;
    Config config;
    Audio audio;
    int difficulty;
    boolean waitInputUp;
    int cursorDirection;
    int prevCursorDirection;
    float cursorDelay;
    int levelNum;
    float levelStartDelay;
    int cheatPos;

    //If the selected item is between storeSelMin and storeSelMax (inclusive),
    //then it is stored in storedSel; if an item's target is -2 (-1 would
    //conflict with the constant NONE), then the item to be selected is
    //retrieved from storedSel;
    int storedSel;
    int storeSelMin;
    int storeSelMax;

    Dialogs(DisplayParams displayParams, Config config, Audio audio) {
        this.displayParams = displayParams;
        this.config = config;
        this.audio = audio;

        difficulty = DIFFICULTY_NORMAL;
        waitInputUp = false;
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
        ctx.useCursor = !config.touchEnabled;
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
            x += (displayParams.vscreenWidth / TILE_SIZE) - item.width;
        } else if (item.align == ALIGN_CENTER) {
            x += ((displayParams.vscreenWidth / TILE_SIZE) - item.width) / 2;
        }

        return x * TILE_SIZE;
    }

    //Returns the absolute Y position of an item on the screen in pixels
    static int itemY(DialogItem item) {
        int y = item.offsetY;

        if (item.align == ALIGN_CENTER) {
            y += ((displayParams.vscreenHeight / TILE_SIZE) - item.height) / 2;
        }

        return y * TILE_SIZE;
    }

    //Called when the touchscreen is tapped
    void onTap(int x, int y) {
        //Nothing to do if no dialog is open
        if (ctx.stackSize <= 0) return;

        int dialogType = ctx.stack[ctx.stackSize - 1].type;
        boolean changeItem = true;

        for (int i = 0; i < ctx.numItems; i++) {
            int ix = itemX(ctx.items[i]);
            int iy = itemY(ctx.items[i]);
            int w = ctx.items[i].width;
            int h = ctx.items[i].height;

            if (x < ix) continue;
            if (x > ix + (w * TILE_SIZE)) continue;
            if (y < iy) continue;
            if (y > iy + (h * TILE_SIZE)) continue;

            if (ctx.items[i].disabled) continue;
            if (ctx.items[i].hidden) continue;

            //Do not change the selection to the audio toggle item when
            //touching it
            if ((dialogType == DLG_MAIN  && i == 5) ||
                (dialogType == DLG_PAUSE && i == 4)) {

                changeItem = false;
            } else {
                //Tapping normally disables the cursor, except when selecting a
                //BGM track (not the "return" item) on the jukebox
                ctx.useCursor = false;
                if (dialogType == DLG_JUKEBOX && i < 4) {
                    ctx.useCursor = true;
                }
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
            if (ctx.useCursor) {
                int item = ctx.stack[ctx.stackSize - 1].selectedItem;
                confirm(item, true);
            }
        }
        if ((inputHit & INPUT_DIALOG_RETURN) > 0) {
            int type = ctx.stack[ctx.stackSize - 1].type;

            if (type == DLG_MAIN) {
                open(DLG_QUIT);
            } else if (type == DLG_TRYAGAIN_TIMEUP) {
                //Do nothing
            } else {
                close();
            }
        }
    }

    void update(float dt) {
        boolean selectionChanged;

        //Nothing to do if no dialog is open
        if (ctx.stackSize <= 0) return;

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

        updateAudioIcon();

        //Update the values of the settings dialog
        if (ctx.stack[ctx.stackSize - 1].type == DLG_SETTINGS) {
            String windowMode = "";

            switch (config.windowMode) {
                case WM_1X: windowMode = "1X"; break;
                case WM_2X: windowMode = "2X"; break;
                case WM_3X: windowMode = "3X"; break;
                case WM_FULLSCREEN: windowMode = "FULLSCREEN"; break;
            }

            ctx.items[0].value = windowMode;
            ctx.items[1].value = (config.scanlinesEnabled) ? "ON" : "OFF";
            ctx.items[2].value = (config.audioEnabled) ? "ON" : "OFF";
            ctx.items[3].value = (config.touchButtonsEnabled) ? "ON" : "OFF";
        }

        //Handle selected item change
        selectionChanged = false;
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

        prevCursorDirection = cursorDirection;

        if (selectionChanged) {
            int sel = ctx.stack[ctx.stackSize - 1].selectedItem;
            int prevSel = sel;

            if (ctx.useCursor) {
                do {
                    sel = ctx.items[sel].targets[cursorDirection];

                    if (sel == -2) {
                        sel = storedSel;
                    }
                } while (ctx.items[sel].disabled || ctx.items[sel].hidden);

                if (sel != prevSel) {
                    audio.playSfx(SFX_DIALOG_SELECT);
                }
            } else {
                sel = 0;
                ctx.useCursor = true;
                audio.playSfx(SFX_DIALOG_SELECT);
            }

            if (sel >= storeSelMin && sel <= storeSelMax) {
                storedSel = sel;
            }

            ctx.stack[ctx.stackSize - 1].selectedItem = sel;
        }
    }

    //Decides whether to show the "enable audio" or "disable audio" icon
    void updateAudioIcon() {
        int spr = config.audioEnabled ? SPR_DIALOG_AUDIO_ON : SPR_DIALOG_AUDIO_OFF;
        int type = ctx.stack[ctx.stackSize - 1].type;

        if (type == DLG_MAIN) {
            ctx.items[5].iconSprite = spr;
        } else if (type == DLG_PAUSE) {
            ctx.items[4].iconSprite = spr;
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
                        open(DLG_SETTINGS);
                        break;

                    case 3:
                        open(DLG_ABOUT);
                        break;

                    case 4:
                        open(DLG_QUIT);
                        break;

                    case 5:
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

            case DLG_SETTINGS:
                switch (item) {
                    case 0:
                        if (config.windowMode != WM_UNSUPPORTED) {
                            open(DLG_WINDOW_MODE);
                        }
                        break;

                    case 1:
                        config.scanlinesEnabled = !config.scanlinesEnabled;
                        break;

                    case 2:
                        config.audioEnabled = !config.audioEnabled;
                        break;

                    case 3:
                        if (config.touchEnabled) {
                            config.touchButtonsEnabled = !config.touchButtonsEnabled;
                        }
                        break;

                    case 4:
                        close();
                        break;
                }
                break;

            case DLG_WINDOW_MODE:
                switch (item) {
                    case 0:
                        config.windowMode = WM_1X;
                        break;

                    case 1:
                        config.windowMode = WM_2X;
                        break;

                    case 2:
                        config.windowMode = WM_3X;
                        break;

                    case 3:
                        config.windowMode = WM_FULLSCREEN;
                        break;

                    case 4:
                        break;
                }
                close();
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
                        open(DLG_TRYAGAIN_PAUSE);
                        break;

                    case 2:
                        open(DLG_SETTINGS);
                        break;

                    case 3:
                        open(DLG_QUIT);
                        break;

                    case 4:
                        config.audioEnabled = !config.audioEnabled;
                        break;
                }
                break;

            case DLG_TRYAGAIN_PAUSE:
                switch (item){
                    case 0:
                        ctx.action = DLGACT_TRYAGAIN_WIPE;
                        break;

                    case 1:
                        close();
                        break;

                    case 2:
                        close();
                        break;
                }
                break;

            case DLG_TRYAGAIN_TIMEUP:
                switch (item){
                    case 0:
                        ctx.action = DLGACT_TRYAGAIN_IMMEDIATE;
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
        int sel = 0;

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

            case DLG_ERROR:
                audio.stopBgm();
                audio.playSfx(SFX_ERROR);
                break;
        }

        if (ctx.useCursor) {
            if (dialogType == DLG_DIFFICULTY) {
                //Default selection to highest unlocked difficulty
                sel = config.progressDifficulty;
            } else if (dialogType == DLG_LEVEL) {
                //Default selection to highest unlocked level
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
            } else if (dialogType == DLG_WINDOW_MODE) {
                //Default selection to current window mode
                switch (config.windowMode) {
                    case WM_1X: sel = 0; break;
                    case WM_2X: sel = 1; break;
                    case WM_3X: sel = 2; break;
                    case WM_FULLSCREEN: sel = 3; break;
                }
            }

            //Do not select a hidden item
            while (ctx.items[sel].hidden) sel++;
        }

        storedSel = sel;
        if (sel < storeSelMin || sel > storeSelMax) {
            storedSel = storeSelMin;
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
            int sel;

            loadItemsAndText(ctx.stack[ctx.stackSize - 1].type);

            sel = ctx.stack[ctx.stackSize - 1].selectedItem;

            storedSel = sel;
            if (sel < storeSelMin || sel > storeSelMax) {
                storedSel = storeSelMin;
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

        //Move the text downwards by two lines because the word "ERROR" is drawn
        //separately by Render.java on the first line
        ctx.text = "\n\n" + msg;

        ctx.textOffsetX = 0;
        ctx.textOffsetY = 0;
        ctx.textWidth = 40;
        ctx.textHeight = 4;
    }

    //Loads the items and text corresponding to a specific dialog
    void loadItemsAndText(int dialogType) {
        int i;

        //Load text
        switch (dialogType) {
            case DLG_ABOUT:
                ctx.text =
                    (char)0x1B + "Alex vs Bus: The Race\n" +
                    (char)0x7F + " 2021-2022 M374LX\n" + // 0x7F = copyright symbol
                    "\n" +
                    (char)0x1B + "Version\n" +
                    " " + VERSION + "\n" +
                    "\n" +
                    (char)0x1B + "Repository\n" +
                    " " + REPOSITORY + "\n" +
                    "\n" +
                    (char)0x1B + "Licenses\n" +
                    " The code is under GNU GPLv3, while the\n" +
                    " assets are under CC BY-SA 4.0.\n" +
                    "\n" +
                    " https://www.gnu.org/licenses/gpl-3.0.en.html\n" +
                    " https://creativecommons.org/licenses/by-sa/4.0";
                ctx.textOffsetX = 0;
                ctx.textOffsetY = 0;
                ctx.textWidth = 47;
                ctx.textHeight = 15;
                break;

            case DLG_CREDITS:
                ctx.text =
                    (char)0x1B + "M374LX" + (char)0x1B +
                            " (http://m374lx.users.sourceforge.net)\n" +
                    " Game design, programming, music, SFX, graphics\n" +
                    "\n" +
                    (char)0x1B + "Hoton Bastos\n" +
                    " Additional game design\n" +
                    "\n" +
                    (char)0x1B + "Harim Pires\n" +
                    " Testing\n" +
                    "\n" +
                    (char)0x1B + "Codeman38" + (char)0x1B +
                            " (https://www.zone38.net)\n" +
                    " \"Press Start 2P\" font\n" +
                    "\n" +
                    (char)0x1B + "YoWorks" + (char)0x1B +
                            " (https://www.yoworks.com)\n" +
                    " \"Telegrama\" font";
                ctx.textOffsetX = 0;
                ctx.textOffsetY = 0;
                ctx.textWidth = 47;
                ctx.textHeight = 15;
                break;

            case DLG_TRYAGAIN_PAUSE:
            case DLG_TRYAGAIN_TIMEUP:
                ctx.text = "TRY AGAIN?";
                ctx.textOffsetX = 0;
                ctx.textOffsetY = 0;
                ctx.textWidth = 10;
                ctx.textHeight = 1;
                break;

            case DLG_QUIT:
                ctx.text = "QUIT?";
                ctx.textOffsetX = 0;
                ctx.textOffsetY = 0;
                ctx.textWidth = 10;
                ctx.textHeight = 1;
                break;
        }

        //Clear items
        ctx.numItems = 0;
        for (i = 0; i < DIALOG_MAX_ITEMS; i++) {
            DialogItem it = ctx.items[i];
            it.offsetX = 0;
            it.offsetY = 0;
            it.caption = "";
            it.value = "";
            it.iconSprite = NONE;
            it.disabled = false;
            it.hidden = false;
        }

        //Load items
        switch (dialogType) {
            case DLG_MAIN:
                setItem(0, 14,  6,  5, -2,  5,  1, SPR_DIALOG_PLAY);
                setItem(1,  6,  6,  0,  5,  0,  2, SPR_DIALOG_JUKEBOX);
                setItem(2,  6,  6,  0,  5,  1,  3, SPR_DIALOG_SETTINGS);
                setItem(3,  6,  6,  0,  5,  2,  4, SPR_DIALOG_ABOUT);
                setItem(4,  6,  6,  0,  5,  3,  5, SPR_DIALOG_QUIT);
                setItem(5,  5,  5, -2,  0,  4,  0, SPR_DIALOG_AUDIO_ON);
                ctx.numItems = 6;
                setItemPosition(0, ALIGN_CENTER, 0, 0); //Play
                positionItemsCenter(1, 4, false, 8, 8);
                setItemPosition(5, ALIGN_TOPRIGHT, -1, 1); //Audio toggle
                break;

            case DLG_DIFFICULTY:
                setItem(0, 10,  5,  3,  3,  3,  1, SPR_DIALOG_NORMAL);
                setItem(1, 10,  5,  3,  3,  0,  2, SPR_DIALOG_HARD);
                setItem(2, 10,  5,  3,  3,  1,  3, SPR_DIALOG_SUPER);
                setItem(3,  5,  5, -2, -2,  2,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 4;
                positionItemsCenter(0, 2, false, 12, 0);
                setItemPosition(3, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_LEVEL:
                setItem(0,  6,  6,  5,  5,  5,  1, SPR_DIALOG_1);
                setItem(1,  6,  6,  5,  5,  0,  2, SPR_DIALOG_2);
                setItem(2,  6,  6,  5,  5,  1,  3, SPR_DIALOG_3);
                setItem(3,  6,  6,  5,  5,  2,  4, SPR_DIALOG_4);
                setItem(4,  6,  6,  5,  5,  3,  5, SPR_DIALOG_5);
                setItem(5,  5,  5, -2, -2,  4,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 6;
                positionItemsCenter(0, 4, false, 8, 0);
                setItemPosition(5, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_JUKEBOX:
                setItem(0,  6,  6,  4,  4,  4,  1, SPR_DIALOG_1);
                setItem(1,  6,  6,  4,  4,  0,  2, SPR_DIALOG_2);
                setItem(2,  6,  6,  4,  4,  1,  3, SPR_DIALOG_3);
                setItem(3,  6,  6,  4,  4,  2,  4, SPR_DIALOG_4);
                setItem(4,  5,  5, -2, -2,  3,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 5;
                positionItemsCenter(0, 3, false, 8, 0);
                setItemPosition(4, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_SETTINGS:
                setItem(0, 32,  3,  4,  1,  4,  4, NONE);
                setItem(1, 32,  3,  0,  2,  4,  4, NONE);
                setItem(2, 32,  3,  1,  3,  4,  4, NONE);
                setItem(3, 32,  3,  2,  4,  4,  4, NONE);
                setItem(4,  5,  5,  3,  0, -2, -2, SPR_DIALOG_RETURN);
                ctx.numItems = 5;
                positionItemsCenter(0, 3, true, 4, 0);
                setItemPosition(4, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "WINDOW MODE";
                ctx.items[1].caption = "SCANLINES";
                ctx.items[2].caption = "AUDIO";
                ctx.items[3].caption = "TOUCHSCREEN BUTTONS";
                break;

            case DLG_WINDOW_MODE:
                setItem(0, 16,  3,  4,  1,  4,  4, NONE);
                setItem(1, 16,  3,  0,  2,  4,  4, NONE);
                setItem(2, 16,  3,  1,  3,  4,  4, NONE);
                setItem(3, 16,  3,  2,  4,  4,  4, NONE);
                setItem(4,  5,  5,  3,  0, -2, -2, SPR_DIALOG_RETURN);
                ctx.numItems = 5;
                positionItemsCenter(0, 3, true, 4, 0);
                setItemPosition(4, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "1X";
                ctx.items[1].caption = "2X";
                ctx.items[2].caption = "3X";
                ctx.items[3].caption = "FULLSCREEN";
                break;

            case DLG_ABOUT:
                setItem(0, 10,  5,  1,  1,  1,  1, NONE);
                setItem(1,  5,  5,  0,  0,  0,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 2;
                setItemPosition(0, ALIGN_CENTER, 0, 13); //Credits
                setItemPosition(1, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "CREDITS";
                break;

            case DLG_CREDITS:
                setItem(0,  5,  5,  0,  0,  0,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 1;
                setItemPosition(0, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_PAUSE:
                setItem(0, 14,  6,  4, -2,  4,  1, SPR_DIALOG_PLAY);
                setItem(1,  6,  6,  0,  4,  0,  2, SPR_DIALOG_TRYAGAIN);
                setItem(2,  6,  6,  0,  4,  1,  3, SPR_DIALOG_SETTINGS);
                setItem(3,  6,  6,  0,  4,  2,  4, SPR_DIALOG_QUIT);
                setItem(4,  5,  5, -2,  0,  3,  0, SPR_DIALOG_AUDIO_ON);
                ctx.numItems = 5;
                setItemPosition(0, ALIGN_CENTER, 0, 0); //Play
                positionItemsCenter(1, 3, false, 8, 8);
                setItemPosition(4, ALIGN_TOPRIGHT, -1, 1); //Audio toggle
                break;

            case DLG_TRYAGAIN_PAUSE:
                setItem(0,  6,  6,  2,  2,  2,  1, SPR_DIALOG_CONFIRM);
                setItem(1,  6,  6,  2,  2,  0,  2, SPR_DIALOG_CANCEL);
                setItem(2,  5,  5, -2, -2,  1,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 3;
                positionItemsCenter(0, 1, false, 8, 8);
                setItemPosition(2, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_TRYAGAIN_TIMEUP:
                setItem(0,  6,  6,  1,  1,  1,  1, SPR_DIALOG_CONFIRM);
                setItem(1,  6,  6,  0,  0,  0,  0, SPR_DIALOG_CANCEL);
                ctx.numItems = 2;
                positionItemsCenter(0, 1, false, 8, 8);
                break;

            case DLG_QUIT:
                setItem(0,  6,  6,  2,  2,  2,  1, SPR_DIALOG_CONFIRM);
                setItem(1,  6,  6,  2,  2,  0,  2, SPR_DIALOG_CANCEL);
                setItem(2,  5,  5, -2, -2,  1,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 3;
                positionItemsCenter(0, 1, false, 8, 8);
                setItemPosition(2, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_ERROR:
                setItem(0,  6,  6,  0,  0,  0,  0, SPR_DIALOG_CONFIRM);
                ctx.numItems = 1;
                setItemPosition(0, ALIGN_CENTER, 0, 8); //Confirm
        }

        if (dialogType == DLG_SETTINGS) {
            //Determine items to be hidden from settings dialog
            ctx.items[0].hidden = (config.windowMode == WM_UNSUPPORTED);
            ctx.items[3].hidden = !config.touchEnabled;
            positionItemsCenter(0, 3, true, 4, 0);
        } else if (dialogType == DLG_DIFFICULTY) {
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
            int numLevels = difficultyNumLevels[difficulty];

            //Hide levels that do not exist in selected difficulty
            for (i = 0; i < 5; i++) {
                if (i > numLevels - 1) {
                    ctx.items[i].hidden = true;
                }
            }
            positionItemsCenter(0, 4, false, 8, 0);

            if (difficulty == config.progressDifficulty && !config.progressCheat) {
                //Disable selection of locked levels
                for (i = config.progressLevel; i <= numLevels - 1; i++) {
                    ctx.items[i].iconSprite = SPR_DIALOG_LOCKED;
                    ctx.items[i].disabled = true;
                }
            }
        }

        //Determine minimum and maximum item to be stored in storedSel
        if (dialogType == DLG_MAIN || dialogType == DLG_PAUSE) {
            storeSelMin = 1;
        } else {
            storeSelMin = 0;
        }
        storeSelMax = ctx.numItems - 2;
    }

    void setItemPosition(int item, int align, int offsetX, int offsetY) {
        DialogItem it = ctx.items[item];

        it.align = align;
        it.offsetX = offsetX;
        it.offsetY = offsetY;
    }

    //Positions a range of items as a line or row (depending on "vertical"
    //parameter) at the center of the screen, with an optional Y offset
    //(offsetY) if the items are positioned horizontally
    void positionItemsCenter(int firstItem, int lastItem, boolean vertical,
            int posDiff, int offsetY) {

        int numVisibleItems = 0;
        int pos;
        int i;

        for (i = firstItem; i <= lastItem; i++) {
            if (!ctx.items[i].hidden) {
                numVisibleItems++;
            }
        }

        pos = -((numVisibleItems - 1) * posDiff);
        pos /= 2;

        for (i = firstItem; i <= lastItem; i++) {
            DialogItem it = ctx.items[i];

            if (it.hidden) continue;

            it.align = ALIGN_CENTER;
            if (vertical) {
                it.offsetY = pos;
            } else {
                it.offsetX = pos;
                it.offsetY = offsetY;
            }
            pos += posDiff;
        }
    }

    //Sets most of the attributes of a dialog item
    void setItem(int item, int width, int height,
            int targetUp, int targetDown, int targetLeft, int targetRight,
            int iconSprite) {

        DialogItem it = ctx.items[item];

        it.width = width;
        it.height = height;
        it.targets[DLGDIR_UP] = targetUp;
        it.targets[DLGDIR_DOWN] = targetDown;
        it.targets[DLGDIR_LEFT] = targetLeft;
        it.targets[DLGDIR_RIGHT] = targetRight;
        it.iconSprite = iconSprite;
    }
}

