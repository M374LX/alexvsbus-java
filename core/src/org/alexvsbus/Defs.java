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

//Class containing constants and plain old data (POD) classes that can be
//statically imported by other files
public class Defs {
    //==========================================================================
    // Constants: general
    //

    //Version and repository
    public static final String VERSION = "pre6";
    public static final String REPOSITORY = "https://github.com/M374LX/alexvsbus-java";

    //Maximum delta time
    static final float MAX_DT = (1.0f / 30.0f);

    //Screen types
    static final int SCR_BLANK = 0;
    static final int SCR_PLAY = 1;
    static final int SCR_PLAY_FREEZE = 2; //Render a play session without updating it
    static final int SCR_FINALSCORE = 3;

    //Delayed action types
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

    //Maximum supported size for the virtual screen
    static final int VSCREEN_MAX_WIDTH  = 480;
    static final int VSCREEN_MAX_HEIGHT = 270;

    //Minimum supported size for the virtual screen
    static final int VSCREEN_MIN_WIDTH  = 256;
    static final int VSCREEN_MIN_HEIGHT = 192;

    //Minimum size for the virtual screen when using automatic sizing (except
    //if the screen is very small)
    static final int VSCREEN_AUTO_MIN_WIDTH  = 416;
    static final int VSCREEN_AUTO_MIN_HEIGHT = 240;

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

    //As in many retro games, 8x8 tiles are commonly used as the basic unit for
    //positioning and size
    static final int TILE_SIZE = 8;

    //Special constant meaning that something does not exist or is unset or
    //inactive
    //
    //Commonly used as an object's X position or as an index within
    //PlayCtx.objs[]
    public static final int NONE = (-1);



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
    static final int INPUT_CFG_FULLSCREEN_TOGGLE = (1 << 9);
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

    //Touchscreen button size
    static final int TOUCH_BUTTON_WIDTH  = 64;
    static final int TOUCH_BUTTON_HEIGHT = 64;

    //Touchscreen button opacity
    static final float TOUCH_BUTTON_OPACITY = 0.45f;



    //==========================================================================
    // Constants: audio
    //

