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

//Class containing passive static classes (without methods), constants, and
//read-only data, all of which can be statically imported by other files
public class Defs {
    //==========================================================================
    // Interface to platform-dependent methods
    //

    public static interface PlatDep {
        Config getConfig();
        void saveConfig();
    }



    //==========================================================================
    // Passive classes: configuration
    //

    public static class Config {
        public int windowMode;
        public boolean audioEnabled;

        public boolean touchEnabled;
        public boolean hideTouchControls;
        public boolean useBackKey;

        public int progressLevel;
        public int progressDifficulty;
        public boolean progressCheat;
    }



    //==========================================================================
    // Passive classes: dialogs
    //

    static class DialogItem {
        int align;
        int offsetX, offsetY;
        int width, height;
        int iconSprite;
        int targets[]; //One target for each of the four directions
        boolean disabled;
    }

    static class DialogStackEntry {
        int type;
        int selectedItem;
    }

    static class DialogCtx {
        int stackSize;
        DialogStackEntry stack[];

        int numItems;
        DialogItem items[];

        String text;
        int textOffsetX; //Offset from center of screen in 8x8 tiles
        int textOffsetY;
        int textWidth; //Width and height in 8x8 tiles
        int textHeight;

        boolean levelSelected;
        boolean selectedVisible;

        boolean showFrame;

        int action;
        int actionParam;
    }



    //==========================================================================
    // Passive classes: gameplay
    //

    static class Camera {
        float x, y;
        float xvel, yvel;
        float xdest;
    }

    static class Player {
        int state; //PLAYER_STATE_* constants
        boolean visible;
        boolean onFloor;
        boolean fell; //Fell into a deep hole
        int height;
        float flickerDelay;

        float x, y; //Position
        float xvel, yvel; //Velocity
        float acc; //Acceleration
        float dec; //Deceleration
        float grav; //Gravity

        //Animation
        int animType;
        int animCurFrame;
        int animMinFrame;
        int animMaxFrame;
        float animDelay;
        float animDelayMax;
        boolean animLoop;
        boolean animReverse;

        int oldState;
        float oldx, oldy;
        int oldAnimType;
    }

    static class Bus {
        float x; //Position
        float xvel; //Velocity
        float acc; //Acceleration

        int routeSign;
        int numCharacters;

        //Wheel animation
        int wheelAnimFrame;
        float wheelAnimDelay;

        //Rear door animation
        int rearDoorAnimFrame;
        int rearDoorAnimDelta; //1 = opening; -1 = closing; 0 = no change
        float rearDoorAnimDelay;

        //Front door animation
        int frontDoorAnimFrame;
        int frontDoorAnimDelta; //1 = opening; -1 = closing; 0 = no change
        float frontDoorAnimDelay;
    }

    //Class used for most game objects, which need only a type and a position
    static class Obj {
        int type;
        int x, y;
    }

    static class CrateBlock {
        int x, y;
        int width, height;
    }

    static class Geyser {
        int obj; //Index of the geyser within PlayCtx.objs[]
        float y;
        float yvel;
        float ydest;
        int movePattern[];
        int movePatternPos;
    }

    //Rope grabbed by the player character
    static class GrabbedRope {
        int obj; //Index of the rope within PlayCtx.objs[]
        float x;
        float xmin, xmax;
        float xvel;
    }

    //Spring hit by the player character
    static class HitSpring {
        int obj; //Index of the spring within PlayCtx.objs[]
        int animFrame;
        float animDelay;
    }

    //Moving banana peel
    static class MovingPeel {
        int obj; //Index of the peel within PlayCtx.objs[]
        float x, y;
        float xmax; //Used only by thrown peels (not by slipped peels)
        float xvel, yvel;
        float grav;
    }

    static class PushableCrate {
        int obj; //Index of the crate within PlayCtx.objs[]
        float x;
        boolean pushed;
        float xmax;
        int solid; //Index within PlayCtx.solids[]
    }

    //An invisible area the player character cannot pass through, which is
    //placed along with the floor, crates, and so on
    static class Solid {
        int type, left, right, top, bottom;
    }

    //Used for both deep holes and underground passageways
    static class Hole {
        int type;
        int x, width;
    }

    static class RespawnPoint {
        int x, y;
    }

    //When the player character reaches the X position of a trigger (regardless
    //of Y position), either a passing car or a hen is triggered
    static class Trigger {
        int x;
        int what; //CAR_BLUE, CAR_SILVER, CAR_YELLOW, or TRIGGER_HEN
    }

    static class PassingCar {
        float x;
        int color; //CAR_BLUE, CAR_SILVER, or CAR_YELLOW
        boolean threwPeel;
        int peelThrowX; //Throw a banana peel when the car reached this X position
        int wheelAnimFrame;
        float wheelAnimDelay;
    }

