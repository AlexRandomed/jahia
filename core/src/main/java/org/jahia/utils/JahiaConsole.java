/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

/* JAHIA                                                            */

/* Class Name   :   JahiaConsole                                    */

/* Function     :   Manages Jahia console messages                  */

/* Created      :   08-10-2000                                      */

/* Author       :   Eric Vassalli                                   */

/* Interface    :                                                   */

/*      print( String )     : prints a message in console           */

/*      println( String )   : prints a message in console with \n   */

/*      startup()           : displays cool startup message :)      */

/*                                         Copyright 2002 Jahia Ltd */

/********************************************************************/

package org.jahia.utils;

import javax.servlet.GenericServlet;

public class JahiaConsole {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaConsole.class);

    /**
     * Constants for logging levels {
     */
    public static final int DEFAULT_LOGGING_LEVEL = 3;
    public static final int MAX_LOGGING_LEVEL = 10;
    public static final int CONSOLE_LOGGING_LEVEL = 9;
    /**
     * }
     */

    /**
     * constructor
     * EV    08.10.2000
     */
    private JahiaConsole() {
        println("JahiaConsole", "***** Starting Jahia Console");
    } // end constructor


    public static void setServlet(GenericServlet servletRef) {
        // do nothing;
    }

    public static void setLoggingLevel(int level) {
        // do nothing;
    }

    /**
     * print
     * EV    08.10.2000
     */
    public static void print(String origin, String msg) {
        logger.debug(origin + " > " + msg);
    }


    /**
     * println
     * EV    08.10.2000
     */
    public static void println(String origin, String msg) {
        logger.debug(origin + "> " + msg);
    }

    /**
     * Small utility function to print stack trace on the Jahia console.
     *
     * @param origin a String representing the origin of the message. Recommended
     *               format is class.method
     * @param t      the exception whose stack trace will be dumped into the Jahia
     *               Console.
     * @author Serge Huber.
     */
    public static void printe(String origin, Throwable t) {
        logger.debug(origin, t);
    }

    /**
     * Prints a message on the console.
     * THIS METHOD SHOULD BE CALLED ONLY IF YOU WANT YOUR MESSAGE TO BE DISPLAYED IN THE
     * RELEASE VERSION OF JAHIA.  Don't abuse ;-)
     */
    public static synchronized void finalPrintln(String origin, String msg) {
        logger.info(origin + "> " + msg);
    }

    public static synchronized void finalPrint(String origin, String msg) {
        logger.info(origin + "> " + msg);
    }


    /**
     * startup
     * EV    08.10.2000
     */
    public static void startup(int buildNumber) {
        String msg = "";
        msg += "***********************************\n";
        msg += "   Starting Jahia - Build " + buildNumber + "\n";
        msg += "       \"Today's a great day ! \"\n";
        msg += "***********************************\n";
        JahiaConsole.println("JahiaConsole.startup", "\n\n" + msg + "\n");
        println("Jahia", "***** Starting Jahia *****");
    }


    /**
     * startupWithTrust
     * AK    20.01.2001
     */
    public static void startupWithTrust(int buildNumber) {
        Integer buildNumberInteger = new Integer(buildNumber);
        String buildString = buildNumberInteger.toString();
        StringBuilder buildBuffer = new StringBuilder();

        for (int i = 0; i < buildString.length(); i++) {
            buildBuffer.append(" ");
            buildBuffer.append(buildString.substring(i, i + 1));
        }

        StringBuilder msg = new StringBuilder(512);
        msg
                .append(
                        "\n\n\n\n"
                                + "                                     ____.\n"
                                + "                         __/\\ ______|    |__/\\.     _______\n"
                                + "              __   .____|    |       \\   |    +----+       \\\n"
                                + "      _______|  /--|    |    |    -   \\  _    |    :    -   \\_________\n"
                                + "     \\\\______: :---|    :    :           |    :    |         \\________>\n"
                                + "             |__\\---\\_____________:______:    :____|____:_____\\\n"
                                + "                                        /_____|\n"
                                + "\n"
                                + "      . . . s t a r t i n g   j a h i a   b u i l d  ")
                .append(buildBuffer.toString())
                .append(
                        " . . .\n"
                                + "\n\n"
                                + "   Copyright 2002-2009 - Jahia Solutions Group SA http://www.jahia.com - All Rights Reserved\n"
                                + "\n\n"
                                + " *******************************************************************************\n"
                                + " * The contents of this software, or the files included with this software,    *\n"
                                + " * are subject to the GNU General Public License (GPL).                        *\n"
                                + " * You may not use this software except in compliance with the license. You    *\n"
                                + " * may obtain a copy of the license at http://www.jahia.com/license. See the   *\n"
                                + " * license for the rights, obligations and limitations governing use of the    *\n"
                                + " * contents of the software.                                                   *\n"
                                + " *******************************************************************************\n"
                                + "\n\n");

        System.out.println (msg.toString());
        System.out.flush();
    }

}