    //Sound effects
    static final int SFX_COIN = 0;
    static final int SFX_CRATE = 1;
    static final int SFX_ERROR = 2;
    static final int SFX_FALL = 3;
    static final int SFX_HIT = 4;
    static final int SFX_HOLE = 5;
    static final int SFX_RESPAWN = 6;
    static final int SFX_SCORE = 7;
    static final int SFX_SELECT = 8;
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
    static final int SPR_HYDRANT = 42;
    static final int SPR_LOGO_SMALL = 43;
    static final int SPR_LOGO_LARGE = 44;
    static final int SPR_MEDAL1 = 45;
    static final int SPR_MEDAL2 = 46;
    static final int SPR_MEDAL3 = 47;
    static final int SPR_OVERHEAD_SIGN = 48;
    static final int SPR_OVERHEAD_SIGN_BASE = 49;
    static final int SPR_OVERHEAD_SIGN_BASE_TOP = 50;
    static final int SPR_PASSAGEWAY_LEFT = 51;
    static final int SPR_PASSAGEWAY_LEFT_FG = 52;
    static final int SPR_PASSAGEWAY_MIDDLE = 53;
    static final int SPR_PASSAGEWAY_RIGHT = 54;
    static final int SPR_PASSAGEWAY_RIGHT_FG = 55;
    static final int SPR_PASSAGEWAY_RIGHT_CLOSED = 56;
    static final int SPR_PAUSE = 57;
    static final int SPR_PLAYER_STAND = 58;
    static final int SPR_PLAYER_WALK = 59;
    static final int SPR_PLAYER_JUMP = 60;
    static final int SPR_PLAYER_GRABROPE = 61;
    static final int SPR_PLAYER_THROWBACK = 62;
    static final int SPR_PLAYER_SLIP = 63;
    static final int SPR_PLAYER_RUN = 64;
    static final int SPR_PLAYER_CLEAN_DUNG = 65;
    static final int SPR_POLE = 66;
    static final int SPR_PUSH_ARROW = 67;
    static final int SPR_ROPE_HORIZONTAL = 68;
    static final int SPR_ROPE_VERTICAL = 69;
    static final int SPR_SPRING = 70;
    static final int SPR_TOUCH_LEFT = 71;
    static final int SPR_TOUCH_LEFT_HELD = 72;
    static final int SPR_TOUCH_RIGHT = 73;
    static final int SPR_TOUCH_RIGHT_HELD = 74;
    static final int SPR_TOUCH_JUMP = 75;
    static final int SPR_TOUCH_JUMP_HELD = 76;
    static final int SPR_TRUCK = 77;
    static final int SPR_DIALOG_PLAY = 78;
    static final int SPR_DIALOG_PLAY_SELECTED = 79;
    static final int SPR_DIALOG_TRYAGAIN = 80;
    static final int SPR_DIALOG_TRYAGAIN_SELECTED = 81;
    static final int SPR_DIALOG_JUKEBOX = 82;
    static final int SPR_DIALOG_JUKEBOX_SELECTED = 83;
    static final int SPR_DIALOG_SETTINGS = 84;
    static final int SPR_DIALOG_SETTINGS_SELECTED = 85;
    static final int SPR_DIALOG_ABOUT = 86;
    static final int SPR_DIALOG_ABOUT_SELECTED = 87;
    static final int SPR_DIALOG_QUIT = 88;
    static final int SPR_DIALOG_QUIT_SELECTED = 89;
    static final int SPR_DIALOG_RETURN = 90;
    static final int SPR_DIALOG_RETURN_SELECTED = 91;
    static final int SPR_DIALOG_AUDIO_ON = 92;
    static final int SPR_DIALOG_AUDIO_ON_SELECTED = 93;
    static final int SPR_DIALOG_AUDIO_OFF = 94;
    static final int SPR_DIALOG_AUDIO_OFF_SELECTED = 95;
    static final int SPR_DIALOG_CONFIRM = 96;
    static final int SPR_DIALOG_CONFIRM_SELECTED = 97;
    static final int SPR_DIALOG_CANCEL = 98;
    static final int SPR_DIALOG_CANCEL_SELECTED = 99;
    static final int SPR_DIALOG_RETURN_SMALL = 100;
    static final int SPR_DIALOG_RETURN_SMALL_SELECTED = 101;
    static final int SPR_DIALOG_1 = 102;
    static final int SPR_DIALOG_1_SELECTED = 103;
    static final int SPR_DIALOG_2 = 104;
    static final int SPR_DIALOG_2_SELECTED = 105;
    static final int SPR_DIALOG_3 = 106;
    static final int SPR_DIALOG_3_SELECTED = 107;
    static final int SPR_DIALOG_4 = 108;
    static final int SPR_DIALOG_4_SELECTED = 109;
    static final int SPR_DIALOG_5 = 110;
    static final int SPR_DIALOG_5_SELECTED = 111;
    static final int SPR_DIALOG_LOCKED = 112;
    static final int SPR_DIALOG_BORDER_TOPLEFT = 113;
    static final int SPR_DIALOG_BORDER_TOPLEFT_SELECTED = 114;
    static final int SPR_DIALOG_BORDER_TOPLEFT_DISABLED = 115;
    static final int SPR_DIALOG_BORDER_TOP = 116;
    static final int SPR_DIALOG_BORDER_TOP_SELECTED = 117;
    static final int SPR_DIALOG_BORDER_TOP_DISABLED = 118;
    static final int SPR_DIALOG_BORDER_LEFT = 119;
    static final int SPR_DIALOG_BORDER_LEFT_SELECTED = 120;
    static final int SPR_DIALOG_BORDER_LEFT_DISABLED = 121;
    static final int SPR_SCANLINE = 122;

    //Logo width in pixels
    static final int LOGO_WIDTH_SMALL = 224;
    static final int LOGO_WIDTH_LARGE = 296;



    //==========================================================================
    // Constants: dialogs
    //

