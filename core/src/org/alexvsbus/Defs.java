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
    // Passive classes: display parameters and configuration
    //

    static class DisplayParams {
        //Size of the game's virtual screen
        int vscreenWidth;
        int vscreenHeight;

        //Offset and size of the viewport, which is the area of the physical
        //screen (or window) in which the virtual screen is displayed
        int viewportOffsetX;
        int viewportOffsetY;
        int viewportWidth;
        int viewportHeight;

        //Screen scale
        int scale;
    }

    public static class Config {
        public int windowMode;
        public boolean audioEnabled;
        public boolean scanlinesEnabled;

        //Touchscreen
        public boolean touchEnabled;
        public boolean touchButtonsEnabled; //Left, Right, and Jump buttons
        public boolean showTouchControls;

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
        String caption;
        String value;
        int iconSprite;
        int targets[]; //One target for each of the four directions
        boolean disabled;
        boolean hidden;
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

        boolean useCursor;

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
        boolean followPlayer;
    }

    static class Player {
        int state; //PLAYER_STATE_* constants
        boolean visible;
        boolean onFloor;
        boolean fell; //Fell into a deep hole
        int height;
        float flickerDelay;
        int animType; //Animation type

        float x, y; //Position
        float xvel, yvel; //Velocity
        float acc; //Acceleration
        float dec; //Deceleration
        float grav; //Gravity

        int oldState;
        float oldx, oldy;
        int oldAnimType;
    }

    static class Bus {
        float x; //Position
        float xvel; //Velocity
        float acc; //Acceleration

        int routeSign;
        int numCharacters; //Number of characters at the rear door
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

    static class Gush {
        int obj; //Index of the gush within PlayCtx.objs[]
        float y;
        float yvel;
        float ydest; //Destination Y position
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

    //Moving banana peel
    static class MovingPeel {
        int obj; //Index of the peel within PlayCtx.objs[]
        float x, y;
        float xdest; //Used only by thrown peels (not by slipped peels)
        float xvel, yvel;
        float grav;
    }

    static class PushableCrate {
        int obj; //Index of the crate within PlayCtx.objs[]
        float x;
        boolean showArrow;
        boolean pushed;
        float xmax;
        int solid; //Index within PlayCtx.solids[]
    }

    static class CutsceneObject {
        int sprite;
        float x, y;
        float xvel, yvel;
        float acc;
        float grav;
        boolean inBus;
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

    //Position the player character can reappear at after falling into a deep hole
    static class RespawnPoint {
        int x, y;
    }

    //When the player character reaches the X position of a trigger (regardless
    //of Y position), either a passing car or a hen is triggered
    static class Trigger {
        int x;
        int what; //CAR_BLUE, CAR_SILVER, CAR_YELLOW, or TRIGGER_HEN
    }

    //Either a single car that appears when triggered and throws a banana peel
    //or the traffic jam of the ending sequence, but not used for parked cars
    static class Car {
        float x;
        float xvel;
        int type; //CAR_BLUE, CAR_SILVER, CAR_YELLOW, or TRAFFIC_JAM
        boolean threwPeel;
        int peelThrowX; //Throw a banana peel when the car reaches this X position
    }

    static class Hen {
        float x;
        float xvel;
        float acc;
    }

    static class CoinSpark {
        int x, y;
        boolean gold;
    }

    static class CrackParticle {
        float x, y;
        float xvel, yvel;
        float grav;
    }

    //Arrow indicating that a crate is pushable
    static class PushArrow {
        float xoffs;
        float xvel;
        float delay;
    }

    //Animation
    static class Anim {
        boolean running;
        boolean loop;
        boolean reverse;
        int frame;
        int numFrames;
        float delay;
        float maxDelay;
    }

    //Gameplay context
    static class PlayCtx {
        boolean canPause;
        int difficulty;
        int levelNum;
        boolean lastLevel; //Last level of current difficulty
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

        float cratePushRemaining;

        Camera cam;
        Player player;
        Bus bus;

        Obj objs[];
        CrateBlock crateBlocks[];
        Gush gushes[];
        GrabbedRope grabbedRope;
        MovingPeel slipPeel;
        MovingPeel thrownPeel;
        PushableCrate pushableCrates[];
        CutsceneObject cutsceneObjects[];
        Solid solids[];

        int hitSpring; //Index within objs[] of the last spring hit by the
                       //player character

        Hole holes[];
        Hole curPassageway; //Passageway the player character is in, if any

        RespawnPoint respawnPoints[];
        Trigger triggers[];

        //Objects that appear when triggered
        Car car;
        Hen hen;

        //Visual effects
        CoinSpark coinSparks[];
        CrackParticle crackParticles[];
        PushArrow pushArrow;

        //Animations
        Anim anims[];

        int nextCoinSpark;
        int nextCrackParticle;

        //Used in the ending sequence
        boolean playerReachedFlagman;
        boolean henReachedFlagman;
        boolean busReachedFlagman;

        int sequenceStep;
        float sequenceDelay;
        boolean skipInitialSequence;
        boolean wipeIn;
        boolean wipeOut;
    }



    //==========================================================================
    // Constants: general
    //

    //Version and repository
    public static final String VERSION = "pre5";
    public static final String REPOSITORY = "https://github.com/M374LX/alexvsbus-java";

    //Maximum delta time
    static final float MAX_DT = 1.0f / 30.0f;

    //Screen types
    static final int SCR_BLANK = 0;
    static final int SCR_LOGO = 1;
    static final int SCR_PLAY = 2;
    static final int SCR_PLAY_FREEZE = 3; //Render a play session without upating it
    static final int SCR_FINALSCORE = 4;

    //Delayed actions
    static final int DELACT_TITLE = 0;
    static final int DELACT_NEXT_DIFFICULTY = 1;
    static final int DELACT_TRY_AGAIN = 2;

    //Difficulty
    public static final int DIFFICULTY_NORMAL = 0;
    public static final int DIFFICULTY_HARD = 1;
    public static final int DIFFICULTY_SUPER = 2;
    public static final int DIFFICULTY_MAX = DIFFICULTY_SUPER;

    //Level load errors
    static final int LVLERR_NONE = 0;
    static final int LVLERR_CANNOT_OPEN = 1;
    static final int LVLERR_TOO_LARGE = 2;
    static final int LVLERR_INVALID = 3;

    //Maximum size of the virtual screen
    public static final int VSCREEN_MAX_WIDTH  = 480;
    public static final int VSCREEN_MAX_HEIGHT = 270;

    //Screen wiping commands
    static final int WIPECMD_IN = 0;
    static final int WIPECMD_OUT = 1;
    static final int WIPECMD_CLEAR = 2;

    //Other screen wiping constants
    static final int WIPE_MAX_VALUE = VSCREEN_MAX_WIDTH;
    static final int WIPE_DELTA = 16;
    static final float WIPE_MAX_DELAY = 0.0005f;

    //Text colors
    static final int TXTCOL_WHITE = 0;
    static final int TXTCOL_GREEN = 1;
    static final int TXTCOL_GRAY  = 2;

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
    static final int INPUT_CFG_SCANLINES_TOGGLE = (1 << 11);

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
    static final int TOUCH_JUMP_OFFSET_X = 64; //Offset from right side of screen
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
    static final int SFX_ERROR = 3;
    static final int SFX_FALL = 4;
    static final int SFX_HIT = 5;
    static final int SFX_HOLE = 6;
    static final int SFX_RESPAWN = 7;
    static final int SFX_SCORE = 8;
    static final int SFX_SLIP = 9;
    static final int SFX_SPRING = 10;
    static final int SFX_TIME = 11;
    static final int NUM_SFX = 12;

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
    static final int SPR_CHARSET_WHITE = 22;
    static final int SPR_CHARSET_GREEN = 23;
    static final int SPR_CHARSET_GRAY = 24;
    static final int SPR_COIN_SILVER = 25;
    static final int SPR_COIN_GOLD = 26;
    static final int SPR_COIN_SPARK_SILVER = 27;
    static final int SPR_COIN_SPARK_GOLD = 28;
    static final int SPR_CRACK_PARTICLE = 29;
    static final int SPR_CRATE = 30;
    static final int SPR_DEEP_HOLE_LEFT = 31;
    static final int SPR_DEEP_HOLE_LEFT_FG = 32;
    static final int SPR_DEEP_HOLE_MIDDLE = 33;
    static final int SPR_DEEP_HOLE_RIGHT = 34;
    static final int SPR_DUNG = 35;
    static final int SPR_ERROR = 36;
    static final int SPR_FLAGMAN = 37;
    static final int SPR_GUSH = 38;
    static final int SPR_GUSH_CRACK = 39;
    static final int SPR_GUSH_HOLE = 40;
    static final int SPR_HEN = 41;
    static final int SPR_HUD_SCORE = 42;
    static final int SPR_HUD_TIME = 43;
    static final int SPR_HYDRANT = 44;
    static final int SPR_LOGO = 45;
    static final int SPR_MEDAL1 = 46;
    static final int SPR_MEDAL2 = 47;
    static final int SPR_MEDAL3 = 48;
    static final int SPR_OVERHEAD_SIGN = 49;
    static final int SPR_OVERHEAD_SIGN_BASE = 50;
    static final int SPR_OVERHEAD_SIGN_BASE_TOP = 51;
    static final int SPR_PASSAGEWAY_LEFT = 52;
    static final int SPR_PASSAGEWAY_LEFT_FG = 53;
    static final int SPR_PASSAGEWAY_MIDDLE = 54;
    static final int SPR_PASSAGEWAY_RIGHT = 55;
    static final int SPR_PASSAGEWAY_RIGHT_FG = 56;
    static final int SPR_PASSAGEWAY_RIGHT_CLOSED = 57;
    static final int SPR_PAUSE = 58;
    static final int SPR_PLAYER_STAND = 59;
    static final int SPR_PLAYER_WALK = 60;
    static final int SPR_PLAYER_JUMP = 61;
    static final int SPR_PLAYER_GRABROPE = 62;
    static final int SPR_PLAYER_THROWBACK = 63;
    static final int SPR_PLAYER_SLIP = 64;
    static final int SPR_PLAYER_RUN = 65;
    static final int SPR_PLAYER_CLEAN_DUNG = 66;
    static final int SPR_POLE = 67;
    static final int SPR_PUSH_ARROW = 68;
    static final int SPR_ROPE_HORIZONTAL = 69;
    static final int SPR_ROPE_VERTICAL = 70;
    static final int SPR_SPRING = 71;
    static final int SPR_TOUCH_LEFT = 72;
    static final int SPR_TOUCH_LEFT_HELD = 73;
    static final int SPR_TOUCH_RIGHT = 74;
    static final int SPR_TOUCH_RIGHT_HELD = 75;
    static final int SPR_TOUCH_JUMP = 76;
    static final int SPR_TOUCH_JUMP_HELD = 77;
    static final int SPR_TRUCK = 78;
    static final int SPR_DIALOG_PLAY = 79;
    static final int SPR_DIALOG_PLAY_SELECTED = 80;
    static final int SPR_DIALOG_JUKEBOX = 81;
    static final int SPR_DIALOG_JUKEBOX_SELECTED = 82;
    static final int SPR_DIALOG_SETTINGS = 83;
    static final int SPR_DIALOG_SETTINGS_SELECTED = 84;
    static final int SPR_DIALOG_ABOUT = 85;
    static final int SPR_DIALOG_ABOUT_SELECTED = 86;
    static final int SPR_DIALOG_QUIT = 87;
    static final int SPR_DIALOG_QUIT_SELECTED = 88;
    static final int SPR_DIALOG_RETURN = 89;
    static final int SPR_DIALOG_RETURN_SELECTED = 90;
    static final int SPR_DIALOG_RESET = 91;
    static final int SPR_DIALOG_RESET_SELECTED = 92;
    static final int SPR_DIALOG_AUDIO_ON = 93;
    static final int SPR_DIALOG_AUDIO_ON_SELECTED = 94;
    static final int SPR_DIALOG_AUDIO_OFF = 95;
    static final int SPR_DIALOG_AUDIO_OFF_SELECTED = 96;
    static final int SPR_DIALOG_TRYAGAIN = 97;
    static final int SPR_DIALOG_TRYAGAIN_SELECTED = 98;
    static final int SPR_DIALOG_CONFIRM = 99;
    static final int SPR_DIALOG_CONFIRM_SELECTED = 100;
    static final int SPR_DIALOG_CANCEL = 101;
    static final int SPR_DIALOG_CANCEL_SELECTED = 102;
    static final int SPR_DIALOG_1 = 103;
    static final int SPR_DIALOG_1_SELECTED = 104;
    static final int SPR_DIALOG_2 = 105;
    static final int SPR_DIALOG_2_SELECTED = 106;
    static final int SPR_DIALOG_3 = 107;
    static final int SPR_DIALOG_3_SELECTED = 108;
    static final int SPR_DIALOG_4 = 109;
    static final int SPR_DIALOG_4_SELECTED = 110;
    static final int SPR_DIALOG_5 = 111;
    static final int SPR_DIALOG_5_SELECTED = 112;
    static final int SPR_DIALOG_LOCKED = 113;
    static final int SPR_DIALOG_BORDER_TOPLEFT = 114;
    static final int SPR_DIALOG_BORDER_TOPLEFT_SELECTED = 115;
    static final int SPR_DIALOG_BORDER_TOPLEFT_DISABLED = 116;
    static final int SPR_DIALOG_BORDER_TOP = 117;
    static final int SPR_DIALOG_BORDER_TOP_SELECTED = 118;
    static final int SPR_DIALOG_BORDER_TOP_DISABLED = 119;
    static final int SPR_DIALOG_BORDER_LEFT = 120;
    static final int SPR_DIALOG_BORDER_LEFT_SELECTED = 121;
    static final int SPR_DIALOG_BORDER_LEFT_DISABLED = 122;
    static final int SPR_SCANLINE = 123;

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
    static final int DLG_SETTINGS = 4;
    static final int DLG_WINDOW_MODE = 5;
    static final int DLG_ABOUT = 6;
    static final int DLG_CREDITS = 7;
    static final int DLG_PAUSE = 8;
    static final int DLG_TRYAGAIN_PAUSE = 9;
    static final int DLG_TRYAGAIN_TIMEUP = 10;
    static final int DLG_QUIT = 11;
    static final int DLG_ERROR = 12;

    //Dialog actions
    static final int DLGACT_QUIT = 0;
    static final int DLGACT_TITLE = 1; //Go to title screen
    static final int DLGACT_PLAY = 2;
    static final int DLGACT_TRYAGAIN_WIPE = 3;
    static final int DLGACT_TRYAGAIN_IMMEDIATE = 4;



    //==========================================================================
    // Constants: gameplay
    //

    //Internal level number of ending sequence
    static final int LVLNUM_ENDING = 8;

    //Maximum numbers
    static final int MAX_OBJS = 160;
    static final int MAX_CRATE_BLOCKS = 32;
    static final int MAX_HOLES = 32;
    static final int MAX_GUSHES = 32;
    static final int MAX_PASSAGEWAYS = 4;
    static final int MAX_PUSHABLE_CRATES = MAX_PASSAGEWAYS;
    static final int MAX_CUTSCENE_OBJECTS = 2;
    static final int MAX_SOLIDS = 96;
    static final int MAX_TRIGGERS = 8;
    static final int MAX_RESPAWN_POINTS = 32;
    static final int MAX_COIN_SPARKS = 12;
    static final int MAX_CRACK_PARTICLES = 12;

    //A level block is the basic unit for positioning objects in the level, as
    //well as for the width of deep holes and passageways
    static final int LEVEL_BLOCK_SIZE = TILE_SIZE * 3;
    static final int VSCREEN_MAX_WIDTH_LEVEL_BLOCKS =
                                        VSCREEN_MAX_WIDTH / LEVEL_BLOCK_SIZE;

    //Floor, holes, light poles, and background
    static final int BACKGROUND_DRAW_Y = 176;
    static final int POLE_DISTANCE = 384; //Distance between light poles
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
    static final int OBJ_GUSH = 5;
    static final int OBJ_GUSH_CRACK = 6;
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
    static final int PLAYER_HEIGHT_SLIP = 38;

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

    //Triggered objects (other than the car colors above)
    static final int TRIGGER_HEN = 3;

    //Used as a type for the Car class to represent the traffic jam of the
    //ending sequence
    static final int TRAFFIC_JAM = 4;

    //Objects with a fixed Y position
    static final int BUS_Y = 128;
    static final int BUS_STOP_SIGN_Y = 176;
    static final int POLE_Y = 120;
    static final int GUSH_CRACK_Y = 260;
    static final int GUSH_INITIAL_Y = 232;
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

    //Animations
    static final int ANIM_PLAYER = 0;
    static final int ANIM_COINS = 1;
    static final int ANIM_GUSHES = 2;
    static final int ANIM_HIT_SPRING = 3;
    static final int ANIM_CRACK_PARTICLES = 4;
    static final int ANIM_BUS_WHEELS = 5;
    static final int ANIM_BUS_DOOR_REAR = 6;
    static final int ANIM_BUS_DOOR_FRONT = 7;
    static final int ANIM_CAR_WHEELS = 8;
    static final int ANIM_HEN = 9;
    static final int ANIM_COIN_SPARKS = 10; //12 positions starting at 10
    static final int ANIM_CUTSCENE_OBJECTS = 22; //2 positions starting at 22
    static final int NUM_ANIMS = 24;

    //Sequence types
    static final int SEQ_NORMAL_PLAY_START = 0;
    static final int SEQ_NORMAL_PLAY = 1;
    static final int SEQ_INITIAL = 10;
    static final int SEQ_BUS_LEAVING = 20;
    static final int SEQ_TIMEUP_BUS_NEAR = 30;
    static final int SEQ_TIMEUP_BUS_FAR = 40;
    static final int SEQ_GOAL_REACHED = 50;
    static final int SEQ_GOAL_REACHED_DEFAULT = 100;
    static final int SEQ_GOAL_REACHED_LEVEL2 = 200;
    static final int SEQ_GOAL_REACHED_LEVEL3 = 300;
    static final int SEQ_GOAL_REACHED_LEVEL4 = 400;
    static final int SEQ_GOAL_REACHED_LEVEL5 = 500;
    static final int SEQ_ENDING = 800;
    static final int SEQ_FINISHED = 999;



    //==========================================================================
    // Read-only data
    //

    //Supported virtual screen widths
    static final int[] vscreenWidths = new int[]{
        480, 432, 424
    };

    //Supported virtual screen heights
    static final int[] vscreenHeights = new int[]{
        270, 256, 240
    };

    //Number of levels per difficulty
    public static final int[] difficultyNumLevels = new int[]{
        5, //DIFFICULTY_NORMAL
        5, //DIFFICULTY_HARD
        3, //DIFFICULTY_SUPER
    };

    //A cheat code
    static final int[] cheatSequence = new int[]{
        3, 0, 2, 1, 3, 0, 1, 2, 1, 3, 0, 2, -1
    };

    //x, y, width, height
    //
    //For sprites with more than one animation frame, the width refers to a
    //single frame
    static final int[] sprites = new int[]{
        0,    296,  96,  192, //SPR_BACKGROUND
        928,  144,  8,   8,   //SPR_BANANA_PEEL
        104,  272,  32,  64,  //SPR_BEARDED_MAN_STAND
        136,  272,  32,  64,  //SPR_BEARDED_MAN_WALK
        328,  272,  32,  64,  //SPR_BEARDED_MAN_JUMP
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
        304,  680,  8,   8,   //SPR_CHARSET_WHITE
        440,  680,  8,   8,   //SPR_CHARSET_GREEN
        576,  680,  8,   8,   //SPR_CHARSET_GRAY
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
        856,  72,   8,   8,   //SPR_DUNG
        152,  696,  40,  8,   //SPR_ERROR
        0,    176,  64,  88,  //SPR_FLAGMAN
        952,  0,    24,  88,  //SPR_GUSH
        1000, 96,   16,  8,   //SPR_GUSH_CRACK
        976,  96,   16,  8,   //SPR_GUSH_HOLE
        760,  88,   32,  32,  //SPR_HEN
        776,  520,  40,  8,   //SPR_HUD_SCORE
        784,  536,  32,  8,   //SPR_HUD_TIME
        872,  32,   16,  24,  //SPR_HYDRANT
        712,  680,  296, 80,  //SPR_LOGO
        288,  168,  32,  32,  //SPR_MEDAL1
        328,  168,  32,  32,  //SPR_MEDAL2
        368,  168,  32,  32,  //SPR_MEDAL3
        816,  0,    16,  32,  //SPR_OVERHEAD_SIGN
        1008, 184,  8,   320, //SPR_OVERHEAD_SIGN_BASE
        840,  8,    16,  24,  //SPR_OVERHEAD_SIGN_BASE_TOP
        176,  368,  24,  120, //SPR_PASSAGEWAY_LEFT
        136,  344,  32,  16,  //SPR_PASSAGEWAY_LEFT_FG
        200,  368,  24,  120, //SPR_PASSAGEWAY_MIDDLE
        224,  368,  24,  120, //SPR_PASSAGEWAY_RIGHT
        176,  344,  24,  16,  //SPR_PASSAGEWAY_RIGHT_FG
        208,  344,  24,  24,  //SPR_PASSAGEWAY_RIGHT_CLOSED
        736,  520,  32,  32,  //SPR_PAUSE
        0,    0,    32,  64,  //SPR_PLAYER_STAND
        32,   0,    32,  64,  //SPR_PLAYER_WALK
        224,  0,    32,  64,  //SPR_PLAYER_JUMP
        256,  0,    32,  64,  //SPR_PLAYER_GRABROPE
        288,  0,    32,  64,  //SPR_PLAYER_THROWBACK
        384,  0,    48,  64,  //SPR_PLAYER_SLIP
        232,  72,   48,  64,  //SPR_PLAYER_RUN
        0,    72,   24,  64,  //SPR_PLAYER_CLEAN_DUNG
        904,  24,   24,  136, //SPR_POLE
        736,  128,  16,  16,  //SPR_PUSH_ARROW
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
        152,  512,  32,  32,  //SPR_DIALOG_SETTINGS
        440,  512,  32,  32,  //SPR_DIALOG_SETTINGS_SELECTED
        192,  512,  32,  32,  //SPR_DIALOG_ABOUT
        480,  512,  32,  32,  //SPR_DIALOG_ABOUT_SELECTED
        232,  512,  32,  32,  //SPR_DIALOG_QUIT
        520,  512,  32,  32,  //SPR_DIALOG_QUIT_SELECTED
        8,    552,  24,  24,  //SPR_DIALOG_RETURN
        296,  552,  24,  24,  //SPR_DIALOG_RETURN_SELECTED
        40,   552,  24,  24,  //SPR_DIALOG_RESET
        328,  552,  24,  24,  //SPR_DIALOG_RESET_SELECTED
        72,   552,  24,  24,  //SPR_DIALOG_AUDIO_ON
        360,  552,  24,  24,  //SPR_DIALOG_AUDIO_ON_SELECTED
        104,  552,  24,  24,  //SPR_DIALOG_AUDIO_OFF
        392,  552,  24,  24,  //SPR_DIALOG_AUDIO_OFF_SELECTED
        208,  592,  32,  32,  //SPR_DIALOG_TRYAGAIN
        496,  592,  32,  32,  //SPR_DIALOG_TRYAGAIN_SELECTED
        152,  552,  32,  32,  //SPR_DIALOG_CONFIRM
        440,  552,  32,  32,  //SPR_DIALOG_CONFIRM_SELECTED
        192,  552,  32,  32,  //SPR_DIALOG_CANCEL
        480,  552,  32,  32,  //SPR_DIALOG_CANCEL_SELECTED
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
        8,    696,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT
        56,   696,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT_SELECTED
        104,  696,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT_DISABLED
        24,   696,  8,   8,   //SPR_DIALOG_BORDER_TOP
        72,   696,  8,   8,   //SPR_DIALOG_BORDER_TOP_SELECTED
        120,  696,  8,   8,   //SPR_DIALOG_BORDER_TOP_DISABLED
        40,   696,  8,   8,   //SPR_DIALOG_BORDER_LEFT
        88,   696,  8,   8,   //SPR_DIALOG_BORDER_LEFT_SELECTED
        136,  696,  8,   8,   //SPR_DIALOG_BORDER_LEFT_DISABLED
        680,  520,  0,   0,   //SPR_SCANLINE
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
        SPR_GUSH, //OBJ_GUSH
        SPR_GUSH_CRACK, //OBJ_GUSH_CRACK
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

    //Gush movement patterns
    //
    //For each pair of values, the first value is the vertical velocity (yvel)
    //and the second value is the destination Y position (ydest)
    //
    //The value zero indicates the end of the pattern
    static final int[] gushMovePattern1 = {
        -64, 224, 64, 232,
        -64, 224, 64, 232,
        -64, 224, 64, 232,
        -64, 224, 64, 232,
        -64, 224, 64, 232,
        -144, 200, 144, 232,
        0,
    };

    static final int[] gushMovePattern2 = {
        -64, 216, 64, 224,
        0,
    };
}

