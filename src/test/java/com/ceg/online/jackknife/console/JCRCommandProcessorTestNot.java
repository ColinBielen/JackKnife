/*
 * JCRCommandProcessorTest.java
 * JUnit 4.x based test
 *
 * Created on May 16, 2007, 11:00 AM
 */

package com.ceg.online.jackknife.console;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author djohnson
 */
@RunWith(JMock.class)
public class JCRCommandProcessorTestNot {
    private Mockery context = new JUnit4Mockery();
    private JCRCommandProcessor jcp;
    private Session session;
    private Node node;
    private Item item;
    private PrintWriter pw;
    private Property property;
    
    public JCRCommandProcessorTestNot() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        session = context.mock(Session.class);
        node = context.mock(Node.class);
        item = context.mock(Item.class);
        property = context.mock(Property.class);
        StringWriter sw = new StringWriter();
        pw = new PrintWriter(sw);
        context.checking(new Expectations(){{
            one(session).getRootNode(); will(returnValue(node));
        }});
        jcp = new JCRCommandProcessor(session, pw);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getCurrentPath() throws Exception {
        JCRCommandProcessor instance = jcp;
        String expResult = "/";
        context.checking(new Expectations() {{
            one(node).getPath(); will(returnValue("/"));
        }});
        String result = instance.getCurrentPath();
        assertEquals(expResult, result);
    }

