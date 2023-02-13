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


package org.alexvsbus.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.alexvsbus.Main;

import static org.alexvsbus.Defs.VSCREEN_MAX_WIDTH;
import static org.alexvsbus.Defs.VSCREEN_MAX_HEIGHT;
import static org.alexvsbus.Defs.VERSION;
import static org.alexvsbus.Defs.WM_1X;
import static org.alexvsbus.Defs.WM_2X;
import static org.alexvsbus.Defs.WM_3X;
import static org.alexvsbus.Defs.WM_FULLSCREEN;
import static org.alexvsbus.Defs.vscreenWidths;
import static org.alexvsbus.Defs.vscreenHeights;

public class DesktopLauncher {
    public static void main(String[] args) {
        int windowMinWidth;
        int windowMinHeight;
        DesktopPlatDep platDep = new DesktopPlatDep();
        Lwjgl3ApplicationConfiguration appConfig = new Lwjgl3ApplicationConfiguration();

        platDep.parseCli(args);
        if (platDep.help) {
            help();
            return;
        } else if (platDep.version) {
            version();
            return;
        }

        platDep.loadConfig();

        if (platDep.getConfig().vscreenAutoSize) {
            windowMinWidth  = VSCREEN_MAX_WIDTH;
            windowMinHeight = VSCREEN_MAX_HEIGHT;
        } else {
            windowMinWidth  = platDep.getConfig().vscreenWidth;
            windowMinHeight = platDep.getConfig().vscreenHeight;
        }

        switch (platDep.config.windowMode) {
            case WM_1X:
                appConfig.setWindowedMode(windowMinWidth, windowMinHeight);
                break;

            case WM_2X:
                appConfig.setWindowedMode(windowMinWidth * 2, windowMinHeight * 2);
                break;

            case WM_3X:
                appConfig.setWindowedMode(windowMinWidth * 3, windowMinHeight * 3);
                break;

            case WM_FULLSCREEN:
                appConfig.setFullscreenMode(appConfig.getDisplayMode());
                break;
        }

        appConfig.setTitle("Alex vs Bus: The Race");
        appConfig.setWindowIcon("icon16.png", "icon32.png", "icon48.png", "icon128.png");
        appConfig.setResizable(platDep.getConfig().resizableWindow);

        new Lwjgl3Application(new Main(platDep), appConfig);
    }

    static void help() {
        int i;

        System.out.println(
        "Alex vs Bus: The Race\n" +
        "\n" +
        "-h, --help            Show this usage information and exit\n" +
        "-v, --version         Show version and license information and exit\n" +
        "-f, --fullscreen      Run in fullscreen mode\n" +
        "-w, --window <size>   Run in windowed mode with the specified window size:\n" +
        "                      1 for 1x window size, 2 for 2x, and 3 for 3x\n" +
        "--audio-on            Enable audio output\n" +
        "--audio-off           Disable audio output\n" +
        "--resizable           Make the window resizable\n" +
        "--scanlines-on        Enable scanlines visual effect\n" +
        "--scanlines-off       Disable scanlines visual effect\n" +
        "--touch               Enable touchscreen controls, which can also be\n" +
        "                      simulated by using the mouse\n" +
        "--vscreen-size <size> Set the size of the virtual screen (vscreen)\n" +
        "\n" +
        "For --vscreen-size, the size can be either \"auto\" or a width and a height\n" +
        "separated by an \"x\" (example: 480x270), with the supported values listed\n" +
        "below.\n"
        );

        System.out.println("Supported width values:");
        for (i = 0; i < vscreenWidths.length; i++) {
            System.out.println(vscreenWidths[i]);
        }
        System.out.println();

        System.out.println("Supported height values:");
        for (i = 0; i < vscreenHeights.length; i++) {
            System.out.println(vscreenHeights[i]);
        }
   }

    static void version() {
        System.out.println(
        "Alex vs Bus: The Race\n" +
        "Version " + VERSION + "\n" +
        "\n" +
        "Copyright (C) 2021-2023 M374LX\n" +
        "\n" +
        "This program is free software: you can redistribute it and/or modify\n" +
        "it under the terms of the GNU General Public License as published by\n" +
        "the Free Software Foundation, either version 3 of the License, or\n" +
        "(at your option) any later version.\n" +
        "\n" +
        "This program is distributed in the hope that it will be useful,\n" +
        "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
        "GNU General Public License for more details.\n" +
        "\n" +
        "You should have received a copy of the GNU General Public License\n" +
        "along with this program.  If not, see <https://www.gnu.org/licenses/>."
        );
    }
}

