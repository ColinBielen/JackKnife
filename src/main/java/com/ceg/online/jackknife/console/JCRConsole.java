package com.ceg.online.jackknife.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import jline.ConsoleReader;

import com.ceg.online.jackknife.session.SessionFactory;

/**
 * The Actual Console.
 * <br/> Currently supported options:<br/>
 * <b>-crx</b> Connect to a CRX repository rather than Jackrabbit. <br/>
 * <b>-host</b> The remote host our repository is connected to.<br/>
 * <b>-e</b> (e)valuate the passed command inside the shell then exit. </br>
 * @author djohnson
 * @author jstanton
 */
public class JCRConsole {
    /** Creates a new instance of JCRConsole */
    public JCRConsole() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean crx = false;
        boolean quitAfterExecute = false;
        boolean endOfCommandReached = false;
        String command = "";

        String host = "localhost";
        Calendar now = Calendar.getInstance();
        final String copyright = "\n\tCopyright (C) "+now.get(Calendar.YEAR)+" Comcast Entertainment Group \n" +
                "\tJackKnife comes with ABSOLUTELY NO WARRANTY ";

        try {
            // process arguments
            for(int i = 0 ; i < args.length ; i++) {
                String argument = args[i];
                //CRX uses a different connection mechanism than jackrabbit...
                if(argument.equals("-crx")) {
                    crx = true;
                    if(quitAfterExecute) endOfCommandReached = true;
                    continue;
                } else if(argument.equals("-host")) {
                    i++;
                    host = args[i];
                    if(quitAfterExecute) endOfCommandReached = true;
                    continue;
                } else if(argument.equals("-e")) {
                	quitAfterExecute = true;
                    continue;
                } else if(quitAfterExecute && !endOfCommandReached){
                	command += argument+" ";
                    continue;
                }
            }

            System.out.println(copyright);
            ConsoleReader con = new ConsoleReader();
            PrintWriter pwOut = new PrintWriter(System.out);

                             
                String line;
	            Session session = SessionFactory.getJCRSession(host, crx);
	            CommandProcessor cp = new JCRCommandProcessor(session, pwOut);
	            if(quitAfterExecute) {
	                try {
	                CommandLineParser parser = new CommandLineParser(new StringReader(command), cp);
	                parser.Command();
	                } catch(Throwable ex) {
	                    pwOut.println(ex.getMessage());
	                }
	                pwOut.flush();
	            } else {
		            while((line = con.readLine(cp.getCurrentPath() + " $ ")) != null) {
		                try {
		                    CommandLineParser parser = new CommandLineParser(new StringReader(line), cp);
		                    parser.Command();
		                } catch(Throwable ex) {
		                    pwOut.println(ex.getMessage());
		                }
		                pwOut.flush();
		            }
	            }
        } catch(RepositoryException rex) {
            rex.printStackTrace();
        } catch(IOException ioex) {
            ioex.printStackTrace();
        }
    }
    
}
