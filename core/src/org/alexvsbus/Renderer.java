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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

class Renderer {
    PlayCtx playCtx;
    DialogCtx dialogCtx;

    Matrix4 mat;
    SpriteBatch spriteBatch;
    TextureRegion textureRegion;

    int drawOffsetX;
    int drawOffsetY;

    int projectionWidth;
    int projectionHeight;

    //Temporary location for drawDigits()
    //
    //To prevent an instantiation each
    //time the method is called and the garbage collector from running more
    //often, it is declared as a class member, rather than being local to the
    //method, and instantiated once in the constructor
    int digitsTemp[];

    Texture gfx;

    //--------------------------------------------------------------------------

    Renderer(PlayCtx playCtx, DialogCtx dialogCtx) {
        this.playCtx = playCtx;
        this.dialogCtx = dialogCtx;

        gfx = null;

        projectionWidth  = 480;
        projectionHeight = 270;

        digitsTemp = new int[12];

        mat = new Matrix4();
        spriteBatch = new SpriteBatch();
        textureRegion = new TextureRegion();

        //Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    void load() {
        gfx = new Texture(Gdx.files.internal("gfx.png"));
    }

    void draw(boolean touchButtons, int inputState, int wipeValue) {
        //Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        textureRegion.setTexture(gfx);

        //Set the matrix so that the Y axis points downwards
        mat.setToOrtho(0, projectionWidth, projectionHeight, 0, 0, 1);
        spriteBatch.setProjectionMatrix(mat);
        spriteBatch.begin();

        if (playCtx.playing) {
            drawPlay();
            drawHud();
            if (touchButtons) drawTouchButtons(inputState);
        } else if (dialogCtx.showLogo) {
            drawSprite(SPR_LOGO, (SCREEN_WIDTH - LOGO_WIDTH) / 2 + 4, 16);
        }

        if (dialogCtx.stackSize > 0) {
            drawDialog();
        }

        //Draw screen wiping effects
        drawSpriteStretch(SPR_BG_BLACK, 0, 0, wipeValue, projectionHeight);

        spriteBatch.end();
    }

    void setViewport(int x, int y, int width, int height) {
        Gdx.gl.glViewport(x, y, width, height);
    }

    void setProjection(int width, int height) {
        projectionWidth  = width;
        projectionHeight = height;
    }

