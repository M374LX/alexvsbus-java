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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

public class Input implements ControllerListener {
    DisplayParams displayParams;
    Config config;

    //Game controller (joystick, joypad, ...)
    float joyAxisX;
    float joyAxisY;
    boolean joyButtonsHeld[];

    //Touchscreen
    boolean touching;
    boolean wasTouching;
    boolean pauseTouched;
    boolean leftTouched;
    boolean rightTouched;
    boolean jumpTouched;

    //Touchscreen tap position converted to virtual screen coordinates (both are
    //set to -1 when the touchscreen is not tapped)
    int tapX;
    int tapY;

    //--------------------------------------------------------------------------

    Input(DisplayParams dp, Config cfg) {
        int i;

        config = cfg;
        displayParams = dp;

        joyButtonsHeld = new boolean[JOY_NUM_BUTTONS];

        if (config.useBackKey) {
            Gdx.input.setCatchKey(Keys.BACK, true);
        }

        tapX = -1;
        tapY = -1;

        Controllers.addListener(this);
    }

    //Returns a bitfield whose bits correspond to the INPUT_* constants
    int read() {
        int actionsHeld = 0;

        actionsHeld |= readJoyButtons();
        actionsHeld |= readJoyAxes();
        actionsHeld |= readKeyboard();
        actionsHeld |= readTouch();

        if (config.useBackKey && Gdx.input.isKeyPressed(Keys.BACK)) {
            actionsHeld |= INPUT_PAUSE_TOUCH;
            actionsHeld |= INPUT_DIALOG_RETURN;
        }

        //Cannot press left and right at the same time
        if ((actionsHeld & INPUT_RIGHT) > 0) {
            actionsHeld &= ~INPUT_LEFT;
        }

        //Cannot press up and down at the same time
        if ((actionsHeld & INPUT_DOWN) > 0) {
            actionsHeld &= ~INPUT_UP;
        }

        //Reset state of touchscreen buttons
        leftTouched  = false;
        rightTouched = false;
        jumpTouched  = false;

        return actionsHeld;
    }

    int getTapX() {
        return tapX;
    }

    int getTapY() {
        return tapY;
    }

    void handleTouch() {
        float x, y;
        int buttonWidth   = TOUCH_BUTTON_WIDTH;
        int vscreenWidth  = displayParams.vscreenWidth;
        int vscreenHeight = displayParams.vscreenHeight;
        int physWidth     = displayParams.physWidth;
        int physHeight    = displayParams.physHeight;
        int scale = displayParams.scale;
        int i;

        if (!config.touchEnabled) return;

        wasTouching = touching;
        touching = false;

        //Check if the screen is being touched and handle left, right, and jump
        //buttons
        for (i = 0; i < 10; i++) {
            if (!Gdx.input.isTouched(i)) continue;

            touching = true;

            if (!config.showTouchControls)   break;
            if (!config.touchButtonsEnabled) break;

            x = Gdx.input.getX(i);
            y = Gdx.input.getY(i);

            //If the Y position is above all buttons (other than pause),
            //nothing to do
            if (y < physHeight - (70 * scale)) continue;

            if (x < buttonWidth * scale) {
                leftTouched = true;
            }
            if (x >= buttonWidth * scale && x < (buttonWidth * 2) * scale) {
                rightTouched = true;
            }
            if (x >= physWidth - (TOUCH_JUMP_OFFSET_X * scale)) {
                jumpTouched = true;
            }
        }

        //Reset tap coordinates
        tapX = -1;
        tapY = -1;

        for (i = 0; i < 10; i++) {
            if (Gdx.input.isTouched(i)) {
                //Convert X coordinate from physical screen to virtual screen
                x  = Gdx.input.getX(i);
                x -= displayParams.viewportOffsetX;
                x /= displayParams.viewportWidth;
                x *= vscreenWidth;

                //Convert Y coordinate from physical screen to virtual screen
                y  = Gdx.input.getY(i);
                y -= displayParams.viewportOffsetY;
                y /= displayParams.viewportHeight;
                y *= displayParams.vscreenHeight;
            } else {
                //Not touching
                x = -1;
                y = -1;
            }

            //Ignore touches outside the virtual screen
            if (x < 0 || x > vscreenWidth || y < 0 || y > vscreenHeight) {
                continue;
            }

            //Handle pause button
            if (config.showTouchControls && !wasTouching) {
                if (x >= vscreenWidth - 32 && y <= 32) {
                    pauseTouched = true;
                }
            }

            //Store tap coordinates
            if (i == 0 && !wasTouching) {
                tapX = (int)x;
                tapY = (int)y;
            }
        }
    }

