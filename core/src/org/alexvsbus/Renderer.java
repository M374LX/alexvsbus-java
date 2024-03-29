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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

class Renderer {
    DisplayParams displayParams;
    Config config;
    PlayCtx playCtx;
    DialogCtx dialogCtx;

    boolean saveFailed;

    Texture gfx;
    TextureRegion textureRegion;
    Matrix4 mat;
    SpriteBatch spriteBatch;

    int drawOffsetX;
    int drawOffsetY;

    //Temporary location for drawDigits()
    //
    //To prevent an instantiation each time the method is called and the
    //garbage collector from running more often, it is declared as a class
    //member, rather than being local to the method, and instantiated once in
    //the constructor
    int digitsTemp[];

    //--------------------------------------------------------------------------

    Renderer(DisplayParams dp, Config cfg, PlayCtx pctx, DialogCtx dctx) {

        displayParams = dp;
        config = cfg;
        playCtx = pctx;
        dialogCtx = dctx;

        textureRegion = new TextureRegion();
        mat = new Matrix4();
        spriteBatch = new SpriteBatch();

        digitsTemp = new int[12];

        //Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    void load() {
        gfx = new Texture(Gdx.files.internal("gfx.png"));
    }

    void draw(int screenType, int inputState, int wipeValue) {
        int vpx = displayParams.viewportOffsetX;
        int vpy = displayParams.viewportOffsetY;
        int vpw = displayParams.viewportWidth;
        int vph = displayParams.viewportHeight;
        int vscreenWidth   = displayParams.vscreenWidth;
        int vscreenHeight  = displayParams.vscreenHeight;

        //Clear entire physical screen to black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Dialogs.isOpen() && dialogCtx.greenBg) {
            Gdx.gl.glClearColor(0, 0.333f, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        Gdx.gl.glViewport(vpx, vpy, vpw, vph);

        textureRegion.setTexture(gfx);

        //Set the matrix so that the Y axis points downwards
        mat.setToOrtho(0, vscreenWidth, vscreenHeight, 0, 0, 1);
        spriteBatch.setProjectionMatrix(mat);
        spriteBatch.begin();

        //Clear virtual screen to black
        drawSpriteStretch(SPR_BG_BLACK, 0, 0, vscreenWidth, vscreenHeight);

        switch (screenType) {
            case SCR_BLANK:
                //Do nothing
                break;

            case SCR_PLAY:
            case SCR_PLAY_FREEZE:
                if (!(Dialogs.isOpen() && dialogCtx.fillScreen)) {
                    drawPlay();
                    drawHud();
                }
                break;

            case SCR_FINALSCORE:
                drawFinalScore();
                break;
        }

        if (saveFailed) {
            String msg = "UNABLE TO SAVE GAME PROGRESS";
            int x = ((vscreenWidth / TILE_SIZE) - msg.length()) / 2;

            drawText(msg, TXTCOL_WHITE, TILE_SIZE * x, TILE_SIZE * 3);
        }

        if (Dialogs.isOpen()) {
            drawDialog();
        }

        //Draw screen wiping effects
        drawSpriteStretch(SPR_BG_BLACK, 0, 0, wipeValue, vscreenHeight);

        drawScanlines();

        //Finish stuff drawn on the virtual screen
        spriteBatch.end();

        if (screenType == SCR_PLAY) {
            drawTouchButtons(inputState);
        }
    }

    void showSaveError(boolean show) {
        saveFailed = show;
    }

    void dispose() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
            spriteBatch = null;
        }

