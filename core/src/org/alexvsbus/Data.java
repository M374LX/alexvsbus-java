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

//Class containing read-only data that can be statically imported by other files
public class Data {
    //Supported virtual screen widths
    public static final int[] vscreenWidths = new int[]{
        480, 432, 416, 320, 256, -1
    };

    //Supported virtual screen heights
    public static final int[] vscreenHeights = new int[]{
        270, 256, 240, 224, 192, -1
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

    //Sprites within the gfx.png file
    //
    //For each sprite: x, y, width, height
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
        672,  472,  24,  32,  //SPR_BUS_ROUTE
        736,  0,    16,  88,  //SPR_BUS_STOP_SIGN
        688,  424,  48,  40,  //SPR_BUS_WHEEL
        448,  288,  136, 56,  //SPR_CAR_BLUE
        448,  352,  136, 56,  //SPR_CAR_SILVER
        448,  416,  136, 56,  //SPR_CAR_YELLOW
        528,  480,  24,  24,  //SPR_CAR_WHEEL
        304,  632,  8,   8,   //SPR_CHARSET_WHITE
        440,  632,  8,   8,   //SPR_CHARSET_GREEN
        576,  632,  8,   8,   //SPR_CHARSET_GRAY
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
        872,  32,   16,  24,  //SPR_HYDRANT
        480,  696,  224, 64,  //SPR_LOGO_SMALL
        712,  680,  296, 80,  //SPR_LOGO_LARGE
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
        688,  512,  32,  32,  //SPR_PAUSE
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
        8,    512,  80,  24,  //SPR_DIALOG_PLAY
        296,  512,  80,  24,  //SPR_DIALOG_PLAY_SELECTED
        104,  512,  24,  24,  //SPR_DIALOG_TRYAGAIN
        392,  512,  24,  24,  //SPR_DIALOG_TRYAGAIN_SELECTED
        136,  512,  24,  24,  //SPR_DIALOG_JUKEBOX
        424,  512,  24,  24,  //SPR_DIALOG_JUKEBOX_SELECTED
        168,  512,  24,  24,  //SPR_DIALOG_SETTINGS
        456,  512,  24,  24,  //SPR_DIALOG_SETTINGS_SELECTED
        200,  512,  24,  24,  //SPR_DIALOG_ABOUT
        488,  512,  24,  24,  //SPR_DIALOG_ABOUT_SELECTED
        232,  512,  24,  24,  //SPR_DIALOG_QUIT
        520,  512,  24,  24,  //SPR_DIALOG_QUIT_SELECTED
        8,    544,  24,  24,  //SPR_DIALOG_RETURN
        296,  544,  24,  24,  //SPR_DIALOG_RETURN_SELECTED
        40,   544,  24,  24,  //SPR_DIALOG_AUDIO_ON
        328,  544,  24,  24,  //SPR_DIALOG_AUDIO_ON_SELECTED
        72,   544,  24,  24,  //SPR_DIALOG_AUDIO_OFF
        360,  544,  24,  24,  //SPR_DIALOG_AUDIO_OFF_SELECTED
        104,  544,  24,  24,  //SPR_DIALOG_CONFIRM
        392,  544,  24,  24,  //SPR_DIALOG_CONFIRM_SELECTED
        136,  544,  24,  24,  //SPR_DIALOG_CANCEL
        424,  544,  24,  24,  //SPR_DIALOG_CANCEL_SELECTED
        168,  544,  24,  24,  //SPR_DIALOG_RETURN_SMALL
        456,  544,  24,  24,  //SPR_DIALOG_RETURN_SMALL_SELECTED
        8,    576,  24,  32,  //SPR_DIALOG_1
        296,  576,  24,  32,  //SPR_DIALOG_1_SELECTED
        40,   576,  24,  32,  //SPR_DIALOG_2
        328,  576,  24,  32,  //SPR_DIALOG_2_SELECTED
        72,   576,  24,  32,  //SPR_DIALOG_3
        360,  576,  24,  32,  //SPR_DIALOG_3_SELECTED
        104,  576,  24,  32,  //SPR_DIALOG_4
        392,  576,  24,  32,  //SPR_DIALOG_4_SELECTED
        136,  576,  24,  32,  //SPR_DIALOG_5
        424,  576,  24,  32,  //SPR_DIALOG_5_SELECTED
        168,  576,  24,  32,  //SPR_DIALOG_LOCKED
        8,    696,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT
        56,   696,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT_SELECTED
        104,  696,  8,   8,   //SPR_DIALOG_BORDER_TOPLEFT_DISABLED
        24,   696,  8,   8,   //SPR_DIALOG_BORDER_TOP
        72,   696,  8,   8,   //SPR_DIALOG_BORDER_TOP_SELECTED
        120,  696,  8,   8,   //SPR_DIALOG_BORDER_TOP_DISABLED
        40,   696,  8,   8,   //SPR_DIALOG_BORDER_LEFT
        88,   696,  8,   8,   //SPR_DIALOG_BORDER_LEFT_SELECTED
        136,  696,  8,   8,   //SPR_DIALOG_BORDER_LEFT_DISABLED
        672,  512,  0,   0,   //SPR_SCANLINE
    };

