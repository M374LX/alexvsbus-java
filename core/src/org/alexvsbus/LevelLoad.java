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

class LevelLoad {
    PlayCtx ctx;
    LineRead lineRead;

    boolean invalid;

    int xMax;
    int numObjs;
    int numCrateBlocks;
    int numGeysers, numGeyserCracks;
    int numSolids;
    int numHoles, numDeepHoles, numPassageways;
    int numRespawnPoints;
    int numTriggers, numCarTriggers;

    //--------------------------------------------------------------------------

    LevelLoad(PlayCtx ctx) {
        this.ctx = ctx;
        lineRead = new LineRead();
    }

    int load(String filename) {
        boolean noObjects = true;
        int x, y, w, h;
        int i, j;

        invalid = false;

        try {
            if (Gdx.files.internal(filename).length() > 4096) { //4 kB
                return LVLERR_TOO_LARGE;
            }

            lineRead.setData(Gdx.files.internal(filename).readString());
        } catch (Exception e) {
            return LVLERR_CANNOT_OPEN;
        }

        xMax = NONE;
        numObjs = 0;
        numCrateBlocks = 0;
        numGeysers = 0;
        numGeyserCracks = 0;
        numSolids = 0;
        numHoles = 0;
        numDeepHoles = 0;
        numPassageways = 0;
        numRespawnPoints = 0;
        numTriggers = 0;
        numCarTriggers = 0;

        ctx.levelSize = NONE;
        ctx.bgColor = NONE;
        ctx.bgm = NONE;

        x = SCREEN_WIDTH_LEVEL_BLOCKS;

        while (!lineRead.endOfData()) {
            String line = lineRead.getLine();
            String tokens[];
            int numTokens;
            int token1, token2, token3, token4;

            if (lineRead.isInvalid()) {
                return LVLERR_INVALID;
            }

            //Skip blank lines
            if (line.length() == 0) {
                continue;
            }

            tokens = line.split(" ");
            numTokens = tokens.length;

            if (numTokens < 2 || numTokens > 5) {
                return LVLERR_INVALID;
            }

            token1 = (numTokens >= 2) ? lineRead.toInt(tokens[1]) : NONE;
            token2 = (numTokens >= 3) ? lineRead.toInt(tokens[2]) : NONE;
            token3 = (numTokens >= 4) ? lineRead.toInt(tokens[3]) : NONE;
            token4 = (numTokens >= 5) ? lineRead.toInt(tokens[4]) : NONE;

            //Invalid integer token
            if (lineRead.isInvalid()) {
                return LVLERR_INVALID;
            }

            if (token1 == NONE) {
                return LVLERR_INVALID;
            }

            if (tokens[0].equals("level-size")) {
                //Error: level size redefinition
                if (ctx.levelSize != NONE) {
                    return LVLERR_INVALID;
                }

                //Error: size out of the allowed range
                if (token1 < 8 || token1 > 32) {
                    return LVLERR_INVALID;
                }

                //Just before the last screen
                xMax = (token1 - 1) * SCREEN_WIDTH_LEVEL_BLOCKS;

                ctx.levelSize = token1 * SCREEN_WIDTH;

                continue;
            } else if (tokens[0].equals("sky-color")) {
                //Error: sky color redefinition
                if (ctx.bgColor != NONE) {
                    return LVLERR_INVALID;
                }

                switch (token1) {
                    case 1:  ctx.bgColor = SPR_BG_SKY1; break;
                    case 2:  ctx.bgColor = SPR_BG_SKY2; break;
                    case 3:  ctx.bgColor = SPR_BG_SKY3; break;
                    default: return LVLERR_INVALID;
                }

                continue;
            } else if (tokens[0].equals("bgm")) {
                //Error: BGM redefinition
                if (ctx.bgm != NONE) {
                    return LVLERR_INVALID;
                }

                switch (token1) {
                    case 1:  ctx.bgm = BGM1; break;
                    case 2:  ctx.bgm = BGM2; break;
                    case 3:  ctx.bgm = BGM3; break;
                    default: return LVLERR_INVALID;
                }

                continue;
            }

            //Error: adding objects without defining the level size, sky color,
            //and BGM
            if (ctx.levelSize == NONE || ctx.bgColor == NONE || ctx.bgm == NONE) {
                return LVLERR_INVALID;
            }

            //The value of token1 is relative to the previous X position
            x += token1;

            if (tokens[0].equals("banana-peel")) {
                addObj(OBJ_BANANA_PEEL, x, token2, true);
            } else if (tokens[0].equals("car-blue")) {
                addObj(OBJ_PARKED_CAR_BLUE, x, NONE, false);
            } else if (tokens[0].equals("car-silver")) {
                addObj(OBJ_PARKED_CAR_SILVER, x, NONE, false);
            } else if (tokens[0].equals("car-yellow")) {
                addObj(OBJ_PARKED_CAR_YELLOW, x, NONE, false);
            } else if (tokens[0].equals("coin-silver")) {
                addObj(OBJ_COIN_SILVER, x, token2, true);
            } else if (tokens[0].equals("coin-gold")) {
                addObj(OBJ_COIN_GOLD, x, token2, true);
            } else if (tokens[0].equals("crates")) {
                addCrateBlock(x, token2, token3, token4);
            } else if (tokens[0].equals("geyser")) {
                addObj(OBJ_GEYSER, x, NONE, false);

                if (numGeysers >= MAX_GEYSERS) {
                    return LVLERR_INVALID;
                }

                ctx.geysers[numGeysers].obj = numObjs - 1;
                ctx.geysers[numGeysers].y = GEYSER_INITIAL_Y;
                ctx.geysers[numGeysers].movePattern = geyserMovePattern1;
                ctx.geysers[numGeysers].movePatternPos = 0;
                ctx.geysers[numGeysers].yvel = geyserMovePattern1[0];
                ctx.geysers[numGeysers].ydest = geyserMovePattern1[1];

                numGeysers++;
            } else if (tokens[0].equals("geyser-crack")) {
                addObj(OBJ_GEYSER_CRACK, x, NONE, false);
            } else if (tokens[0].equals("hydrant")) {
                addObj(OBJ_HYDRANT, x, NONE, false);
            } else if (tokens[0].equals("overhead-sign")) {
                addObj(OBJ_OVERHEAD_SIGN, x, token2, true);
            } else if (tokens[0].equals("rope")) {
                addObj(OBJ_ROPE_HORIZONTAL, x, NONE, false);
                addObj(OBJ_ROPE_VERTICAL, x, NONE, false);
            } else if (tokens[0].equals("spring")) {
                addObj(OBJ_SPRING, x, token2, true);
            } else if (tokens[0].equals("truck")) {
                addObj(OBJ_PARKED_TRUCK, x, NONE, false);
            } else if (tokens[0].equals("trigger-car-blue")) {
                addTrigger(x, CAR_BLUE);
            } else if (tokens[0].equals("trigger-car-silver")) {
                addTrigger(x, CAR_SILVER);
            } else if (tokens[0].equals("trigger-car-yellow")) {
                addTrigger(x, CAR_YELLOW);
            } else if (tokens[0].equals("trigger-hen")) {
                addTrigger(x, TRIGGER_HEN);
            } else if (tokens[0].equals("respawn-point")) {
                addRespawnPoint(x, token2);
            } else if (tokens[0].equals("deep-hole")) {
                addHole(HOLE_DEEP, x, token2);
            } else if (tokens[0].equals("passageway")) {
                addHole(HOLE_PASSAGEWAY_EXIT_CLOSED, x, token2);

                //Pushable crate over passageway entry
                addObj(OBJ_CRATE_PUSHABLE, x, NONE, false);
                ctx.pushableCrates[numPassageways - 1].obj = numObjs - 1;
            } else {
                //Error: invalid object type
                return LVLERR_INVALID;
            }

            if (invalid) {
                return LVLERR_INVALID;
            }

            noObjects = false;
        }

        if (noObjects) {
            return LVLERR_INVALID;
        }

        //Error: running out of geysers due to geyser cracks
        if (numGeysers + numGeyserCracks > MAX_GEYSERS) {
            return LVLERR_INVALID;
        }

        //Error: running out of positions in ctx.objs[] due to banana peels
        //thrown by triggered cars
        if (numObjs + numCarTriggers > MAX_OBJS) {
            return LVLERR_INVALID;
        }

        //Error: the number of respawn points is not the same as the number of
        //deep holes
        if (numRespawnPoints != numDeepHoles) {
            return LVLERR_INVALID;
        }

        //Ensure every respawn point is close enough to the corresponding deep
        //hole but not placed after it or over another deep hole
        for (i = 0, j = 0; i < numHoles; i++) {
            int hx = ctx.holes[i].x;
            int rx = ctx.respawnPoints[j].x;

            //Skip passageways
            if (ctx.holes[i].type != HOLE_DEEP) {
                continue;
            }

            if (rx >= hx || rx < hx - 4) {
                return LVLERR_INVALID;
            }

            if (i > 1) {
                int prevHole = i - 1;

                //Find previous deep hole (skip passageways)
                while (prevHole >= 0 && ctx.holes[prevHole].type != HOLE_DEEP) {
                    prevHole--;
                }

                if (prevHole >= 0) {
                    //Right side of previous deep hole
                    hx = ctx.holes[prevHole].x + ctx.holes[prevHole].width - 2;

                    if (rx <= hx) {
                        return LVLERR_INVALID;
                    }
                }
            }

            j++;
        }

        //First solid for the floor
        addSolid(SOL_FULL, 0, FLOOR_Y, ctx.levelSize, 80);

        //Convert positions from level blocks to pixels for deep holes and
        //passageways and adjust solids around them
        for (i = 0; i < numHoles; i++) {
            int prevObRight, obLeft, obWidth;
            boolean isDeep = (ctx.holes[i].type == HOLE_DEEP);

            x = ctx.holes[i].x;
            w = ctx.holes[i].width;

            prevObRight = x * LEVEL_BLOCK_SIZE;
            obLeft = (x + w) * LEVEL_BLOCK_SIZE;
            obWidth = ctx.levelSize - obLeft;

            if (isDeep) { //Deep hole
                prevObRight += 12;
                obLeft -= 8;
            } else { //Passageway
                prevObRight += 6;
            }

            //Adjust the previous floor solid so that it does not cover the
            //hole
            ctx.solids[i].right = prevObRight;

            //Add solid for the floor after the hole
            addSolid(SOL_FULL, obLeft, FLOOR_Y, obWidth, 80);

            //Too many solids
            if (invalid) {
                return LVLERR_INVALID;
            }

            //Convert position
            ctx.holes[i].x *= LEVEL_BLOCK_SIZE;
        }

        //Convert positions from level blocks to pixels and add solids for
        //objects in ctx.objs[]
        for (i = 0; i < numObjs; i++) {
            x = ctx.objs[i].x * LEVEL_BLOCK_SIZE;
            y = ctx.objs[i].y;
            if (y != NONE) {
                y *= LEVEL_BLOCK_SIZE;
            }

            switch (ctx.objs[i].type) {
                case OBJ_BANANA_PEEL:
                    x += 16;
                    y -= 8;
                    break;

                case OBJ_PARKED_CAR_BLUE:
                case OBJ_PARKED_CAR_SILVER:
                case OBJ_PARKED_CAR_YELLOW:
                    y = PARKED_CAR_Y;
                    addSolid(SOL_FULL, x + 1, y + 18, 26, 4);
                    addSolid(SOL_SLOPE_UP, x + 27, y + 2, 13, 15);
                    addSolid(SOL_VERTICAL, x + 46, y + 2, 22, 4);
                    addSolid(SOL_SLOPE_DOWN, x + 72, y + 2, 20, 18);
                    addSolid(SOL_KEEP_ON_TOP, x + 100, y + 20, 4, 4);
                    addSolid(SOL_FULL, x + 104, y + 22, 23, 4);
                    break;

                case OBJ_COIN_SILVER:
                case OBJ_COIN_GOLD:
                    x += 8;
                    break;

                case OBJ_CRATE_PUSHABLE:
                    y = PUSHABLE_CRATE_Y;
                    break;

                case OBJ_GEYSER:
                    y = GEYSER_INITIAL_Y;
                    break;

                case OBJ_GEYSER_CRACK:
                    y = GEYSER_CRACK_Y;
                    break;

                case OBJ_HYDRANT:
                    y = HYDRANT_Y;
                    addSolid(SOL_FULL, x + 4, y + 8, 8, 4);
                    break;

                case OBJ_OVERHEAD_SIGN:
                    y -= 8;
                    addSolid(SOL_FULL, x + 12, y, 4, 32);
                    break;

                case OBJ_ROPE_HORIZONTAL:
                    x += 10;
                    y = ROPE_Y;
                    break;

                case OBJ_ROPE_VERTICAL:
                    x += 32;
                    y = ROPE_Y + 5;
                    break;

                case OBJ_SPRING:
                    x += 8;
                    y += 8;
                    break;

                case OBJ_PARKED_TRUCK:
                    y = PARKED_TRUCK_Y;
                    addSolid(SOL_FULL, x, y + 4, 224, 96);
                    addSolid(SOL_FULL, x + 224, y + 23, 55, 80);
                    break;
            }

            //Too many solids
            if (invalid) {
                return LVLERR_INVALID;
            }

            //Apply converted position
            ctx.objs[i].x = x;
            ctx.objs[i].y = y;
        }

        //Set properties for ctx.pushableCrates[]
        for (i = 0; i < numPassageways; i++) {
            int obj = ctx.pushableCrates[i].obj;
            x = ctx.objs[obj].x;
            ctx.pushableCrates[i].x = x;
            ctx.pushableCrates[i].xmax = x + LEVEL_BLOCK_SIZE;
        }

        //Convert positions from level blocks to pixels and add solids for crate
        //blocks
        for (i = 0; i < numCrateBlocks; i++) {
            x = ctx.crateBlocks[i].x * LEVEL_BLOCK_SIZE;
            y = ctx.crateBlocks[i].y * LEVEL_BLOCK_SIZE;
            w = ctx.crateBlocks[i].width * LEVEL_BLOCK_SIZE;
            h = ctx.crateBlocks[i].height * LEVEL_BLOCK_SIZE;
            addSolid(SOL_FULL, x, y, w, h);

            //Too many solids
            if (invalid) {
                return LVLERR_INVALID;
            }

            //Apply converted position
            ctx.crateBlocks[i].x = x;
            ctx.crateBlocks[i].y = y;
        }

        //Convert respawn point positions from level blocks to pixels
        for (i = 0; i < numRespawnPoints; i++) {
            ctx.respawnPoints[i].x *= LEVEL_BLOCK_SIZE;
            ctx.respawnPoints[i].x += 3;

            ctx.respawnPoints[i].y *= LEVEL_BLOCK_SIZE;
            ctx.respawnPoints[i].y -= 12;
        }

        //Convert trigger positions from level blocks to pixels
        for (i = 0; i < numTriggers; i++) {
            ctx.triggers[i].x *= LEVEL_BLOCK_SIZE;
        }

        //Add solids for passageways and pushable crates over passageway entries
        //
        //There is exactly one pushable crate for each passageway
        for (i = 0, j = 0; i < numHoles; i++) {
            int sol;

            if (ctx.holes[i].type == HOLE_DEEP) {
                continue;
            }

            x = ctx.holes[i].x;
            w = ctx.holes[i].width * LEVEL_BLOCK_SIZE;

            //Bottom solid
            addSolid(SOL_FULL, x, PASSAGEWAY_BOTTOM_Y, w, 4);

            //Top solid
            addSolid(SOL_FULL, x + LEVEL_BLOCK_SIZE, FLOOR_Y, w - 46, 13);

            //Entry and exit solids, which prevent the player character from
            //leaving the passageway through the entry or entering it through
            //the exit
            addSolid(SOL_PASSAGEWAY_ENTRY, x + 6, FLOOR_Y, 18, 13);
            addSolid(SOL_PASSAGEWAY_EXIT, x + w - 22, FLOOR_Y, 22, 13);

            ctx.holes[i].x = x;

            //Pushable crate solid
            x = (int)ctx.pushableCrates[j].x;
            y = PUSHABLE_CRATE_Y;
            sol = addSolid(SOL_FULL, x, y, CRATE_WIDTH, CRATE_HEIGHT);
            ctx.pushableCrates[j].solid = sol;
            j++;

            //Too many solids
            if (invalid) {
                return LVLERR_INVALID;
            }
        }

        return LVLERR_NONE;
    }