    static class Hen {
        float x;
        int animFrame;
        float animDelay;
    }

    static class CoinSpark {
        int x, y;
        boolean gold;
        int animFrame;
        float animDelay;
    }

    static class CrackParticle {
        float x, y;
        float xvel, yvel;
        float grav;
    }

    //Gameplay context
    static class PlayCtx {
        boolean playing;
        boolean canPause;
        int difficulty;
        int levelNum;
        int levelSize;
        int bgColor;
        int bgOffsetX;
        int bgm;

        int busStopSignX;
        int poleX;

        int score;
        int time;
        float timeDelay;
        boolean timeRunning;
        boolean timeUp;
        boolean goalReached;
        boolean countingScore;

        boolean playerInBus;
        int playerBusOffsetX;

        float cratePushRemaining;

        Camera cam;
        Player player;
        Bus bus;

        Obj objs[];
        CrateBlock crateBlocks[];
        Geyser geysers[];
        GrabbedRope grabbedRope;
        HitSpring hitSpring;
        MovingPeel slipPeel;
        MovingPeel thrownPeel;
        PushableCrate pushableCrates[];
        Solid solids[];

        Hole holes[];
        Hole curPassageway; //Passageway the player character is in, if any

        RespawnPoint respawnPoints[];
        Trigger triggers[];

        //Objects that appear when triggered
        PassingCar passingCar;
        Hen hen;

        //Visual effects
        CoinSpark coinSparks[];
        CrackParticle crackParticles[];

        int nextCoinSpark;
        int nextCrackParticle;

        int geyserAnimFrame;
        float geyserAnimDelay;

        int coinAnimFrame;
        float coinAnimDelay;

        int crackParticleAnimFrame;
        float crackParticleAnimDelay;

        int sequencePart;
        float sequenceDelay;
        boolean wipeToBlack;
        boolean wipeFromBlack;
    }



    //==========================================================================
    // Constants: general
    //

    //Version and repository
    public static final String VERSION = "pre1";
    public static final String REPOSITORY = "https://github.com/M374LX/alexvsbus-java";

    //Maximum delta time
    static final float MAX_DT = 1.0f / 30.0f;

    //Difficulty
    public static final int DIFFICULTY_NORMAL = 0;
    public static final int DIFFICULTY_HARD = 1;
    public static final int DIFFICULTY_MAX = DIFFICULTY_HARD;

    //Number of levels per difficulty
    public static final int NUM_LEVELS = 5;

    //Level load errors
    static final int LVLERR_NONE = 0;
    static final int LVLERR_CANNOT_OPEN = 1;
    static final int LVLERR_TOO_LARGE = 2;
    static final int LVLERR_INVALID = 3;

    //Screen size
    public static final int SCREEN_WIDTH = 480;
    public static final int SCREEN_MIN_HEIGHT = 270;
    static final float DEFAULT_ASPECT_RATIO = 16.0f / 9.0f;

    //Screen wiping effects
    static final int WIPE_MAX_VALUE = SCREEN_WIDTH;
    static final int WIPE_DELTA = 16;
    static final float WIPE_DELAY = 0.0005f;

    //Like in many retro games, 8x8 tiles are commonly used as the basic unit
    //for positioning and size
    static final int TILE_SIZE = 8;

    //Special constant meaning that something does not exist or is unset or
    //inactive
    //
    //Commonly used as an object's X position or as an index within
    //PlayCtx.objs[]
    public static final int NONE = -1;



    //==========================================================================
    // Constants: configuration
    //

    //Window modes
    public static final int WM_UNSUPPORTED = -1;
    public static final int WM_UNSET = 0;
    public static final int WM_1X = 1;
    public static final int WM_2X = 2;
    public static final int WM_3X = 3;
    public static final int WM_FULLSCREEN = 4;



    //==========================================================================
    // Constants: input
    //

    //Player input actions (bitfield)
    static final int INPUT_UP = (1 << 0);
    static final int INPUT_DOWN = (1 << 1);
    static final int INPUT_LEFT = (1 << 2);
    static final int INPUT_RIGHT = (1 << 3);
    static final int INPUT_JUMP = (1 << 4);
    static final int INPUT_PAUSE = (1 << 5);
    static final int INPUT_PAUSE_TOUCH = (1 << 6);
    static final int INPUT_DIALOG_CONFIRM = (1 << 7);
    static final int INPUT_DIALOG_RETURN = (1 << 8);
    static final int INPUT_CFG_WINDOW_MODE = (1 << 9);
    static final int INPUT_CFG_AUDIO_TOGGLE = (1 << 10);

