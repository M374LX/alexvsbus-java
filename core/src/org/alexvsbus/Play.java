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

class Play {
    Audio audio;
    PlayCtx ctx; //Gameplay context

    float dt; //Delta time (time elapsed since the previous frame)

    boolean ignoreUserInput;
    boolean inputLeft,  oldInputLeft;
    boolean inputRight, oldInputRight;
    boolean inputJump,  oldInputJump;
    int jumpPressY;

    //--------------------------------------------------------------------------

    Play(Audio audio) {
        this.audio = audio;
    }

    PlayCtx newCtx() {
        int i;

        ctx = new PlayCtx();

        ctx.playing = false;
        ctx.canPause = false;
        ctx.difficulty = DIFFICULTY_NORMAL;
        ctx.levelNum = -1;

        ctx.cam = new Camera();
        ctx.player = new Player();
        ctx.bus = new Bus();
        ctx.grabbedRope = new GrabbedRope();
        ctx.hitSpring = new HitSpring();
        ctx.thrownPeel = new MovingPeel();
        ctx.slipPeel = new MovingPeel();
        ctx.passingCar = new PassingCar();
        ctx.hen = new Hen();

        ctx.objs = new Obj[MAX_OBJS];
        for (i = 0; i < MAX_OBJS; i++) {
            ctx.objs[i] = new Obj();
        }

        ctx.crateBlocks = new CrateBlock[MAX_CRATE_BLOCKS];
        for (i = 0; i < MAX_CRATE_BLOCKS; i++) {
            ctx.crateBlocks[i] = new CrateBlock();
        }

        ctx.geysers = new Geyser[MAX_GEYSERS];
        for (i = 0; i < MAX_GEYSERS; i++) {
            ctx.geysers[i] = new Geyser();
        }

        ctx.pushableCrates = new PushableCrate[MAX_PUSHABLE_CRATES];
        for (i = 0; i < MAX_PUSHABLE_CRATES; i++) {
            ctx.pushableCrates[i] = new PushableCrate();
        }

        ctx.cutsceneObjects = new CutsceneObject[MAX_CUTSCENE_OBJECTS];
        for (i = 0; i < MAX_CUTSCENE_OBJECTS; i++) {
            ctx.cutsceneObjects[i] = new CutsceneObject();
        }

        ctx.solids = new Solid[MAX_SOLIDS];
        for (i = 0; i < MAX_SOLIDS; i++) {
            ctx.solids[i] = new Solid();
        }

        ctx.holes = new Hole[MAX_HOLES];
        for (i = 0; i < MAX_HOLES; i++) {
            ctx.holes[i] = new Hole();
        }

        ctx.respawnPoints = new RespawnPoint[MAX_RESPAWN_POINTS];
        for (i = 0; i < MAX_RESPAWN_POINTS; i++) {
            ctx.respawnPoints[i] = new RespawnPoint();
        }

        ctx.triggers = new Trigger[MAX_TRIGGERS];
        for (i = 0; i < MAX_TRIGGERS; i++) {
            ctx.triggers[i] = new Trigger();
        }

        ctx.coinSparks = new CoinSpark[MAX_COIN_SPARKS];
        for (i = 0; i < MAX_COIN_SPARKS; i++) {
            ctx.coinSparks[i] = new CoinSpark();
        }

        ctx.crackParticles = new CrackParticle[MAX_CRACK_PARTICLES];
        for (i = 0; i < MAX_CRACK_PARTICLES; i++) {
            ctx.crackParticles[i] = new CrackParticle();
        }

        return ctx;
    }

    void clear() {
        int i;

        dt = 0;

        ignoreUserInput = true;
        inputLeft = false;
        inputRight = false;
        inputJump = false;
        jumpPressY = -9999;

        ctx.canPause = false;
        ctx.time = 90;
        ctx.timeRunning = false;
        ctx.timeUp = false;
        ctx.goalReached = false;
        ctx.countingScore = false;

        ctx.busStopSignX = 440;

        ctx.cratePushRemaining = 0.75f;

        ctx.cam.x = 0;
        ctx.cam.y = 0;
        ctx.cam.xvel = 0;
        ctx.cam.yvel = 0;

        ctx.player.x = 96;
        ctx.player.oldx = 96;
        ctx.player.y = 200;
        ctx.player.oldy = 200;
        ctx.player.xvel = 0;
        ctx.player.yvel = 0;
        ctx.player.fell = false;
        ctx.player.onFloor = false;
        ctx.player.animType = PLAYER_ANIM_STAND;
        ctx.player.oldAnimType = PLAYER_ANIM_STAND;
        ctx.player.state = PLAYER_STATE_NORMAL;
        ctx.player.oldState = NONE;
        handlePlayerStateChange();

        ctx.bus.x = 24;
        ctx.bus.xvel = 0;
        ctx.bus.acc = 0;
        ctx.bus.wheelAnimFrame = 0;
        ctx.bus.wheelAnimDelay = 0;
        ctx.bus.rearDoorAnimFrame = 3;
        ctx.bus.rearDoorAnimDelta = 0;
        ctx.bus.frontDoorAnimFrame = 0;
        ctx.bus.frontDoorAnimDelta = 0;

        ctx.grabbedRope.obj = NONE;
        ctx.hitSpring.obj = NONE;
        ctx.slipPeel.obj = NONE;
        ctx.thrownPeel.obj = NONE;

        ctx.passingCar.x = NONE;
        ctx.hen.x = NONE;

        for (i = 0; i < MAX_OBJS; i++) {
            ctx.objs[i].type = NONE;
        }

        for (i = 0; i < MAX_CRATE_BLOCKS; i++) {
            ctx.crateBlocks[i].x = NONE;
        }

        for (i = 0; i < MAX_GEYSERS; i++) {
            ctx.geysers[i].obj = NONE;
        }

        for (i = 0; i < MAX_PUSHABLE_CRATES; i++) {
            ctx.pushableCrates[i].obj = NONE;
            ctx.pushableCrates[i].pushed = false;
        }

        for (i = 0; i < MAX_CUTSCENE_OBJECTS; i++) {
            ctx.cutsceneObjects[i].sprite = NONE;
            ctx.cutsceneObjects[i].x = 0;
            ctx.cutsceneObjects[i].y = 0;
            ctx.cutsceneObjects[i].xvel = 0;
            ctx.cutsceneObjects[i].yvel = 0;
            ctx.cutsceneObjects[i].acc = 0;
            ctx.cutsceneObjects[i].grav = 0;
            ctx.cutsceneObjects[i].inBus = false;
            ctx.cutsceneObjects[i].animCurFrame = 0;
            ctx.cutsceneObjects[i].animNumFrames = 1;
            ctx.cutsceneObjects[i].animLoop = false;
        }

        for (i = 0; i < MAX_SOLIDS; i++) {
            ctx.solids[i].type = NONE;
        }

        for (i = 0; i < MAX_HOLES; i++) {
            ctx.holes[i].x = NONE;
        }

        ctx.curPassageway = null;

        for (i = 0; i < MAX_RESPAWN_POINTS; i++) {
            ctx.respawnPoints[i].x = NONE;
        }

        for (i = 0; i < MAX_TRIGGERS; i++) {
            ctx.triggers[i].x = NONE;
        }

        for (i = 0; i < MAX_COIN_SPARKS; i++) {
            ctx.coinSparks[i].x = NONE;
        }

        for (i = 0; i < MAX_CRACK_PARTICLES; i++) {
            ctx.crackParticles[i].x = NONE;
        }

        ctx.nextCoinSpark = 0;
        ctx.nextCrackParticle = 0;

        ctx.geyserAnimFrame = 0;
        ctx.geyserAnimDelay = 0.1f;

        ctx.coinAnimFrame = 0;
        ctx.coinAnimDelay = 0.1f;

        ctx.crackParticleAnimFrame = 0;
        ctx.crackParticleAnimDelay = 0.1f;

        ctx.sequenceStep = SEQ_INITIAL_DELAY;
        ctx.sequenceDelay = 1;
        ctx.wipeToBlack = false;
        ctx.wipeFromBlack = false;
    }

    void setInput(int inputState) {
        if (ignoreUserInput) return;

        oldInputLeft  = inputLeft;
        oldInputRight = inputRight;
        oldInputJump  = inputJump;

        inputLeft  = (inputState & INPUT_LEFT)  > 0;
        inputRight = (inputState & INPUT_RIGHT) > 0;
        inputJump  = (inputState & INPUT_JUMP)  > 0;

        if (inputJump && !oldInputJump) {
            jumpPressY = (int)ctx.player.y;
        }
    }