    //--------------------------------------------------------------------------

    void addObj(int type, int x, int y, boolean useY) {
        int i;

        if (numObjs >= MAX_OBJS
            || y > 15
            || (useY && y == NONE)
            || x > xMax) {

            invalid = true;
            return;
        }

        //Check object repetition
        for (i = 0; i < numObjs; i++) {
            Obj obj = ctx.objs[i];

            if (obj.type == type && obj.x == x && obj.y == y) {
                invalid = true;
                return;
            }
        }

        ctx.objs[numObjs].type = type;
        ctx.objs[numObjs].x = x;
        ctx.objs[numObjs].y = y;

        numObjs++;
    }

    void addCrateBlock(int x, int y, int w, int h) {
        int x2 = x + w - 1;
        int y2 = y + h - 1;
        int i;

        if (numCrateBlocks >= MAX_CRATE_BLOCKS
            || x > xMax - 2 || y > 15
            || w < 1 || w > 8
            || h < 1 || h > 8) {

            invalid = true;
            return;
        }

        if (x2 > xMax - 2) {
            //Error: crate block width extends beyond or too close to level's
            //right boundary
            invalid = true;
            return;
        }

        //Check crate block repetition or overlap
        for (i = 0; i < numCrateBlocks; i++) {
            CrateBlock ct = ctx.crateBlocks[i];
            int cx2 = ct.x + ct.width - 1;
            int cy2 = ct.y + ct.height - 1;

            if (ct.x == x && ct.y == y) {
                invalid = true;
                return;
            }

            if (x <= cx2 && y2 >= ct.y && y <= cy2) {
                invalid = true;
                return;
            }
        }

        ctx.crateBlocks[numCrateBlocks].x = x;
        ctx.crateBlocks[numCrateBlocks].y = y;
        ctx.crateBlocks[numCrateBlocks].width = w;
        ctx.crateBlocks[numCrateBlocks].height = h;

        numCrateBlocks++;
    }

