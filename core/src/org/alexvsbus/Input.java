/*
 * Alex vs Bus
 * Copyright (C) 2021-2022 M374LX
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
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;

public class Input extends ControllerAdapter {
    Config config;

    //Game controller (joystick, joypad, ...)
    float joyAxisX;
    float joyAxisY;
    boolean joyButtonsHeld[];

    //Touchscreen
    boolean pauseTouched;
    boolean leftTouched;
    boolean rightTouched;
    boolean jumpTouched;

    //--------------------------------------------------------------------------

    Input(Config config) {
        int i;

        this.config = config;

        pauseTouched = false;
        leftTouched  = false;
        rightTouched = false;
        jumpTouched  = false;

        joyAxisX = 0;
        joyAxisY = 0;

        joyButtonsHeld = new boolean[JOY_NUM_BUTTONS];
        for (i = 0; i < JOY_NUM_BUTTONS; i++) {
            joyButtonsHeld[i] = false;
        }

        if (config.useBackKey) {
            Gdx.input.setCatchKey(Keys.BACK, true);
        }

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
            actionsHeld |= INPUT_PAUSE;
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

    void onTouch(float x, float y, float projectionHeight) {
        if (!config.touchEnabled || !config.showTouchControls) return;

        //Pause
        if (x >= SCREEN_WIDTH - 32 && y <= 32) {
            pauseTouched = true;
            return;
        }

        //If the Y position is above all buttons (other than pause), nothing
        //to do
        if (y < projectionHeight - 70) return;

        if (x < 64) leftTouched = true;
        if (x >= 64 && x < 128) rightTouched = true;
        if (x >= SCREEN_WIDTH - 64) jumpTouched = true;
    }

    //--------------------------------------------------------------------------

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
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

    @Override
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

    @Override
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

        boolean cfgWindowMode      = Gdx.input.isKeyPressed(Keys.F5);
        boolean cfgAudioToggle     = Gdx.input.isKeyPressed(Keys.F6);
        boolean cfgScanlinesToggle = Gdx.input.isKeyPressed(Keys.F7);

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

        if (cfgWindowMode) {
            actionsHeld |= INPUT_CFG_WINDOW_MODE;
        }
        if (cfgAudioToggle) {
            actionsHeld |= INPUT_CFG_AUDIO_TOGGLE;
        }
        if (cfgScanlinesToggle) {
            actionsHeld |= INPUT_CFG_SCANLINES_TOGGLE;
        }

        return actionsHeld;
    }

    int readTouch() {
        if (!config.touchEnabled || !config.showTouchControls) return 0;

        int actionsHeld = 0;

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

