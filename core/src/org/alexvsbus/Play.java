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

class Play {
    DisplayParams displayParams;
    Audio audio;
    PlayCtx ctx; //Gameplay context

    float deltaTime; //Time elapsed since the previous frame

    boolean ignoreUserInput;
    boolean inputLeft,  oldInputLeft;
    boolean inputRight, oldInputRight;
    boolean inputJump,  oldInputJump;
    float jumpTimeout;

    //--------------------------------------------------------------------------

    Play(DisplayParams dp, Audio a) {
        displayParams = dp;
        audio = a;
    }

    PlayCtx newCtx() {
        int i;

        ctx = new PlayCtx();

        ctx.cam = new Camera();
        ctx.player = new Player();
        ctx.bus = new Bus();
        ctx.grabbedRope = new GrabbedRope();
        ctx.thrownPeel = new MovingPeel();
        ctx.slipPeel = new MovingPeel();
        ctx.car = new Car();
        ctx.hen = new Hen();
        ctx.pushArrow = new PushArrow();

        ctx.objs = new Obj[MAX_OBJS];
        for (i = 0; i < MAX_OBJS; i++) {
            ctx.objs[i] = new Obj();
        }

        ctx.crateBlocks = new CrateBlock[MAX_CRATE_BLOCKS];
        for (i = 0; i < MAX_CRATE_BLOCKS; i++) {
            ctx.crateBlocks[i] = new CrateBlock();
        }

        ctx.gushes = new Gush[MAX_GUSHES];
        for (i = 0; i < MAX_GUSHES; i++) {
            ctx.gushes[i] = new Gush();
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

        ctx.anims = new Anim[NUM_ANIMS];
        for (i = 0; i < NUM_ANIMS; i++) {
            ctx.anims[i] = new Anim();
        }

        return ctx;
    }

    void clear() {
        int i;

        deltaTime = 0;

        ignoreUserInput = true;
        inputLeft = false;
        inputRight = false;
        inputJump = false;
        jumpTimeout = 0;

        ctx.canPause = false;
        ctx.time = 90;
        ctx.timeRunning = false;
        ctx.timeUp = false;
        ctx.goalReached = false;
        ctx.countingScore = false;

        ctx.cratePushRemaining = 0.75f;

        ctx.cam.x = 0;
        ctx.cam.y = 0;
        ctx.cam.xvel = 0;
        ctx.cam.yvel = 0;
        ctx.cam.followPlayer = false;
        ctx.cam.fixedAtLeftmost = false;
        ctx.cam.fixedAtRightmost = false;

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

        ctx.grabbedRope.obj = NONE;
        ctx.slipPeel.obj = NONE;
        ctx.thrownPeel.obj = NONE;

        ctx.hitSpring = NONE;

        ctx.car.x = NONE;
        ctx.hen.x = NONE;

        for (i = 0; i < MAX_OBJS; i++) {
            ctx.objs[i].type = NONE;
        }

        for (i = 0; i < MAX_CRATE_BLOCKS; i++) {
            ctx.crateBlocks[i].x = NONE;
        }

        for (i = 0; i < MAX_GUSHES; i++) {
            ctx.gushes[i].obj = NONE;
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

        setAnimation(ANIM_PLAYER, true, true, false, 1, 0.1f);
        setAnimation(ANIM_COINS, true, true, false, 3, 0.1f);
        setAnimation(ANIM_GUSHES, true, true, false, 3, 0.05f);
        setAnimation(ANIM_HIT_SPRING, false, false, false, 6, 0.02f);
        setAnimation(ANIM_CRACK_PARTICLES, true, true, false, 2, 0.1f);
        setAnimation(ANIM_BUS_WHEELS, false, true, false, 3, 0.1f);
        setAnimation(ANIM_BUS_DOOR_REAR, false, false, false, 4, 0.1f);
        setAnimation(ANIM_BUS_DOOR_FRONT, false, false, false, 4, 0.1f);
        setAnimation(ANIM_CAR_WHEELS, false, true, false, 2, 0.05f);
        setAnimation(ANIM_HEN, false, true, false, 4, 0.05f);

        for (i = 0; i < MAX_COIN_SPARKS; i++) {
            setAnimation(ANIM_COIN_SPARKS + i, false, false, false, 4, 0.05f);
        }
        for (i = 0; i < MAX_CUTSCENE_OBJECTS; i++) {
            setAnimation(ANIM_CUTSCENE_OBJECTS + i, false, false, false, 1, 0);
        }

        ctx.nextCoinSpark = 0;
        ctx.nextCrackParticle = 0;

        ctx.pushArrow.xoffs = 0;
        ctx.pushArrow.xvel = 0;
        ctx.pushArrow.delay = 1;

        ctx.playerReachedFlagman = false;
        ctx.henReachedFlagman = false;
        ctx.busReachedFlagman = false;

        ctx.wipeIn = false;
        ctx.wipeOut = false;
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
            jumpTimeout = JUMP_TIMEOUT;
        }
    }

    void update(float dt) {
        deltaTime = dt;

        beginUpdate();
        updateRemainingTime();
        updateScoreCount();
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
        movePushArrow();
        positionBusStopSign();
        positionLightPole();
        applyBackgroundOffset();
        updateSequence();
    }

    //--------------------------------------------------------------------------

    void adaptToScreenSize() {
        Camera cam = ctx.cam;
        int vscreenWidth = displayParams.vscreenWidth;

        cam.xmin = 0;
        cam.xmax = ctx.levelSize - vscreenWidth;
        cam.followPlayerMinX = 64;
        cam.followPlayerMaxX = vscreenWidth / 2;

        if (vscreenWidth <= 256) {
            cam.followPlayerMinX  = 32;
            cam.followPlayerMaxX -= 64;
            cam.xmin = 40;
        } else if (vscreenWidth <= 320) {
            cam.followPlayerMinX  = 32;
            cam.followPlayerMaxX -= 56;
            cam.xmin = 40;
        }

        positionCamera();
        applyBackgroundOffset();
    }

    void positionCamera() {
        Camera cam = ctx.cam;

        if (cam.followPlayer) {
            if (cam.xvel == 0) {
                if (ctx.player.x > cam.x + cam.followPlayerMaxX) {
                    //Move right
                    cam.x = ctx.player.x - cam.followPlayerMaxX;
                } else if (ctx.player.x < cam.x + cam.followPlayerMinX) {
                    //Move left
                    cam.x = ctx.player.x - cam.followPlayerMinX;
                }
            }

            if (cam.yvel == 0) {
                //This is not the final Y position of the camera, as the camera's
                //vertical movement is ignored if it is over the floor and the
                //virtual screen (vscreen) is high enough
                if (ctx.player.y < 104) {
                    cam.y = ctx.player.y - 104;
                }
            }
        }

        //Keep the camera within the level boundaries
        if (cam.fixedAtRightmost || cam.x > cam.xmax) {
            cam.x = cam.xmax;
            cam.xvel = 0;
        }
        if (cam.fixedAtLeftmost || cam.x < cam.xmin) {
            cam.x = cam.xmin;
            cam.xvel = 0;
        }
    }

    void setAnimation(int anim, boolean running, boolean loop, boolean reverse,
                                                int numFrames, float delay) {

        Anim a = ctx.anims[anim];

        a.running = running;
        a.loop = loop;
        a.reverse = reverse;
        a.numFrames = numFrames;
        a.frame = reverse ? numFrames - 1 : 0;
        a.delay = delay;
        a.maxDelay = delay;
    }

    void startAnimation(int anim) {
        Anim a = ctx.anims[anim];

        a.running = true;
        a.delay = a.maxDelay;
        a.frame = a.reverse ? a.numFrames - 1 : 0;
    }

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

    //Moves the bus to the end of the level
    void moveBusToEnd() {
        ctx.bus.acc = 0;
        ctx.bus.xvel = 0;
        ctx.bus.x = ctx.levelSize - 456;

        //Make rear door closed
        ctx.anims[ANIM_BUS_DOOR_REAR].running = false;
        ctx.anims[ANIM_BUS_DOOR_REAR].frame = 0;
        ctx.anims[ANIM_BUS_DOOR_REAR].reverse = false;

        //Make front door open
        ctx.anims[ANIM_BUS_DOOR_FRONT].running = false;
        ctx.anims[ANIM_BUS_DOOR_FRONT].frame = 3;
        ctx.anims[ANIM_BUS_DOOR_FRONT].reverse = true;

        //Bus route sign
        if (ctx.lastLevel) {
            //Finish (checkered flag) sign
            ctx.bus.routeSign = 0;
        } else {
            //Sign corresponding to the next level
            ctx.bus.routeSign = ctx.levelNum + 1;
        }
    }

    void showPlayerInBus() {
        CutsceneObject cutscenePlayer = ctx.cutsceneObjects[0];
        Anim anim = ctx.anims[ANIM_CUTSCENE_OBJECTS + 0];

        cutscenePlayer.sprite = SPR_PLAYER_STAND;
        cutscenePlayer.inBus = true;
        cutscenePlayer.x = 342;
        cutscenePlayer.y = BUS_Y + 36;

        anim.frame = 0;
        anim.numFrames = 1;

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

        if (jumpTimeout > 0) {
            jumpTimeout -= deltaTime;

            if (jumpTimeout < 0) {
                jumpTimeout = 0;
            }
        }
    }

    //Updates the remaining time and acts if the time has run out
    void updateRemainingTime() {
        if (!ctx.timeRunning) return;

        ctx.timeDelay -= deltaTime;
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

        ctx.timeDelay -= deltaTime;
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

    //Updates the position of most game objects, not including the player
    //character and the camera
    void moveObjects() {
        MovingPeel peel;
        int i;

        //Bus
        ctx.bus.xvel += ctx.bus.acc * deltaTime;
        ctx.bus.x += ctx.bus.xvel * deltaTime;

        //Thrown peel
        peel = ctx.thrownPeel;
        if (peel.obj != NONE) {
            Obj obj = ctx.objs[peel.obj];

            peel.yvel += peel.grav * deltaTime;
            peel.x += peel.xvel * deltaTime;
            peel.y += peel.yvel * deltaTime;
            if (peel.y >= 256) {
                //Stop the peel when it hits the floor
                obj.type = OBJ_BANANA_PEEL;
                peel.x = peel.xdest;
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

            peel.yvel += peel.grav * deltaTime;
            peel.x += peel.xvel * deltaTime;
            peel.y += peel.yvel * deltaTime;
            if (peel.y >= 400) {
                //Deactivate the peel when it goes too far downwards
                obj.type = NONE;
                peel.obj = NONE;
            }

            obj.x = (int)peel.x;
            obj.y = (int)peel.y;
        }

        //Gushes
        for (i = 0; i < MAX_GUSHES; i++) {
            Gush gush = ctx.gushes[i];

            float y = gush.y;
            float yvel = gush.yvel;
            float ydest = gush.ydest;

            //Ignore inexistent gushes
            if (gush.obj == NONE) continue;

            y += yvel * deltaTime;

            //If the gush reaches its destination Y position
            if ((yvel < 0 && y <= ydest) || (yvel > 0 && y >= ydest)) {
                y = ydest;

                //Advance within the movement pattern and loop if its end is
                //reached
                gush.movePatternPos += 2;
                if (gush.movePattern[gush.movePatternPos] == 0) {
                    gush.movePatternPos = 0;
                }

                gush.yvel  = gush.movePattern[gush.movePatternPos];
                gush.ydest = gush.movePattern[gush.movePatternPos + 1];
            }

            gush.y = y;
            ctx.objs[gush.obj].y = (int)y;
        }

        //Grabbed rope
        if (ctx.grabbedRope.obj != NONE) {
            Obj obj = ctx.objs[ctx.grabbedRope.obj];

            ctx.grabbedRope.x += ctx.grabbedRope.xvel * deltaTime;

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

                crate.x += 72 * deltaTime;
                if (crate.x >= crate.xmax) crate.x = crate.xmax;

                ctx.objs[crate.obj].x = (int)crate.x;
                sol.left = (int)crate.x;
                sol.right = (int)crate.x + 24;
            }
        }

        //Passing car and ending sequence traffic jam
        if (ctx.car.x != NONE) {
            ctx.car.x += ctx.car.xvel * deltaTime;

            if (ctx.car.x >= ctx.cam.x + VSCREEN_MAX_WIDTH + 64) {
                if (ctx.car.type != TRAFFIC_JAM) {
                    ctx.car.x = NONE;
                }
            }
        }

        //Hen
        if (ctx.hen.x != NONE) {
            ctx.hen.xvel += ctx.hen.acc * deltaTime;
            ctx.hen.x += ctx.hen.xvel * deltaTime;

            if (ctx.hen.x > ctx.cam.x + VSCREEN_MAX_WIDTH + 64) {
                ctx.hen.x = NONE;
            }
        }

        //Crack particles
        for (i = 0; i < MAX_CRACK_PARTICLES; i++) {
            CrackParticle ptcl = ctx.crackParticles[i];

            //Ignore inexistent particles
            if (ptcl.x == NONE) continue;

            ptcl.yvel += ptcl.grav * deltaTime;
            ptcl.x += ptcl.xvel * deltaTime;
            ptcl.y += ptcl.yvel * deltaTime;

            if (ptcl.y > 400) {
                ptcl.x = NONE;
            }
        }

        //Cutscene objects
        for (i = 0; i < MAX_CUTSCENE_OBJECTS; i++) {
            CutsceneObject cobj = ctx.cutsceneObjects[i];

            //Ignore inexistent cutscene objects
            if (cobj.sprite == NONE) continue;

            cobj.xvel += cobj.acc * deltaTime;
            cobj.yvel += cobj.grav * deltaTime;
            cobj.x += cobj.xvel * deltaTime;
            cobj.y += cobj.yvel * deltaTime;
        }
    }

    //Acts if the passing car has reached the X position at which it throws a
    //banana peel
    void handleCarThrownPeel() {
        if (ctx.car.x == NONE || ctx.car.threwPeel) return;
        if (ctx.car.type == TRAFFIC_JAM) return;
        if (ctx.car.x < ctx.car.peelThrowX) return;

        for (int i = 0; i < MAX_OBJS; i++) {
            if (ctx.objs[i].type == NONE) {
                ctx.objs[i].type = OBJ_BANANA_PEEL_MOVING;

                ctx.thrownPeel.obj = i;
                ctx.thrownPeel.x = ctx.car.peelThrowX + 90;
                ctx.thrownPeel.y = 200;
                ctx.thrownPeel.xdest = ctx.thrownPeel.x + 70;
                ctx.thrownPeel.xvel = 140;
                ctx.thrownPeel.yvel = -10;
                ctx.thrownPeel.grav = 500;

                ctx.car.threwPeel = true;

                break;
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
            pl.xvel -= pl.dec * deltaTime;
            if (pl.xvel <= 0) pl.xvel = 0;
        } else if (pl.xvel < 0 && pl.acc >= 0) {
            pl.xvel += pl.dec * deltaTime;
            if (pl.xvel >= 0) pl.xvel = 0;
        } else {
            pl.xvel += pl.acc * deltaTime;

            //Limit velocity
            if (pl.xvel < -90) pl.xvel = -90;
            if (pl.xvel > 210) pl.xvel = 210;
        }

        //Gravity
        pl.yvel += pl.grav * deltaTime;
        if (pl.yvel > 300) pl.yvel = 300; //Limit velocity

        //Update position
        pl.x += pl.xvel * deltaTime;
        pl.y += pl.yvel * deltaTime;

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

        int ledgeSolid = NONE;

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

            //Detect if the player character's bounding box is on a ledge while
            //the sprite appears to be standing on the air, so we can prevent
            //this weird visual effect
            if (sol.type == SOL_FULL && pl.xvel == 0 && sol.top == plBottom) {
                if (sol.right <= plLeft + 4) {
                    //If this is the case, store the solid number
                    ledgeSolid = i;
                } else if (ledgeSolid != i && sol.left <= plRight) {
                    //If the bottom-right point of the player character's
                    //bounding box is on a different solid, then the player
                    //character is not really on a ledge
                    ledgeSolid = NONE;
                }
            }

            if (movedDown) {
                int type = sol.type;
                int top = sol.top;
                boolean checkLimit = false;

                if (sol.bottom < plTop) {
                    continue;
                }

                if (type == SOL_PASSAGEWAY_ENTRY) {
                    //When moving down, ignore passageway entry solids, which
                    //are intended to prevent the player character from leaving
                    //the passageway through the entry
                    continue;
                } else if (type == SOL_SLOPE_UP) {
                    if (plRight < sol.right) {
                        top = sol.bottom + (sol.left - plRight);
                    }
                    checkLimit = true;
                } else if (type == SOL_SLOPE_DOWN) {
                    if (plLeft > sol.left) {
                        top = sol.top - (sol.left - plLeft);
                    }
                    checkLimit = true;
                } else if (type == SOL_KEEP_ON_TOP) {
                    checkLimit = true;
                } else {
                    if (top >= plBottom) {
                        checkLimit = true;
                    }
                }

                if (checkLimit && top < limit) {
                    limit = top;
                }
            } else if (movedUp) {
                if (sol.type == SOL_PASSAGEWAY_EXIT && pl.yvel < -160) {
                    //Ignore passageway exit solids if the player is moving
                    //upwards at a high enough velocity, as when hitting a
                    //spring
                    continue;
                }

                if (sol.bottom > limit && sol.bottom <= plTop) {
                    limit = sol.bottom;
                }
            }
        }

        if (movedDown && limit <= pl.y + pl.height) {
            if (ledgeSolid != NONE) {
                pl.x = ctx.solids[ledgeSolid].right - PLAYER_BOX_OFFSET_X;
            } else {
                pl.y = limit - pl.height;
                pl.yvel = 0;
                pl.onFloor = true;
            }
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
        int i, j;

        for (i = 0; i < MAX_OBJS; i++) {
            CoinSpark spk;
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

                case OBJ_GUSH:
                    objLeft += 3;
                    objRight = objLeft + 9;
                    objBottom += 72;
                    break;

                case OBJ_GUSH_CRACK:
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
                    spk = ctx.coinSparks[ctx.nextCoinSpark];
                    spk.x = obj.x;
                    spk.y = obj.y;
                    spk.gold = (obj.type == OBJ_COIN_GOLD);

                    startAnimation(ANIM_COIN_SPARKS + ctx.nextCoinSpark);

                    ctx.nextCoinSpark++;
                    ctx.nextCoinSpark %= MAX_COIN_SPARKS;

                    //Remove the coin
                    obj.type = NONE;

                    break;

                case OBJ_GUSH:
                    thrownBack = true;
                    break;

                case OBJ_GUSH_CRACK:
                    obj.type = OBJ_GUSH;

                    for (j = 0; j < MAX_GUSHES; j++) {
                        if (ctx.gushes[j].obj == NONE) {
                            ctx.gushes[j].obj = i;
                            ctx.gushes[j].y = 266;
                            ctx.gushes[j].movePattern = Data.gushMovePattern2;
                            ctx.gushes[j].movePatternPos = 0;
                            ctx.gushes[j].yvel = -140;
                            ctx.gushes[j].ydest = Data.gushMovePattern2[1];

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
                        ctx.hitSpring = i;
                        startAnimation(ANIM_HIT_SPRING);
                    }
                    break;
            }
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
        }

        //Act if the player character has been thrown back by a gush
        if (thrownBack) {
            audio.playSfx(SFX_HIT);
            pl.state = PLAYER_STATE_THROWBACK;
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
            ctx.cratePushRemaining -= deltaTime;
            if (ctx.cratePushRemaining <= 0) {
                //Finished pushing
                ctx.cratePushRemaining = 0.75f;
                crate.showArrow = false;
                crate.pushed = true;
                audio.playSfx(SFX_CRATE);
            }
        }
    }

    //Acts when the player character reaches the X position of a trigger, which
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
                ctx.hen.x = tr.x - (VSCREEN_MAX_WIDTH / 2) - 32;
                ctx.hen.xvel = 350;
                ctx.hen.acc = 0;
                startAnimation(ANIM_HEN);
            } else { //If not a hen, then trigger a passing car
                ctx.car.x = tr.x - (VSCREEN_MAX_WIDTH / 2) - 128;
                ctx.car.xvel = 1200;
                ctx.car.type = tr.what;
                ctx.car.threwPeel = false;
                ctx.car.peelThrowX = tr.x + 72;
                startAnimation(ANIM_CAR_WHEELS);
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
            if (pl.onFloor && jumpTimeout > 0) {
                pl.yvel = -154;
                jumpTimeout = 0;
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
            jumpTimeout = 0;

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
            jumpTimeout = 0;

            pl.visible = !pl.visible;

            pl.flickerDelay -= deltaTime;
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
        if (ctx.timeUp || ctx.player.y < 324) return;

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
        if (ctx.cam.x > rx - 64) {
            ctx.cam.xdest = rx - 64;
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
            cam.x += cam.xvel * deltaTime;

            if (cam.xvel > 0 && cam.x >= cam.xdest) {
                cam.xvel = 0;
            } else if (cam.xvel < 0 && cam.x <= cam.xdest) {
                cam.xvel = 0;
            }
        }

        //Vertical camera movement
        if (cam.yvel != 0) {
            cam.y += cam.yvel * deltaTime;
            if (cam.yvel < 0 && cam.y <= 0) {
                cam.y = 0;
                cam.yvel = 0;
            } else if (cam.yvel > 0 && cam.y >= 95) {
                cam.y = 95;
                cam.yvel = 0;
            }
        }

        positionCamera();
    }

    //Prevents the player character from moving off the level's boundaries
    void keepPlayerWithinLimits() {
        if (ctx.player.x < 32) {
            ctx.player.x = 32;
            ctx.player.xvel = 0;

            if (ctx.player.onFloor) {
                ctx.player.animType = PLAYER_ANIM_STAND;
            }
        }
    }

    //Acts if the player character's animation type has changed
    void handlePlayerAnimationChange() {
        int animType = ctx.player.animType;

        //Nothing to do if the animation has not changed
        if (animType == ctx.player.oldAnimType) return;

        switch (animType) {
            case PLAYER_ANIM_STAND:
                setAnimation(ANIM_PLAYER, true, false, false, 1, 0.0f);
                break;

            case PLAYER_ANIM_WALK:
                setAnimation(ANIM_PLAYER, true, true,  false, 6, 0.1f);
                break;

            case PLAYER_ANIM_WALKBACK:
                setAnimation(ANIM_PLAYER, true, true,  true,  6, 0.1f);
                break;

            case PLAYER_ANIM_JUMP:
                setAnimation(ANIM_PLAYER, true, true,  false, 1, 0.0f);
                break;

            case PLAYER_ANIM_SLIP:
                setAnimation(ANIM_PLAYER, true, false, false, 4, 0.05f);
                break;

            case PLAYER_ANIM_SLIPREV:
                setAnimation(ANIM_PLAYER, true, false, true,  4, 0.05f);
                break;

            case PLAYER_ANIM_THROWBACK:
                setAnimation(ANIM_PLAYER, true, false, false, 3, 0.05f);
                break;

            case PLAYER_ANIM_GRABROPE:
                setAnimation(ANIM_PLAYER, true, false, false, 1, 0.05f);
                break;
        }
    }

    //Updates all animations
    void updateAnimations() {
        int i;

        //Set animation speed for bus wheels
        ctx.anims[ANIM_BUS_WHEELS].running = false;
        if (ctx.bus.xvel > 0) {
            float maxDelay = 0.1f;
            if (ctx.bus.xvel > 64)  maxDelay = 0.05f;
            if (ctx.bus.xvel > 128) maxDelay = 0.025f;

            ctx.anims[ANIM_BUS_WHEELS].running = true;
            ctx.anims[ANIM_BUS_WHEELS].maxDelay = maxDelay;

            if (ctx.anims[ANIM_BUS_WHEELS].delay > maxDelay) {
                ctx.anims[ANIM_BUS_WHEELS].delay = maxDelay;
            }
        }

        //Update animations
        for (i = 0; i < NUM_ANIMS; i++) {
            Anim anim = ctx.anims[i];

            if (!anim.running) continue;

            anim.delay -= deltaTime;
            if (anim.delay > 0) continue;

            anim.delay = anim.maxDelay;

            if (anim.reverse) {
                anim.frame--;

                if (anim.frame < 0) {
                    anim.frame = anim.loop ? anim.numFrames - 1 : 0;
                }
            } else {
                anim.frame++;

                if (anim.frame >= anim.numFrames) {
                    anim.frame = anim.loop ? 0 : anim.numFrames - 1;
                }
            }
        }
    }

    //Moves the arrows indicating that a crate is pushable
    void movePushArrow() {
        ctx.pushArrow.xoffs += ctx.pushArrow.xvel * deltaTime;
        if (ctx.pushArrow.xoffs >= 8) {
            ctx.pushArrow.xoffs = 8;
            ctx.pushArrow.xvel = -32;
        }
        if (ctx.pushArrow.xvel < 0 && ctx.pushArrow.xoffs <= 0) {
            ctx.pushArrow.xoffs = 0;
            ctx.pushArrow.xvel = 0;
        }

        ctx.pushArrow.delay -= deltaTime;
        if (ctx.pushArrow.delay <= 0) {
            ctx.pushArrow.delay = 0;

            if (ctx.pushArrow.xoffs == 0) {
                ctx.pushArrow.xvel = 32;
                ctx.pushArrow.delay = 1;
            }
        }
    }

    //Positions the bus stop sign
    void positionBusStopSign() {
        if (ctx.levelNum == 1 || ctx.cam.x > VSCREEN_MAX_WIDTH) {
            //The sign is at the end of the level
            ctx.busStopSignX = ctx.levelSize - 40;
        } else {
            //The sign is at the start of the level
            ctx.busStopSignX = 176;
        }
    }

    //Positions the first light pole (the position of the second pole is
    //calculated later when rendering)
    void positionLightPole() {
        int camx = (int)ctx.cam.x + (VSCREEN_MAX_WIDTH / 2);
        ctx.poleX = camx - (camx % POLE_DISTANCE) + 16;
    }

    //Sets the offset of the background image based on the position of the
    //camera
    void applyBackgroundOffset() {
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
        Camera cam = ctx.cam;
        int levelSize = ctx.levelSize;

        //Cutscene objects
        CutsceneObject cutscenePlayer = ctx.cutsceneObjects[0];
        CutsceneObject beardedMan = ctx.cutsceneObjects[1];
        CutsceneObject bird = ctx.cutsceneObjects[1];
        CutsceneObject dung = ctx.cutsceneObjects[0];
        CutsceneObject flagman = ctx.cutsceneObjects[1];

        //Cutscene object animations
        Anim cutscenePlayerAnim = ctx.anims[ANIM_CUTSCENE_OBJECTS + 0];
        Anim beardedManAnim = ctx.anims[ANIM_CUTSCENE_OBJECTS + 1];
        Anim birdAnim = ctx.anims[ANIM_CUTSCENE_OBJECTS + 1];
        Anim flagmanAnim = ctx.anims[ANIM_CUTSCENE_OBJECTS + 1];

        ctx.sequenceDelay -= deltaTime;
        if (ctx.sequenceDelay > 0) return;

        ctx.sequenceDelay = 0;

        switch (ctx.sequenceStep) {
            //------------------------------------------------------------------
            case 0: //SEQ_NORMAL_PLAY_START
                moveBusToEnd();
                ignoreUserInput = false;
                cam.followPlayer = true;
                cam.fixedAtLeftmost = false;
                ctx.timeRunning = true;
                ctx.timeDelay = 1;
                ctx.canPause = true;
                ctx.sequenceStep = SEQ_NORMAL_PLAY;
                break;


            //------------------------------------------------------------------
            case 1: //SEQ_NORMAL_PLAY
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
                    jumpTimeout = 0;

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
            case 10: //SEQ_INITIAL
                if (ctx.levelNum != LVLNUM_ENDING) {
                    //Start with bus rear door open
                    ctx.anims[ANIM_BUS_DOOR_REAR].frame = 3;
                    ctx.anims[ANIM_BUS_DOOR_REAR].reverse = true;
                }

                ignoreUserInput = true;
                ctx.timeRunning = false;

                if (ctx.levelNum == 1 || ctx.skipInitialSequence) {
                    moveBusToEnd();
                    ctx.sequenceStep = SEQ_NORMAL_PLAY_START;
                } else {
                    ctx.sequenceStep++;
                }
                ctx.sequenceDelay = 1;

                break;

            case 11:
                startAnimation(ANIM_BUS_DOOR_REAR);
                bus.acc = 256;
                bus.xvel = 4;
                ctx.sequenceDelay = 2;
                ctx.sequenceStep = SEQ_NORMAL_PLAY_START;
                break;


            //------------------------------------------------------------------
            case 20: //SEQ_BUS_LEAVING
                //Bus leaves while closing the front door
                startAnimation(ANIM_BUS_DOOR_FRONT);
                bus.acc = 256;
                bus.xvel = 4;
                ctx.sequenceDelay = 2;
                ctx.sequenceStep++;
                break;

            case 21:
                //Screen wipes to black
                ctx.wipeOut = true;
                ctx.sequenceDelay = 1;
                ctx.sequenceStep++;
                break;

            case 22:
                ctx.sequenceStep = SEQ_FINISHED;
                break;


            //------------------------------------------------------------------
            case 30: //SEQ_TIMEUP_BUS_NEAR
                //Camera moves towards the bus
                if (ctx.hen.x != NONE && ctx.hen.x - cam.x < -32) {
                    ctx.hen.x = NONE;
                }
                if (ctx.car.x != NONE) break; //Wait until the car and hen are
                if (ctx.hen.x != NONE) break; //not visible anymore
                cam.followPlayer = false;
                cam.xdest = levelSize;
                cam.xvel = CAMERA_XVEL;
                cam.yvel = 0;
                ctx.sequenceStep++;
                break;

            case 31:
                //Camera stops and bus leaves
                if (cam.xvel != 0) break;
                if (cam.yvel != 0) break;
                cam.fixedAtRightmost = true;
                ctx.sequenceDelay = 0.2f;
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 40: //SEQ_TIMEUP_BUS_FAR
                //Screen wipes to black
                cam.followPlayer = false;
                cam.xvel = 0;
                cam.yvel = 0;
                ctx.car.x = NONE;
                ctx.hen.x = NONE;
                ctx.wipeOut = true;
                ctx.sequenceDelay = 0.6f;
                ctx.sequenceStep++;
                break;

            case 41:
                //Camera is placed so the bus is visible and screen wipes from
                //black
                pl.state = PLAYER_STATE_INACTIVE;
                cam.x = cam.xmax;
                cam.y = 0;
                cam.fixedAtRightmost = true;
                ctx.wipeIn = true;
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
                            //A banana peel is thrown from the right side of the
                            //screen
                            ctx.objs[0].type = OBJ_BANANA_PEEL_MOVING;
                            ctx.objs[0].x = levelSize;
                            ctx.objs[0].y = BUS_Y + 72;
                            ctx.thrownPeel.obj = 0;
                            ctx.thrownPeel.x = ctx.objs[0].x;
                            ctx.thrownPeel.y = ctx.objs[0].y;
                            ctx.thrownPeel.xdest = (int)bus.x + 345;
                            ctx.thrownPeel.xvel = -512;
                            ctx.thrownPeel.yvel = 200;
                            ctx.thrownPeel.grav = 500;
                            ctx.sequenceStep++;
                        }
                    } else if (ctx.levelNum == 4) {
                        if (pl.x >= bus.x + 120) {
                            //A bird appears
                            bird.sprite = SPR_BIRD;
                            bird.x = cam.x - 16;
                            bird.y = 120;
                            bird.xvel = 304;
                            birdAnim.running = true;
                            birdAnim.frame = 0;
                            birdAnim.numFrames = 4;
                            birdAnim.delay = 0.1f;
                            birdAnim.maxDelay = 0.1f;
                            birdAnim.loop = true;
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
                    //Player character decelerates
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
                    //Player character jumps into the bus
                    pl.x = bus.x + 342;
                    pl.xvel = 0;
                    jumpTimeout = JUMP_TIMEOUT; //Trigger a jump
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
                    //Player character is now in the bus and score count starts
                    showPlayerInBus();
                    startScoreCount();
                    ctx.sequenceStep++;
                }
                break;

            case 101:
                if (!ctx.countingScore) {
                    //Score count finished
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
                    //Player character is now in the bus and score count starts
                    showPlayerInBus();
                    startScoreCount();
                    ctx.sequenceStep++;
                }
                break;

            case 201:
                if (!ctx.countingScore) {
                    //Score count finished
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 202:
                //Bus front door closes
                startAnimation(ANIM_BUS_DOOR_FRONT);
                ctx.sequenceDelay = 0.5f;
                ctx.sequenceStep++;
                break;

            case 203:
                //Bearded man comes from the right side of the screen
                cutscenePlayer.sprite = NONE;
                beardedMan.sprite = SPR_BEARDED_MAN_WALK;
                beardedMan.x = ctx.levelSize;
                beardedMan.y = 203;
                beardedMan.xvel = -150;
                beardedManAnim.running = true;
                beardedManAnim.frame = 0;
                beardedManAnim.numFrames = 6;
                beardedManAnim.delay = 0.1f;
                beardedManAnim.maxDelay = 0.1f;
                beardedManAnim.loop = true;
                ctx.sequenceStep++;
                break;

            case 204:
                if (beardedMan.x <= bus.x + 380) {
                    //Bearded man decelerates
                    beardedMan.x = bus.x + 380;
                    beardedMan.acc = 256;
                    ctx.sequenceStep++;
                }
                break;

            case 205:
                if (beardedMan.xvel >= 0 || beardedMan.x <= bus.x + 337) {
                    //Bearded man stops and bus front door opens
                    beardedMan.sprite = SPR_BEARDED_MAN_STAND;
                    beardedMan.x = bus.x + 337;
                    beardedMan.xvel = 0;
                    beardedMan.acc = 0;
                    beardedManAnim.frame = 0;
                    beardedManAnim.numFrames = 1;
                    ctx.anims[ANIM_BUS_DOOR_FRONT].reverse = false;
                    startAnimation(ANIM_BUS_DOOR_FRONT);
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 206:
                //Bearded man jumps into the bus
                beardedMan.sprite = SPR_BEARDED_MAN_JUMP;
                beardedMan.yvel = -154;
                beardedMan.grav = 230;
                ctx.sequenceStep++;
                break;

            case 207:
                if (beardedMan.y >= BUS_Y + 35 && beardedMan.yvel > 0) {
                    //Bearded man is now in the bus
                    beardedMan.sprite = SPR_BEARDED_MAN_STAND;
                    beardedMan.grav = 0;
                    beardedMan.yvel = 0;
                    beardedMan.x -= bus.x; //Make position relative to the bus
                    beardedMan.y = BUS_Y + 35;
                    beardedMan.inBus = true;
                    ctx.sequenceDelay = 0.25f;
                    ctx.sequenceStep++;
                }
                break;

            case 208:
                ctx.anims[ANIM_BUS_DOOR_FRONT].reverse = true;
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 300: //SEQ_GOAL_REACHED_LEVEL3
                //Player character slips on a banana peel and hits the floor
                if (pl.onFloor) {
                    ctx.sequenceDelay = 0.25f;
                    ctx.sequenceStep++;
                }
                break;

            case 301:
                inputRight = !inputRight;
                oldInputRight = !inputRight;
                if (pl.state == PLAYER_STATE_GETUP) {
                    //Player character gets up after slipping on a banana peel
                    //and starts walking again
                    inputRight = true;
                    oldInputRight = false;
                    ctx.sequenceStep++;
                }
                break;

            case 302:
                if (pl.x >= bus.x + 342) {
                    //Player character jumps into the bus
                    pl.x = bus.x + 342;
                    pl.xvel = 0;
                    inputRight = false;
                    jumpTimeout = JUMP_TIMEOUT; //Trigger a jump
                    ctx.sequenceStep++;
                }
                break;

            case 303:
                if (pl.yvel > 0 && pl.y >= BUS_Y + 36) {
                    //Player character is now in the bus and score count starts
                    showPlayerInBus();
                    startScoreCount();
                    ctx.sequenceStep++;
                }
                break;

            case 304:
                if (!ctx.countingScore) {
                    //Score count finished
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
                    //Player character stops at bus front door
                    pl.x = bus.x + 342;
                    pl.xvel = 0;
                }
                if (bird.x >= bus.x + 353) {
                    //Bird dung appears
                    dung.sprite = SPR_DUNG;
                    dung.x = bus.x + 353;
                    dung.y = bird.y;
                    dung.yvel = 256;
                    ctx.sequenceStep++;
                }
                break;

            case 401:
                if (dung.y >= pl.y + 12) {
                    //Bird dung hits the player character
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
                //Player character cleans the dung
                cutscenePlayerAnim.running = true;
                cutscenePlayerAnim.frame = 0;
                cutscenePlayerAnim.numFrames = 9;
                cutscenePlayerAnim.delay = 0.2f;
                cutscenePlayerAnim.maxDelay = 0.2f;
                cutscenePlayerAnim.loop = false;
                ctx.sequenceDelay = 2.0f;
                ctx.sequenceStep++;
                break;

            case 403:
                //Player character finishes cleaning the dung
                pl.visible = true;
                cutscenePlayer.sprite = NONE;
                ctx.sequenceDelay = 0.25f;
                ctx.sequenceStep++;
                break;

            case 404:
                //Player character jumps into the bus
                jumpTimeout = JUMP_TIMEOUT; //Trigger a jump
                ctx.sequenceStep++;
                break;

            case 405:
                if (pl.yvel > 0 && pl.y >= BUS_Y + 36) {
                    //Player character is now in the bus and score count starts
                    showPlayerInBus();
                    startScoreCount();
                    ctx.sequenceStep++;
                }
                break;

            case 406:
                if (!ctx.countingScore) {
                    //Score count finished
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 407:
                ctx.sequenceStep = SEQ_BUS_LEAVING;
                break;


            //------------------------------------------------------------------
            case 500: //SEQ_GOAL_REACHED_LEVEL5
                //Bus leaves before the player character can enter it
                startAnimation(ANIM_BUS_DOOR_FRONT);
                bus.acc = 256;
                bus.xvel = 4;
                ctx.sequenceStep++;
                break;

            case 501:
                if (bus.x >= ctx.levelSize + 32) {
                    //Player character starts running crazily
                    bus.acc = 0;
                    bus.xvel = 0;
                    pl.visible = false;
                    cutscenePlayer.sprite = SPR_PLAYER_RUN;
                    cutscenePlayer.x = pl.x;
                    cutscenePlayer.y = pl.y;
                    cutscenePlayer.xvel = 128;
                    cutscenePlayer.acc = 512;
                    cutscenePlayerAnim.running = true;
                    cutscenePlayerAnim.numFrames = 4;
                    cutscenePlayerAnim.loop = true;
                    cutscenePlayerAnim.delay = 0.1f;
                    cutscenePlayerAnim.maxDelay = 0.1f;
                    ctx.sequenceStep++;
                }
                break;

            case 502:
                if (cutscenePlayer.x >= ctx.levelSize + 32) {
                    //Score count starts
                    startScoreCount();
                    cutscenePlayer.xvel = 0;
                    ctx.sequenceStep++;
                }
                break;

            case 503:
                if (!ctx.countingScore) {
                    //Score count finished
                    ctx.sequenceDelay = 0.5f;
                    ctx.sequenceStep++;
                }
                break;

            case 504:
                //Screen wipes to black
                ctx.wipeOut = true;
                ctx.sequenceDelay = 1;
                ctx.sequenceStep++;
                break;

            case 505:
                ctx.sequenceStep = SEQ_FINISHED;
                break;


            //------------------------------------------------------------------
            case 800: //SEQ_ENDING
                //Ending sequence, with a traffic jam and a flagman
                pl.visible = false;
                pl.state = PLAYER_STATE_INACTIVE;

                cam.x = VSCREEN_MAX_WIDTH + 24;

                ctx.car.x = VSCREEN_MAX_WIDTH + 16;
                ctx.car.type = TRAFFIC_JAM;
                ctx.car.xvel = 0;

                bus.x = VSCREEN_MAX_WIDTH + 16 - 408;
                bus.xvel = 0;
                bus.routeSign = 0; //Finish (checkered flag) sign

                flagman.sprite = SPR_FLAGMAN;
                flagman.x = VSCREEN_MAX_WIDTH * 2 + 32;
                flagman.y = 180;
                flagmanAnim.running = false;
                flagmanAnim.loop = false;
                flagmanAnim.reverse = false;
                flagmanAnim.frame = 3;
                flagmanAnim.numFrames = 4;
                flagmanAnim.delay = 0.1f;
                flagmanAnim.maxDelay = 0.1f;

                ctx.sequenceDelay = 1;
                ctx.sequenceStep++;
                break;

            case 801:
                //Camera moves to the right
                cam.xvel = CAMERA_XVEL / 4;
                cam.xdest = VSCREEN_MAX_WIDTH * 2 - 136;
                ctx.sequenceDelay = 3;
                ctx.sequenceStep++;
                break;

            case 802:
                //Traffic jam starts moving
                ctx.car.xvel = 64;
                bus.xvel = 64;
                ctx.anims[ANIM_CAR_WHEELS].delay = 0.1f;
                ctx.anims[ANIM_CAR_WHEELS].maxDelay = 0.1f;
                startAnimation(ANIM_CAR_WHEELS);
                ctx.sequenceStep++;
                break;

            case 803:
                if (ctx.car.x >= VSCREEN_MAX_WIDTH + 152) {
                    //Traffic jam stops
                    ctx.car.x = VSCREEN_MAX_WIDTH + 152;
                    ctx.car.xvel = 0;
                    bus.x = ctx.car.x - 400;
                    bus.xvel = 0;
                    ctx.anims[ANIM_CAR_WHEELS].running = false;
                    ctx.anims[ANIM_CAR_WHEELS].frame = 0;
                    ctx.sequenceDelay = 1;
                    ctx.sequenceStep++;
                }
                break;

            case 804:
                //Player character appears from the left side of the screen and
                //is running crazily
                cutscenePlayer.sprite = SPR_PLAYER_RUN;
                cutscenePlayer.x = cam.x - 80;
                cutscenePlayer.y = 204;
                cutscenePlayer.xvel = 210;
                cutscenePlayerAnim.running = true;
                cutscenePlayerAnim.numFrames = 4;
                cutscenePlayerAnim.loop = true;
                cutscenePlayerAnim.delay = 0.1f;
                cutscenePlayerAnim.maxDelay = 0.1f;
                ctx.sequenceStep++;
                break;

            case 805:
                if (cutscenePlayer.x > flagman.x && !ctx.playerReachedFlagman) {
                    //Player character reaches the flagman, who swings the flag
                    ctx.playerReachedFlagman = true;
                    flagmanAnim.frame = 0;
                    flagmanAnim.running = true;
                }
                if (cutscenePlayer.x >= cam.x + 304) {
                    //Player character decelerates
                    cutscenePlayer.x = cam.x + 304;
                    cutscenePlayer.acc = -256;
                    ctx.sequenceStep++;
                }
                break;

            case 806:
                if (cutscenePlayer.xvel <= 128) {
                    if (cutscenePlayer.sprite == SPR_PLAYER_RUN) {
                        cutscenePlayer.sprite = SPR_PLAYER_WALK;
                        cutscenePlayer.x += 8;
                    }
                }
                if (cutscenePlayer.xvel <= 0 || cutscenePlayer.x >= cam.x + 392) {
                    //Player character stops
                    cutscenePlayer.x = cam.x + 392;
                    cutscenePlayer.xvel = 0;
                    cutscenePlayer.acc = 0;
                    cutscenePlayer.sprite = SPR_PLAYER_STAND;
                    cutscenePlayerAnim.running = false;
                    cutscenePlayerAnim.frame = 0;
                    ctx.sequenceDelay = 1;
                    ctx.sequenceStep++;
                }
                break;

            case 807:
                //Traffic jam starts moving
                ctx.car.xvel = 64;
                bus.xvel = 64;
                startAnimation(ANIM_CAR_WHEELS);
                ctx.sequenceStep++;
                break;

            case 808:
                if (ctx.car.x >= VSCREEN_MAX_WIDTH + 424) {
                    //Traffic jam stops
                    ctx.car.x = VSCREEN_MAX_WIDTH + 424;
                    ctx.car.xvel = 0;
                    bus.x = ctx.car.x - 400;
                    bus.xvel = 0;
                    ctx.anims[ANIM_CAR_WHEELS].running = false;
                    ctx.anims[ANIM_CAR_WHEELS].frame = 0;
                    ctx.sequenceDelay = 1;
                    ctx.sequenceStep++;
                }
                break;

            case 809:
                //Hen appears from the left side of the screen
                ctx.hen.x = cam.x - 64;
                ctx.hen.xvel = 350;
                startAnimation(ANIM_HEN);
                ctx.sequenceStep++;
                break;

            case 810:
                if (ctx.hen.x >= cam.x + 120) {
                    //Hen decelerates
                    ctx.hen.x = cam.x + 120;
                    ctx.hen.acc = -256;
                    ctx.sequenceStep++;
                }
                break;

            case 811:
                if (ctx.hen.x > flagman.x && !ctx.henReachedFlagman) {
                    //Hen reaches the flagman, who swings the flag
                    ctx.henReachedFlagman = true;
                    flagmanAnim.frame = 0;
                    flagmanAnim.running = true;
                }
                if (ctx.hen.xvel <= 0 || ctx.hen.x >= cam.x + 352) {
                    //Hen stops
                    ctx.hen.x = cam.x + 352;
                    ctx.hen.xvel = 0;
                    ctx.hen.acc = 0;
                    ctx.anims[ANIM_HEN].running = false;
                    ctx.anims[ANIM_HEN].frame = 1;
                    ctx.sequenceDelay = 1;
                    ctx.sequenceStep++;
                }
                break;

            case 812:
                //Traffic jam starts moving
                ctx.car.xvel = 64;
                bus.xvel = 64;
                startAnimation(ANIM_CAR_WHEELS);
                ctx.sequenceStep++;
                break;

            case 813:
                if (bus.x >= cam.x - 60) {
                    //Bus reaches the flagman, who swings the flag
                    ctx.busReachedFlagman = true;
                    flagmanAnim.frame = 0;
                    flagmanAnim.running = true;

                    //Traffic jam stops
                    ctx.car.x = cam.x - 60 + 400;
                    ctx.car.xvel = 0;
                    bus.x = cam.x - 60;
                    bus.xvel = 0;
                    ctx.anims[ANIM_CAR_WHEELS].running = false;
                    ctx.anims[ANIM_CAR_WHEELS].frame = 0;
                    startAnimation(ANIM_BUS_DOOR_FRONT);
                    ctx.sequenceDelay = 3;
                    ctx.sequenceStep++;
                }
                break;

            case 814:
                //Screen wipes to black
                ctx.wipeOut = true;
                ctx.sequenceDelay = 1;
                ctx.sequenceStep++;
                break;

            case 815:
                ctx.sequenceStep = SEQ_FINISHED;
                break;
        }
    }
}