    //Input actions from game controller (joystick, joypad, ...) buttons
    static final int JOY_A = 0;
    static final int JOY_B = 1;
    static final int JOY_X = 2;
    static final int JOY_START = 3;
    static final int JOY_SELECT = 4;
    static final int JOY_DPAD_UP = 5;
    static final int JOY_DPAD_DOWN = 6;
    static final int JOY_DPAD_LEFT = 7;
    static final int JOY_DPAD_RIGHT = 8;
    static final int JOY_NUM_BUTTONS = 9;

    static final float JOY_AXIS_DEADZONE = 0.2f;

    //Touchscreen button positions
    static final int TOUCH_LEFT_X = 0;
    static final int TOUCH_LEFT_OFFSET_Y = 72;
    static final int TOUCH_RIGHT_X = 64;
    static final int TOUCH_RIGHT_OFFSET_Y = 64;
    static final int TOUCH_JUMP_X = SCREEN_WIDTH - 64;
    static final int TOUCH_JUMP_OFFSET_Y = 64;

    //Touchscreen button opacity
    static final float TOUCH_OPACITY = 0.45f;



    //==========================================================================
    // Constants: audio
    //

    //Sound effects
    static final int SFX_COIN = 0;
    static final int SFX_CRATE = 1;
    static final int SFX_DIALOG_SELECT = 2;
    static final int SFX_DIALOG_CONFIRM = 3;
    static final int SFX_ERROR = 4;
    static final int SFX_FALL = 5;
    static final int SFX_HIT = 6;
    static final int SFX_HOLE = 7;
    static final int SFX_RESPAWN = 8;
    static final int SFX_SCORE = 9;
    static final int SFX_SLIP = 10;
    static final int SFX_SPRING = 11;
    static final int SFX_TIME = 12;
    static final int NUM_SFX = 13;

    //Background music (BGM) tracks
    static final int BGMTITLE = 0;
    static final int BGM1 = 1;
    static final int BGM2 = 2;
    static final int BGM3 = 3;



    //==========================================================================
    // Constants: graphics
    //