    void update(float dt) {
        this.dt = dt;

        beginUpdate();
        updateRemainingTime();
        updateScoreCount();
        shiftBusToEnd();
        moveObjects();
        handleCarThrownPeel();
        movePlayer();
        handleSolids();
        handlePassageways();
        handlePlayerInteractions();
        handleTriggers();
        doPlayerStateSpecifics();
        handleFallSound();
        handleRespawn();
        handlePlayerStateChange();
        moveCamera();
        keepPlayerWithinLimits();
        handlePlayerAnimationChange();
        updateAnimations();
        findLightPolePosition();
        findBackgroundOffset();
        updateSequence();
    }

    //--------------------------------------------------------------------------

    void addCrackParticles(int x, int y) {
        ctx.crackParticles[ctx.nextCrackParticle].x = x;
        ctx.crackParticles[ctx.nextCrackParticle].y = y;
        ctx.crackParticles[ctx.nextCrackParticle].xvel = -15;
        ctx.crackParticles[ctx.nextCrackParticle].yvel = -120;
        ctx.crackParticles[ctx.nextCrackParticle].grav =  200;
        ctx.nextCrackParticle++;
        ctx.nextCrackParticle %= MAX_CRACK_PARTICLES;

        ctx.crackParticles[ctx.nextCrackParticle].x = x;
        ctx.crackParticles[ctx.nextCrackParticle].y = y;
        ctx.crackParticles[ctx.nextCrackParticle].xvel = -5;
        ctx.crackParticles[ctx.nextCrackParticle].yvel = -190;
        ctx.crackParticles[ctx.nextCrackParticle].grav =  200;
        ctx.nextCrackParticle++;
        ctx.nextCrackParticle %= MAX_CRACK_PARTICLES;

        ctx.crackParticles[ctx.nextCrackParticle].x = x;
        ctx.crackParticles[ctx.nextCrackParticle].y = y;
        ctx.crackParticles[ctx.nextCrackParticle].xvel =  15;
        ctx.crackParticles[ctx.nextCrackParticle].yvel = -120;
        ctx.crackParticles[ctx.nextCrackParticle].grav =  200;
        ctx.nextCrackParticle++;
        ctx.nextCrackParticle %= MAX_CRACK_PARTICLES;

        ctx.crackParticles[ctx.nextCrackParticle].x = x;
        ctx.crackParticles[ctx.nextCrackParticle].y = y;
        ctx.crackParticles[ctx.nextCrackParticle].xvel =  5;
        ctx.crackParticles[ctx.nextCrackParticle].yvel = -190;
        ctx.crackParticles[ctx.nextCrackParticle].grav =  200;
        ctx.nextCrackParticle++;
        ctx.nextCrackParticle %= MAX_CRACK_PARTICLES;
    }

    void showPlayerInBus() {
        CutsceneObject cutscenePlayer = ctx.cutsceneObjects[0];

        cutscenePlayer.sprite = SPR_PLAYER_STAND;
        cutscenePlayer.inBus = true;
        cutscenePlayer.x = 342;
        cutscenePlayer.y = BUS_Y + 36;
        cutscenePlayer.animCurFrame = 0;
        cutscenePlayer.animNumFrames = 1;

        ctx.player.state = PLAYER_STATE_INACTIVE;
        ctx.player.visible = false;
    }

    void startScoreCount() {
        ctx.countingScore = true;
        ctx.timeDelay = 0.1f;
    }

    //--------------------------------------------------------------------------

    //Begins the update
    void beginUpdate() {
        Player pl = ctx.player;

        pl.oldx = pl.x;
        pl.oldy = pl.y;
        pl.oldState = pl.state;
        pl.oldAnimType = pl.animType;
        pl.onFloor = false;
    }

    //Updates the remaining time and acts if the time has run out
    void updateRemainingTime() {
        if (!ctx.timeRunning) return;

        ctx.timeDelay -= dt;
        if (ctx.timeDelay <= 0) {
            ctx.timeDelay = 1;
            ctx.time--;

            if (ctx.time <= 10 && ctx.time >= 0) {
                audio.playSfx(SFX_TIME);
            }

            if (ctx.time < 0) {
                ctx.time = 0;
                ctx.timeRunning = false;
                ctx.timeUp = true;
            }
        }
    }

    //Does the score counting from the remaining time after the level's goal is
    //reached
    void updateScoreCount() {
        if (!ctx.countingScore) return;

        ctx.timeDelay -= dt;
        if (ctx.timeDelay <= 0) {
            ctx.timeDelay = 0.1f;

            if (ctx.time > 0) {
                ctx.time--;
                ctx.score += 10;
                audio.playSfx(SFX_SCORE);
            }

            if (ctx.time <= 0) {
                ctx.time = 0;
                ctx.countingScore = false;
            }
        }
    }

    //Shifts the position of the bus and the bus stop sign from the start to
    //the end of the level
    void shiftBusToEnd() {
        //Nothing to do if the bus position has been already shifted
        if (ctx.bus.x > SCREEN_WIDTH) return;

        if (ctx.levelNum == 1 || ctx.cam.x > SCREEN_WIDTH * 2) {
            ctx.bus.x = ctx.levelSize - 456;
            ctx.busStopSignX = ctx.levelSize - 40;

            //Set rear door closed
            ctx.bus.rearDoorAnimFrame = 0;
            ctx.bus.rearDoorAnimDelta = 0;

            //Set front door open
            ctx.bus.frontDoorAnimFrame = 3;
            ctx.bus.frontDoorAnimDelta = 0;

            //Next bus route sign
            if (ctx.lastLevel) {
                ctx.bus.routeSign = 4; //Finish (checkered flag) sign
            } else {
                ctx.bus.routeSign++;
            }
        }
    }

    //Updates the position of most game objects, not including the player
    //character and the camera
    void moveObjects() {
        MovingPeel peel;
        int i;

        //Bus
        ctx.bus.xvel += ctx.bus.acc * dt;
        ctx.bus.x += ctx.bus.xvel * dt;

        //Thrown peel
        peel = ctx.thrownPeel;
        if (peel.obj != NONE) {
            Obj obj = ctx.objs[peel.obj];

            peel.yvel += peel.grav * dt;
            peel.x += peel.xvel * dt;
            peel.y += peel.yvel * dt;
            if (peel.y >= 256) {
                //Stop the peel when it hits the floor
                obj.type = OBJ_BANANA_PEEL;
                peel.x = peel.xmax;
                peel.y = 256;
                peel.obj = NONE;
            }

            obj.x = (int)peel.x;
            obj.y = (int)peel.y;
        }

        //Slipped peel
        peel = ctx.slipPeel;
        if (peel.obj != NONE) {
            Obj obj = ctx.objs[peel.obj];

            peel.yvel += peel.grav * dt;
            peel.x += peel.xvel * dt;
            peel.y += peel.yvel * dt;
            if (peel.y >= 400) {
                //Deactivate the peel when it goes too far down
                obj.type = NONE;
                peel.obj = NONE;
            }

            obj.x = (int)peel.x;
            obj.y = (int)peel.y;
        }

        //Geysers
        for (i = 0; i < MAX_GEYSERS; i++) {
            Geyser gsr = ctx.geysers[i];

            float y = gsr.y;
            float yvel = gsr.yvel;
            float ydest = gsr.ydest;

            //Ignore inexistent geysers
            if (gsr.obj == NONE) continue;

            y += yvel * dt;

            //If the geyser reaches its destination Y position
            if ((yvel < 0 && y <= ydest) || (yvel > 0 && y >= ydest)) {
                y = ydest;

                //Advance within the movement pattern and loop if its end is
                //reached
                gsr.movePatternPos += 2;
                if (gsr.movePattern[gsr.movePatternPos] == 0) {
                    gsr.movePatternPos = 0;
                }

                gsr.yvel = gsr.movePattern[gsr.movePatternPos];
                gsr.ydest = gsr.movePattern[gsr.movePatternPos + 1];
            }

            gsr.y = y;
            ctx.objs[gsr.obj].y = (int)y;
        }

        //Grabbed rope
        if (ctx.grabbedRope.obj != NONE) {
            Obj obj = ctx.objs[ctx.grabbedRope.obj];

            ctx.grabbedRope.x += ctx.grabbedRope.xvel * dt;

            if (ctx.grabbedRope.x >= ctx.grabbedRope.xmax) {
                ctx.grabbedRope.x = ctx.grabbedRope.xmax;
                ctx.grabbedRope.xvel = -192;
            } else if (ctx.grabbedRope.x <= ctx.grabbedRope.xmin) {
                ctx.grabbedRope.x = ctx.grabbedRope.xmin;
                ctx.grabbedRope.obj = NONE;
            }

            obj.x = (int)ctx.grabbedRope.x;
        }

        //Pushable crate
        for (i = 0; i < MAX_PUSHABLE_CRATES; i++) {
            PushableCrate crate = ctx.pushableCrates[i];

            if (crate.obj != NONE && crate.pushed) {
                Solid sol = ctx.solids[crate.solid];

                crate.x += 72 * dt;
                if (crate.x >= crate.xmax) crate.x = crate.xmax;

                ctx.objs[crate.obj].x = (int)crate.x;
                sol.left = (int)crate.x;
                sol.right = (int)crate.x + 24;
            }
        }

        //Passing car
        if (ctx.passingCar.x != NONE) {
            ctx.passingCar.x += 1200 * dt;

            if (ctx.passingCar.x >= ctx.cam.x + SCREEN_WIDTH + 64) {
                ctx.passingCar.x = NONE;
            }
        }

        //Hen
        if (ctx.hen.x != NONE) {
            ctx.hen.x += 350 * dt;

            if (ctx.hen.x > ctx.cam.x + SCREEN_WIDTH + 64) {
                ctx.hen.x = NONE;
            }
        }

        //Crack particles
        for (i = 0; i < MAX_CRACK_PARTICLES; i++) {
            CrackParticle ptcl = ctx.crackParticles[i];

            //Ignore inexistent particles
            if (ptcl.x == NONE) continue;

            ptcl.yvel += ptcl.grav * dt;
            ptcl.x += ptcl.xvel * dt;
            ptcl.y += ptcl.yvel * dt;

            if (ptcl.y > 400) {
                ptcl.x = NONE;
            }
        }

        //Cutscene objects
        for (i = 0; i < MAX_CUTSCENE_OBJECTS; i++) {
            CutsceneObject cobj = ctx.cutsceneObjects[i];

            //Ignore inexistent cutscene objects
            if (cobj.sprite == NONE) continue;

            cobj.xvel += cobj.acc * dt;
            cobj.yvel += cobj.grav * dt;
            cobj.x += cobj.xvel * dt;
            cobj.y += cobj.yvel * dt;
        }
    }