    static final int DIALOG_MAX_STACK_SIZE = 8;
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
    static final int DLG_DISPLAY_SETTINGS = 5;
    static final int DLG_VSCREEN_SIZE = 6;
    static final int DLG_VSCREEN_WIDTH = 7;
    static final int DLG_VSCREEN_HEIGHT = 8;
    static final int DLG_WINDOW_SCALE = 9;
    static final int DLG_AUDIO_SETTINGS = 10;
    static final int DLG_ABOUT = 11;
    static final int DLG_CREDITS = 12;
    static final int DLG_PAUSE = 13;
    static final int DLG_TRYAGAIN_PAUSE = 14;
    static final int DLG_TRYAGAIN_TIMEUP = 15;
    static final int DLG_QUIT = 16;
    static final int DLG_ERROR = 17;

    //Dialog action types
    static final int DLGACT_QUIT = 0;
    static final int DLGACT_TITLE = 1; //Go to title screen
    static final int DLGACT_PLAY = 2;
    static final int DLGACT_TRYAGAIN = 3;



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
    static final int LEVEL_BLOCK_SIZE = (TILE_SIZE * 3);
    static final int VSCREEN_MAX_WIDTH_LEVEL_BLOCKS =
                                        (VSCREEN_MAX_WIDTH / LEVEL_BLOCK_SIZE);

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

    //Even if the player presses the jump button before the character hits the
    //floor, a timer is started and a jump is triggered if the character hits
    //the floor before this amount of time passes
    static final float JUMP_TIMEOUT = 0.2f;

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
    // Interface to platform-dependent methods
    //

    public static interface PlatDep {
        void postInit();
        void setMinWindowSize(int width, int height);
        Config getConfig();
        boolean saveConfig();
    }



    //==========================================================================
    // Passive classes: display parameters and configuration
    //

    static class DisplayParams {
        //Size of the game's virtual screen
        int vscreenWidth;
        int vscreenHeight;

        //Size of the physical screen (or window)
        int physWidth;
        int physHeight;

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
        //Display
        public boolean fullscreen;
        public boolean fixedWindowMode;
        public boolean resizableWindow;
        public int windowScale, oldWindowScale;
        public boolean scanlinesEnabled;

        //Virtual screen size
        public boolean vscreenAutoSize, oldVscreenAutoSize;
        public int vscreenWidth;
        public int vscreenHeight;

        //Audio
        public boolean audioEnabled;
        public boolean musicEnabled;
        public boolean sfxEnabled;

        //Touchscreen
        public boolean touchEnabled;
        public boolean touchButtonsEnabled; //Left, Right, and Jump buttons
        public boolean showTouchControls;

        //Back key
        public boolean useBackKey;

        //Game progress
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
        String displayName;

        int stackSize;
        DialogStackEntry stack[];

        int numItems;
        DialogItem items[];

        String text;
        int textOffsetX; //Offset from center of screen in 8x8 tiles
        int textOffsetY;
        int textWidth; //Width and height in 8x8 tiles
        int textHeight;
        boolean textBorder;

        boolean levelSelected;
        boolean selectedVisible;

        boolean useCursor;
        boolean showLogo;
        boolean greenBg;
        boolean showFrame;
        boolean fillScreen;

        int action; //DLGACT_* constants
        int actionParam;
    }



    //==========================================================================
    // Passive classes: gameplay
    //

    static class Camera {
        float x, y;
        float xvel, yvel;
        float xdest;
        float xmin;
        float xmax;

        boolean followPlayer;
        float followPlayerMinX;
        float followPlayerMaxX;

        boolean fixedAtLeftmost;
        boolean fixedAtRightmost;
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
        int nextCoinSpark;
        int nextCrackParticle;

        //Animations
        Anim anims[];

        //Used in the ending sequence
        boolean playerReachedFlagman;
        boolean henReachedFlagman;
        boolean busReachedFlagman;

        //Sequence
        int sequenceStep;
        float sequenceDelay;
        boolean skipInitialSequence;
        boolean wipeIn;
        boolean wipeOut;
    }
}