    //Sprites
    static final int SPR_BACKGROUND = 0;
    static final int SPR_BANANA_PEEL = 1;
    static final int SPR_BEARDED_MAN_STAND = 2;
    static final int SPR_BEARDED_MAN_WALK = 3;
    static final int SPR_BEARDED_MAN_JUMP = 4;
    static final int SPR_BG_BLACK = 5;
    static final int SPR_BG_SKY1 = 6;
    static final int SPR_BG_SKY2 = 7;
    static final int SPR_BG_SKY3 = 8;
    static final int SPR_BIRD = 9;
    static final int SPR_BUS = 10;
    static final int SPR_BUS_CHARACTER_1 = 11;
    static final int SPR_BUS_CHARACTER_2 = 12;
    static final int SPR_BUS_CHARACTER_3 = 13;
    static final int SPR_BUS_DOOR = 14;
    static final int SPR_BUS_ROUTE = 15;
    static final int SPR_BUS_STOP_SIGN = 16;
    static final int SPR_BUS_WHEEL = 17;
    static final int SPR_CAR_BLUE = 18;
    static final int SPR_CAR_SILVER = 19;
    static final int SPR_CAR_YELLOW = 20;
    static final int SPR_CAR_WHEEL = 21;
    static final int SPR_CHARSET = 22;
    static final int SPR_COIN_SILVER = 23;
    static final int SPR_COIN_GOLD = 24;
    static final int SPR_COIN_SPARK_SILVER = 25;
    static final int SPR_COIN_SPARK_GOLD = 26;
    static final int SPR_CRACK_PARTICLE = 27;
    static final int SPR_CRATE = 28;
    static final int SPR_DEEP_HOLE_LEFT = 29;
    static final int SPR_DEEP_HOLE_LEFT_FG = 30;
    static final int SPR_DEEP_HOLE_MIDDLE = 31;
    static final int SPR_DEEP_HOLE_RIGHT = 32;
    static final int SPR_DIGITS = 33;
    static final int SPR_DUNG = 34;
    static final int SPR_FLAGMAN = 35;
    static final int SPR_GEYSER = 36;
    static final int SPR_GEYSER_CRACK = 37;
    static final int SPR_GEYSER_HOLE = 38;
    static final int SPR_HEN = 39;
    static final int SPR_HUD_SCORE = 40;
    static final int SPR_HUD_TIME = 41;
    static final int SPR_HYDRANT = 42;
    static final int SPR_LOGO = 43;
    static final int SPR_OVERHEAD_SIGN = 44;
    static final int SPR_OVERHEAD_SIGN_BASE = 45;
    static final int SPR_PASSAGEWAY_LEFT = 46;
    static final int SPR_PASSAGEWAY_LEFT_FG = 47;
    static final int SPR_PASSAGEWAY_MIDDLE = 48;
    static final int SPR_PASSAGEWAY_RIGHT = 49;
    static final int SPR_PASSAGEWAY_RIGHT_FG = 50;
    static final int SPR_PAUSE = 51;
    static final int SPR_PLAYER_STAND = 52;
    static final int SPR_PLAYER_WALK = 53;
    static final int SPR_PLAYER_JUMP = 54;
    static final int SPR_PLAYER_GRABROPE = 55;
    static final int SPR_PLAYER_THROWBACK = 56;
    static final int SPR_PLAYER_SLIP = 57;
    static final int SPR_PLAYER_RUN = 58;
    static final int SPR_PLAYER_CLEAN_DUNG = 59;
    static final int SPR_POLE = 60;
    static final int SPR_ROPE_HORIZONTAL = 61;
    static final int SPR_ROPE_VERTICAL = 62;
    static final int SPR_SPRING = 63;
    static final int SPR_TOUCH_LEFT = 64;
    static final int SPR_TOUCH_LEFT_HELD = 65;
    static final int SPR_TOUCH_RIGHT = 66;
    static final int SPR_TOUCH_RIGHT_HELD = 67;
    static final int SPR_TOUCH_JUMP = 68;
    static final int SPR_TOUCH_JUMP_HELD = 69;
    static final int SPR_TRUCK = 70;
    static final int SPR_DIALOG_PLAY = 71;
    static final int SPR_DIALOG_PLAY_SELECTED = 72;
    static final int SPR_DIALOG_JUKEBOX = 73;
    static final int SPR_DIALOG_JUKEBOX_SELECTED = 74;
    static final int SPR_DIALOG_OPTIONS = 75;
    static final int SPR_DIALOG_OPTIONS_SELECTED = 76;
    static final int SPR_DIALOG_ABOUT = 77;
    static final int SPR_DIALOG_ABOUT_SELECTED = 78;
    static final int SPR_DIALOG_QUIT = 79;
    static final int SPR_DIALOG_QUIT_SELECTED = 80;
    static final int SPR_DIALOG_RETURN = 81;
    static final int SPR_DIALOG_RETURN_SELECTED = 82;
    static final int SPR_DIALOG_CREDITS = 83;
    static final int SPR_DIALOG_CREDITS_SELECTED = 84;
    static final int SPR_DIALOG_RESET = 85;
    static final int SPR_DIALOG_RESET_SELECTED = 86;
    static final int SPR_DIALOG_AUDIO_ON = 87;
    static final int SPR_DIALOG_AUDIO_ON_SELECTED = 88;
    static final int SPR_DIALOG_AUDIO_OFF = 89;
    static final int SPR_DIALOG_AUDIO_OFF_SELECTED = 90;
    static final int SPR_DIALOG_CONFIRM = 91;
    static final int SPR_DIALOG_CONFIRM_SELECTED = 92;
    static final int SPR_DIALOG_CANCEL = 93;
    static final int SPR_DIALOG_CANCEL_SELECTED = 94;
    static final int SPR_DIALOG_NORMAL = 95;
    static final int SPR_DIALOG_NORMAL_SELECTED = 96;
    static final int SPR_DIALOG_HARD = 97;
    static final int SPR_DIALOG_HARD_SELECTED = 98;
    static final int SPR_DIALOG_HARD_DISABLED = 99;
    static final int SPR_DIALOG_1 = 100;
    static final int SPR_DIALOG_1_SELECTED = 101;
    static final int SPR_DIALOG_2 = 102;
    static final int SPR_DIALOG_2_SELECTED = 103;
    static final int SPR_DIALOG_3 = 104;
    static final int SPR_DIALOG_3_SELECTED = 105;
    static final int SPR_DIALOG_4 = 106;
    static final int SPR_DIALOG_4_SELECTED = 107;
    static final int SPR_DIALOG_5 = 108;
    static final int SPR_DIALOG_5_SELECTED = 109;
    static final int SPR_DIALOG_LOCKED = 110;
    static final int SPR_DIALOG_BORDER_TOPLEFT = 111;
    static final int SPR_DIALOG_BORDER_TOPLEFT_SELECTED = 112;
    static final int SPR_DIALOG_BORDER_TOPLEFT_DISABLED = 113;
    static final int SPR_DIALOG_BORDER_TOP = 114;
    static final int SPR_DIALOG_BORDER_TOP_SELECTED = 115;
    static final int SPR_DIALOG_BORDER_TOP_DISABLED = 116;
    static final int SPR_DIALOG_BORDER_LEFT = 117;
    static final int SPR_DIALOG_BORDER_LEFT_SELECTED = 118;
    static final int SPR_DIALOG_BORDER_LEFT_DISABLED = 119;

    static final int LOGO_WIDTH = 296;