    void dispose() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }

        if (gfx != null) {
            gfx.dispose();
            gfx = null;
        }
    }

    //--------------------------------------------------------------------------

    void drawPlay() {
        PlayCtx ctx = playCtx;
        int x, y, spr, frame;
        int i;

        //Background color
        drawSpriteStretch(ctx.bgColor, 0, 0, projectionWidth, projectionHeight);

        drawOffsetY = (int)ctx.cam.y - (projectionHeight - SCREEN_MIN_HEIGHT);

        //Background image
        drawSpriteRepeat(SPR_BACKGROUND, -ctx.bgOffsetX, BACKGROUND_DRAW_Y, 6, 1);

        drawOffsetX = (int)ctx.cam.x;

        //Holes (background part)
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
                drawSprite(spr, x, y);

                //Middle
                x += LEVEL_BLOCK_SIZE;
                spr = (isDeep ? SPR_DEEP_HOLE_MIDDLE : SPR_PASSAGEWAY_MIDDLE);
                drawSpriteRepeat(spr, x, y, w - 2, 1);

                //Right
                x = ctx.holes[i].x + ((w - 1) * LEVEL_BLOCK_SIZE);
                spr = (isDeep ? SPR_DEEP_HOLE_RIGHT : SPR_PASSAGEWAY_RIGHT);
                if (!isDeep && !exitOpened) spr = SPR_PASSAGEWAY_MIDDLE;
                drawSprite(spr, x, y);
            }
        }

        //Passing car
        if (ctx.passingCar.x != NONE) {
            x = (int)ctx.passingCar.x;
            y = PASSING_CAR_Y;
            frame = ctx.passingCar.wheelAnimFrame;

            spr = SPR_CAR_BLUE;
            if (ctx.passingCar.color == CAR_SILVER) {
                spr = SPR_CAR_SILVER;
            } else if (ctx.passingCar.color == CAR_YELLOW) {
                spr = SPR_CAR_YELLOW;
            }

            drawSprite(spr, x, y); //Car body
            drawSpriteFrame(SPR_CAR_WHEEL, x + 16, y + 32, frame); //Rear wheel
            drawSpriteFrame(SPR_CAR_WHEEL, x + 96, y + 32, frame); //Front wheel
        }

        //Hen
        if (ctx.hen.x != NONE) {
            drawSpriteFrame(SPR_HEN, (int)ctx.hen.x, HEN_Y, ctx.hen.animFrame);
        }

        //Bus body, wheels, and route sign
        x = (int)ctx.bus.x;
        y = BUS_Y;
        drawSprite(SPR_BUS, x, y);
        if (ctx.bus.routeSign != NONE) {
            drawSpriteFrame(SPR_BUS_ROUTE, x + 308, y + 48, ctx.bus.routeSign);
        }
        frame = ctx.bus.wheelAnimFrame;
        drawSpriteFrame(SPR_BUS_WHEEL, x + 104, y + 80, frame);
        drawSpriteFrame(SPR_BUS_WHEEL, x + 296, y + 80, frame);

        //Characters at bus rear door
        if (ctx.bus.numCharacters >= 1) {
            drawSprite(SPR_BUS_CHARACTER_1, x + 72, y + 24);
        }
        if (ctx.bus.numCharacters >= 2) {
            drawSprite(SPR_BUS_CHARACTER_2, x + 64, y + 24);
        }
        if (ctx.bus.numCharacters >= 3) {
            drawSprite(SPR_BUS_CHARACTER_3, x + 80, y + 24);
        }

        //Player character (if in the bus)
        if (ctx.playerInBus) {
            spr = playerAnimSprites[ctx.player.animType];
            x = (int)ctx.player.x;
            y = (int)ctx.player.y;
            frame = ctx.player.animCurFrame;
            drawSpriteFrame(spr, x, y, frame);
        }

        //Bus doors
        x = (int)ctx.bus.x;
        y = BUS_Y;
        frame = ctx.bus.rearDoorAnimFrame;
        drawSpriteFrame(SPR_BUS_DOOR, x + 64,  y + 16, frame);
        frame = ctx.bus.frontDoorAnimFrame;
        drawSpriteFrame(SPR_BUS_DOOR, x + 344, y + 16, frame);

        //Light poles (at most two are visible)
        drawSprite(SPR_POLE, ctx.poleX, POLE_Y);
        drawSprite(SPR_POLE, ctx.poleX + POLE_DISTANCE, POLE_Y);

        //Bus stop sign
        drawSprite(SPR_BUS_STOP_SIGN, ctx.busStopSignX, BUS_STOP_SIGN_Y);

        //Crate blocks
        for (i = 0; i < MAX_CRATE_BLOCKS; i++) {
            CrateBlock block = playCtx.crateBlocks[i];
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

            if (obj.type == OBJ_GEYSER) {
                int w = sprites[SPR_GEYSER * 4 + 2];
                int h = 265 - obj.y;
                if (h <= 0) h = 1;

                frame = ctx.geyserAnimFrame;

                drawSpritePart(SPR_GEYSER, obj.x, obj.y, frame * w, 0, w, h);

                //Geyser hole
                drawSprite(SPR_GEYSER_HOLE, obj.x, 263);
            } else {
                frame = 0;

                if (obj.type == OBJ_SPRING) {
                    frame = 5;

                    if (i == ctx.hitSpring.obj) {
                        frame = ctx.hitSpring.animFrame;
                    }
                }

                drawSpriteFrame(objSprites[obj.type], obj.x, obj.y, frame);
            }
        }

        //Player character (if not in the bus)
        if (!ctx.playerInBus && ctx.player.visible) {
            spr = playerAnimSprites[ctx.player.animType];
            x = (int)ctx.player.x;
            y = (int)ctx.player.y;
            frame = ctx.player.animCurFrame;
            drawSpriteFrame(spr, x, y, frame);
        }

        //Holes (foreground part)
        for (i = 0; i < MAX_HOLES; i++) {
            x = ctx.holes[i].x;
            y = BACKGROUND_DRAW_Y + 88;

            if (x != NONE) {
                if (ctx.holes[i].type == HOLE_DEEP) {
                    drawSprite(SPR_DEEP_HOLE_LEFT_FG, x, y);
                } else {
                    //Left
                    drawSprite(SPR_PASSAGEWAY_LEFT_FG, x, y);

                    //Right
                    x += (ctx.holes[i].width - 1) * LEVEL_BLOCK_SIZE;
                    drawSprite(SPR_PASSAGEWAY_RIGHT_FG, x, y);
                }
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
                frame = ctx.coinAnimFrame;
            } else if (obj.type == OBJ_COIN_GOLD) {
                frame = ctx.coinAnimFrame;
            } else {
                continue;
            }

            drawSpriteFrame(objSprites[obj.type], obj.x, obj.y, frame);
        }

        //Overhead sign bases
        for (i = 0; i < MAX_OBJS; i++) {
            Obj obj = ctx.objs[i];
            int h;

            if (obj.type == OBJ_OVERHEAD_SIGN) {
                spr = SPR_OVERHEAD_SIGN_BASE;
                x = obj.x + 28;
                y = obj.y + 28;
                h = 272 - y;
                drawSpritePart(spr, x, y, 0, 320 - h, 4, h);
            }
        }

        //Crack particles
        for (i = 0; i < MAX_CRACK_PARTICLES; i++) {
            x = (int)ctx.crackParticles[i].x;
            y = (int)ctx.crackParticles[i].y;
            frame = ctx.crackParticleAnimFrame;

            if (x != NONE) {
                drawSpriteFrame(SPR_CRACK_PARTICLE, x, y, frame);
            }
        }

        //Coin sparks
        for (i = 0; i < MAX_COIN_SPARKS; i++) {
            x = ctx.coinSparks[i].x;
            y = ctx.coinSparks[i].y;

            if (x != NONE) {
                boolean gold = ctx.coinSparks[i].gold;
                spr = gold ? SPR_COIN_SPARK_GOLD : SPR_COIN_SPARK_SILVER;
                frame = ctx.coinSparks[i].animFrame;
                drawSpriteFrame(spr, x, y, frame);
            }
        }

        //Reset draw offset
        drawOffsetX = 0;
        drawOffsetY = 0;
    }

    void drawHud() {
        drawSpriteStretch(SPR_BG_BLACK, 0, 0, SCREEN_WIDTH, 24);

        drawSprite(SPR_HUD_SCORE, 1, 1);
        drawDigits(1, 9, playCtx.score, 6);

        drawSprite(SPR_HUD_TIME, 225, 1);
        drawDigits(233, 9, playCtx.time, 2);
    }

    void drawTouchButtons(int inputState) {
        int x, y, spr;
        boolean dialogOpen = (dialogCtx.stackSize > 0);

        //Pause
        if (playCtx.canPause && !dialogOpen) {
            drawSprite(SPR_PAUSE, SCREEN_WIDTH - 24, 0);
        }

        //Left
        x = TOUCH_LEFT_X;
        y = projectionHeight - TOUCH_LEFT_OFFSET_Y;
        spr = SPR_TOUCH_LEFT;
        if (!dialogOpen && (inputState & INPUT_LEFT) > 0) {
            spr = SPR_TOUCH_LEFT_HELD;
        }
        drawSpriteTransparent(spr, x, y, TOUCH_OPACITY);

        //Right
        x = TOUCH_RIGHT_X;
        y = projectionHeight - TOUCH_RIGHT_OFFSET_Y;
        spr = SPR_TOUCH_RIGHT;
        if (!dialogOpen && (inputState & INPUT_RIGHT) > 0) {
            spr = SPR_TOUCH_RIGHT_HELD;
        }
        drawSpriteTransparent(spr, x, y, TOUCH_OPACITY);

        //Jump
        x = TOUCH_JUMP_X;
        y = projectionHeight - TOUCH_JUMP_OFFSET_Y;
        spr = SPR_TOUCH_JUMP;
        if (!dialogOpen && (inputState & INPUT_JUMP) > 0) {
            spr = SPR_TOUCH_JUMP_HELD;
        }
        drawSpriteTransparent(spr, x, y, TOUCH_OPACITY);
    }

    void drawDialog() {
        int selectedItem = dialogCtx.stack[dialogCtx.stackSize - 1].selectedItem;
        int i;

        if (dialogCtx.showFrame) {
            int y = ((projectionHeight / TILE_SIZE) - 10) / 2 * TILE_SIZE;
            drawSpriteStretch(SPR_BG_BLACK, 168, y, 144, 144);
        }

        if (dialogCtx.text.length() > 0) {
            int tx = ((projectionWidth  / TILE_SIZE) + dialogCtx.textOffsetX) / 2;
            int ty = ((projectionHeight / TILE_SIZE) + dialogCtx.textOffsetY) / 2;

            int x = tx * TILE_SIZE;
            int y = ty * TILE_SIZE;
            int w = dialogCtx.textWidth;
            int h = dialogCtx.textHeight;

            drawDialogBorder(x - 16, y - 16, w + 4, h + 4, false, false);
            drawText(dialogCtx.text, x, y);
        }

        for (i = 0; i < dialogCtx.numItems; i++) {
            if (selectedItem != i) {
                if (!dialogCtx.levelSelected) {
                    drawDialogItem(dialogCtx.items[i], false);
                }
            } else if (dialogCtx.selectedVisible) {
                drawDialogItem(dialogCtx.items[i], true);
            }
        }
    }

    void drawDialogItem(DialogItem item, boolean selected) {
        int spr;
        int i, j;
        int x, y;
        int w = item.width;
        int h = item.height;

        x = Dialogs.itemX(item);
        y = Dialogs.itemY(item, projectionHeight);

        drawDialogBorder(x, y, w, h, selected, item.disabled);

        spr = item.iconSprite;
        if (selected) spr++;

        x = Dialogs.itemX(item) + TILE_SIZE;
        y = Dialogs.itemY(item, projectionHeight) + TILE_SIZE;

        drawSprite(spr, x, y);
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

                drawSpriteFlip(spr, x + i * 8, y + j * 8, hflip, vflip);
            }
        }
    }

    //--------------------------------------------------------------------------

    void drawRegion(int dx, int dy, int dw, int dh,
                                            int sx, int sy, int sw, int sh,
                                            boolean hflip, boolean vflip) {

        dx -= drawOffsetX;
        dy -= drawOffsetY;

        //Skip drawing what is outside the screen
        if (dx < -dw || dx > projectionWidth) return;

        textureRegion.setRegion(sx, sy, sw, sh);

        //Negate vflip because, while the Y axis points upwards by default on
        //libGDX, we use the well known convention with the Y axis pointing
        //downwards
        textureRegion.flip(hflip, !vflip);

        spriteBatch.draw(textureRegion, dx, dy, dw, dh);
    }

    void drawSpritePart(int spr, int dx, int dy,
                                            int sx, int sy, int sw, int sh) {

        sx += sprites[spr * 4 + 0];
        sy += sprites[spr * 4 + 1];

        drawRegion(dx, dy, sw, sh, sx, sy, sw, sh, false, false);
    }

    void drawSpriteFrameFlip(int spr, int dx, int dy, int frame,
                                                boolean hflip, boolean vflip) {

        int w  = sprites[spr * 4 + 2];
        int h  = sprites[spr * 4 + 3];
        int sx = sprites[spr * 4 + 0] + (frame * w);
        int sy = sprites[spr * 4 + 1];

        drawRegion(dx, dy, w, h, sx, sy, w, h, hflip, vflip);
    }

    void drawSpriteFrame(int spr, int dx, int dy, int frame) {
        drawSpriteFrameFlip(spr, dx, dy, frame, false, false);
    }

    void drawSpriteFlip(int spr, int dx, int dy, boolean hflip, boolean vflip) {
        drawSpriteFrameFlip(spr, dx, dy, 0, hflip, vflip);
    }

    void drawSprite(int spr, int dx, int dy) {
        drawSpriteFrame(spr, dx, dy, 0);
    }

    void drawSpriteRepeat(int spr, int dx, int dy, int xrep, int yrep) {
        int w = sprites[spr * 4 + 2];
        int h = sprites[spr * 4 + 3];

        for (int i = 0; i < xrep; i++) {
            for (int j = 0; j < yrep; j++) {
                drawSprite(spr, dx + (i * w), dy + (j * h));
            }
        }
    }

    void drawSpriteStretch(int spr, int dx, int dy, int w, int h) {
        int sw = sprites[spr * 4 + 2];
        int sh = sprites[spr * 4 + 3];
        int sx = sprites[spr * 4 + 0];
        int sy = sprites[spr * 4 + 1];

        drawRegion(dx, dy, w, h, sx, sy, sw, sh, false, false);
    }

    void drawSpriteTransparent(int spr, int dx, int dy, float opacity) {
        Color c = spriteBatch.getColor();
        spriteBatch.setColor(c.r, c.g, c.b, opacity);
        drawSprite(spr, dx, dy);
        spriteBatch.setColor(c.r, c.g, c.b, 1); //Reset opacity
    }

    void drawDigits(int x, int y, int value, int width) {
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
            drawSpriteFrame(SPR_DIGITS, x, y, digitsTemp[i]);
            x += 8;
        }
    }

    void drawText(String text, int x, int y) {
        int i;
        int len = text.length();

        int dx = x;
        int dy = y;

        for (i = 0; i < len; i++) {
            int c, sx, sy;

            c = text.charAt(i);

            if (c == '\n') {
                dy += 8;
                dx = x;
            } else {
                c -= ' ';
                sx = (c % 16) * 8;
                sy = (c / 16) * 8;

                drawSpritePart(SPR_CHARSET, dx, dy, sx, sy, 8, 8);

                dx += 8;
            }
        }
    }
}