    //Acts if the passing car has reached the X position at which it throws a
    //banana peel
    void handleCarThrownPeel() {
        if (ctx.passingCar.x == NONE || ctx.passingCar.threwPeel) return;

        if (ctx.passingCar.x >= ctx.passingCar.peelThrowX) {
            for (int i = 0; i < MAX_OBJS; i++) {
                if (ctx.objs[i].type == NONE) {
                    ctx.objs[i].type = OBJ_BANANA_PEEL_MOVING;

                    ctx.thrownPeel.obj = i;
                    ctx.thrownPeel.x = ctx.passingCar.peelThrowX + 90;
                    ctx.thrownPeel.y = 200;
                    ctx.thrownPeel.xmax = ctx.thrownPeel.x + 70;
                    ctx.thrownPeel.xvel = 140;
                    ctx.thrownPeel.yvel = -10;
                    ctx.thrownPeel.grav = 500;

                    ctx.passingCar.threwPeel = true;

                    break;
                }
            }
        }
    }

    //Updates the position of the player character (without taking solids into
    //account, as solids are handled by handleSolids())
    void movePlayer() {
        Player pl = ctx.player;

        if (pl.state == PLAYER_STATE_INACTIVE) return;

        //Deceleration and acceleration
        if (pl.xvel > 0 && pl.acc <= 0) {
            pl.xvel -= pl.dec * dt;
            if (pl.xvel <= 0) pl.xvel = 0;
        } else if (pl.xvel < 0 && pl.acc >= 0) {
            pl.xvel += pl.dec * dt;
            if (pl.xvel >= 0) pl.xvel = 0;
        } else {
            pl.xvel += pl.acc * dt;

            //Limit velocity
            if (pl.xvel < -90) pl.xvel = -90;
            if (pl.xvel > 210) pl.xvel = 210;
        }

        //Gravity
        pl.yvel += pl.grav * dt;
        if (pl.yvel > 300) pl.yvel = 300; //Limit velocity

        //Update position
        pl.x += pl.xvel * dt;
        pl.y += pl.yvel * dt;

        //Update position relative to the rope if grabbing one
        if (pl.state == PLAYER_STATE_GRABROPE) {
            if (pl.y >= 167) {
                pl.y = 167;
                pl.yvel = 0;
            }

            pl.x = ctx.grabbedRope.x - 19;
        }
    }

    //Prevents the player character from moving across solids
    void handleSolids() {
        Player pl = ctx.player;

        int plLeft = (int)pl.oldx + PLAYER_BOX_OFFSET_X;
        int plRight = (int)plLeft + PLAYER_BOX_WIDTH;
        int plTop = (int)pl.oldy;
        int plBottom = (int)plTop + pl.height;

        int plNewLeft = (int)pl.x + PLAYER_BOX_OFFSET_X;
        int plNewRight = plNewLeft + PLAYER_BOX_WIDTH;

        boolean movedRight = (pl.x > pl.oldx);
        boolean movedLeft = (pl.x < pl.oldx);
        boolean movedDown = (pl.y > pl.oldy);
        boolean movedUp = (pl.y < pl.oldy);

        int limit;
        int i;


        //----------------------------------------------------------------------
        // First, do the X axis
        //
        limit = movedRight ? 30000 : 0;

        for (i = 0; i < MAX_SOLIDS; i++) {
            Solid sol = ctx.solids[i];

            //Ignore inexistent solids
            if (sol.type == NONE) continue;

            if (sol.type != SOL_FULL) continue;
            if (sol.top >= plBottom || sol.bottom < plTop) continue;

            if (movedRight && sol.left < limit && sol.left >= plRight) {
                limit = sol.left;
            } else if (movedLeft && sol.right > limit && sol.right <= plLeft) {
                limit = sol.right;
            }
        }

        if (movedRight && limit <= plNewRight) {
            plNewRight = limit;
            plNewLeft = plNewRight - PLAYER_BOX_WIDTH;
            pl.x = plNewLeft - PLAYER_BOX_OFFSET_X;
            pl.xvel = 0;
        } else if (movedLeft && limit >= plNewLeft) {
            plNewLeft = limit;
            plNewRight = plNewLeft + PLAYER_BOX_WIDTH;
            pl.x = plNewLeft - PLAYER_BOX_OFFSET_X;
            pl.xvel = 0;
        }

        plLeft = plNewLeft;
        plRight = plLeft + PLAYER_BOX_WIDTH;


        //----------------------------------------------------------------------
        // Second, do the Y axis
        //
        limit = movedUp ? 0 : 30000;

        for (i = 0; i < MAX_SOLIDS; i++) {
            Solid sol = ctx.solids[i];

            //Ignore inexistent solids
            if (sol.type == NONE) continue;

            if (sol.left >= plRight || sol.right <= plLeft) continue;

            if (movedDown) {
                int top = sol.top;

                //When moving down, ignore passageway entry solids, which are
                //intended to prevent the player character from leaving the
                //passageway through the entry
                if (sol.type == SOL_PASSAGEWAY_ENTRY) {
                    continue;
                }

                if (sol.type == SOL_SLOPE_UP || sol.type == SOL_SLOPE_DOWN) {
                    if (sol.type == SOL_SLOPE_UP && plRight < sol.right) {
                        top = sol.bottom + (sol.left - plRight);
                    } else if (sol.type == SOL_SLOPE_DOWN && plLeft > sol.left) {
                        top = sol.top - (sol.left - plLeft);
                    }

                    if (top < limit) {
                        limit = top;
                    }
                } else if (sol.type == SOL_KEEP_ON_TOP) {
                    if (plLeft > sol.left || plRight < sol.right) {
                        limit = sol.top;
                    }
                }

                if (top < limit && top >= plBottom) {
                    limit = top;
                }
            } else if (movedUp) {
                //Ignore passageway exit solids if the player is moving upwards
                //at a high enough velocity, as when hitting a spring
                if (sol.type == SOL_PASSAGEWAY_EXIT && pl.yvel < -160) {
                    continue;
                }

                if (sol.bottom > limit && sol.bottom <= plTop) {
                    limit = sol.bottom;
                }
            }
        }

        if (movedDown && limit <= pl.y + pl.height) {
            pl.y = limit - pl.height;
            pl.yvel = 0;
            pl.onFloor = true;
        } else if (movedUp && limit > pl.y) {
            pl.y = limit;
            pl.yvel = 0;
        }
    }