    //==========================================================================
    // Constants: dialogs
    //

    static final int DIALOG_MAX_STACK_SIZE = 4;
    static final int DIALOG_MAX_ITEMS = 16;

    //Cursor movement directions
    static final int DLGDIR_UP = 0;
    static final int DLGDIR_DOWN = 1;
    static final int DLGDIR_LEFT = 2;
    static final int DLGDIR_RIGHT = 3;

    //Item alignment
    static final int ALIGN_CENTER = 0;
    static final int ALIGN_TOPLEFT = 1;
    static final int ALIGN_TOPRIGHT = 2;

    //Dialog types
    static final int DLG_MAIN = 0;
    static final int DLG_DIFFICULTY = 1;
    static final int DLG_LEVEL = 2;
    static final int DLG_JUKEBOX = 3;
    static final int DLG_ABOUT = 4;
    static final int DLG_CREDITS = 5;
    static final int DLG_PAUSE = 6;
    static final int DLG_QUIT = 7;
    static final int DLG_ERROR = 8;

    //Dialog actions
    static final int DLGACT_QUIT = 0;
    static final int DLGACT_PLAY = 1;



    //==========================================================================
    // Constants: gameplay
    //

    //Maximum numbers
    static final int MAX_OBJS = 160;
    static final int MAX_CRATE_BLOCKS = 32;
    static final int MAX_HOLES = 32;
    static final int MAX_GEYSERS = 32;
    static final int MAX_PASSAGEWAYS = 4;
    static final int MAX_PUSHABLE_CRATES = MAX_PASSAGEWAYS;
    static final int MAX_SOLIDS = 96;
    static final int MAX_TRIGGERS = 8;
    static final int MAX_RESPAWN_POINTS = 32;
    static final int MAX_COIN_SPARKS = 12;
    static final int MAX_CRACK_PARTICLES = 12;

    //A level block is the basic unit for positioning objects in the level, as
    //well as for the width of deep holes and passageways
    static final int LEVEL_BLOCK_SIZE = TILE_SIZE * 3;
    static final int SCREEN_WIDTH_LEVEL_BLOCKS = SCREEN_WIDTH / LEVEL_BLOCK_SIZE;

    //Floor, holes, light poles, and background
    static final int BACKGROUND_DRAW_Y = 176;
    static final int POLE_DISTANCE = 384;
    static final int FLOOR_Y = 264;
    static final int PASSAGEWAY_BOTTOM_Y = 360;

    //Crate size
    static final int CRATE_WIDTH = 24;
    static final int CRATE_HEIGHT = 24;

    //Hole types
    static final int HOLE_DEEP = 0;
    static final int HOLE_PASSAGEWAY_EXIT_CLOSED = 1;
    static final int HOLE_PASSAGEWAY_EXIT_OPENED = 2;

    //Solid types
    static final int SOL_FULL = 0;
    static final int SOL_VERTICAL = 1;
    static final int SOL_SLOPE_UP = 2;
    static final int SOL_SLOPE_DOWN = 3;
    static final int SOL_KEEP_ON_TOP = 4;
    static final int SOL_PASSAGEWAY_ENTRY = 5;
    static final int SOL_PASSAGEWAY_EXIT = 6;

    //Object types (for objects that use PlayCtx.objs[])
    static final int OBJ_COIN_SILVER = 0;
    static final int OBJ_COIN_GOLD = 1;
    static final int OBJ_CRATE_PUSHABLE = 2;
    static final int OBJ_BANANA_PEEL = 3;
    static final int OBJ_BANANA_PEEL_MOVING = 4;
    static final int OBJ_GEYSER = 5;
    static final int OBJ_GEYSER_CRACK = 6;
    static final int OBJ_ROPE_HORIZONTAL = 7;
    static final int OBJ_ROPE_VERTICAL = 8;
    static final int OBJ_SPRING = 9;
    static final int OBJ_HYDRANT = 10;
    static final int OBJ_OVERHEAD_SIGN = 11;
    static final int OBJ_PARKED_CAR_BLUE = 12;
    static final int OBJ_PARKED_CAR_SILVER = 13;
    static final int OBJ_PARKED_CAR_YELLOW = 14;
    static final int OBJ_PARKED_TRUCK = 15;

    //Player character's bounding box and height
    static final int PLAYER_BOX_OFFSET_X = 8;
    static final int PLAYER_BOX_WIDTH = 12;
    static final int PLAYER_HEIGHT_NORMAL = 60;
    static final int PLAYER_HEIGHT_SLIP = 37;

    //Player character's states
    static final int PLAYER_STATE_NORMAL = 0;
    static final int PLAYER_STATE_SLIP = 1;
    static final int PLAYER_STATE_GETUP = 2;
    static final int PLAYER_STATE_THROWBACK = 3;
    static final int PLAYER_STATE_GRABROPE = 4;
    static final int PLAYER_STATE_FLICKER = 5;
    static final int PLAYER_STATE_INACTIVE = 6;