    @Test
    public void setCurrentPath() throws Exception {
        String path = "/";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(session).getRootNode(); will(returnValue(node));
        }});
        instance.setCurrentPath(path);
    }
    
    @Test
    public void setCurrentPath2() throws Exception {
        final String path = "bigpig";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(node).getNode(path);
        }});
        instance.setCurrentPath(path);
    }
        
    @Test
    public void listPath() throws Exception {
    	try {
	        final String path = "/fido";
	        JCRCommandProcessor instance = jcp;
	
	        context.checking(new Expectations() {{
	            one(session).getItem(path); will(returnValue(item));
	            one(item).isNode();
	        }});
	        List result = instance.listPath(path);
    	} catch (ClassCastException cceex) {
    		//Can't tell if it would be a Node or a Property...
    	}
    }

    @Test
    public void listCurrentPath() throws Exception {
        JCRCommandProcessor instance = jcp;
        List expResult = null;
        context.checking(new Expectations() {{
            one(node).getProperties();
            one(node).getNodes();
        }});
        List result = instance.listCurrentPath();
    }

    @Test
    public void processQuery() throws Exception {
        String queryString = "";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(session).getWorkspace();
        }});
        instance.processQuery(queryString);
    }

    @Test
    public void removeNode() throws Exception {
        String path = "/EOL";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(session).getRootNode(); will(returnValue(node));
            one(node).getNode("EOL"); will(returnValue(node));
            one(node).remove();
            one(session).save();
        }});
        instance.removeNode(path);
    }

    @Test
    public void moveNodeRelSrcRelDestNoRename() throws Exception {
        String sourcePath = "things";
        String destPath = "../whatnot/";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node newParentNode = context.mock(Node.class);
            one(node).getNode("things"); will(returnValue(node));
            one(node).getNode("../whatnot"); will(returnValue(newParentNode));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot/things");
            one(session).save();
        }});
        instance.moveNode(sourcePath, destPath);
    }

    @Test
    public void moveNodeRelSrcRelDestRename() throws Exception {
        String sourcePath = "things";
        String destPath = "../whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node newParentNode = context.mock(Node.class);
            one(node).getNode("things"); will(returnValue(node));
            one(node).getNode("../whatnot"); will(returnValue(newParentNode));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot");
            one(session).save();
        }});
        instance.moveNode(sourcePath, destPath);
    }

    @Test
    public void moveNodeAbsoluteSrcRelDestNoRename() throws Exception {
        String sourcePath = "/EOL/stuff/things";
        String destPath = "../whatnot/";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things"); will(returnValue(node));
            one(node).getNode("../whatnot"); will(returnValue(newParentNode));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot/things");
            one(session).save();
        }});
        instance.moveNode(sourcePath, destPath);
    }

    @Test
    public void moveNodeAbsoluteSrcRelDestRename() throws Exception {
        String sourcePath = "/EOL/stuff/things";
        String destPath = "../whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things"); will(returnValue(node));
            one(node).getNode("../whatnot"); will(returnValue(newParentNode));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot");
            one(session).save();
        }});
        instance.moveNode(sourcePath, destPath);
    }

    @Test
    public void moveNodeRelSrcAbsoluteDestNoRename() throws Exception {
        String sourcePath = "things";
        String destPath = "/EOL/stuff/whatnot/";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            one(node).getNode("things"); will(returnValue(node));
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff"); will(returnValue(newParentNode));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot/things");
            one(session).save();
        }});
        instance.moveNode(sourcePath, destPath);
    }

    @Test
    public void moveNodeRelSrcAbsoluteDestRename() throws Exception {
        String sourcePath = "things";
        String destPath = "/EOL/stuff/whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            one(node).getNode("things"); will(returnValue(node));
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff"); will(returnValue(newParentNode));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot");
            one(session).save();
        }});
        instance.moveNode(sourcePath, destPath);
    }

    @Test
    public void moveNodeAbsoluteSrcAbsoluteDestNoRename() throws Exception {
        String sourcePath = "/EOL/stuff/things";
        String destPath = "/EOL/stuff/whatnot/";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things"); will(returnValue(node));
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff"); will(returnValue(newParentNode));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot/things");
            one(session).save();
        }});
        instance.moveNode(sourcePath, destPath);
    }

    @Test
    public void moveNodeAbsoluteSrcAbsoluteDestRename() throws Exception {
        String sourcePath = "/EOL/stuff/things";
        String destPath = "/EOL/stuff/whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things"); will(returnValue(node));
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff"); will(returnValue(newParentNode));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff"));
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot");
            one(session).save();
        }});
        instance.moveNode(sourcePath, destPath);
    }

    @Test
    public void overwriteNodeRelSrcRelDestNotChild() throws Exception {
        String sourcePath = "things";
        String destPath = "../whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            //abs source path
            one(node).getNode("things"); will(returnValue(node));
            // rel dest path
            one(node).getNode("../whatnot"); will(returnValue(newParentNode));
            //is child
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
            one(node).getDepth();
            one(newParentNode).getDepth();
            one(newParentNode).remove(); 
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot");
            one(session).save();
        }});
        instance.overwriteNode(sourcePath, destPath);
    }

    @Test
    public void overwriteNodeRelSrcAbsoluteDestNotChild() throws Exception {
        String sourcePath = "things";
        String destPath = "/EOL/stuff/whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            // abs source
            one(node).getNode("things"); will(returnValue(node));
            // abs dest
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/whatnot"); will(returnValue(newParentNode));
            //is child
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
            one(node).getDepth();
            one(newParentNode).getDepth();
            one(newParentNode).remove(); 
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot");
            one(session).save();
        }});
        instance.overwriteNode(sourcePath, destPath);
    }

    @Test
    public void overwriteNodeRelSrcRelDestChild() throws Exception {
        String sourcePath = "things";
        String destPath = "../whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            // abs source
            one(node).getNode("things"); will(returnValue(node));
            // abs dest
            one(node).getNode("../whatnot"); will(returnValue(newParentNode));
            //is child
            one(node).getDepth();
            one(newParentNode).getDepth();
            one(node).getPath(); will(returnValue("EOL/stuff/whatnot/things"));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
        }});
        instance.overwriteNode(sourcePath, destPath);
    }

    @Test
    public void overwriteNodeRelSrcAbsoluteDestChild() throws Exception {
        String sourcePath = "things";
        String destPath = "/EOL/stuff/whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            // abs source
            one(node).getNode("things"); will(returnValue(node));
            // abs dest
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/whatnot"); will(returnValue(newParentNode));
            //is child
            one(node).getDepth();
            one(newParentNode).getDepth();
            one(node).getPath(); will(returnValue("EOL/stuff/whatnot/things"));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
        }});
        instance.overwriteNode(sourcePath, destPath);
    }

    @Test
    public void overwriteNodeAbsoluteSrcRelDestNotChild() throws Exception {
        String sourcePath = "/EOL/stuff/things";
        String destPath = "../whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            //abs source path
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things"); will(returnValue(node));
            // rel dest path
            one(node).getNode("../whatnot"); will(returnValue(newParentNode));
            //is child
            one(node).getDepth();
            one(newParentNode).getDepth();
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
            one(newParentNode).remove(); 
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot");
            one(session).save();
        }});
        instance.overwriteNode(sourcePath, destPath);
    }

    @Test
    public void overwriteNodeAbsoluteSrcRelDestNotChildIndexed() throws Exception {
    	try {
	        String sourcePath = "/EOL/stuff/things";
	        String destPath = "../whatnot[1]";
	        JCRCommandProcessor instance = jcp;
	        context.checking(new Expectations() {{
	            Node rootNode = context.mock(Node.class);
	            Node newParentNode = context.mock(Node.class);
	            //abs source path
	            //one(session).getRootNode(); will(returnValue(rootNode));
	        }});
	        instance.overwriteNode(sourcePath, destPath);
	        Assert.fail();
    	} catch (IllegalArgumentException e) {
    		//
    	}
    }

    @Test
    public void overwriteNodeAbsoluteSrcAbsoluteDestNotChild() throws Exception {
        String sourcePath = "/EOL/stuff/things";
        String destPath = "/EOL/stuff/whatnot";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            // abs source
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things"); will(returnValue(node));
            // abs dest
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/whatnot"); will(returnValue(newParentNode));
            //is child
            one(node).getDepth();
            one(newParentNode).getDepth();
            one(node).getPath(); will(returnValue("EOL/stuff/things"));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/whatnot"));
            one(newParentNode).remove(); 
            one(session).move("EOL/stuff/things", "EOL/stuff/whatnot");
            one(session).save();
        }});
        instance.overwriteNode(sourcePath, destPath);
    }

    @Test
    public void overwriteNodeAbsoluteSrcRelDestChild() throws Exception {
        String sourcePath = "/EOL/stuff/things/whatnot";
        String destPath = "../things";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            //abs source path
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things/whatnot"); will(returnValue(node));
            // rel dest path
            one(node).getNode("../things"); will(returnValue(newParentNode));
            //is child
            one(node).getDepth();
            one(newParentNode).getDepth();
            one(node).getPath(); will(returnValue("EOL/stuff/things/whatnot"));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/things"));
        }});
        instance.overwriteNode(sourcePath, destPath);
    }

    @Test
    public void overwriteNodeAbsoluteSrcAbsoluteDestChild() throws Exception {
        String sourcePath = "/EOL/stuff/things/whatnot";
        String destPath = "/EOL/stuff/things";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            Node rootNode = context.mock(Node.class);
            Node newParentNode = context.mock(Node.class);
            // abs source
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things/whatnot"); will(returnValue(node));
            // abs dest
            one(session).getRootNode(); will(returnValue(rootNode));
            one(rootNode).getNode("EOL/stuff/things"); will(returnValue(newParentNode));
            //is child
            one(node).getDepth();
            one(newParentNode).getDepth();
            one(node).getPath(); will(returnValue("EOL/stuff/things/whatnot"));
            one(newParentNode).getPath(); will(returnValue("EOL/stuff/things"));
        }});
        instance.overwriteNode(sourcePath, destPath);
    }

    @Test
    public void makeNode() throws Exception {
        String path = "/EOL";
        String nodetype = "fido:fido";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(session).getRootNode(); will(returnValue(node));
            one(node).addNode("EOL", "fido:fido");
            one(session).save();
        }});
        instance.makeNode(path, nodetype);
    }
    
    @Test
    public void makeNodeFromRoot() throws Exception {
        String path = "/root/EOL";
        String nodetype = "fido:fido";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(session).getRootNode(); will(returnValue(node));
            one(node).getNode("root"); will(returnValue(node));
            one(node).addNode("EOL", "fido:fido");
            one(session).save();
        }});
        instance.makeNode(path, nodetype);
    }
    
    @Test
    public void makeNodeLocal() throws Exception {
        String path = "EOL";
        String nodetype = "fido:fido";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(node).addNode("EOL", "fido:fido");
            one(session).save();
        }});
        instance.makeNode(path, nodetype);
    }

    @Test
    public void makeNodeLocal2() throws Exception {
        String path = "parent/EOL";
        String nodetype = "fido:fido";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(node).getNode("parent"); will(returnValue(node));
            one(node).addNode("EOL", "fido:fido");
            one(session).save();
        }});
        instance.makeNode(path, nodetype);
    }
    
    @Test
    public void setPropertyLong() throws Exception {
        String prop = "parent/EOL";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(node).getProperty("parent/EOL"); will(returnValue(property));
            one(property).setValue(new Long(100));
            one(session).save();
        }});
        instance.setProperty(prop, new Long(100));
    }
    
    @Test
    public void setPropertyLong2() throws Exception {
        String prop = "EOL";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(node).getProperty("EOL"); will(returnValue(property));
            one(property).setValue(new Long(100));
            one(session).save();
        }});
        instance.setProperty(prop, new Long(100));
    }
    
    @Test
    public void setPropertyLongAbsolute() throws Exception {
        String prop = "/parent/EOL";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(session).getRootNode(); will(returnValue(node));
            one(node).getProperty("parent/EOL"); will(returnValue(property));
            one(property).setValue(new Long(100));
            one(session).save();
        }});
        instance.setProperty(prop, new Long(100));
    }
    
    @Test
    public void setPropertyStringAbsolute() throws Exception {
        String prop = "/parent/EOL";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(session).getRootNode(); will(returnValue(node));
            one(node).getProperty("parent/EOL"); will(returnValue(property));
            one(property).setValue("hello world");
            one(session).save();
        }});
        instance.setProperty(prop, "hello world");
    }

    @Test
    public void setPropertyStringLocal() throws Exception {
        String prop = "parent/EOL";
        JCRCommandProcessor instance = jcp;
        context.checking(new Expectations() {{
            one(node).getProperty("parent/EOL"); will(returnValue(property));
            one(property).setValue("setPropertyStringLocal");
            one(session).save();
        }});
        instance.setProperty(prop, "setPropertyStringLocal");
    }
    
    @Test
    public void setPropertyDateAbsolute() throws Exception {
        String prop = "/parent/child";
        final Date date = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        context.checking(new Expectations() {{
            one(session).getRootNode(); will(returnValue(node));
            one(node).getProperty("parent/child"); will(returnValue(property));
            one(property).setValue(cal);
            one(session).save();
        }});
        jcp.setProperty(prop, date);
    }
    
    @Test
    public void setPropertyDateLocal() throws Exception {
        final String prop = "parent/child";
        final Date date = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        context.checking(new Expectations() {{
            one(node).getProperty(prop); will(returnValue(property));
            one(property).setValue(cal);
            one(session).save();
        }});
        jcp.setProperty(prop, date);
    }
    
    @Test
    public void setPropertyReferenceAbsolute() throws Exception {
        final String prop = "/parent/child";
        final String uuid = "not really a uuid";

        context.checking(new Expectations() {{
            one(session).getRootNode(); will(returnValue(node));
            one(node).getProperty("parent/child"); will(returnValue(property));
            one(property).setValue(uuid);
            one(session).save();
        }});
        jcp.setReferenceProperty(prop, uuid);
    }
    
    @Test 
    public void addSingleMixin() throws Exception {
    	final String path = "EOL";
    	final String mixin = "eol:testMixin";
    	final List<String> mixins = new ArrayList<String>();
    	mixins.add(mixin);
        context.checking(new Expectations() {{
            one(node).getNode(path); will(returnValue(node));
            one(node).addMixin(mixin);
            one(session).save();
        }});
        jcp.addMixin(mixins, path);
    }
    
    @Test 
    public void addMultiMixin() throws Exception {
    	final String path = "EOL";
    	final String mixin1 = "eol:testMixin";
    	final String mixin2 = "eol:testMixin";
    	final String mixin3 = "eol:testMixin";
    	final List<String> mixins = new ArrayList<String>();
    	mixins.add(mixin1);
    	mixins.add(mixin2);
    	mixins.add(mixin3);
        context.checking(new Expectations() {{
            one(node).getNode(path); will(returnValue(node));
            one(node).addMixin(mixin1);
            one(node).addMixin(mixin2);
            one(node).addMixin(mixin3);
            one(session).save();
        }});
        jcp.addMixin(mixins, path);
    }
}
