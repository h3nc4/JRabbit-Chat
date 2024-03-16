/*
 *  Copyright 2024 Henrique Almeida
 *
 * This file is part of JRabbit Chat.
 *
 * JRabbit Chat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * JRabbit Chat is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU
 * General Public License along with JRabbit Chat. If not, see
 * <https://www.gnu.org/licenses/>.
*/
package app;

/**
 * Utils for the Chat application
 */
public class Utils {
    private static int maxretries = 5;

    /**
     * Prevent instantiation of this class
     *
     * @throws InstantiationError if the class is instantiated
     */
    private Utils() {
        throw new InstantiationError("Cannot instantiate Utils");
    }

    /**
     * Read a string from the console
     *
     * @param msg message to display
     * @return string read from the console
     * @throws RuntimeException if the console is not available
     */
    public static String readStr(String msg) throws RuntimeException {
        try {
            String input = System.console().readLine(msg).trim();
            return input.isEmpty() ? readStr("Error: Invalid value. Type something: ") : input;
        } catch (NullPointerException e) {
            System.err.println("Error: Console is not available.");
            if (maxretries-- > 0)
                return readStr(msg);
            throw new RuntimeException("Console is not available");
        }
    };

    /**
     * Read an integer from the console
     *
     * @param msg message to display
     * @return integer read from the console
     *
     */
    public static Integer readInt(String msg) throws RuntimeException {
        try {
            return Integer.parseInt(Utils.readStr(msg));
        } catch (NumberFormatException e) {
            return readInt("Error: Invalid value. Type an integer: ");
        }
    };
}
