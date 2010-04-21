/*
 * CommandLineParserTest.java
 * JUnit 4.x based test
 *
 * Created on April 26, 2007, 4:14 AM
 */

package com.ceg.online.jackknife.console;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author djohnson
 */
@RunWith(JMock.class)
public class CommandLineParserTest {
    Mockery context = new JUnit4Mockery();
    CommandProcessor cp;
    
    public CommandLineParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        cp = context.mock(CommandProcessor.class);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    public void parseLine(String command) throws ParseException {
        StringReader sr = new StringReader(command);
        CommandLineParser parser = new CommandLineParser(sr, cp);
        parser.Command();
    }

    
    @Test
    public void basicLSCurrentPath() throws Exception {
        context.checking(new Expectations(){{
            one(cp).listCurrentPath();
        }});
        String cmd = "ls";
        parseLine(cmd);
    }

    
    @Test
    public void basicLS() throws Exception {
        final String path = "EOL/blah/L3dd/";
        context.checking(new Expectations(){{
            one(cp).listPath(path);
        }});
        String cmd = "ls " + path;
        parseLine(cmd);
    }
    
    @Test
    public void lsTestLongPath() throws Exception {
        final String path = "12345";
        context.checking(new Expectations(){{
            one(cp).listPath(path);
        }});
        String cmd = "ls " + path;
        parseLine(cmd);
    }
    
    
    @Test
    public void basicAbsoluteLS() throws Exception {
        final String path = "/EOL/blah/L3dd/";
        context.checking(new Expectations(){{
            one(cp).listPath(path);
        }});
        String cmd = "ls " + path;
        parseLine(cmd);
    }
    
    @Test
    public void lsTestLongPathAbsolute() throws Exception {
        final String path = "/12345";
        context.checking(new Expectations(){{
            one(cp).listPath(path);
        }});
        String cmd = "ls " + path;
        parseLine(cmd);
    }

    @Test
    public void findPath() throws Exception {
        final String param = "/EOL/blah/L3dd/";
        context.checking(new Expectations(){{
            one(cp).find(param);
        }});
        String cmd = "find " + param;
        parseLine(cmd);
    }

    @Test
    public void findStrange() throws Exception {
        final String param = "L:3d-d/_18k";
        context.checking(new Expectations(){{
            one(cp).find(param);
        }});
        String cmd = "find " + param;
        parseLine(cmd);
    }

    @Test
    public void findName() throws Exception {
        final String param = "L3dd";
        context.checking(new Expectations(){{
            one(cp).find(param);
        }});
        String cmd = "find " + param;
        parseLine(cmd);
    }

    @Test
    public void listPath() throws Exception {
        final String path = "/EOL/blah/L3dd/";
        context.checking(new Expectations(){{
            one(cp).listPath(path);
        }});
        String cmd = "ls " + path;
        parseLine(cmd);
    }
    
    
    @Test
    public void listPathMulti() throws Exception {
        final String path = "/EOL/blah/L3dd[2]";
        context.checking(new Expectations(){{
            one(cp).listPath(path);
        }});
        String cmd = "ls " + path;
        parseLine(cmd);
    }
    
    @Test
    public void listPathMulti2() throws Exception {
        final String path = "/EOL[3]/blah/L3dd[2]";
        context.checking(new Expectations(){{
            one(cp).listPath(path);
        }});
        String cmd = "ls " + path;
        parseLine(cmd);
    }

    @Test
    public void listPath2() throws Exception {
        final String path = "/";
        context.checking(new Expectations(){{
            one(cp).listPath(path);
        }});
        String cmd = "ls " + path;
        parseLine(cmd);
    }

    @Test
    public void mvCommandTest() throws Exception {
        final String source = "EOL/blah/L3dd[2]";
        final String dest = "/";
        context.checking(new Expectations(){{
            one(cp).moveNode(source, dest);
        }});
        String cmd = "mv " + source + " " + dest;
        parseLine(cmd);    
    }

    @Test
    public void mvCommandTest2() throws Exception {
        final String source = "/EOL/blah/L3dd/";
        final String dest = "/EOL/blah/L3dder[2]/";
        context.checking(new Expectations(){{
            one(cp).moveNode(source, dest);
        }});
        String cmd = "mv " + source + " " + dest;
        parseLine(cmd);    
    }
    
    @Test
    public void mvLongPath() throws Exception {
        final String source = "2007";
        final String dest = "2006";
        context.checking(new Expectations(){{
            one(cp).moveNode(source, dest);
        }});
        String cmd = "mv " + source + " " + dest;
        parseLine(cmd);    
    }
    
    @Test
    public void mvLongPath3() throws Exception {
        final String source = "2007[9]";
        final String dest = "../us/en[1]/stuff/2006";
        context.checking(new Expectations(){{
            one(cp).moveNode(source, dest);
        }});
        String cmd = "mv " + source + " " + dest;
        parseLine(cmd);    
    }
    