    //Acts if the player character is entering or leaving an underground
    //passageway, which includes the vertical camera movement and opening the
    //exit of the passageway
    void handlePassageways() {
        Player pl = ctx.player;
        int plLeft = (int)pl.x + PLAYER_BOX_OFFSET_X;
        int plRight = plLeft + PLAYER_BOX_WIDTH;
        int plTop = (int)pl.y;
        int plBottom = plTop + pl.height;
        int i;

        for (i = 0; i < MAX_HOLES; i++) {
            if (ctx.holes[i].x == NONE) {
                break; //No more holes
            } else if (ctx.holes[i].type == HOLE_DEEP) {
                continue; //Skip holes that are not passageways
            }

            Hole pw = ctx.holes[i];
            int pwLeft = pw.x;
            int pwRight = pwLeft + (pw.width * LEVEL_BLOCK_SIZE);
            int pwEntryRight = pwLeft + LEVEL_BLOCK_SIZE;

            //Check if the player is entering a passageway
            if (ctx.curPassageway == null && plBottom >= FLOOR_Y + 4) {
                if (plLeft > pwLeft && plLeft < pwEntryRight) {
                    ctx.curPassageway = pw;

                    //Move camera down
                    if (!ctx.timeUp) {
                        ctx.cam.yvel = CAMERA_YVEL;
                    }
                }
            }
        }

        //Check if the player is leaving a passageway
        if (ctx.curPassageway != null) {
            Hole pw = ctx.curPassageway;
            int pwRight = pw.x + (pw.width * LEVEL_BLOCK_SIZE);

            if (plLeft > pwRight - 32) {
                //Check if the player is opening the passageway exit, but only
                //if the character is moving upwards at a high enough velocity,
                //as when hitting a spring
                if (pl.yvel < -160 && plTop < FLOOR_Y + 8) {
                    if (pw.type == HOLE_PASSAGEWAY_EXIT_CLOSED) {
                        audio.playSfx(SFX_HOLE);
                        addCrackParticles(pwRight - 16, 276);
                        pw.type = HOLE_PASSAGEWAY_EXIT_OPENED;
                    }
                }

                if (plTop < FLOOR_Y - 54) {
                    ctx.curPassageway = null;

                    //Move camera up
                    if (!ctx.timeUp) {
                        ctx.cam.yvel = -CAMERA_YVEL;
                    }
                }
            }
        }
    }

    //Handles the interactions between the player character and most other
    //objects
    void handlePlayerInteractions() {
        Player pl = ctx.player;
        int plLeft = (int)pl.x + PLAYER_BOX_OFFSET_X;
        int plTop = (int)pl.y;
        int plRight = plLeft + PLAYER_BOX_WIDTH;
        int plBottom = plTop + pl.height;
        boolean collectedCoin = false;
        boolean slipped = false;
        boolean thrownBack = false;
        boolean interacted = false;
        int i, j;

        for (i = 0; i < MAX_OBJS; i++) {
            Obj obj = ctx.objs[i];
            int objLeft, objRight, objTop, objBottom;

            //Ignore inexistent objects
            if (obj.type == NONE) continue;

            //Ignore objects the player character does not interact with
            if (obj.type == OBJ_BANANA_PEEL_MOVING) continue;
            if (obj.type == OBJ_HYDRANT) continue;
            if (obj.type == OBJ_OVERHEAD_SIGN) continue;
            if (obj.type == OBJ_PARKED_CAR_BLUE) continue;
            if (obj.type == OBJ_PARKED_CAR_SILVER) continue;
            if (obj.type == OBJ_PARKED_CAR_YELLOW) continue;
            if (obj.type == OBJ_PARKED_TRUCK) continue;
            if (obj.type == OBJ_ROPE_HORIZONTAL) continue;

            //Except for coins, the player character only interacts with other
            //objects when in the normal state
            if (obj.type != OBJ_COIN_SILVER && obj.type != OBJ_COIN_GOLD) {
                if (pl.state != PLAYER_STATE_NORMAL) {
                    continue;
                }
            }

            objLeft = obj.x;
            objTop = obj.y;
            objRight = objLeft;
            objBottom = objTop;

            //Determine the bounding box of the object
            switch (obj.type) {
                case OBJ_BANANA_PEEL:
                    objLeft += 1;
                    objRight = objLeft + 6;
                    objTop += 2;
                    objBottom = objTop;
                    break;

                case OBJ_COIN_SILVER:
                case OBJ_COIN_GOLD:
                    objLeft += 2;
                    objRight = objLeft + 4;
                    objTop += 2;
                    objBottom = objTop + 4;
                    break;

                case OBJ_GEYSER:
                    objLeft += 3;
                    objRight = objLeft + 9;
                    objBottom += 72;
                    break;

                case OBJ_GEYSER_CRACK:
                    objLeft += 3;
                    objRight = objLeft + 10;
                    break;

                case OBJ_ROPE_VERTICAL:
                    objRight += 4;
                    objBottom += 64;
                    break;

                case OBJ_SPRING:
                    objRight += 16;
                    objTop += 8;
                    objBottom += 8;
                    break;
            }

            if (obj.type == OBJ_ROPE_VERTICAL) {
                //For vertical ropes, check interaction using a point close to
                //the player character
                int px = (int)pl.x + 21;
                int py = (int)pl.y + 28;

                if (px < objLeft || px > objRight)  continue;
                if (py < objTop  || py > objBottom) continue;
            } else {
                //For other object types, check interaction using player
                //character's bounding box
                if (plRight  < objLeft || plLeft > objRight)  continue;
                if (plBottom < objTop  || plTop  > objBottom) continue;
            }

            interacted = true;

            switch (obj.type) {
                case OBJ_BANANA_PEEL:
                    ctx.slipPeel.obj = i;
                    ctx.slipPeel.x = obj.x;
                    ctx.slipPeel.y = obj.y;
                    obj.type = OBJ_BANANA_PEEL_MOVING;
                    slipped = true;
                    break;

                case OBJ_COIN_SILVER:
                case OBJ_COIN_GOLD:
                    collectedCoin = true;
                    ctx.score += (obj.type == OBJ_COIN_GOLD) ? 100 : 50;

                    //Add spark
                    ctx.coinSparks[ctx.nextCoinSpark].x = obj.x;
                    ctx.coinSparks[ctx.nextCoinSpark].y = obj.y;
                    ctx.coinSparks[ctx.nextCoinSpark].animFrame = 0;
                    ctx.coinSparks[ctx.nextCoinSpark].animDelay = 0.05f;
                    ctx.coinSparks[ctx.nextCoinSpark].gold =
                                                (obj.type == OBJ_COIN_GOLD);

                    ctx.nextCoinSpark++;
                    if (ctx.nextCoinSpark >= MAX_COIN_SPARKS) {
                        ctx.nextCoinSpark = 0;
                    }

                    obj.type = NONE;

                    break;

                case OBJ_GEYSER:
                    thrownBack = true;
                    break;

                case OBJ_GEYSER_CRACK:
                    obj.type = OBJ_GEYSER;

                    for (j = 0; j < MAX_GEYSERS; j++) {
                        if (ctx.geysers[j].obj == NONE) {
                            ctx.geysers[j].obj = i;
                            ctx.geysers[j].y = 266;
                            ctx.geysers[j].movePattern = geyserMovePattern2;
                            ctx.geysers[j].movePatternPos = 0;
                            ctx.geysers[j].yvel = -140;
                            ctx.geysers[j].ydest = geyserMovePattern2[1];

                            addCrackParticles(obj.x + 6, 276);

                            if (pl.state == PLAYER_STATE_NORMAL) {
                                thrownBack = true;
                            }

                            break;
                        }

                    }

                    break;

                case OBJ_ROPE_VERTICAL:
                    if (ctx.grabbedRope.obj == i) {
                        //Cannot grab the same rope again right after releasing
                        //it
                        if (ctx.grabbedRope.x > ctx.grabbedRope.xmax - 64) {
                            break;
                        }
                    } else if (ctx.grabbedRope.obj != NONE) {
                        Obj rope = ctx.objs[ctx.grabbedRope.obj];
                        rope.x = (int)ctx.grabbedRope.xmin;
                        ctx.grabbedRope.obj = NONE;
                    }

                    if (ctx.grabbedRope.obj == NONE) {
                        ctx.grabbedRope.xmin = obj.x;
                        ctx.grabbedRope.xmax = obj.x + 352;
                    }

                    pl.state = PLAYER_STATE_GRABROPE;
                    ctx.grabbedRope.obj = i;
                    ctx.grabbedRope.x = obj.x;
                    ctx.grabbedRope.xvel = 256;

                    break;

                case OBJ_SPRING:
                    if (pl.yvel >= 0) {
                        audio.playSfx(SFX_SPRING);
                        pl.yvel = -244;
                        ctx.hitSpring.obj = i;
                        ctx.hitSpring.animFrame = 0;
                        ctx.hitSpring.animDelay = 0.02f;
                    }
                    break;
            }

            if (interacted) break;
        }

        //Play a sound effect if the player character has collected a coin
        if (collectedCoin) {
            audio.playSfx(SFX_COIN);
        }

        //Act if the player character has slipped on a banana peel
        if (slipped) {
            audio.playSfx(SFX_SLIP);
            pl.state = PLAYER_STATE_SLIP;

            ctx.slipPeel.xvel = 150;
            ctx.slipPeel.yvel = -200;
            ctx.slipPeel.grav = 500;

            //Retreat camera if needed
            if (!ctx.timeUp && pl.x < ctx.cam.x + 64) {
                ctx.cam.xdest = ctx.cam.x - 64;
                ctx.cam.xvel  = -CAMERA_XVEL;
            }
        }

        //Act if the player character has been thrown back by a geyser
        if (thrownBack) {
            audio.playSfx(SFX_HIT);
            pl.state = PLAYER_STATE_THROWBACK;

            //Retreat camera if needed
            if (!ctx.timeUp && pl.x < ctx.cam.x + 128) {
                ctx.cam.xdest = ctx.cam.x -128;
                ctx.cam.xvel  = -CAMERA_XVEL;
            }
        }

        //Handle pushable crates
        if (!inputRight) ctx.cratePushRemaining = 0.75f;
        for (i = 0; i < MAX_PUSHABLE_CRATES; i++) {
            PushableCrate crate = ctx.pushableCrates[i];
            Solid sol;
            int x = (int)ctx.player.x + 24;
            int y = (int)ctx.player.y + 48;

            //Skip crates that do not exist or have been pushed
            if (crate.obj == NONE || crate.pushed) continue;

            //If the point does not overlap the crate's solid, then the player
            //is not pushing the crate
            sol = ctx.solids[crate.solid];
            if (sol.type == NONE) continue;
            if (x < sol.left) continue;
            if (x > sol.right) continue;
            if (y < sol.top) continue;
            if (y > sol.bottom) continue;

            //If we got here, then the player is pushing the crate
            ctx.cratePushRemaining -= dt;
            if (ctx.cratePushRemaining <= 0) {
                //Finished pushing
                ctx.cratePushRemaining = 0.75f;
                crate.pushed = true;
                audio.playSfx(SFX_CRATE);
            }
        }
    }

