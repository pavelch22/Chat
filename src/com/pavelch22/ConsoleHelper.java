package com.pavelch22;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class for work with the console.
 */
public class ConsoleHelper {
    private static BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Writes a message to the console.
     *
     * @param message a message to write.
     */
    public static void writeMessage(String message) {
        System.out.println(message);
    }

    /**
     * Reads a string from the console.
     *
     * @return a string from the console.
     */
    public static String readString() {
        String s = null;
        while (s == null) {
            try {
                s = console.readLine();
            } catch (IOException e) {
                writeMessage("Error. Try again.");
            }
        }
        return s;
    }

    /**
     * Reads an integer from the console.
     *
     * @return an integer from the console.
     */
    public static int readInt() {
        Integer number = null;
        while (number == null) {
            try {
                number = Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                writeMessage("Error. Try again.");
            }
        }
        return number;
    }
}