    //Sprite corresponding to each player character animation type
    static final int[] playerAnimSprites = new int[]{
        SPR_PLAYER_STAND,     //PLAYER_ANIM_STAND
        SPR_PLAYER_WALK,      //PLAYER_ANIM_WALK
        SPR_PLAYER_WALK,      //PLAYER_ANIM_WALKBACK
        SPR_PLAYER_JUMP,      //PLAYER_ANIM_JUMP
        SPR_PLAYER_SLIP,      //PLAYER_ANIM_SLIP
        SPR_PLAYER_SLIP,      //PLAYER_ANIM_SLIPREV
        SPR_PLAYER_THROWBACK, //PLAYER_ANIM_THROWBACK
        SPR_PLAYER_GRABROPE,  //PLAYER_ANIM_GRABROPE
    };

    //Sprite corresponding to each object type (OBJ_* constants)
    static final int[] objSprites = new int[]{
        SPR_COIN_SILVER,      //OBJ_COIN_SILVER
        SPR_COIN_GOLD,        //OBJ_COIN_GOLD
        SPR_CRATE,            //OBJ_CRATE_PUSHABLE
        SPR_BANANA_PEEL,      //OBJ_BANANA_PEEL
        SPR_BANANA_PEEL,      //OBJ_BANANA_PEEL_MOVING
        SPR_GUSH,             //OBJ_GUSH
        SPR_GUSH_CRACK,       //OBJ_GUSH_CRACK
        SPR_ROPE_HORIZONTAL,  //OBJ_ROPE_HORIZONTAL
        SPR_ROPE_VERTICAL,    //OBJ_ROPE_VERTICAL
        SPR_SPRING,           //OBJ_SPRING
        SPR_HYDRANT,          //OBJ_HYDRANT
        SPR_OVERHEAD_SIGN,    //OBJ_OVERHEAD_SIGN
        SPR_CAR_BLUE,         //OBJ_PARKED_CAR_BLUE
        SPR_CAR_SILVER,       //OBJ_PARKED_CAR_SILVER
        SPR_CAR_YELLOW,       //OBJ_PARKED_CAR_YELLOW
        SPR_TRUCK,            //OBJ_PARKED_TRUCK
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

    static final String[] dialogDisplayNames = {
        "",                   //DLG_MAIN
        "DIFFICULTY SELECT",  //DLG_DIFFICULTY
        "LEVEL SELECT",       //DLG_LEVEL
        "JUKEBOX",            //DLG_JUKEBOX
        "SETTINGS",           //DLG_SETTINGS
        "DISPLAY SETTINGS",   //DLG_DISPLAY_SETTINGS
        "VSCREEN SIZE",       //DLG_VSCREEN_SIZE
        "VSCREEN WIDTH",      //DLG_VSCREEN_WIDTH
        "VSCREEN HEIGHT",     //DLG_VSCREEN_HEIGHT
        "WINDOW SCALE",       //DLG_WINDOW_SCALE
        "AUDIO SETTINGS",     //DLG_AUDIO_SETTINGS
        "ABOUT",              //DLG_ABOUT
        "CREDITS",            //DLG_CREDITS
        "",                   //DLG_PAUSE
        "CONFIRMATION",       //DLG_TRYAGAIN_PAUSE
        "CONFIRMATION",       //DLG_TRYAGAIN_TIMEUP
        "CONFIRMATION",       //DLG_QUIT
        "",                   //DLG_ERROR
    };

    static final String[] sfxFiles = {
        "coin.wav",           //SFX_COIN
        "crate.wav",          //SFX_CRATE
        "error.wav",          //SFX_ERROR
        "fall.wav",           //SFX_FALL
        "hit.wav",            //SFX_HIT
        "hole.wav",           //SFX_HOLE
        "respawn.wav",        //SFX_RESPAWN
        "score.wav",          //SFX_SCORE
        "select.wav",         //SFX_SELECT
        "slip.wav",           //SFX_SLIP
        "spring.wav",         //SFX_SPRING
        "time.wav",           //SFX_TIME
    };

    static final String[] bgmFiles = {
        "bgmtitle.ogg",       //BGMTITLE
        "bgm1.ogg",           //BGM1
        "bgm2.ogg",           //BGM2
        "bgm3.ogg",           //BGM3
    };
}