    //Acts when the player character reaches the position of a trigger, which
    //causes the appearance of a passing car or hen
    void handleTriggers() {
        int plx = (int)ctx.player.x;
        int i;

        for (i = 0; i < MAX_TRIGGERS; i++) {
            Trigger tr = ctx.triggers[i];

            //Ignore triggers that do not exist or the player character has not
            //reached
            if (tr.x == NONE || tr.x > plx) continue;

            if (tr.what == TRIGGER_HEN) {
                ctx.hen.x = tr.x - (SCREEN_WIDTH / 2) - 32;
                ctx.hen.animFrame = 0;
                ctx.hen.animDelay = 0.05f;
            } else { //If not a hen, then trigger a passing car
                ctx.passingCar.x = tr.x - (SCREEN_WIDTH / 2) - 128;
                ctx.passingCar.color = tr.what;
                ctx.passingCar.threwPeel = false;
                ctx.passingCar.peelThrowX = tr.x + 72;
                ctx.passingCar.wheelAnimFrame = 0;
                ctx.passingCar.wheelAnimDelay = 0.05f;
            }

            tr.x = NONE;
        }
    }

    //Does the specifics of the player character's current state
    void doPlayerStateSpecifics() {
        Player pl = ctx.player;
        boolean stateChanged = (pl.state != pl.oldState);

        if (pl.state == PLAYER_STATE_NORMAL) {
            pl.acc = 0;
            if (inputRight) {
                pl.acc = 210;
            } else if (inputLeft) {
                pl.acc = -210;
            }

            //Jump
            //By comparing the current Y position to the one when the jump
            //button was just pressed, the character can jump even if the
            //button is pressed moments before hitting the floor
            if (inputJump && pl.onFloor) {
                if (jumpPressY >= pl.y - 6) {
                    pl.yvel = -154;
                    jumpPressY = -99999;
                }
            }

            //Decide animation type
            if (!pl.onFloor) {
                pl.animType = PLAYER_ANIM_JUMP;
            } else if (pl.xvel > 0) {
                pl.animType = PLAYER_ANIM_WALK;
            } else if (pl.xvel < 0) {
                pl.animType = PLAYER_ANIM_WALKBACK;
            } else {
                pl.animType = PLAYER_ANIM_STAND;
            }
        } else if (pl.state == PLAYER_STATE_SLIP) {
            if (!stateChanged && pl.onFloor) {
                pl.xvel = 0;

                //Get up on player input
                if (inputLeft && !oldInputLeft) {
                    pl.state = PLAYER_STATE_GETUP;
                }
                if (inputRight && !oldInputRight) {
                    pl.state = PLAYER_STATE_GETUP;
                }
                if (inputJump && !oldInputJump) {
                    pl.state = PLAYER_STATE_GETUP;
                }
            }
        } else if (pl.state == PLAYER_STATE_GETUP) {
            //Prevent jump if the button is held until the character finishes
            //getting up
            jumpPressY = -99999;

            if (pl.yvel >= 0) {
                pl.height = PLAYER_HEIGHT_NORMAL;
                if (pl.onFloor) {
                    pl.state = PLAYER_STATE_NORMAL;
                }
            }
        } else if (pl.state == PLAYER_STATE_THROWBACK) {
            if (!stateChanged && pl.onFloor) {
                pl.state = PLAYER_STATE_NORMAL;
            }
        } else if (pl.state == PLAYER_STATE_GRABROPE) {
            GrabbedRope rope = ctx.grabbedRope;
            if (pl.x < rope.xmax - 16 && rope.xvel <= 0) {
                //Release the rope
                pl.state = PLAYER_STATE_NORMAL;
            }
        } else if (pl.state == PLAYER_STATE_FLICKER) {
            //Prevent jump if the button is held until the flicker finishes
            jumpPressY = -99999;

            pl.visible = !pl.visible;

            pl.flickerDelay -= dt;
            if (pl.flickerDelay <= 0) {
                pl.state = PLAYER_STATE_NORMAL;
            }
        }
    }

    //Checks if the player character has fallen into a deep hole on the ground
    //and plays the fall sound effect if so
    void handleFallSound() {
        Player pl = ctx.player;
        int plBottom = (int)pl.y + pl.height;
        boolean inPassageway = (ctx.curPassageway != null);

        if (!ctx.timeUp && !pl.fell && !inPassageway) {
            if (plBottom > FLOOR_Y + 8 && pl.yvel > 0) {
                audio.playSfx(SFX_FALL);
                pl.fell = true;
            }
        }
    }

    //Handles the respawning (reappearance) of the player character after
    //falling into a deep hole
    void handleRespawn() {
        int rx = 0, ry = 0;
        int i;

        //No respawn on time up or if the player character's Y position is
        //above (lower than) 324
        if (ctx.timeUp) return;
        if (ctx.player.y < 324) return;

        for (i = 0; i < MAX_RESPAWN_POINTS; i++) {
            RespawnPoint rp = ctx.respawnPoints[i];

            //Leave the loop on the first respawn point that does not exist or
            //is to the right of the player character
            if (rp.x == NONE || rp.x > ctx.player.x) break;

            rx = rp.x;
            ry = rp.y;
        }

        ctx.player.x = rx;
        ctx.player.y = ry;
        ctx.player.oldx = rx;
        ctx.player.oldy = ry;
        ctx.player.state = PLAYER_STATE_FLICKER;
        ctx.player.fell = false;

        //Retreat camera if needed
        if (ctx.cam.x > rx - 32) {
            ctx.cam.xdest = rx - 32;
            ctx.cam.xvel = -CAMERA_XVEL;
        }

        audio.stopSfx(SFX_FALL);
        audio.playSfx(SFX_RESPAWN);
    }