        if (gfx != null) {
            gfx.dispose();
            gfx = null;
        }
    }

    //--------------------------------------------------------------------------

    void drawPlay() {
        PlayCtx ctx = playCtx;

        int vscreenWidth  = displayParams.vscreenWidth;
        int vscreenHeight = displayParams.vscreenHeight;

        int x, y, spr, frame;
        int camy, topcamy; //Current and topmost camera Y position
        int i;

        //Background color
        drawSpriteStretch(ctx.bgColor, 0, 0, vscreenWidth, vscreenHeight);

        camy = (int)ctx.cam.y;

        //Determine topmost camera Y position from virtual screen (vscreen)
        //height
        topcamy = 0;
        if (vscreenHeight < 224) {
            topcamy = vscreenHeight - 224;
        }

        if (camy < topcamy) {
            camy = topcamy;
        }

        drawOffsetY = camy - (vscreenHeight - VSCREEN_MAX_HEIGHT);

        //Background image
        drawSpriteRepeat(SPR_BACKGROUND, -ctx.bgOffsetX, BACKGROUND_DRAW_Y, 6, 1);

        drawOffsetX = (int)ctx.cam.x;
        if (vscreenWidth <= 320 && ctx.levelNum == LVLNUM_ENDING) {
            drawOffsetX += 168;
        }

        //Deep holes and passageways (background part)
        for (i = 0; i < MAX_HOLES; i++) {
            int w = ctx.holes[i].width;

            x = ctx.holes[i].x;
            y = BACKGROUND_DRAW_Y + 72;

            if (x != NONE) {
                int type = ctx.holes[i].type;
                boolean isDeep = (type == HOLE_DEEP);
                boolean exitOpened = (type == HOLE_PASSAGEWAY_EXIT_OPENED);

                //Left
                spr = (isDeep ? SPR_DEEP_HOLE_LEFT : SPR_PASSAGEWAY_LEFT);
                drawSprite(spr, x, y, 0);

                //Middle
                x += LEVEL_BLOCK_SIZE;
                spr = (isDeep ? SPR_DEEP_HOLE_MIDDLE : SPR_PASSAGEWAY_MIDDLE);
                drawSpriteRepeat(spr, x, y, w - 2, 1);

                //Right
                x = ctx.holes[i].x + ((w - 1) * LEVEL_BLOCK_SIZE);
                spr = (isDeep ? SPR_DEEP_HOLE_RIGHT : SPR_PASSAGEWAY_RIGHT);
                drawSprite(spr, x, y, 0);

                //Passageway exit if not opened
                if (!isDeep && !exitOpened) {
                    spr = SPR_PASSAGEWAY_RIGHT_CLOSED;
                    drawSprite(spr, x, y, 0);
                }
            }
        }

        //Bus body, wheels, and route sign
        x = (int)ctx.bus.x;
        y = BUS_Y;
        drawSprite(SPR_BUS, x, y, 0);
        if (ctx.bus.routeSign != NONE) {
            if (ctx.bus.routeSign == 0) {
                //Finish (checkered flag) sign
                frame = 4;
            } else {
                //Frame zero corresponds to the sign containing number two, as
                //there is no number one sign
                frame = ctx.bus.routeSign - 2;
            }

            if (frame >= 0) {
                drawSprite(SPR_BUS_ROUTE, x + 308, y + 48, frame);
            }
        }
        frame = ctx.anims[ANIM_BUS_WHEELS].frame;
        drawSprite(SPR_BUS_WHEEL, x + 104, y + 80, frame);
        drawSprite(SPR_BUS_WHEEL, x + 296, y + 80, frame);

        //Characters at bus rear door
        if (ctx.bus.numCharacters >= 1) {
            drawSprite(SPR_BUS_CHARACTER_1, x + 72, y + 24, 0);
        }
        if (ctx.bus.numCharacters >= 2) {
            drawSprite(SPR_BUS_CHARACTER_2, x + 64, y + 24, 0);
        }
        if (ctx.bus.numCharacters >= 3) {
            drawSprite(SPR_BUS_CHARACTER_3, x + 80, y + 24, 0);
        }

        //Cutscene objects (if in the bus)
        for (i = 0; i < MAX_CUTSCENE_OBJECTS; i++) {
            CutsceneObject cobj = ctx.cutsceneObjects[i];

            if (cobj.sprite == NONE || !cobj.inBus) continue;

            spr = cobj.sprite;
            x = (int)cobj.x + (int)ctx.bus.x;
            y = (int)cobj.y;
            frame = ctx.anims[ANIM_CUTSCENE_OBJECTS + i].frame;
            drawSprite(spr, x, y, frame);
        }

        //Bus doors
        x = (int)ctx.bus.x;
        y = BUS_Y;
        frame = ctx.anims[ANIM_BUS_DOOR_REAR].frame;
        drawSprite(SPR_BUS_DOOR, x + 64,  y + 16, frame);
        frame = ctx.anims[ANIM_BUS_DOOR_FRONT].frame;
        drawSprite(SPR_BUS_DOOR, x + 344, y + 16, frame);

        //Passing car and ending sequence traffic jam
        if (ctx.car.x != NONE) {
            int numCars;

            x = (int)ctx.car.x;
            y = PASSING_CAR_Y;
            frame = ctx.anims[ANIM_CAR_WHEELS].frame;

            if (ctx.car.type == TRAFFIC_JAM) { //Traffic jam
                numCars = 6;
                spr = SPR_CAR_BLUE;
            } else { //Single car
                numCars = 1;

                spr = SPR_CAR_BLUE;
                if (ctx.car.type == CAR_SILVER) {
                    spr = SPR_CAR_SILVER;
                } else if (ctx.car.type == CAR_YELLOW) {
                    spr = SPR_CAR_YELLOW;
                }
            }

            for (i = 0; i < numCars; i++) {
                drawSprite(spr, x, y, 0); //Car body
                drawSprite(SPR_CAR_WHEEL, x + 16, y + 32, frame); //Rear wheel
                drawSprite(SPR_CAR_WHEEL, x + 96, y + 32, frame); //Front wheel

                x += 136;

                //Next car color
                switch (spr) {
                    case SPR_CAR_BLUE:   spr = SPR_CAR_SILVER; break;
                    case SPR_CAR_SILVER: spr = SPR_CAR_YELLOW; break;
                    case SPR_CAR_YELLOW: spr = SPR_CAR_BLUE;   break;
                }
            }
        }

        //Hen
        if (ctx.hen.x != NONE) {
            frame = ctx.anims[ANIM_HEN].frame;
            drawSprite(SPR_HEN, (int)ctx.hen.x, HEN_Y, frame);
        }

        //Light poles (at most two are visible)
        drawSprite(SPR_POLE, ctx.poleX, POLE_Y, 0);
        drawSprite(SPR_POLE, ctx.poleX + POLE_DISTANCE, POLE_Y, 0);

        //Bus stop sign
        drawSprite(SPR_BUS_STOP_SIGN, ctx.busStopSignX, BUS_STOP_SIGN_Y, 0);

        //Crate blocks
        for (i = 0; i < MAX_CRATE_BLOCKS; i++) {
            CrateBlock block = ctx.crateBlocks[i];
            x = block.x;
            y = block.y;

            if (x != NONE) {
                drawSpriteRepeat(SPR_CRATE, x, y, block.width, block.height);
            }
        }

        //Objects that use PlayCtx.objs[] and are drawn behind the player
        //character
        for (i = 0; i < MAX_OBJS; i++) {
            Obj obj = ctx.objs[i];

            //Ignore inexistent objects
            if (obj.type == NONE) continue;

            //Skip objects that are drawn in front of the player character, as
            //those will be drawn later
            if (obj.type == OBJ_COIN_SILVER) continue;
            if (obj.type == OBJ_COIN_GOLD) continue;
            if (obj.type == OBJ_BANANA_PEEL) continue;
            if (obj.type == OBJ_BANANA_PEEL_MOVING) continue;

            if (obj.type == OBJ_GUSH) {
                int w = Data.sprites[SPR_GUSH * 4 + 2];
                int h = 265 - obj.y;
                if (h <= 0) h = 1;

                frame = ctx.anims[ANIM_GUSHES].frame;

                drawSpritePart(SPR_GUSH, obj.x, obj.y, frame * w, 0, w, h);

                //Gush hole
                drawSprite(SPR_GUSH_HOLE, obj.x, 263, 0);
            } else {
                frame = 0;

                if (obj.type == OBJ_SPRING) {
                    frame = 5;

                    if (i == ctx.hitSpring) {
                        frame = ctx.anims[ANIM_HIT_SPRING].frame;
                    }
                }

                drawSprite(Data.objSprites[obj.type], obj.x, obj.y, frame);
            }
        }

        //Player character
        if (ctx.player.visible) {
            spr = Data.playerAnimSprites[ctx.player.animType];
            x = (int)ctx.player.x;
            y = (int)ctx.player.y;
            frame = ctx.anims[ANIM_PLAYER].frame;
            drawSprite(spr, x, y, frame);
        }

        //Cutscene objects (if not in the bus)
        for (i = 0; i < MAX_CUTSCENE_OBJECTS; i++) {
            CutsceneObject cobj = ctx.cutsceneObjects[i];

            if (cobj.sprite == NONE || cobj.inBus) continue;

            spr = cobj.sprite;
            x = (int)cobj.x;
            y = (int)cobj.y;
            frame = ctx.anims[ANIM_CUTSCENE_OBJECTS + i].frame;
            drawSprite(spr, x, y, frame);
        }

        //Medal icons (used in the ending sequence)
        if (ctx.playerReachedFlagman) {
            x = (int)ctx.cutsceneObjects[0].x;
            y = 160;

            if (ctx.cutsceneObjects[0].sprite == SPR_PLAYER_RUN) {
                x += 8;
            }

            drawSprite(SPR_MEDAL1, x, y, 0);
        }
        if (ctx.henReachedFlagman) {
            x = (int)ctx.hen.x;
            y = 184;
            drawSprite(SPR_MEDAL2, x, y, 0);
        }
        if (ctx.busReachedFlagman) {
            x = (int)ctx.bus.x + 343;
            y = 120;
            drawSprite(SPR_MEDAL3, x, y, 0);
        }

        //Deep holes and passageways (foreground part)
        for (i = 0; i < MAX_HOLES; i++) {
            x = ctx.holes[i].x;
            y = BACKGROUND_DRAW_Y + 88;

            if (x != NONE) {
                if (ctx.holes[i].type == HOLE_DEEP) {
                    drawSprite(SPR_DEEP_HOLE_LEFT_FG, x, y, 0);
                } else {
                    //Left
                    drawSprite(SPR_PASSAGEWAY_LEFT_FG, x, y, 0);

                    //Right
                    x += (ctx.holes[i].width - 1) * LEVEL_BLOCK_SIZE;
                    drawSprite(SPR_PASSAGEWAY_RIGHT_FG, x, y, 0);
                }
            }
        }

        //When slipping, the player character is drawn in front of hole
        //foregrounds
        if (ctx.player.visible) {
            int state = ctx.player.state;

            if (state == PLAYER_STATE_SLIP || state == PLAYER_STATE_GETUP) {
                spr = Data.playerAnimSprites[ctx.player.animType];
                x = (int)ctx.player.x;
                y = (int)ctx.player.y;
                frame = ctx.anims[ANIM_PLAYER].frame;
                drawSprite(spr, x, y, frame);
            }
        }

        //Objects that use PlayCtx.objs[] and are drawn in front of the player
        //character
        for (i = 0; i < MAX_OBJS; i++) {
            Obj obj = ctx.objs[i];

            if (obj.type == OBJ_BANANA_PEEL) {
                frame = 0;
            } else if (obj.type == OBJ_BANANA_PEEL_MOVING) {
                frame = 0;
            } else if (obj.type == OBJ_COIN_SILVER) {
                frame = ctx.anims[ANIM_COINS].frame;
            } else if (obj.type == OBJ_COIN_GOLD) {
                frame = ctx.anims[ANIM_COINS].frame;
            } else {
                continue;
            }

            drawSprite(Data.objSprites[obj.type], obj.x, obj.y, frame);
        }

        //Pushable crate arrows
        for (i = 0; i < MAX_PUSHABLE_CRATES; i++) {
            PushableCrate crate = ctx.pushableCrates[i];

            if (crate.obj != NONE && crate.showArrow) {
                x = ctx.objs[crate.obj].x - 24 + (int)ctx.pushArrow.xoffs;
                y = FLOOR_Y - 20;

                drawSprite(SPR_PUSH_ARROW, x, y, 0);
            }
        }

        //Overhead sign bases
        for (i = 0; i < MAX_OBJS; i++) {
            Obj obj = ctx.objs[i];
            int h;

            if (obj.type == OBJ_OVERHEAD_SIGN) {
                spr = SPR_OVERHEAD_SIGN_BASE_TOP;
                x = obj.x + 16;
                y = obj.y + 8;
                drawSprite(spr, x, y, 0);

                spr = SPR_OVERHEAD_SIGN_BASE;
                x = obj.x + 24;
                y = obj.y + 32;
                h = 272 - y;
                drawSpritePart(spr, x, y, 0, 320 - h, 8, h);
            }
        }

        //Crack particles
        for (i = 0; i < MAX_CRACK_PARTICLES; i++) {
            x = (int)ctx.crackParticles[i].x;
            y = (int)ctx.crackParticles[i].y;
            frame = ctx.anims[ANIM_CRACK_PARTICLES].frame;

            if (x != NONE) {
                drawSprite(SPR_CRACK_PARTICLE, x, y, frame);
            }
        }

        //Coin sparks
        for (i = 0; i < MAX_COIN_SPARKS; i++) {
            x = ctx.coinSparks[i].x;
            y = ctx.coinSparks[i].y;

            if (x != NONE) {
                boolean gold = ctx.coinSparks[i].gold;
                spr = gold ? SPR_COIN_SPARK_GOLD : SPR_COIN_SPARK_SILVER;
                frame = ctx.anims[ANIM_COIN_SPARKS + i].frame;
                drawSprite(spr, x, y, frame);
            }
        }

        //Reset draw offset
        drawOffsetX = 0;
        drawOffsetY = 0;
    }

    void drawHud() {
        int x, h;

        //Determine HUD height
        h = TILE_SIZE * 2;
        if (config.touchEnabled && displayParams.vscreenHeight > 224) {
            h = TILE_SIZE * 3;
        }
        if (saveFailed) {
            h = TILE_SIZE * 4;
        }

        drawSpriteStretch(SPR_BG_BLACK, 0, 0, displayParams.vscreenWidth, h);

        drawText("SCORE", TXTCOL_WHITE, 0, 0);
        drawDigits(playCtx.score, 6, 0, 8);

        x = (displayParams.vscreenWidth / 2) - (2 * TILE_SIZE);

        drawText("TIME", TXTCOL_WHITE, x, 0);
        if (playCtx.levelNum == LVLNUM_ENDING) {
            drawText("--", TXTCOL_WHITE, x + TILE_SIZE, 8);
        } else {
            drawDigits(playCtx.time, 2, x + TILE_SIZE, 8);
        }

        //Touchscreen pause button
        if (config.showTouchControls && playCtx.canPause) {
            if (!Dialogs.isOpen()) {
                drawSprite(SPR_PAUSE, displayParams.vscreenWidth - 24, 0, 0);
            }
        }
    }

    void drawFinalScore() {
        int cx = (displayParams.vscreenWidth  / TILE_SIZE) / 2 * TILE_SIZE;
        int cy = (displayParams.vscreenHeight / TILE_SIZE) / 2 * TILE_SIZE;
        int x = 0;
        String msg = "";

        drawText("SCORE:", TXTCOL_WHITE, cx - 7 * TILE_SIZE, cy - TILE_SIZE);
        drawDigits(playCtx.score, 6, cx + 1 * TILE_SIZE, cy - TILE_SIZE);

        switch (playCtx.difficulty) {
            case DIFFICULTY_NORMAL:
                x = cx - 12 * TILE_SIZE;
                msg = "GET READY FOR HARD MODE!";
                break;

            case DIFFICULTY_HARD:
                x = cx - 12 * TILE_SIZE;
                msg = "GET READY FOR SUPER MODE!";
                break;

            case DIFFICULTY_SUPER:
                x = cx - 4 * TILE_SIZE;
                msg = "THE  END";
                break;
        }

        drawText(msg, TXTCOL_WHITE, x, cy + TILE_SIZE);
    }

    void drawDialog() {
        DialogCtx ctx = dialogCtx;
        int vscreenWidth  = displayParams.vscreenWidth;
        int vscreenHeight = displayParams.vscreenHeight;
        int selectedItem  = ctx.stack[ctx.stackSize - 1].selectedItem;
        int i;

        //Center of the main area of the dialog in tiles
        int cx = (vscreenWidth / TILE_SIZE) / 2;
        int cy = Dialogs.centerTileY();

        //Draw dialog frame
        if (ctx.showFrame) {
            //Frame size and position in tiles
            int tw = 28;
            int th = 18;
            int tx = cx - (tw / 2);
            int ty = cy - (th / 2);

            int w = tw * TILE_SIZE;
            int h = th * TILE_SIZE;
            int x = tx * TILE_SIZE;
            int y = ty * TILE_SIZE;

            drawSpriteStretch(SPR_BG_BLACK, x, y, w, h);
        }

        //Draw logo
        if (ctx.showLogo) {
            int spr, logoWidth, x, y;

            if (vscreenWidth <= 320 || vscreenHeight <= 224) {
                spr = SPR_LOGO_SMALL;
                logoWidth = LOGO_WIDTH_SMALL;
            } else {
                spr = SPR_LOGO_LARGE;
                logoWidth = LOGO_WIDTH_LARGE;
            }

            x = (vscreenWidth - logoWidth) / 2 + 4;
            y = (vscreenHeight <= 192) ? 0 : 16;

            drawSprite(spr, x, y, 0);
        }

        //Draw dialog display name
        if (ctx.displayName.length() > 0 && !ctx.levelSelected) {
            //Text position in tiles
            int tx = cx - 10;
            int ty = (vscreenHeight <= 192) ? 2 : 3;

            int x = tx * TILE_SIZE;
            int y = ty * TILE_SIZE;
            int w = 20;
            int h = (vscreenHeight <= 192) ? 3 : 5;

            if (vscreenWidth <= 256) {
                boolean centerBar = true;

                //If the virtual screen (vscreen) is 256 pixels wide or less,
                //center the name bar only if there is no selectable item at
                //the top-left corner
                for (i = 0; i < ctx.numItems; i++) {
                    if (ctx.items[i].align == ALIGN_TOPLEFT) {
                        centerBar = false;
                    }
                }

                w = centerBar ? 14 : 16;
                tx = cx - 7;
                x = tx * TILE_SIZE;
            }

            drawDialogBorder(x - 16, 8, w + 4, h, false, false);
            drawText(ctx.displayName, TXTCOL_WHITE, x, y);
        }

        //Draw dialog text
        if (ctx.text.length() > 0) {
            //Text position in tiles
            int tx = cx - (ctx.textWidth  / 2) + ctx.textOffsetX;
            int ty = cy - (ctx.textHeight / 2) + ctx.textOffsetY;

            int x = tx * TILE_SIZE;
            int y = ty * TILE_SIZE;
            int w = ctx.textWidth;
            int h = ctx.textHeight;

            if (ctx.textBorder) {
                drawDialogBorder(x - 16, y - 16, w + 4, h + 4, false, false);
            }

            drawText(ctx.text, TXTCOL_WHITE, x, y);

            if (ctx.stack[ctx.stackSize - 1].type == DLG_ERROR) {
                drawSprite(SPR_ERROR, x, y, 0);
            }
        }

        //Draw dialog items
        for (i = 0; i < ctx.numItems; i++) {
            if (selectedItem != i) {
                if (!ctx.levelSelected) {
                    drawDialogItem(ctx.items[i], false);
                }
            } else if (ctx.selectedVisible) {
                drawDialogItem(ctx.items[i], ctx.useCursor);
            }
        }
    }

    void drawDialogItem(DialogItem item, boolean selected) {
        int x, y;
        int w = item.width;
        int h = item.height;

        if (item.hidden) return;

        x = Dialogs.itemX(item);
        y = Dialogs.itemY(item);

        drawDialogBorder(x, y, w, h, selected, item.disabled);

        //Draw item caption
        if (item.caption.length() > 0) {
            int xoffs = TILE_SIZE;
            int yoffs = TILE_SIZE * (item.height / 2);
            int color;

            if (item.disabled) {
                color = TXTCOL_GRAY;
            } else if (selected) {
                color = TXTCOL_GREEN;
            } else {
                color = TXTCOL_WHITE;
            }

            drawText(item.caption, color, x + xoffs, y + yoffs);
        }

        //Draw item value
        if (item.value.length() > 0) {
            int xoffs = ((w - 1) * TILE_SIZE) - (item.value.length() * TILE_SIZE);
            int yoffs = TILE_SIZE * (item.height / 2);
            int color;

            if (item.disabled) {
                color = TXTCOL_GRAY;
            } else if (selected) {
                color = TXTCOL_GREEN;
            } else {
                color = TXTCOL_WHITE;
            }

            drawText(item.value, color, x + xoffs, y + yoffs);
        }

        //Draw item icon
        if (item.iconSprite != NONE) {
            int spr = item.iconSprite;
            if (selected) spr++;

            x = Dialogs.itemX(item) + TILE_SIZE;
            y = Dialogs.itemY(item) + TILE_SIZE;

            drawSprite(spr, x, y, 0);
        }
    }

    void drawDialogBorder(int x, int y, int width, int height, boolean selected,
                                                            boolean disabled) {

        int i, j;
        int spr;

        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                boolean hflip, vflip;

                if (i == 0) {
                    if (j == 0) {
                        spr = SPR_DIALOG_BORDER_TOPLEFT;
                        hflip = false;
                        vflip = false;
                    } else if (j == height - 1) {
                        spr = SPR_DIALOG_BORDER_TOPLEFT;
                        hflip = false;
                        vflip = true;
                    } else {
                        spr = SPR_DIALOG_BORDER_LEFT;
                        hflip = false;
                        vflip = false;
                    }
                } else if (i == width - 1) {
                    if (j == 0) {
                        spr = SPR_DIALOG_BORDER_TOPLEFT;
                        hflip = true;
                        vflip = false;
                    } else if (j == height - 1) {
                        spr = SPR_DIALOG_BORDER_TOPLEFT;
                        hflip = true;
                        vflip = true;
                    } else {
                        spr = SPR_DIALOG_BORDER_LEFT;
                        hflip = true;
                        vflip = false;
                    }
                } else {
                    if (j == 0) {
                        spr = SPR_DIALOG_BORDER_TOP;
                        hflip = false;
                        vflip = false;
                    } else if (j == height - 1) {
                        spr = SPR_DIALOG_BORDER_TOP;
                        hflip = false;
                        vflip = true;
                    } else {
                        spr = SPR_BG_BLACK;
                        hflip = false;
                        vflip = false;
                    }
                }

                if (spr != SPR_BG_BLACK) {
                    if (disabled) {
                        spr += 2;
                    } else if (selected) {
                        spr++;
                    }
                }

                drawSpriteFlip(spr, x + i * 8, y + j * 8, 0, hflip, vflip);
            }
        }
    }

    void drawScanlines() {
        int vscreenWidth  = displayParams.vscreenWidth;
        int vscreenHeight = displayParams.vscreenHeight;
        int scale = displayParams.scale;
        int line;

        if (!config.scanlinesEnabled || scale < 2) return;

        mat.setToOrtho(0, vscreenWidth * scale, vscreenHeight * scale, 0, 0, 1);
        spriteBatch.setProjectionMatrix(mat);
        spriteBatch.setColor(1, 1, 1, 0.5f);

        for (line = 0; line < displayParams.viewportHeight; line += scale) {
            int dy = line + (scale - 1);
            int dw = displayParams.viewportWidth;
            int sx = Data.sprites[SPR_SCANLINE * 4 + 0];
            int sy = Data.sprites[SPR_SCANLINE * 4 + 1];

            drawRegion(0, dy, dw, 1, sx, sy, 8, 1, false, false);
        }

        spriteBatch.setColor(1, 1, 1, 1); //Reset opacity
    }

    //Draws touchscreen left, right, and jump buttons
    void drawTouchButtons(int inputState) {
        int spr;
        int x, y;
        int sx, sy;
        int w, h;
        int spw, sph;

        if (!config.showTouchControls || !config.touchButtonsEnabled) return;

        //Set viewport to use the entire physical screen
        w = displayParams.physWidth;
        h = displayParams.physHeight;
        Gdx.gl.glViewport(0, 0, w, h);

        //Scaled physical screen width and height
        spw = displayParams.physWidth  / displayParams.scale;
        sph = displayParams.physHeight / displayParams.scale;

        mat.setToOrtho(0, spw, sph, 0, 0, 1);
        spriteBatch.setProjectionMatrix(mat);
        spriteBatch.begin();

        //Make buttons transparent
        spriteBatch.setColor(1, 1, 1, TOUCH_BUTTON_OPACITY);

        //Size of the buttons
        w = TOUCH_BUTTON_WIDTH;
        h = TOUCH_BUTTON_HEIGHT;

        //Draw left button
        spr = ((inputState & INPUT_LEFT) > 0) ? SPR_TOUCH_LEFT_HELD : SPR_TOUCH_LEFT;
        sx  = Data.sprites[spr * 4 + 0];
        sy  = Data.sprites[spr * 4 + 1];
        x   = TOUCH_LEFT_X;
        y   = sph - TOUCH_LEFT_OFFSET_Y;
        textureRegion.setRegion(sx, sy, w, h);
        textureRegion.flip(false, true);
        spriteBatch.draw(textureRegion, x, y, w, h);

        //Draw right button
        spr = ((inputState & INPUT_RIGHT) > 0) ? SPR_TOUCH_RIGHT_HELD : SPR_TOUCH_RIGHT;
        sx  = Data.sprites[spr * 4 + 0];
        sy  = Data.sprites[spr * 4 + 1];
        x   = TOUCH_RIGHT_X;
        y   = sph - TOUCH_RIGHT_OFFSET_Y;
        textureRegion.setRegion(sx, sy, w, h);
        textureRegion.flip(false, true);
        spriteBatch.draw(textureRegion, x, y, w, h);

        //Draw jump button
        spr = ((inputState & INPUT_JUMP) > 0) ? SPR_TOUCH_JUMP_HELD : SPR_TOUCH_JUMP;
        sx  = Data.sprites[spr * 4 + 0];
        sy  = Data.sprites[spr * 4 + 1];
        x   = spw - TOUCH_JUMP_OFFSET_X;
        y   = sph - TOUCH_JUMP_OFFSET_Y;
        textureRegion.setRegion(sx, sy, w, h);
        textureRegion.flip(false, true);
        spriteBatch.draw(textureRegion, x, y, w, h);

        //Reset opacity
        spriteBatch.setColor(1, 1, 1, 1);

        spriteBatch.end();
    }

    //--------------------------------------------------------------------------

    void drawRegion(int dx, int dy, int dw, int dh,
                                            int sx, int sy, int sw, int sh,
                                            boolean hflip, boolean vflip) {

        dx -= drawOffsetX;
        dy -= drawOffsetY;

        //Skip drawing what is outside the screen
        if (dx < -dw || dx > displayParams.vscreenWidth) return;

        textureRegion.setRegion(sx, sy, sw, sh);

        //Negate vflip because, while the Y axis points upwards by default on
        //libGDX, we use the well known convention with the Y axis pointing
        //downwards
        textureRegion.flip(hflip, !vflip);

        spriteBatch.draw(textureRegion, dx, dy, dw, dh);
    }

    void drawSpritePart(int spr, int dx, int dy, int sx, int sy, int sw, int sh) {

        sx += Data.sprites[spr * 4 + 0];
        sy += Data.sprites[spr * 4 + 1];

        drawRegion(dx, dy, sw, sh, sx, sy, sw, sh, false, false);
    }

    void drawSpriteFlip(int spr, int dx, int dy, int frame,
                                                boolean hflip, boolean vflip) {

        int w  = Data.sprites[spr * 4 + 2];
        int h  = Data.sprites[spr * 4 + 3];
        int sx = Data.sprites[spr * 4 + 0] + (frame * w);
        int sy = Data.sprites[spr * 4 + 1];

        drawRegion(dx, dy, w, h, sx, sy, w, h, hflip, vflip);
    }

    void drawSprite(int spr, int dx, int dy, int frame) {
        drawSpriteFlip(spr, dx, dy, frame, false, false);
    }

    void drawSpriteRepeat(int spr, int dx, int dy, int xrep, int yrep) {
        int w = Data.sprites[spr * 4 + 2];
        int h = Data.sprites[spr * 4 + 3];

        for (int i = 0; i < xrep; i++) {
            for (int j = 0; j < yrep; j++) {
                drawSprite(spr, dx + (i * w), dy + (j * h), 0);
            }
        }
    }

    void drawSpriteStretch(int spr, int dx, int dy, int w, int h) {
        int sx = Data.sprites[spr * 4 + 0];
        int sy = Data.sprites[spr * 4 + 1];
        int sw = Data.sprites[spr * 4 + 2];
        int sh = Data.sprites[spr * 4 + 3];

        drawRegion(dx, dy, w, h, sx, sy, sw, sh, false, false);
    }

    void drawDigits(int value, int width, int x, int y) {
        int numDigits = 0;
        int i;

        //Convert value to decimal digits
        if (value <= 0) {
            digitsTemp[0] = 0;
            numDigits = 1;
        } else {
            while (value > 0) {
                if (numDigits >= 12) break;

                digitsTemp[numDigits] = value % 10;
                value /= 10;
                numDigits++;
            }
        }

        //Pad with leading zeros
        while (numDigits < width) {
            if (numDigits >= 12) break;

            digitsTemp[numDigits] = 0;
            numDigits++;
        }

        for (i = numDigits - 1; i >= 0; i--) {
            drawSpritePart(SPR_CHARSET_WHITE, x, y, digitsTemp[i] * 8, 8, 8, 8);
            x += 8;
        }
    }

    //The character 0x1B (which corresponds to ASCII Escape) is used by this
    //method to switch between white and green characters
    //
    //The parameter "color" specifies the initial color: TXTCOL_WHITE,
    //TXTCOL_GREEN, or TXTCOL_GRAY
    //
    //The newline (\n) character also reverts to the initial color
    void drawText(String text, int color, int x, int y) {
        int i;
        int len = text.length();
        int initialColor = color;

        int spr;
        int dx = x;
        int dy = y;

        for (i = 0; i < len; i++) {
            int c, sx, sy;

            c = text.charAt(i);

            if (c == 0x1B) {
                color = (color == TXTCOL_GREEN) ? TXTCOL_WHITE : TXTCOL_GREEN;
            } else if (c == '\n') {
                dy += 8;
                dx = x;
                color = initialColor;
            } else {
                c -= ' ';
                sx = (c % 16) * 8;
                sy = (c / 16) * 8;

                switch (color) {
                    case TXTCOL_GREEN: spr = SPR_CHARSET_GREEN; break;
                    case TXTCOL_GRAY:  spr = SPR_CHARSET_GRAY;  break;
                    default:           spr = SPR_CHARSET_WHITE; break;
                }

                drawSpritePart(spr, dx, dy, sx, sy, 8, 8);

                dx += 8;
            }
        }
    }
}

