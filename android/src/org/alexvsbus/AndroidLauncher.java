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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration appConfig = new AndroidApplicationConfiguration();
        appConfig.maxSimultaneousSounds = 4;
        appConfig.useAccelerometer = false;
        appConfig.useCompass = false;
        appConfig.useGyroscope = false;
        appConfig.useImmersiveMode = true;
        appConfig.useWakelock = true;

        SharedPreferences prefs = getSharedPreferences("alexvsbus", Context.MODE_PRIVATE);

        AndroidPlatDep platDep = new AndroidPlatDep(prefs);
        platDep.loadConfig();

        initialize(new Main(platDep), appConfig);
    }
}