    //Acts if the player character's state has changed
    void handlePlayerStateChange() {
        Player pl = ctx.player;

        //Nothing to do if the state has not changed
        if (pl.state == pl.oldState) return;

        pl.acc = 0;
        pl.dec = 0;
        pl.height = PLAYER_HEIGHT_NORMAL;
        pl.visible = true;

        switch (pl.state) {
            case PLAYER_STATE_NORMAL:
                pl.dec = 256;
                pl.grav = 230;
                break;

            case PLAYER_STATE_SLIP:
                pl.xvel = -10;
                pl.yvel = -20;
                pl.height = PLAYER_HEIGHT_SLIP;
                pl.animType = PLAYER_ANIM_SLIP;
                break;

            case PLAYER_STATE_GETUP:
                pl.xvel = 0;
                pl.yvel = -120;
                pl.height = PLAYER_HEIGHT_SLIP;
                pl.animType = PLAYER_ANIM_SLIPREV;
                break;

            case PLAYER_STATE_THROWBACK:
                pl.xvel = -100;
                pl.yvel = -140;
                pl.animType = PLAYER_ANIM_THROWBACK;
                break;

            case PLAYER_STATE_GRABROPE:
                pl.grav = 0;
                pl.xvel = 0;
                pl.yvel = 110;
                pl.animType = PLAYER_ANIM_GRABROPE;
                break;

            case PLAYER_STATE_FLICKER:
                pl.flickerDelay = 0.5f;
                pl.grav = 0;
                pl.xvel = 0;
                pl.yvel = 0;
                pl.animType = PLAYER_ANIM_STAND;
                break;

            case PLAYER_STATE_INACTIVE:
                pl.x = -1;
                pl.y = -1;
                pl.xvel = 0;
                pl.yvel = 0;
                pl.acc = 0;
                pl.grav = 0;
                pl.visible = false;
                break;
        }
    }

    //Updates the position of the camera
    void moveCamera() {
        Camera cam = ctx.cam;

        //Horizontal camera movement
        if (cam.xvel != 0) {
            cam.x += cam.xvel * dt;

            if (cam.xvel > 0 && cam.x >= cam.xdest) {
                cam.xvel = 0;
            } else if (cam.xvel < 0 && cam.x <= cam.xdest) {
                cam.xvel = 0;
            }
        } else {
            //If the camera is not doing a horizontal movement, it follows the
            //player character
            if (ctx.player.x > cam.x + 240) {
                cam.x = ctx.player.x - 240;
            }
        }

        //Keep the camera within the level boundaries
        if (cam.x > ctx.levelSize - SCREEN_WIDTH) {
            cam.x = ctx.levelSize - SCREEN_WIDTH;
            cam.xvel = 0;
        }
        if (cam.x < 0) {
            cam.x = 0;
            cam.xvel = 0;
        }

        //Vertical camera movement
        cam.y += cam.yvel * dt;
        if (cam.yvel < 0 && cam.y <= 0) {
            cam.y = 0;
            cam.yvel = 0;
        } else if (cam.yvel > 0 && cam.y >= 95) {
            cam.y = 95;
            cam.yvel = 0;
        }
    }

    //Prevents the player character from moving off the limits, which can
    //correspond either to the camera's position or the level's boundaries
    void keepPlayerWithinLimits() {
        //In most cases, the limit is relative to the position of the camera
        int leftLimit = (int)ctx.cam.x + 8;

        //In these cases, ignore the position of the camera and use the level's
        //left boundary instead
        if (ctx.timeUp) leftLimit = 32;
        if (ctx.player.state == PLAYER_STATE_FLICKER) leftLimit = 32;
        if (ctx.cam.xvel < 0) leftLimit = 32;
        if (leftLimit < 32) leftLimit = 32;

        if (ctx.player.x < leftLimit) {
            ctx.player.x = leftLimit;
            ctx.player.xvel = 0;

            if (ctx.player.onFloor) ctx.player.animType = PLAYER_ANIM_STAND;
        }
    }

    //Acts if the player character's animation type has changed
    void handlePlayerAnimationChange() {
        Player pl = ctx.player;

        //Nothing to do if the animation has not changed
        if (pl.animType == pl.oldAnimType) return;

        pl.animLoop = false;
        pl.animReverse = false;

        switch (pl.animType) {
            case PLAYER_ANIM_STAND:
                pl.animMinFrame = 0;
                pl.animMaxFrame = 0;
                pl.animDelayMax = 0;
                break;

            case PLAYER_ANIM_WALK:
                pl.animMinFrame = 0;
                pl.animMaxFrame = 5;
                pl.animDelayMax = 0.1f;
                pl.animLoop     = true;
                break;

            case PLAYER_ANIM_WALKBACK:
                pl.animMinFrame = 0;
                pl.animMaxFrame = 5;
                pl.animDelayMax = 0.1f;
                pl.animLoop     = true;
                pl.animReverse  = true;
                break;

            case PLAYER_ANIM_JUMP:
                pl.animMinFrame = 0;
                pl.animMaxFrame = 0;
                pl.animDelayMax = 0.1f;
                break;

            case PLAYER_ANIM_SLIP:
                pl.animMinFrame = 0;
                pl.animMaxFrame = 3;
                pl.animDelayMax = 0.05f;
                break;

            case PLAYER_ANIM_SLIPREV:
                pl.animMinFrame = 0;
                pl.animMaxFrame = 3;
                pl.animDelayMax = 0.05f;
                pl.animReverse  = true;
                break;

            case PLAYER_ANIM_THROWBACK:
                pl.animMinFrame = 0;
                pl.animMaxFrame = 2;
                pl.animDelayMax = 0.05f;
                break;

            case PLAYER_ANIM_GRABROPE:
                pl.animMinFrame = 0;
                pl.animMaxFrame = 0;
                pl.animDelayMax = 0.05f;
                break;
        }

        pl.animCurFrame = (pl.animReverse) ? pl.animMaxFrame : pl.animMinFrame;
        pl.animDelay = pl.animDelayMax;
    }

    //Updates all animations
    void updateAnimations() {
        Player pl = ctx.player;
        Bus bus = ctx.bus;
        int i;

        //Player character
        pl.animDelay -= dt;
        if (pl.animDelay <= 0) {
            pl.animDelay = pl.animDelayMax;

            if (pl.animReverse) {
                pl.animCurFrame--;

                if (pl.animCurFrame < pl.animMinFrame) {
                    pl.animCurFrame =
                        pl.animLoop ? pl.animMaxFrame : pl.animMinFrame;
                }
            } else {
                pl.animCurFrame++;

                if (pl.animCurFrame > pl.animMaxFrame) {
                    pl.animCurFrame =
                        pl.animLoop ? pl.animMinFrame : pl.animMaxFrame;
                }
            }
        }

        //Coins
        ctx.coinAnimDelay -= dt;
        if (ctx.coinAnimDelay <= 0) {
            ctx.coinAnimDelay = 0.1f;
            ctx.coinAnimFrame++;
            if (ctx.coinAnimFrame >= 3) {
                ctx.coinAnimFrame = 0;
            }
        }

        //Geysers
        ctx.geyserAnimDelay -= dt;
        if (ctx.geyserAnimDelay <= 0) {
            ctx.geyserAnimDelay = 0.025f;
            ctx.geyserAnimFrame++;
            if (ctx.geyserAnimFrame >= 3) {
                ctx.geyserAnimFrame = 0;
            }
        }

        //Hit spring
        if (ctx.hitSpring.obj >= 0 && ctx.hitSpring.animFrame < 5) {
            ctx.hitSpring.animDelay -= dt;
            if (ctx.hitSpring.animDelay <= 0) {
                ctx.hitSpring.animDelay = 0.02f;
                ctx.hitSpring.animFrame++;
            }
        }

        //Crack particles
        ctx.crackParticleAnimDelay -= dt;
        if (ctx.crackParticleAnimDelay <= 0) {
            ctx.crackParticleAnimDelay = 0.1f;
            ctx.crackParticleAnimFrame++;
            if (ctx.crackParticleAnimFrame >= 2) {
                ctx.crackParticleAnimFrame = 0;
            }
        }

        //Coin sparks
        for (i = 0; i < MAX_COIN_SPARKS; i++) {
            CoinSpark spk = ctx.coinSparks[i];
            if (spk.x > 0 && spk.animFrame < 4) {
                spk.animDelay -= dt;
                if (spk.animDelay <= 0) {
                    spk.animDelay = 0.05f;
                    spk.animFrame++;
                }
            }
        }

        //Bus wheels
        if (bus.xvel > 0) {
            float maxDelay = 0.1f;
            if (bus.xvel > 64)  maxDelay = 0.05f;
            if (bus.xvel > 128) maxDelay = 0.025f;

            if (bus.wheelAnimDelay > maxDelay) {
                bus.wheelAnimDelay = maxDelay;
            }

            bus.wheelAnimDelay -= dt;
            if (bus.wheelAnimDelay <= 0) {
                bus.wheelAnimDelay = maxDelay;
                bus.wheelAnimFrame++;
                if (bus.wheelAnimFrame >= 3) bus.wheelAnimFrame = 0;
            }
        }

        //Bus rear door
        bus.rearDoorAnimDelay -= dt;
        if (bus.rearDoorAnimDelay <= 0) {
            bus.rearDoorAnimDelay = 0.1f;
            bus.rearDoorAnimFrame += bus.rearDoorAnimDelta;

            if (bus.rearDoorAnimFrame < 0) {
                bus.rearDoorAnimFrame = 0;
                bus.rearDoorAnimDelta = 0;
            }
            if (bus.rearDoorAnimFrame > 3) {
                bus.rearDoorAnimFrame = 3;
                bus.rearDoorAnimDelta = 0;
            }
        }

        //Bus front door
        bus.frontDoorAnimDelay -= dt;
        if (bus.frontDoorAnimDelay <= 0) {
            bus.frontDoorAnimDelay = 0.1f;
            bus.frontDoorAnimFrame += bus.frontDoorAnimDelta;

            if (bus.frontDoorAnimFrame < 0) {
                bus.frontDoorAnimFrame = 0;
                bus.frontDoorAnimDelta = 0;
            }
            if (bus.frontDoorAnimFrame > 3) {
                bus.frontDoorAnimFrame = 3;
                bus.frontDoorAnimDelta = 0;
            }
        }

        //Passing car wheels
        if (ctx.passingCar.x != NONE) {
            ctx.passingCar.wheelAnimDelay -= dt;
            if (ctx.passingCar.wheelAnimDelay <= 0) {
                ctx.passingCar.wheelAnimDelay = 0.05f;
                ctx.passingCar.wheelAnimFrame++;
                if (ctx.passingCar.wheelAnimFrame >= 2) {
                    ctx.passingCar.wheelAnimFrame = 0;
                }
            }
        }

        //Hen
        if (ctx.hen.x != NONE) {
            ctx.hen.animDelay -= dt;
            if (ctx.hen.animDelay <= 0) {
                ctx.hen.animDelay = 0.05f;
                ctx.hen.animFrame++;
                if (ctx.hen.animFrame >= 4) {
                    ctx.hen.animFrame = 0;
                }
            }
        }

        //Cutscene objects
        for (i = 0; i < MAX_CUTSCENE_OBJECTS; i++) {
            CutsceneObject cobj = ctx.cutsceneObjects[i];

            if (cobj.sprite == NONE || cobj.animNumFrames <= 1) continue;

            cobj.animDelay -= dt;
            if (cobj.animDelay <= 0) {
                cobj.animDelay = cobj.animDelayMax;
                cobj.animCurFrame++;
                if (cobj.animCurFrame >= cobj.animNumFrames) {
                    if (cobj.animLoop) {
                        cobj.animCurFrame = 0;
                    } else {
                        cobj.animCurFrame = cobj.animNumFrames - 1;
                    }
                }
            }
        }
    }

