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

import static org.alexvsbus.Defs.NONE;

//Class that receives the contents of a file (level or configuration) and
//allows each line to be read individually
public class LineRead {
    String data;
    int numLinesRead;
    int dataPos;
    int dataLength;
    boolean dataEnded;
    boolean invalid;

    public void setData(String data) {
        this.data = data;
        numLinesRead = 0;
        dataPos = 0;
        dataLength = data.length();
        dataEnded = false;
        invalid = false;
    }

    public boolean endOfData() {
        return dataEnded;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public String getLine() {
        String line = "";
        int lineEnd;
        int len;
        int i;

        if (dataEnded) {
            return "";
        }

        //Error: too many lines in the file
        if (numLinesRead >= 255) {
            invalid = true;
            return "";
        }

        lineEnd = data.indexOf('\n', dataPos);
        if (lineEnd == -1) {
            lineEnd = dataLength;
            dataEnded = true;
        }

        line = data.substring(dataPos, lineEnd);
        dataPos = lineEnd + 1;
        len = line.length();

        //Error: line longer than 32 characters
        if (len > 32) {
            invalid = true;
            return "";
        }

        //Check if the line contains any invalid character
        for (i = 0; i < len; i++) {
            char c = line.charAt(0);

            if (c == ' ' || c == '\t') continue; //Whitespace
            if (c >= '0' && c <=  '9') continue; //Digit
            if (c >= 'A' && c <=  'Z') continue; //Alphabetic (upper case)
            if (c >= 'a' && c <=  'z') continue; //Alphabetic (lower case)
            if (c == '-') continue; //Hyphen

            invalid = true;
            return "";
        }

        numLinesRead++;

        //Convert to lowercase and remove excessive whitespaces
        return line.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    int toInt(String str) {
        int len, i;

        len = str.length();
        if (len == 0) {
            return NONE;
        } else if (len > 4) {
            invalid = true;
            return NONE;
        }

        for (i = 0; i < len; i++) {
            char c = str.charAt(i);

            if (c < '0' || c > '9') {
                invalid = true;
                return NONE;
            }
        }

        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return NONE;
        }
    }
}