    @Test
    public void overwriteTest() throws Exception {
    	context.checking(new Expectations() {{
    		one(cp).overwriteNode("hello", "goodbye");
    	}});
    	parseLine("mv -o hello goodbye");
    }
    
    @Test
    public void overwriteTest3() throws Exception {
    	context.checking(new Expectations() {{
    		one(cp).overwriteNode("hello", "goodbye");
    	}});
    	parseLine("mv -O hello goodbye");
    }
    
    @Test
    public void overwriteTest4() throws Exception {
    	context.checking(new Expectations() {{
    		one(cp).overwriteNode("hello[2]", "goodbye");
    	}});
    	parseLine("mv -O hello[2] goodbye");
    }
    
    @Test
    public void queryTest() throws Exception {
        final String query = "select * from this is a test";
        context.checking(new Expectations(){{
            one(cp).processQuery(query);
        }});
        String cmd = "query " + query ;
        parseLine(cmd);
    }
    
    @Test
    public void query2Test() throws Exception {
        final String query = "select * from eol:Story where publishDate > DATE '2007-01-01'";
        context.checking(new Expectations(){{
            one(cp).processQuery(query);
        }});
        String cmd = "query " + query ;
        parseLine(cmd);
    }
    
    
    @Test
    public void queryReferences() throws Exception {
        context.checking(new Expectations(){{
            one(cp).showReferences("74e1d54c-e894-40a6-bc2f-6728b63cfd17");
        }});
        String cmd = "sr 74e1d54c-e894-40a6-bc2f-6728b63cfd17";
        parseLine(cmd);
    }
    
    
    @Test
    public void setLongProperty() throws Exception {
        context.checking(new Expectations(){{
            one(cp).setProperty("EOL", new Long(1002));
        }});
        String cmd = "sp EOL 1002";
        parseLine(cmd);
        
        context.checking(new Expectations(){{
            one(cp).setProperty("EOL", new Long(100));
        }});
        parseLine("setprop EOL 100");
    }
    
    
    @Test
    public void setStringProperty() throws Exception {
        context.checking(new Expectations(){{
            one(cp).setProperty("EOL", "this is a test");
        }});
        String cmd = "sp EOL \"this is a test\"";
        parseLine(cmd);
    }
    
    @Test
    public void changeDirectory() throws Exception {
        context.checking(new Expectations(){{
            one(cp).setCurrentPath("2007");
        }});
        String cmd = "cd 2007";
        parseLine(cmd);
    }
    
    @Test
    public void makeNode() throws Exception {
        context.checking(new Expectations(){{
            one(cp).makeNode("fido", "nt:unstructured");
        }});
        String cmd = "mknode nt:unstructured fido";
        parseLine(cmd);
    }
    
    @Test
    public void addMixinRelPathIndexed() throws Exception {
    	final String path = "EOL/stuff/Things[2]/";
    	final List<String> mixins = new ArrayList<String>();
    	mixins.add("one");
    	mixins.add("two");
    	mixins.add("three");
    	mixins.add("four");
        context.checking(new Expectations(){{
        	one(cp).addMixin(mixins, path);
	    }});
	    String cmd = "addmixin one two three four "+path;
	    parseLine(cmd);
    }
    
    @Test
    public void addMixinAbsPathIndexed() throws Exception {
    	final String path = "/EOL/stuff/Things[2]";
    	final List<String> mixins = new ArrayList<String>();
    	mixins.add("one");
    	mixins.add("two");
    	mixins.add("three");
        context.checking(new Expectations(){{
        	one(cp).addMixin(mixins, path);
	    }});
	    String cmd = "addmixin one two three "+path;
	    parseLine(cmd);
    }
    
    @Test
    public void addMixinRelPath() throws Exception {
    	final String path = "EOL/stuff/Things";
    	final List<String> mixins = new ArrayList<String>();
    	mixins.add("one");
    	mixins.add("two");
    	mixins.add("three");
    	mixins.add("four");
        context.checking(new Expectations(){{
        	one(cp).addMixin(mixins, path);
	    }});
	    String cmd = "addmixin one two three four "+path;
	    parseLine(cmd);
    }
    
    @Test
    public void addMixinAbsPath() throws Exception {
    	final String path = "/EOL/stuff/Things";
    	final List<String> mixins = new ArrayList<String>();
    	mixins.add("one");
    	mixins.add("two");
    	mixins.add("three");
    	mixins.add("four");
        context.checking(new Expectations(){{
        	one(cp).addMixin(mixins, path);
	    }});
	    String cmd = "addmixin one two three four "+path;
	    parseLine(cmd);
    }
    
}