    //Determines the position of the light pole
    void findLightPolePosition() {
        int camx = (int)ctx.cam.x + (SCREEN_WIDTH / 2);
        ctx.poleX = camx - (camx % POLE_DISTANCE) + 16;
    }

    //Determines the offset of the background image from camera position
    void findBackgroundOffset() {
        ctx.bgOffsetX = (int)ctx.cam.x % 96;
    }

    //Updates the sequences, like the player character entering the bus when
    //the level's goal is reached
    //
    //Normal play (SEQ_NORMAL_PLAY) is treated as one of the sequences and is
    //where the start of a "goal reached" or "time up" sequence is checked
    void updateSequence() {
        Player pl = ctx.player;
        Bus bus = ctx.bus;
        CutsceneObject cutscenePlayer = ctx.cutsceneObjects[0];
        CutsceneObject beardedMan = ctx.cutsceneObjects[1];
        CutsceneObject bird = ctx.cutsceneObjects[1];
        CutsceneObject dung = ctx.cutsceneObjects[0];
        Camera cam = ctx.cam;
        int levelSize = ctx.levelSize;

        ctx.sequenceDelay -= dt;
        if (ctx.sequenceDelay > 0) return;

        ctx.sequenceDelay = 0;

        switch (ctx.sequenceStep) {
            //------------------------------------------------------------------
            case 0: //SEQ_NORMAL_PLAY
                if (pl.x >= levelSize - 426) {
                    ctx.goalReached = true;
                    ctx.timeUp = false;
                }
                if (ctx.timeUp || ctx.goalReached) {
                    ctx.canPause = false;
                    ctx.timeRunning = false;
                    ignoreUserInput = true;
                    inputLeft = false;
                    inputRight = false;
                    inputJump = false;
                    jumpPressY = 9999;

                    if (ctx.timeUp) {
                        ctx.sequenceDelay = 1;
                        if (pl.x >= levelSize - 960) {
                            ctx.sequenceStep = SEQ_TIMEUP_BUS_NEAR;
                        } else {
                            ctx.sequenceStep = SEQ_TIMEUP_BUS_FAR;
                        }
                    } else { //Goal reached
                        inputRight = true;
                        ctx.sequenceStep = SEQ_GOAL_REACHED;
                    }
                }
                break;


            //------------------------------------------------------------------
            case 10: //SEQ_INITIAL_DELAY
                ignoreUserInput = false;
                ctx.timeRunning = true;
                ctx.timeDelay = 1;
                ctx.sequenceStep = SEQ_NORMAL_PLAY;
                ctx.canPause = true;
                break;


            //------------------------------------------------------------------
            case 20: //SEQ_BUS_LEAVING
                bus.frontDoorAnimDelay = 0.1f;
                bus.frontDoorAnimDelta = -1;
                bus.acc = 256;
                bus.xvel = 4;
                ctx.sequenceDelay = 2;
                ctx.sequenceStep++;
                break;

            case 21:
                ctx.wipeToBlack = true;
                ctx.sequenceDelay = 1;
                ctx.sequenceStep++;
                break;

            case 22:
                ctx.sequenceStep = SEQ_FINISHED;
                break;


            //------------------------------------------------------------------
            case 30: //SEQ_TIMEUP_BUS_NEAR
                cam.xdest = levelSize - (SCREEN_WIDTH / 2);
                cam.xvel = CAMERA_XVEL;
                cam.yvel = 0;
                ctx.sequenceStep++;
                break;

            case 31:
                if (cam.xvel != 0) break;
                if (cam.yvel != 0) break;
                ctx.sequenceDelay = 0.2f;
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 40: //SEQ_TIMEUP_BUS_FAR
                cam.xvel = 0;
                cam.yvel = 0;
                ctx.wipeToBlack = true;
                ctx.sequenceDelay = 0.6f;
                ctx.sequenceStep++;
                break;

            case 41:
                pl.state = PLAYER_STATE_INACTIVE;
                cam.x = levelSize - SCREEN_WIDTH;
                cam.y = 0;
                ctx.wipeFromBlack = true;
                ctx.sequenceDelay = 0.6f;
                ctx.sequenceStep++;
                break;

            case 42:
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 50: //SEQ_GOAL_REACHED
                if (ctx.difficulty != DIFFICULTY_SUPER) {
                    if (ctx.levelNum == 3) {
                        if (pl.x > bus.x + 192) {
                            ctx.objs[0].type = OBJ_BANANA_PEEL_MOVING;
                            ctx.objs[0].x = levelSize;
                            ctx.objs[0].y = BUS_Y + 72;
                            ctx.thrownPeel.obj = 0;
                            ctx.thrownPeel.x = ctx.objs[0].x;
                            ctx.thrownPeel.y = ctx.objs[0].y;
                            ctx.thrownPeel.xmax = (int)bus.x + 345;
                            ctx.thrownPeel.xvel = -512;
                            ctx.thrownPeel.yvel = 200;
                            ctx.thrownPeel.grav = 500;
                            ctx.sequenceStep++;
                        }
                    } else if (ctx.levelNum == 4) {
                        if (pl.x >= bus.x + 120) {
                            bird.sprite = SPR_BIRD;
                            bird.x = cam.x - 16;
                            bird.y = 120;
                            bird.xvel = 304;
                            bird.animCurFrame = 0;
                            bird.animNumFrames = 4;
                            bird.animDelay = 0.1f;
                            bird.animDelayMax = 0.1f;
                            bird.animLoop = true;
                            ctx.sequenceStep++;
                        }
                    } else {
                        ctx.sequenceStep++;
                    }
                } else {
                    ctx.sequenceStep++;
                }
                break;

            case 51:
                if (pl.x >= bus.x + 256) {
                    pl.x = bus.x + 256;
                    inputRight = false;
                    ctx.sequenceStep++;
                }
                break;


            case 52:
                if (pl.state == PLAYER_STATE_SLIP) {
                    inputRight = false;
                    ctx.sequenceStep++;
                } else if (bird.sprite == SPR_BIRD) {
                    ctx.sequenceStep++;
                } else if (pl.xvel <= 0 || pl.x >= bus.x + 342) {
                    pl.x = bus.x + 342;
                    pl.xvel = 0;
                    inputJump = true;
                    ctx.sequenceStep++;
                }
                break;

            case 53:
                if (ctx.difficulty == DIFFICULTY_SUPER || ctx.levelNum == 1) {
                    ctx.sequenceStep = SEQ_GOAL_REACHED_DEFAULT;
                } else if (ctx.levelNum == 2) {
                    ctx.sequenceStep = SEQ_GOAL_REACHED_LEVEL2;
                } else if (ctx.levelNum == 3) {
                    ctx.sequenceStep = SEQ_GOAL_REACHED_LEVEL3;
                } else if (ctx.levelNum == 4) {
                    ctx.sequenceStep = SEQ_GOAL_REACHED_LEVEL4;
                } else if (ctx.levelNum == 5) {
                    ctx.sequenceStep = SEQ_GOAL_REACHED_LEVEL5;
                }
                break;


            //------------------------------------------------------------------
            case 100: //SEQ_GOAL_REACHED_DEFAULT
                inputJump = false;
                if (pl.yvel > 0 && pl.y >= BUS_Y + 36) {
                    showPlayerInBus();
                    startScoreCount();
                    ctx.sequenceStep++;
                }
                break;

            case 101:
                if (!ctx.countingScore) {
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 102:
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 200: //SEQ_GOAL_REACHED_LEVEL2
                inputJump = false;
                if (pl.yvel > 0 && pl.y >= BUS_Y + 36) {
                    showPlayerInBus();
                    startScoreCount();
                    ctx.sequenceStep++;
                }
                break;

            case 201:
                if (!ctx.countingScore) {
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 202:
                bus.frontDoorAnimDelay = 0.1f;
                bus.frontDoorAnimDelta = -1;
                ctx.sequenceDelay = 0.5f;
                ctx.sequenceStep++;
                break;

            case 203:
                cutscenePlayer.sprite = NONE;
                beardedMan.sprite = SPR_BEARDED_MAN_WALK;
                beardedMan.x = ctx.levelSize;
                beardedMan.y = 203;
                beardedMan.xvel = -150;
                beardedMan.animCurFrame = 0;
                beardedMan.animNumFrames = 6;
                beardedMan.animDelay = 0.1f;
                beardedMan.animDelayMax = 0.1f;
                beardedMan.animLoop = true;
                ctx.sequenceStep++;
                break;

            case 204:
                if (beardedMan.x <= bus.x + 380) {
                    beardedMan.x = bus.x + 380;
                    beardedMan.acc = 256;
                    ctx.sequenceStep++;
                }
                break;

            case 205:
                if (beardedMan.xvel >= 0 || beardedMan.x <= bus.x + 337) {
                    beardedMan.sprite = SPR_BEARDED_MAN_STAND;
                    beardedMan.x = bus.x + 337;
                    beardedMan.xvel = 0;
                    beardedMan.acc = 0;
                    beardedMan.animCurFrame = 0;
                    beardedMan.animNumFrames = 1;
                    bus.frontDoorAnimDelay = 0.1f;
                    bus.frontDoorAnimDelta = 1;
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 206:
                beardedMan.sprite = SPR_BEARDED_MAN_JUMP;
                beardedMan.yvel = -154;
                beardedMan.grav = 230;
                ctx.sequenceStep++;
                break;

            case 207:
                if (beardedMan.y > 163 && beardedMan.yvel > 0) {
                    beardedMan.sprite = SPR_BEARDED_MAN_STAND;
                    beardedMan.grav = 0;
                    beardedMan.yvel = 0;
                    beardedMan.x -= bus.x; //Make it relative to the bus
                    beardedMan.y = 163;
                    beardedMan.inBus = true;
                    ctx.sequenceDelay = 0.25f;
                    ctx.sequenceStep++;
                }
                break;

            case 208:
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 300: //SEQ_GOAL_REACHED_LEVEL3
                if (pl.onFloor) {
                    ctx.sequenceDelay = 0.25f;
                    ctx.sequenceStep++;
                }
                break;

            case 301:
                inputRight = !inputRight;
                oldInputRight = !inputRight;
                if (pl.state == PLAYER_STATE_GETUP) {
                    inputRight = true;
                    oldInputRight = false;
                    ctx.sequenceStep++;
                }
                break;

            case 302:
                if (pl.x >= bus.x + 342) {
                    pl.x = bus.x + 342;
                    pl.xvel = 0;
                    inputRight = false;
                    inputJump = true;
                    jumpPressY = 9999;
                    ctx.sequenceStep++;
                }
                break;

            case 303:
                if (pl.yvel > 0 && pl.y >= BUS_Y + 36) {
                    showPlayerInBus();
                    startScoreCount();
                    ctx.sequenceStep++;
                }
                break;

            case 304:
                if (!ctx.countingScore) {
                    ctx.sequenceDelay = 0.25f;
                    ctx.sequenceStep++;
                }
                break;

            case 305:
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 400: //SEQ_GOAL_REACHED_LEVEL4
                if (pl.x >= bus.x + 342) {
                    pl.x = bus.x + 342;
                    pl.xvel = 0;
                }
                if (bird.x >= bus.x + 353) {
                    dung.sprite = SPR_DUNG;
                    dung.x = bus.x + 353;
                    dung.y = bird.y;
                    dung.yvel = 256;
                    dung.animLoop = true;
                    ctx.sequenceStep++;
                }
                break;

            case 401:
                if (dung.y >= pl.y + 12) {
                    dung.sprite = NONE;
                    dung.yvel = 0;
                    pl.visible = false;
                    cutscenePlayer.sprite = SPR_PLAYER_CLEAN_DUNG;
                    cutscenePlayer.x = pl.x;
                    cutscenePlayer.y = pl.y;
                    ctx.sequenceDelay = 0.25f;
                    ctx.sequenceStep++;
                }
                break;

            case 402:
                cutscenePlayer.animCurFrame = 0;
                cutscenePlayer.animNumFrames = 9;
                cutscenePlayer.animDelay = 0.2f;
                cutscenePlayer.animDelayMax = 0.2f;
                cutscenePlayer.animLoop = false;
                ctx.sequenceDelay = 2.0f;
                ctx.sequenceStep++;
                break;

            case 403:
                pl.visible = true;
                cutscenePlayer.sprite = NONE;
                ctx.sequenceDelay = 0.25f;
                ctx.sequenceStep++;
                break;

            case 404:
                inputJump = true;
                ctx.sequenceStep++;
                break;

            case 405:
                if (pl.yvel > 0 && pl.y >= BUS_Y + 36) {
                    showPlayerInBus();
                    startScoreCount();
                    ctx.sequenceStep++;
                }
                break;

            case 406:
                if (!ctx.countingScore) {
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 407:
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 500: //SEQ_GOAL_REACHED_LEVEL5
                bus.frontDoorAnimDelay = 0.1f;
                bus.frontDoorAnimDelta = -1;
                bus.acc = 256;
                bus.xvel = 4;
                ctx.sequenceStep++;
                break;

            case 501:
                if (bus.x >= ctx.levelSize + 32) {
                    bus.acc = 0;
                    bus.xvel = 0;
                    pl.visible = false;
                    cutscenePlayer.sprite = SPR_PLAYER_RUN;
                    cutscenePlayer.x = pl.x;
                    cutscenePlayer.y = pl.y;
                    cutscenePlayer.xvel = 128;
                    cutscenePlayer.acc = 512;
                    cutscenePlayer.animNumFrames = 4;
                    cutscenePlayer.animLoop = true;
                    cutscenePlayer.animDelay = 0.1f;
                    cutscenePlayer.animDelayMax = 0.1f;
                    ctx.sequenceStep++;
                }
                break;

            case 502:
                if (cutscenePlayer.x >= ctx.levelSize + 32) {
                    startScoreCount();
                    cutscenePlayer.xvel = 0;
                    ctx.sequenceStep++;
                }
                break;

            case 503:
                if (!ctx.countingScore) {
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 504:
                ctx.wipeToBlack = true;
                ctx.sequenceDelay = 1;
                ctx.sequenceStep++;
                break;

            case 505:
                ctx.sequenceStep = SEQ_FINISHED;
                break;
        }
    }
}

