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

class Dialogs {
    DialogCtx ctx;
    static DisplayParams displayParams;
    Config config;
    Audio audio;
    boolean waitInputUp;
    int cursorDirection;
    int prevCursorDirection;
    float cursorDelay;
    int difficulty; //Selected difficulty
    int levelNum;   //Selected level number
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

        ctx.displayName = "";
        ctx.action = NONE;
        ctx.levelSelected = false;
        ctx.selectedVisible = true;
        ctx.useCursor = !config.touchEnabled;
        ctx.showLogo = false;
        ctx.greenBg = false;
        ctx.showFrame = false;
        ctx.fillScreen = false;
        ctx.text = "";

        return ctx;
    }

    //Returns the Y position in tiles corresponding to the center of the
    //dialog's main area (where most items are placed), which is distinct from
    //the top area (where the display name and often the return item are placed)
    static int centerTileY() {
        int vscreenHeightTiles = displayParams.vscreenHeight / TILE_SIZE;
        int topAreaHeight = (displayParams.vscreenHeight <= 192) ? 3 : 7;
        int mainAreaHeight = vscreenHeightTiles - topAreaHeight;

        return mainAreaHeight / 2 + topAreaHeight;
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
            y += centerTileY() - item.height / 2;
        }

        return y * TILE_SIZE;
    }

    //Called when the touchscreen is tapped
    void onTap(int x, int y) {
        //Nothing to do if no dialog is open or a level has been selected
        if (ctx.stackSize <= 0 || ctx.levelSelected) return;

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
        //Nothing to do if no dialog is open or a level has been selected
        if (ctx.stackSize <= 0 || ctx.levelSelected) return;

        if (waitInputUp) {
            if (inputHeld == 0) waitInputUp = false;

            cursorDirection = NONE;
            prevCursorDirection = NONE;

            return;
        }

        //Move cursor
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

        //Confirm
        if ((inputHit & INPUT_DIALOG_CONFIRM) > 0) {
            if (ctx.useCursor) {
                int item = ctx.stack[ctx.stackSize - 1].selectedItem;
                confirm(item, true);
            }
        }

        //Return
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
        updateValues();

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

    //Updates the values displayed by dialog items
    void updateValues() {
        int dialogType = ctx.stack[ctx.stackSize - 1].type;

        if (dialogType == DLG_SETTINGS) {
            ctx.items[2].value = (config.touchButtonsEnabled) ? "ON" : "OFF";
        } else if (dialogType == DLG_DISPLAY_SETTINGS) {
            ctx.items[0].value = (config.fullscreen) ? "ON" : "OFF";
            ctx.items[3].value = (config.scanlinesEnabled) ? "ON" : "OFF";

            if (config.resizableWindow) {
                ctx.items[1].value = "---";
            } else {
                ctx.items[1].value = "" + config.windowScale;
            }
        } else if (dialogType == DLG_VSCREEN_SIZE) {
            if (config.vscreenAutoSize) {
                ctx.items[0].value = "AUTO";
                ctx.items[1].value = "" + displayParams.vscreenWidth;
                ctx.items[2].value = "" + displayParams.vscreenHeight;
            } else {
                ctx.items[0].value = "MANUAL";
                ctx.items[1].value = "" + config.vscreenWidth;
                ctx.items[2].value = "" + config.vscreenHeight;
            }
        } else if (dialogType == DLG_AUDIO_SETTINGS) {
            ctx.items[0].value = (config.audioEnabled) ? "ON" : "OFF";
            ctx.items[1].value = (config.musicEnabled) ? "ON" : "OFF";
            ctx.items[2].value = (config.sfxEnabled) ? "ON" : "OFF";
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
                if (item < ctx.numItems - 1) {
                    ctx.levelSelected = true;
                    levelNum = item + 1;
                    levelStartDelay = 0.75f;
                } else {
                    //Return
                    close();
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
                        open(DLG_DISPLAY_SETTINGS);
                        break;

                    case 1:
                        open(DLG_AUDIO_SETTINGS);
                        break;

                    case 2:
                        if (config.touchEnabled) {
                            config.touchButtonsEnabled = !config.touchButtonsEnabled;
                        }
                        break;

                    case 3:
                        close();
                        break;
                }
                break;

            case DLG_DISPLAY_SETTINGS:
                switch (item) {
                    case 0:
                        if (!config.fixedWindowMode) {
                            config.fullscreen = !config.fullscreen;
                        }
                        break;

                    case 1:
                        open(DLG_WINDOW_SCALE);
                        break;

                    case 2:
                        open(DLG_VSCREEN_SIZE);
                        break;

                    case 3:
                        config.scanlinesEnabled = !config.scanlinesEnabled;
                        break;

                    case 4:
                        close();
                        break;
                }
                break;

            case DLG_WINDOW_SCALE:
                if (item < 3) {
                    config.windowScale = item + 1;
                }
                close();
                break;

            case DLG_VSCREEN_SIZE:
                switch (item) {
                    case 0:
                        if (config.vscreenAutoSize) {
                            config.vscreenAutoSize = false;
                            config.vscreenWidth  = displayParams.vscreenWidth;
                            config.vscreenHeight = displayParams.vscreenHeight;

                            //Enable vscreen width and height items
                            ctx.items[1].disabled = false;
                            ctx.items[2].disabled = false;
                        } else {
                            config.vscreenAutoSize = true;

                            //Disable vscreen width and height items
                            ctx.items[1].disabled = true;
                            ctx.items[2].disabled = true;
                        }
                        break;

                    case 1:
                        open(DLG_VSCREEN_WIDTH);
                        break;

                    case 2:
                        open(DLG_VSCREEN_HEIGHT);
                        break;

                    case 3:
                        close();
                        break;
                }
                break;

            case DLG_VSCREEN_WIDTH:
                if (item < 5) {
                    config.vscreenWidth = vscreenWidths[item];
                }
                close();
                break;

            case DLG_VSCREEN_HEIGHT:
                if (item < 5) {
                    config.vscreenHeight = vscreenHeights[item];
                }
                close();
                break;

            case DLG_AUDIO_SETTINGS:
                switch (item) {
                    case 0:
                        config.audioEnabled = !config.audioEnabled;
                        break;

                    case 1:
                        config.musicEnabled = !config.musicEnabled;
                        break;

                    case 2:
                        config.sfxEnabled = !config.sfxEnabled;
                        break;

                    case 3:
                        close();
                        break;
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

        loadDialog(dialogType);

        switch (dialogType) {
            case DLG_VSCREEN_SIZE:
                ctx.greenBg = true;
                break;

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

        //Determine item to be selected by default (if not the first one)
        if (ctx.useCursor) {
            if (dialogType == DLG_DIFFICULTY) {
                //Highest unlocked difficulty
                sel = config.progressDifficulty;
            } else if (dialogType == DLG_LEVEL) {
                //Highest unlocked level
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
            } else if (dialogType == DLG_WINDOW_SCALE) {
                //Current window scale
                sel = config.windowScale - 1;
            } else if (dialogType == DLG_VSCREEN_WIDTH) {
                int i;

                //Current vscreen width
                for (i = 0; i < vscreenWidths.length; i++) {
                    if (vscreenWidths[i] == config.vscreenWidth) {
                        sel = i;
                    }
                }
            } else if (dialogType == DLG_VSCREEN_HEIGHT) {
                int i;

                //Current vscreen height
                for (i = 0; i < vscreenHeights.length; i++) {
                    if (vscreenHeights[i] == config.vscreenHeight) {
                        sel = i;
                    }
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
        } else if (dialogType == DLG_VSCREEN_SIZE) {
            ctx.greenBg = false;
        } else if (dialogType == DLG_PAUSE) {
            ctx.showFrame = false;
        } else if (dialogType == DLG_ERROR) {
            ctx.action = DLGACT_QUIT;
        }

        ctx.text = "";

        ctx.stackSize--;
        if (ctx.stackSize > 0) {
            int sel;

            loadDialog(ctx.stack[ctx.stackSize - 1].type);

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
        //separately by Renderer.java on the first line
        ctx.text = "\n\n" + msg;

        ctx.textOffsetX = 0;
        ctx.textOffsetY = -3;
        ctx.textWidth = 26;
        ctx.textHeight = 4;
        ctx.textBorder = true;
    }

    //Loads a specific dialog
    void loadDialog(int dialogType) {
        String displayName = "";
        int i;

        ctx.showLogo = (dialogType == DLG_MAIN);
        ctx.fillScreen = (dialogType != DLG_PAUSE);

        //Set the name to be displayed at the top of the screen
        displayName = "";
        switch (dialogType) {
            case DLG_DIFFICULTY:       displayName = "DIFFICULTY SELECT"; break;
            case DLG_LEVEL:            displayName = "LEVEL SELECT";      break;
            case DLG_JUKEBOX:          displayName = "JUKEBOX";           break;
            case DLG_SETTINGS:         displayName = "SETTINGS";          break;
            case DLG_DISPLAY_SETTINGS: displayName = "DISPLAY SETTINGS";  break;
            case DLG_WINDOW_SCALE:     displayName = "WINDOW SCALE";      break;
            case DLG_VSCREEN_SIZE:     displayName = "VSCREEN SIZE";      break;
            case DLG_VSCREEN_WIDTH:    displayName = "VSCREEN WIDTH";     break;
            case DLG_VSCREEN_HEIGHT:   displayName = "VSCREEN HEIGHT";    break;
            case DLG_AUDIO_SETTINGS:   displayName = "AUDIO SETTINGS";    break;
            case DLG_ABOUT:            displayName = "ABOUT";             break;
            case DLG_CREDITS:          displayName = "CREDITS";           break;
            case DLG_TRYAGAIN_PAUSE:   displayName = "CONFIRMATION";      break;
            case DLG_TRYAGAIN_TIMEUP:  displayName = "CONFIRMATION";      break;
            case DLG_QUIT:             displayName = "CONFIRMATION";      break;
        }
        ctx.displayName = displayName;

        //Set text for confirmation dialogs
        switch (dialogType) {
            case DLG_TRYAGAIN_PAUSE:
            case DLG_TRYAGAIN_TIMEUP:
                ctx.text = "TRY AGAIN?";
                ctx.textOffsetX = 0;
                ctx.textOffsetY = -4;
                ctx.textWidth   = 10;
                ctx.textHeight  = 1;
                ctx.textBorder  = true;
                break;

            case DLG_QUIT:
                ctx.text = "QUIT?";
                ctx.textOffsetX = 0;
                ctx.textOffsetY = -4;
                ctx.textWidth   = 10;
                ctx.textHeight  = 1;
                ctx.textBorder  = true;
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
                setItem(0, 12,  5,  5, -2,  5,  1, SPR_DIALOG_PLAY);
                setItem(1,  5,  5,  0,  5,  0,  2, SPR_DIALOG_JUKEBOX);
                setItem(2,  5,  5,  0,  5,  1,  3, SPR_DIALOG_SETTINGS);
                setItem(3,  5,  5,  0,  5,  2,  4, SPR_DIALOG_ABOUT);
                setItem(4,  5,  5,  0,  5,  3,  5, SPR_DIALOG_QUIT);
                setItem(5,  5,  5, -2,  0,  4,  0, SPR_DIALOG_AUDIO_ON);
                ctx.numItems = 6;
                setItemPosition(0, ALIGN_CENTER, 0, -2); //Play
                positionItemsCenter(1, 4, false, 7, 5);
                setItemPosition(5, ALIGN_TOPRIGHT, -1, 1); //Audio toggle
                break;

            case DLG_DIFFICULTY:
                setItem(0,  8,  5,  3,  3,  3,  1, NONE);
                setItem(1,  8,  5,  3,  3,  0,  2, NONE);
                setItem(2,  8,  5,  3,  3,  1,  3, NONE);
                setItem(3,  5,  5, -2, -2,  2,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 4;
                positionItemsCenter(0, 2, false, 10, 0);
                setItemPosition(3, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "NORMAL";
                ctx.items[1].caption = "HARD";
                ctx.items[2].caption = "SUPER";
                break;

            case DLG_LEVEL:
                setItem(0,  5,  6,  5,  5,  5,  1, SPR_DIALOG_1);
                setItem(1,  5,  6,  5,  5,  0,  2, SPR_DIALOG_2);
                setItem(2,  5,  6,  5,  5,  1,  3, SPR_DIALOG_3);
                setItem(3,  5,  6,  5,  5,  2,  4, SPR_DIALOG_4);
                setItem(4,  5,  6,  5,  5,  3,  5, SPR_DIALOG_5);
                setItem(5,  5,  5, -2, -2,  4,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 6;
                positionItemsCenter(0, 4, false, 6, 0);
                setItemPosition(5, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_JUKEBOX:
                setItem(0,  5,  6,  4,  4,  4,  1, SPR_DIALOG_1);
                setItem(1,  5,  6,  4,  4,  0,  2, SPR_DIALOG_2);
                setItem(2,  5,  6,  4,  4,  1,  3, SPR_DIALOG_3);
                setItem(3,  5,  6,  4,  4,  2,  4, SPR_DIALOG_4);
                setItem(4,  5,  5, -2, -2,  3,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 5;
                positionItemsCenter(0, 3, false, 6, 0);
                setItemPosition(4, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_SETTINGS:
                setItem(0, 26,  3,  3,  1,  3,  3, NONE);
                setItem(1, 26,  3,  0,  2,  3,  3, NONE);
                setItem(2, 26,  3,  1,  3,  3,  3, NONE);
                setItem(3,  5,  5,  2,  0, -2, -2, SPR_DIALOG_RETURN);
                ctx.numItems = 4;
                positionItemsCenter(0, 2, true, 4, 0);
                setItemPosition(3, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "DISPLAY SETTINGS";
                ctx.items[1].caption = "AUDIO SETTINGS";
                ctx.items[2].caption = "TOUCHSCREEN BUTTONS";
                break;

            case DLG_DISPLAY_SETTINGS:
                setItem(0, 26,  3,  4,  1,  4,  4, NONE);
                setItem(1, 26,  3,  0,  2,  4,  4, NONE);
                setItem(2, 26,  3,  1,  3,  4,  4, NONE);
                setItem(3, 26,  3,  2,  4,  4,  4, NONE);
                setItem(4,  5,  5,  3,  0, -2, -2, SPR_DIALOG_RETURN);
                ctx.numItems = 5;
                positionItemsCenter(0, 3, true, 4, 0);
                setItemPosition(4, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "FULLSCREEN";
                ctx.items[1].caption = "WINDOW SCALE";
                ctx.items[2].caption = "VSCREEN SIZE";
                ctx.items[3].caption = "SCANLINES";
                break;

            case DLG_WINDOW_SCALE:
                setItem(0, 16,  3,  3,  1,  3,  3, NONE);
                setItem(1, 16,  3,  0,  2,  3,  3, NONE);
                setItem(2, 16,  3,  1,  3,  3,  3, NONE);
                setItem(3,  5,  5,  2,  0, -2, -2, SPR_DIALOG_RETURN);
                ctx.numItems = 4;
                positionItemsCenter(0, 2, true, 4, 0);
                setItemPosition(3, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "1";
                ctx.items[1].caption = "2";
                ctx.items[2].caption = "3";
                break;

            case DLG_VSCREEN_SIZE:
                setItem(0, 26,  3,  3,  1,  3,  3, NONE);
                setItem(1, 26,  3,  0,  2,  3,  3, NONE);
                setItem(2, 26,  3,  1,  3,  3,  3, NONE);
                setItem(3,  5,  5,  2,  0, -2, -2, SPR_DIALOG_RETURN);
                ctx.numItems = 4;
                positionItemsCenter(0, 2, true, 4, 0);
                setItemPosition(3, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "MODE";
                ctx.items[1].caption = "WIDTH";
                ctx.items[2].caption = "HEIGHT";
                break;

            case DLG_VSCREEN_WIDTH:
                setItem(0,  5,  3,  5,  5,  4,  1, NONE);
                setItem(1,  5,  3,  5,  5,  0,  2, NONE);
                setItem(2,  5,  3,  5,  5,  1,  3, NONE);
                setItem(3,  5,  3,  5,  5,  2,  4, NONE);
                setItem(4,  5,  3,  5,  5,  3,  5, NONE);
                setItem(5,  5,  5, -2, -2,  3,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 6;
                positionItemsCenter(0, 4, false, 6, 0);
                setItemPosition(5, ALIGN_TOPLEFT, 1, 1); //Return

                //Note: the width options here should be the same as in the
                //vscreenWidths array, found in the Defs class
                ctx.items[0].caption = "480";
                ctx.items[1].caption = "432";
                ctx.items[2].caption = "416";
                ctx.items[3].caption = "320";
                ctx.items[4].caption = "256";
                break;

            case DLG_VSCREEN_HEIGHT:
                setItem(0,  5,  3,  5,  5,  5,  1, NONE);
                setItem(1,  5,  3,  5,  5,  0,  2, NONE);
                setItem(2,  5,  3,  5,  5,  1,  3, NONE);
                setItem(3,  5,  3,  5,  5,  2,  4, NONE);
                setItem(4,  5,  3,  5,  5,  3,  5, NONE);
                setItem(5,  5,  5, -2, -2,  4,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 6;
                positionItemsCenter(0, 4, false, 6, 0);
                setItemPosition(5, ALIGN_TOPLEFT, 1, 1); //Return

                //Note: the height options here should be the same as in the
                //vscreenHeights array, found in the Defs class
                ctx.items[0].caption = "270";
                ctx.items[1].caption = "256";
                ctx.items[2].caption = "240";
                ctx.items[3].caption = "224";
                ctx.items[4].caption = "192";
                break;

            case DLG_AUDIO_SETTINGS:
                setItem(0, 26,  3,  3,  1,  3,  3, NONE);
                setItem(1, 26,  3,  0,  2,  3,  3, NONE);
                setItem(2, 26,  3,  1,  3,  3,  3, NONE);
                setItem(3,  5,  5,  2,  0, -2, -2, SPR_DIALOG_RETURN);
                ctx.numItems = 4;
                positionItemsCenter(0, 2, true, 4, 0);
                setItemPosition(3, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "AUDIO";
                ctx.items[1].caption = "MUSIC";
                ctx.items[2].caption = "SFX";
                break;

            case DLG_ABOUT:
                setItem(0, 16,  3,  1,  1,  1,  1, NONE);
                setItem(1,  5,  5,  0,  0,  0,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 2;
                setItemPosition(0, ALIGN_CENTER, 0, 14); //Credits
                setItemPosition(1, ALIGN_TOPLEFT, 1, 1); //Return
                ctx.items[0].caption = "CREDITS";
                break;

            case DLG_CREDITS:
                setItem(0,  5,  5,  0,  0,  0,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 1;
                setItemPosition(0, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_PAUSE:
                setItem(0, 12,  5,  4, -2,  4,  1, SPR_DIALOG_PLAY);
                setItem(1,  5,  5,  0,  4,  0,  2, SPR_DIALOG_TRYAGAIN);
                setItem(2,  5,  5,  0,  4,  1,  3, SPR_DIALOG_SETTINGS);
                setItem(3,  5,  5,  0,  4,  2,  4, SPR_DIALOG_QUIT);
                setItem(4,  5,  5, -2,  0,  3,  0, SPR_DIALOG_AUDIO_ON);
                ctx.numItems = 5;
                setItemPosition(0, ALIGN_CENTER, 0, -4); //Play
                positionItemsCenter(1, 3, false, 7, 3);
                setItemPosition(4, ALIGN_TOPRIGHT, -1, 1); //Audio toggle
                break;

            case DLG_TRYAGAIN_PAUSE:
                setItem(0,  5,  5,  2,  2,  2,  1, SPR_DIALOG_CONFIRM);
                setItem(1,  5,  5,  2,  2,  0,  2, SPR_DIALOG_CANCEL);
                setItem(2,  5,  5, -2, -2,  1,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 3;
                positionItemsCenter(0, 1, false, 9, 4);
                setItemPosition(2, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_TRYAGAIN_TIMEUP:
                setItem(0,  5,  5,  1,  1,  1,  1, SPR_DIALOG_CONFIRM);
                setItem(1,  5,  5,  0,  0,  0,  0, SPR_DIALOG_CANCEL);
                ctx.numItems = 2;
                positionItemsCenter(0, 1, false, 9, 4);
                break;

            case DLG_QUIT:
                setItem(0,  5,  5,  2,  2,  2,  1, SPR_DIALOG_CONFIRM);
                setItem(1,  5,  5,  2,  2,  0,  2, SPR_DIALOG_CANCEL);
                setItem(2,  5,  5, -2, -2,  1,  0, SPR_DIALOG_RETURN);
                ctx.numItems = 3;
                positionItemsCenter(0, 1, false, 9, 4);
                setItemPosition(2, ALIGN_TOPLEFT, 1, 1); //Return
                break;

            case DLG_ERROR:
                setItem(0,  5,  5,  0,  0,  0,  0, SPR_DIALOG_CONFIRM);
                ctx.numItems = 1;
                setItemPosition(0, ALIGN_CENTER, 0, 5); //Confirm
        }

        //Determine items to be hidden or disabled
        if (dialogType == DLG_SETTINGS) {
            //Hide "Touchscreen buttons" item if not using a touchscreen
            ctx.items[2].hidden   = !config.touchEnabled;

            //Reposition items
            positionItemsCenter(0, 2, true, 4, 0);
        } else if (dialogType == DLG_DISPLAY_SETTINGS) {
            //Hide "Fullscreen" item if the window mode is fixed
            ctx.items[0].hidden   = config.fixedWindowMode;

            //Hide "Window scale" item if the window mode is fixed
            ctx.items[1].hidden   = config.fixedWindowMode;

            //Disable "Window scale" item if the window is not resizable
            ctx.items[1].disabled = config.resizableWindow;

            //Reposition items
            positionItemsCenter(0, 3, true, 4, 0);
        } else if (dialogType == DLG_VSCREEN_SIZE) {
            //Disable vscreen width and height items if vscreen auto size is
            //enabled
            ctx.items[1].disabled = config.vscreenAutoSize;
            ctx.items[2].disabled = config.vscreenAutoSize;
        } else if (dialogType == DLG_VSCREEN_WIDTH) {
            if (config.fixedWindowMode) {
                //Disable vscreen width values that are too large for the
                //physical screen (but keep the smallest available value)
                for (i = 0; i < ctx.numItems - 2; i++) {
                    if (vscreenWidths[i] > displayParams.physWidth) {
                        ctx.items[i].disabled = true;
                    }
                }
            }
        } else if (dialogType == DLG_VSCREEN_HEIGHT) {
            if (config.fixedWindowMode) {
                //Disable vscreen height values that are too large for the
                //physical screen (but keep the smallest available value)
                for (i = 0; i < ctx.numItems - 2; i++) {
                    if (vscreenHeights[i] > displayParams.physHeight) {
                        ctx.items[i].disabled = true;
                    }
                }
            }
        } else if (dialogType == DLG_DIFFICULTY) {
            if (!config.progressCheat) {
                if (config.progressDifficulty < DIFFICULTY_HARD) {
                    //Disable "hard" if it has not been unlocked
                    ctx.items[1].disabled = true;
                }
                if (config.progressDifficulty < DIFFICULTY_SUPER) {
                    //Disable "super" if it has not been unlocked
                    ctx.items[2].disabled = true;
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
            positionItemsCenter(0, 4, false, 6, 0);

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

        adaptToScreenSize();
    }

    void adaptToScreenSize() {
        int vscreenWidth  = displayParams.vscreenWidth;
        int vscreenHeight = displayParams.vscreenHeight;
        int dialogType;
        int i;

        if (ctx.stackSize <= 0) return;

        dialogType = ctx.stack[ctx.stackSize - 1].type;

        //Set the size of the return icon
        for (i = 0; i < ctx.numItems; i++) {
            int iconSprite = ctx.items[i].iconSprite;

            if (iconSprite == SPR_DIALOG_RETURN || iconSprite == SPR_DIALOG_RETURN_SMALL) {
                if (vscreenHeight <= 192) {
                    ctx.items[i].iconSprite = SPR_DIALOG_RETURN_SMALL;
                    ctx.items[i].height = 3;
                } else {
                    ctx.items[i].iconSprite = SPR_DIALOG_RETURN;
                    ctx.items[i].height = 5;
                }
            }
        }

        //Set the text
        if (vscreenWidth <= 256) {
            if (dialogType == DLG_ABOUT) {
                ctx.text =
                    (char)0x1B + "Alex vs Bus: The Race\n" +
                    (char)0x7F + " 2021-2023 M374LX\n" + //0x7F = copyright symbol
                    "\n" +
                    (char)0x1B + "Version\n" +
                    " " + VERSION + "\n" +
                    "\n" +
                    (char)0x1B + "Licenses\n" +
                    " The code is under GNU GPLv3,\n" +
                    " while the assets are under\n" +
                    " CC BY-SA 4.0.";
            } else if (dialogType == DLG_CREDITS) {
                ctx.text =
                    (char)0x1B + "M374LX\n" +
                    " Game design, programming,\n" +
                    " music, SFX, graphics\n" +
                    "\n" +
                    (char)0x1B + "Hoton Bastos\n" +
                    " Additional game design\n" +
                    "\n" +
                    (char)0x1B + "Harim Pires\n" +
                    " Testing\n" +
                    "\n" +
                    (char)0x1B + "Codeman38\n" +
                    " \"Press Start 2P\" font\n" +
                    "\n" +
                    (char)0x1B + "YoWorks\n" +
                    " \"Telegrama\" font";
            }
        } else {
            if (dialogType == DLG_ABOUT) {
                ctx.text =
                    (char)0x1B + "Alex vs Bus: The Race\n" +
                    (char)0x7F + " 2021-2023 M374LX\n" + //0x7F = copyright symbol
                    "\n" +
                    (char)0x1B + "Version\n" +
                    " " + VERSION + "\n" +
                    "\n" +
                    (char)0x1B + "Repository\n" +
                    " " + url(REPOSITORY) + "\n" +
                    "\n" +
                    (char)0x1B + "Licenses\n" +
                    " The code is under GNU GPLv3, while\n" +
                    " the assets are under CC BY-SA 4.0.\n" +
                    "\n" +
                    " " + url("https://www.gnu.org/licenses/gpl-3.0.en.html") + "\n" +
                    " " + url("https://creativecommons.org/licenses/by-sa/4.0");
            } else if (dialogType == DLG_CREDITS) {
                ctx.text =
                    (char)0x1B + "M374LX" + (char)0x1B +
                            " (" + url("http://m374lx.users.sourceforge.net") + ")\n" +
                    " Game design, programming,\n" +
                    " music, SFX, graphics\n" +
                    "\n" +
                    (char)0x1B + "Hoton Bastos\n" +
                    " Additional game design\n" +
                    "\n" +
                    (char)0x1B + "Harim Pires\n" +
                    " Testing\n" +
                    "\n" +
                    (char)0x1B + "Codeman38" + (char)0x1B +
                            " (" + url("https://www.zone38.net") + ")\n" +
                    " \"Press Start 2P\" font\n" +
                    "\n" +
                    (char)0x1B + "YoWorks" + (char)0x1B +
                            " (" + url("https://www.yoworks.com") + ")\n" +
                    " \"Telegrama\" font";
            }
        }

        //If the screen is too small, hide the audio toggle icon on the main
        //dialog
        if (dialogType == DLG_MAIN) {
            ctx.items[5].hidden = false;

            if (vscreenWidth <= 256) {
                ctx.items[5].hidden = true;

                if (ctx.useCursor) {
                    if (ctx.stack[ctx.stackSize - 1].selectedItem == 5) {
                        ctx.stack[ctx.stackSize - 1].selectedItem = 0;
                    }
                }
            }
        }

        //In smaller screens, move the "Credits" item upwards a bit
        if (dialogType == DLG_ABOUT) {
            ctx.items[0].offsetY = 11;

            if (vscreenHeight <= 256) {
                ctx.items[0].offsetY = 10;
            }
            if (vscreenHeight <= 224) {
                ctx.items[0].offsetY = 8;
            }
            if (vscreenHeight <= 192) {
                ctx.items[0].offsetY = 9;
            }
        }

        //Set the offset, size, and border of the text on "About" and "Credits"
        //dialogs
        if (dialogType == DLG_ABOUT || dialogType == DLG_CREDITS) {
            ctx.textBorder  = true;
            ctx.textOffsetX = 0;
            ctx.textOffsetY = -2;
            ctx.textWidth   = 47;
            ctx.textHeight  = 15;

            if (vscreenWidth <= 320) {
                ctx.textWidth = 36;
            }
            if (vscreenWidth <= 256) {
                ctx.textWidth = 28;
            }

            if (vscreenWidth <= 320 || vscreenHeight <= 224) {
                ctx.textBorder = false;
                ctx.textOffsetX = -1;
            }

            if (vscreenHeight <= 192) {
                ctx.textOffsetY = -1;
            }
        }
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

            if (!it.hidden) {
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

    //Just removes the protocol (http:// or https://) from a URL if the width
    //of the virtual screen (vscreen) is 320 or less
    String url(String str) {
        if (displayParams.vscreenWidth <= 320) {
            if (str.startsWith("https://")) {
                return str.substring(8);
            } else if (str.startsWith("http://")) {
                return str.substring(7);
            }
        }

        return str;
    }
}