    //Player character's animation types
    static final int PLAYER_ANIM_STAND = 0;
    static final int PLAYER_ANIM_WALK = 1;
    static final int PLAYER_ANIM_WALKBACK = 2;
    static final int PLAYER_ANIM_JUMP = 3;
    static final int PLAYER_ANIM_SLIP = 4;
    static final int PLAYER_ANIM_SLIPREV = 5; //Reverse slip
    static final int PLAYER_ANIM_THROWBACK = 6;
    static final int PLAYER_ANIM_GRABROPE = 7;

    //Car colors
    static final int CAR_BLUE = 0;
    static final int CAR_SILVER = 1;
    static final int CAR_YELLOW = 2;

    //Triggers (other than the car colors above)
    static final int TRIGGER_HEN = 3;

    //Objects with a fixed Y position
    static final int BUS_Y = 128;
    static final int BUS_STOP_SIGN_Y = 176;
    static final int POLE_Y = 120;
    static final int GEYSER_CRACK_Y = 260;
    static final int GEYSER_INITIAL_Y = 232;
    static final int HYDRANT_Y = 240;
    static final int PARKED_CAR_Y = 208;
    static final int PARKED_TRUCK_Y = 136;
    static final int ROPE_Y = 144;
    static final int PUSHABLE_CRATE_Y = 240;
    static final int PASSING_CAR_Y = 184;
    static final int HEN_Y = 224;

    //Camera velocity
    static final int CAMERA_XVEL = 700;
    static final int CAMERA_YVEL = 400;

    //Sequence parts
    static final int SEQ_NORMAL_PLAY = 0;
    static final int SEQ_INITIAL_DELAY = 10;
    static final int SEQ_BUS_LEAVING = 20;
    static final int SEQ_TIMEUP_BUS_NEAR = 30;
    static final int SEQ_TIMEUP_BUS_FAR = 40;
    static final int SEQ_GOAL_REACHED = 100;
    static final int SEQ_FINISHED = 999;



    //==========================================================================
    // Read-only data
    //

    static final int[] cheatSequence = new int[]{
        3, 0, 2, 1, 3, 0, 1, 2, 1, 3, 0, 2, -1
    };