    void addHole(int type, int x, int w) {
        int x2 = x + w - 1;
        int maxWidth = (type == HOLE_DEEP) ? 16 : 32;
        int i;

        if (numHoles >= MAX_HOLES
            || x > xMax - 2
            || w < 2 || w > maxWidth) {

            invalid = true;
            return;
        }

        if (x2 > xMax - 2) {
            //Error: hole width extends beyond or too close to level's right
            //boundary
            invalid = true;
            return;
        }

        //Check hole repetition or overlap
        for (i = 0; i < numHoles; i++) {
            int hx1 = ctx.holes[i].x;
            int hx2 = hx1 + ctx.holes[i].width - 1;

            if (hx1 == x || x <= hx2) {
                invalid = true;
                return;
            }
        }

        ctx.holes[numHoles].type = type;
        ctx.holes[numHoles].x = x;
        ctx.holes[numHoles].width = w;

        if (type == HOLE_DEEP) {
            numDeepHoles++;
        } else {
            numPassageways++;

            //Too many passageways
            if (numPassageways > MAX_PASSAGEWAYS) {
                invalid = true;
                return;
            }
        }

        numHoles++;
    }

    void addRespawnPoint(int x, int y) {
        int i;

        if (numRespawnPoints >= MAX_RESPAWN_POINTS
            || x > xMax || y > 16) {

            invalid = true;
            return;
        }

        //Check respawn point repetition
        for (i = 0; i < numRespawnPoints; i++) {
            RespawnPoint rp = ctx.respawnPoints[i];

            if (rp.x == x && rp.y == y) {
                invalid = true;
                return;
            }
        }

        ctx.respawnPoints[numRespawnPoints].x = x;
        ctx.respawnPoints[numRespawnPoints].y = y;

        numRespawnPoints++;
    }

    void addTrigger(int x, int what) {
        int i;

        if (numTriggers >= MAX_TRIGGERS || x > xMax - 20) {
            invalid = true;
            return;
        }

        //Check trigger repetition or excessive proximity
        for (i = 0; i < numTriggers; i++) {
            int tx = ctx.triggers[i].x;

            if (tx == x || tx > x - 28) {
                invalid = true;
                return;
            }
        }

        ctx.triggers[numTriggers].x = x;
        ctx.triggers[numTriggers].what = what;

        numTriggers++;

        if (what != TRIGGER_HEN) {
            numCarTriggers++;
        }
    }

    int addSolid(int type, int x, int y, int width, int height) {
        if (numSolids >= MAX_SOLIDS) {
            invalid = true;
            return -1;
        }

        ctx.solids[numSolids].type = type;
        ctx.solids[numSolids].left = x;
        ctx.solids[numSolids].right = x + width;
        ctx.solids[numSolids].top = y;
        ctx.solids[numSolids].bottom = y + height;
        numSolids++;

        return numSolids - 1;
    }
}

