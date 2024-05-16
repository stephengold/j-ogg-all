/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: Properties.java,v 1.1 2003/03/03 21:53:17 jarnbjo Exp $
 * -----------------------------------------------------------
 *
 * $Author: jarnbjo $
 *
 * Description:
 *
 * Copyright 2002-2003 Tor-Einar Jarnbjo
 * -----------------------------------------------------------
 *
 * Change History
 * -----------------------------------------------------------
 * $Log: Properties.java,v $
 * Revision 1.1  2003/03/03 21:53:17  jarnbjo
 * no message
 */
package de.jarnbjo.flac;

class Properties {
    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Properties() {
    }

    protected static boolean analyze() {
        return Boolean.parseBoolean(System.getProperty("analyze", "false"));
    }
}