    //x, y, width, height
    static final int[] sprites = new int[]{
        0,    296,  96,  192, //SPR_BACKGROUND
        928,  144,  8,   8,   //SPR_BANANA_PEEL
        104,  272,  24,  64,  //SPR_BEARDED_MAN_STAND
        136,  272,  32,  64,  //SPR_BEARDED_MAN_WALK
        328,  272,  24,  64,  //SPR_BEARDED_MAN_JUMP
        984,  144,  8,   8,   //SPR_BG_BLACK
        992,  144,  8,   8,   //SPR_BG_SKY1
        1000, 144,  8,   8,   //SPR_BG_SKY2
        1008, 144,  8,   8,   //SPR_BG_SKY3
        784,  72,   16,  8,   //SPR_BIRD
        592,  312,  408, 104, //SPR_BUS
        600,  424,  24,  64,  //SPR_BUS_CHARACTER_1
        624,  424,  24,  64,  //SPR_BUS_CHARACTER_2
        648,  424,  24,  64,  //SPR_BUS_CHARACTER_3
        840,  424,  40,  88,  //SPR_BUS_DOOR
        672,  472,  32,  32,  //SPR_BUS_ROUTE
        736,  0,    16,  88,  //SPR_BUS_STOP_SIGN
        688,  424,  48,  40,  //SPR_BUS_WHEEL
        448,  288,  136, 56,  //SPR_CAR_BLUE
        448,  352,  136, 56,  //SPR_CAR_SILVER
        448,  416,  136, 56,  //SPR_CAR_YELLOW
        528,  480,  24,  24,  //SPR_CAR_WHEEL
        576,  680,  8,   8,   //SPR_CHARSET
        928,  112,  8,   8,   //SPR_COIN_SILVER
        928,  128,  8,   8,   //SPR_COIN_GOLD
        976,  112,  8,   8,   //SPR_COIN_SPARK_SILVER
        976,  128,  8,   8,   //SPR_COIN_SPARK_GOLD
        872,  64,   8,   8,   //SPR_CRACK_PARTICLE
        872,  0,    24,  24,  //SPR_CRATE
        104,  368,  24,  120, //SPR_DEEP_HOLE_LEFT
        104,  344,  24,  16,  //SPR_DEEP_HOLE_LEFT_FG
        128,  368,  24,  120, //SPR_DEEP_HOLE_MIDDLE
        152,  368,  24,  120, //SPR_DEEP_HOLE_RIGHT
        736,  552,  8,   8,   //SPR_DIGITS
        856,  72,   8,   8,   //SPR_DUNG
        8,    176,  8,   88,  //SPR_FLAGMAN
        952,  0,    24,  88,  //SPR_GEYSER
        1000, 96,   16,  8,   //SPR_GEYSER_CRACK
        976,  96,   16,  8,   //SPR_GEYSER_HOLE
        760,  88,   32,  32,  //SPR_HEN
        776,  520,  40,  8,   //SPR_HUD_SCORE
        784,  536,  32,  8,   //SPR_HUD_TIME
        872,  32,   16,  24,  //SPR_HYDRANT
        712,  680,  296, 80,  //SPR_LOGO
        824,  0,    40,  40,  //SPR_OVERHEAD_SIGN
        1008, 184,  8,   320, //SPR_OVERHEAD_SIGN_BASE
        176,  368,  24,  120, //SPR_PASSAGEWAY_LEFT
        136,  344,  32,  16,  //SPR_PASSAGEWAY_LEFT_FG
        200,  368,  24,  120, //SPR_PASSAGEWAY_MIDDLE
        224,  368,  24,  120, //SPR_PASSAGEWAY_RIGHT
        176,  344,  24,  16,  //SPR_PASSAGEWAY_RIGHT_FG
        736,  520,  32,  32,  //SPR_PAUSE
        0,    0,    32,  64,  //SPR_PLAYER_STAND
        32,   0,    32,  64,  //SPR_PLAYER_WALK
        224,  0,    32,  64,  //SPR_PLAYER_JUMP
        256,  0,    32,  64,  //SPR_PLAYER_GRABROPE
        288,  0,    32,  64,  //SPR_PLAYER_THROWBACK
        384,  0,    48,  64,  //SPR_PLAYER_SLIP
        208,  72,   48,  64,  //SPR_PLAYER_RUN
        0,    72,   24,  64,  //SPR_PLAYER_CLEAN_DUNG
        904,  24,   24,  136, //SPR_POLE
        616,  168,  400, 16,  //SPR_ROPE_HORIZONTAL
        936,  56,   8,   48,  //SPR_ROPE_VERTICAL
        760,  120,  24,  16,  //SPR_SPRING
        824,  520,  64,  64,  //SPR_TOUCH_LEFT
        824,  584,  64,  64,  //SPR_TOUCH_LEFT_HELD
        888,  520,  64,  64,  //SPR_TOUCH_RIGHT
        888,  584,  64,  64,  //SPR_TOUCH_RIGHT_HELD
        952,  520,  64,  64,  //SPR_TOUCH_JUMP
        952,  584,  64,  64,  //SPR_TOUCH_JUMP_HELD
        720,  184,  280, 128, //SPR_TRUCK
        8,    512,  96,  32,  //SPR_DIALOG_PLAY
        296,  512,  96,  32,  //SPR_DIALOG_PLAY_SELECTED
        112,  512,  32,  32,  //SPR_DIALOG_JUKEBOX
        400,  512,  32,  32,  //SPR_DIALOG_JUKEBOX_SELECTED
        152,  512,  32,  32,  //SPR_DIALOG_OPTIONS
        440,  512,  32,  32,  //SPR_DIALOG_OPTIONS_SELECTED
        192,  512,  32,  32,  //SPR_DIALOG_ABOUT
        480,  512,  32,  32,  //SPR_DIALOG_ABOUT_SELECTED
        232,  512,  32,  32,  //SPR_DIALOG_QUIT
        520,  512,  32,  32,  //SPR_DIALOG_QUIT_SELECTED
        8,    552,  24,  24,  //SPR_DIALOG_RETURN
        296,  552,  24,  24,  //SPR_DIALOG_RETURN_SELECTED
        208,  592,  56,  24,  //SPR_DIALOG_CREDITS
        496,  592,  56,  24,  //SPR_DIALOG_CREDITS_SELECTED
        40,   552,  24,  24,  //SPR_DIALOG_RESET
        328,  552,  24,  24,  //SPR_DIALOG_RESET_SELECTED
        72,   552,  24,  24,  //SPR_DIALOG_AUDIO_ON
        360,  552,  24,  24,  //SPR_DIALOG_AUDIO_ON_SELECTED
        104,  552,  24,  24,  //SPR_DIALOG_AUDIO_OFF
        392,  552,  24,  24,  //SPR_DIALOG_AUDIO_OFF_SELECTED
        152,  552,  32,  32,  //SPR_DIALOG_CONFIRM
        440,  552,  32,  32,  //SPR_DIALOG_CONFIRM_SELECTED
        192,  552,  32,  32,  //SPR_DIALOG_CANCEL
        480,  552,  32,  32,  //SPR_DIALOG_CANCEL_SELECTED
        8,    632,  48,  24,  //SPR_DIALOG_NORMAL
        296,  632,  48,  24,  //SPR_DIALOG_NORMAL_SELECTED
        64,   632,  48,  24,  //SPR_DIALOG_HARD
        352,  632,  48,  24,  //SPR_DIALOG_HARD_SELECTED
        120,  632,  48,  24,  //SPR_DIALOG_HARD_DISABLED
        8,    592,  32,  32,  //SPR_DIALOG_1
        296,  592,  32,  32,  //SPR_DIALOG_1_SELECTED
        48,   592,  32,  32,  //SPR_DIALOG_2
        336,  592,  32,  32,  //SPR_DIALOG_2_SELECTED
        88,   592,  32,  32,  //SPR_DIALOG_3
        376,  592,  32,  32,  //SPR_DIALOG_3_SELECTED
        128,  592,  32,  32,  //SPR_DIALOG_4
        416,  592,  32,  32,  //SPR_DIALOG_4_SELECTED
        168,  592,  32,  32,  //SPR_DIALOG_5
        456,  592,  32,  32,  //SPR_DIALOG_5_SELECTED
        232,  552,  32,  32,  //SPR_DIALOG_LOCKED
        144,  672,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT
        192,  672,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT_SELECTED
        240,  672,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT_DISABLED
        160,  672,  8,   8,   //SPR_DIALOG_BORDER_TOP
        208,  672,  8,   8,   //SPR_DIALOG_BORDER_TOP_SELECTED
        256,  672,  8,   8,   //SPR_DIALOG_BORDER_TOP_DISABLED
        176,  672,  8,   8,   //SPR_DIALOG_BORDER_LEFT
        224,  672,  8,   8,   //SPR_DIALOG_BORDER_LEFT_SELECTED
        272,  672,  8,   8,   //SPR_DIALOG_BORDER_LEFT_DISABLED
    };