    //--------------------------------------------------------------------------

    public void connected(Controller controller) {

    }

    public void disconnected(Controller controller) {

    }

    public boolean buttonDown(Controller controller, int buttonCode) {
        int i;

        if (buttonCode == controller.getMapping().buttonA) {
            joyButtonsHeld[JOY_A] = true;
        }
        if (buttonCode == controller.getMapping().buttonB) {
            joyButtonsHeld[JOY_B] = true;
        }
        if (buttonCode == controller.getMapping().buttonX) {
            joyButtonsHeld[JOY_X] = true;
        }
        if (buttonCode == controller.getMapping().buttonStart) {
            joyButtonsHeld[JOY_START] = true;
        }
        if (buttonCode == controller.getMapping().buttonBack) {
            joyButtonsHeld[JOY_SELECT] = true;
        }
        if (buttonCode == controller.getMapping().buttonDpadUp) {
            joyButtonsHeld[JOY_DPAD_UP] = true;
        }
        if (buttonCode == controller.getMapping().buttonDpadDown) {
            joyButtonsHeld[JOY_DPAD_DOWN] = true;
        }
        if (buttonCode == controller.getMapping().buttonDpadLeft) {
            joyButtonsHeld[JOY_DPAD_LEFT] = true;
        }
        if (buttonCode == controller.getMapping().buttonDpadRight) {
            joyButtonsHeld[JOY_DPAD_RIGHT] = true;
        }

        return false;
    }

    public boolean buttonUp(Controller controller, int buttonCode) {
        int i;

        if (buttonCode == controller.getMapping().buttonA) {
            joyButtonsHeld[JOY_A] = false;
        }
        if (buttonCode == controller.getMapping().buttonB) {
            joyButtonsHeld[JOY_B] = false;
        }
        if (buttonCode == controller.getMapping().buttonX) {
            joyButtonsHeld[JOY_X] = false;
        }
        if (buttonCode == controller.getMapping().buttonStart) {
            joyButtonsHeld[JOY_START] = false;
        }
        if (buttonCode == controller.getMapping().buttonBack) {
            joyButtonsHeld[JOY_SELECT] = false;
        }
        if (buttonCode == controller.getMapping().buttonDpadUp) {
            joyButtonsHeld[JOY_DPAD_UP] = false;
        }
        if (buttonCode == controller.getMapping().buttonDpadDown) {
            joyButtonsHeld[JOY_DPAD_DOWN] = false;
        }
        if (buttonCode == controller.getMapping().buttonDpadLeft) {
            joyButtonsHeld[JOY_DPAD_LEFT] = false;
        }
        if (buttonCode == controller.getMapping().buttonDpadRight) {
            joyButtonsHeld[JOY_DPAD_RIGHT] = false;
        }

        return false;
    }

    public boolean axisMoved(Controller c, int axisCode, float value) {
        if (axisCode == 0) {
            joyAxisX = value;
        } else if (axisCode == 1) {
            joyAxisY = value;
        }

        return false;
    }

    //--------------------------------------------------------------------------

    int readKeyboard() {
        int actionsHeld = 0;

        boolean ctrlLeft  = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT);
        boolean ctrlRight = Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
        boolean space     = Gdx.input.isKeyPressed(Keys.SPACE);
        boolean enter     = Gdx.input.isKeyPressed(Keys.ENTER);
        boolean escape    = Gdx.input.isKeyPressed(Keys.ESCAPE);
        boolean up        = Gdx.input.isKeyPressed(Keys.UP);
        boolean down      = Gdx.input.isKeyPressed(Keys.DOWN);
        boolean left      = Gdx.input.isKeyPressed(Keys.LEFT);
        boolean right     = Gdx.input.isKeyPressed(Keys.RIGHT);
        boolean w         = Gdx.input.isKeyPressed(Keys.W);
        boolean s         = Gdx.input.isKeyPressed(Keys.S);
        boolean a         = Gdx.input.isKeyPressed(Keys.A);
        boolean d         = Gdx.input.isKeyPressed(Keys.D);
        boolean f5        = Gdx.input.isKeyPressed(Keys.F5);
        boolean f6        = Gdx.input.isKeyPressed(Keys.F6);
        boolean f7        = Gdx.input.isKeyPressed(Keys.F7);

