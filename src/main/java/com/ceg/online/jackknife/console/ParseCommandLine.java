/*
 * ParseCommandLine.java
 *
 * Created on April 26, 2007, 4:11 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.ceg.online.jackknife.console;

import org.apache.commons.cli.*;

import java.io.StringReader;

/**
 *
 * @author djohnson
 * @author jstanton
 */
public class ParseCommandLine {
    static public void parseLine(String command) throws ParseException {        
        StringReader cmd = new StringReader(command);
        CommandLineParser cp = new CommandLineParser(cmd);
        cp.Command();
    }
}