    //Sprite corresponding to each player character animation type
    static final int[] playerAnimSprites = new int[]{
        SPR_PLAYER_STAND, //PLAYER_ANIM_STAND
        SPR_PLAYER_WALK, //PLAYER_ANIM_WALK
        SPR_PLAYER_WALK, //PLAYER_ANIM_WALKBACK
        SPR_PLAYER_JUMP, //PLAYER_ANIM_JUMP
        SPR_PLAYER_SLIP, //PLAYER_ANIM_SLIP
        SPR_PLAYER_SLIP, //PLAYER_ANIM_SLIPREV
        SPR_PLAYER_THROWBACK, //PLAYER_ANIM_THROWBACK
        SPR_PLAYER_GRABROPE, //PLAYER_ANIM_GRABROPE
    };

    //Sprite corresponding to each object type (OBJ_* constants)
    static final int[] objSprites = new int[]{
        SPR_COIN_SILVER, //OBJ_COIN_SILVER
        SPR_COIN_GOLD, //OBJ_COIN_GOLD
        SPR_CRATE, //OBJ_CRATE_PUSHABLE
        SPR_BANANA_PEEL, //OBJ_BANANA_PEEL
        SPR_BANANA_PEEL, //OBJ_BANANA_PEEL_MOVING
        SPR_GEYSER, //OBJ_GEYSER
        SPR_GEYSER_CRACK, //OBJ_GEYSER_CRACK
        SPR_ROPE_HORIZONTAL, //OBJ_ROPE_HORIZONTAL
        SPR_ROPE_VERTICAL, //OBJ_ROPE_VERTICAL
        SPR_SPRING, //OBJ_SPRING
        SPR_HYDRANT, //OBJ_HYDRANT
        SPR_OVERHEAD_SIGN, //OBJ_OVERHEAD_SIGN
        SPR_CAR_BLUE, //OBJ_PARKED_CAR_BLUE
        SPR_CAR_SILVER, //OBJ_PARKED_CAR_SILVER
        SPR_CAR_YELLOW, //OBJ_PARKED_CAR_YELLOW
        SPR_TRUCK, //OBJ_PARKED_TRUCK
    };

    //Geyser movement patterns
    //
    //For each pair of values, the first value is the vertical velocity (yvel)
    //and the second value is the destination Y position (ydest)
    //
    //The value zero indicates the end of the pattern
    static final int[] geyserMovePattern1 = {
        -64, 224, 64, 232,
        -64, 224, 64, 232,
        -64, 224, 64, 232,
        -64, 224, 64, 232,
        -64, 224, 64, 232,
        -144, 200, 144, 232,
        0,
    };

    static final int[] geyserMovePattern2 = {
        -64, 216, 64, 224,
        0,
    };
}