        if (ctrlLeft || ctrlRight || space) {
            actionsHeld |= INPUT_JUMP;
            actionsHeld |= INPUT_DIALOG_CONFIRM;
        }
        if (enter) {
            actionsHeld |= INPUT_DIALOG_CONFIRM;
        }
        if (escape) {
            actionsHeld |= INPUT_PAUSE;
            actionsHeld |= INPUT_DIALOG_RETURN;
        }
        if (up || w) {
            actionsHeld |= INPUT_UP;
            actionsHeld |= INPUT_JUMP;
        }
        if (down || s) {
            actionsHeld |= INPUT_DOWN;
        }
        if (left || a) {
            actionsHeld |= INPUT_LEFT;
        }
        if (right || d) {
            actionsHeld |= INPUT_RIGHT;
        }
        if (f5) {
            actionsHeld |= INPUT_CFG_FULLSCREEN_TOGGLE;
        }
        if (f6) {
            actionsHeld |= INPUT_CFG_AUDIO_TOGGLE;
        }
        if (f7) {
            actionsHeld |= INPUT_CFG_SCANLINES_TOGGLE;
        }

        return actionsHeld;
    }

    int readTouch() {
        int actionsHeld = 0;

        if (!config.touchEnabled || !config.showTouchControls) return 0;

        if (pauseTouched) {
            actionsHeld |= INPUT_PAUSE_TOUCH;
            pauseTouched = false;

            return actionsHeld;
        }

        if (leftTouched) {
            actionsHeld |= INPUT_LEFT;
        }
        if (rightTouched) {
            actionsHeld |= INPUT_RIGHT;
        }
        if (jumpTouched) {
            actionsHeld |= INPUT_JUMP;
        }

        return actionsHeld;
    }

    int readJoyButtons() {
        int actionsHeld = 0;

        if (joyButtonsHeld[JOY_A]) {
            actionsHeld |= INPUT_JUMP;
            actionsHeld |= INPUT_DIALOG_CONFIRM;
        }
        if (joyButtonsHeld[JOY_B]) {
            actionsHeld |= INPUT_JUMP;
            actionsHeld |= INPUT_DIALOG_RETURN;
        }
        if (joyButtonsHeld[JOY_X]) {
            actionsHeld |= INPUT_JUMP;
            actionsHeld |= INPUT_DIALOG_RETURN;
        }
        if (joyButtonsHeld[JOY_START]) {
            actionsHeld |= INPUT_PAUSE;
            actionsHeld |= INPUT_DIALOG_CONFIRM;
        }
        if (joyButtonsHeld[JOY_SELECT]) {
            actionsHeld |= INPUT_PAUSE;
            actionsHeld |= INPUT_DIALOG_RETURN;
        }
        if (joyButtonsHeld[JOY_DPAD_UP]) {
            actionsHeld |= INPUT_UP;
        }
        if (joyButtonsHeld[JOY_DPAD_DOWN]) {
            actionsHeld |= INPUT_DOWN;
        }
        if (joyButtonsHeld[JOY_DPAD_LEFT]) {
            actionsHeld |= INPUT_LEFT;
        }
        if (joyButtonsHeld[JOY_DPAD_RIGHT]) {
            actionsHeld |= INPUT_RIGHT;
        }

        return actionsHeld;
    }

    int readJoyAxes() {
        int actionsHeld = 0;

        if (joyAxisX >= JOY_AXIS_DEADZONE) {
            actionsHeld |= INPUT_RIGHT;
        } else if (joyAxisX <= -JOY_AXIS_DEADZONE) {
            actionsHeld |= INPUT_LEFT;
        }

        if (joyAxisY >= JOY_AXIS_DEADZONE) {
            actionsHeld |= INPUT_DOWN;
        } else if (joyAxisY <= -JOY_AXIS_DEADZONE) {
            actionsHeld |= INPUT_UP;
        }

        return actionsHeld;
    }
}

