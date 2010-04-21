package com.ceg.online.jackknife.importer.nodetype;

import org.junit.Test;
import org.junit.Before;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.junit.Assert.*;

import javax.jcr.Session;

import com.ceg.online.jackknife.repository.TestSessionFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Test class for JackRabbitCNFImporter
 */
public class TestJackRabbitCNFImporter {
    private Log log = LogFactory.getLog(TestJackRabbitCNFImporter.class);
    private Session session;
    @Before
    public void setUp() {
       try {
       session = TestSessionFactory.getSession();
       } catch(Exception e) {
           fail("Problem getting test session"+e.getMessage());
       }
       try {

       }catch(Exception e) {
         fail("Problem getting CNF file.");
       }
    }

    @Test
    public void doImport() {
        try {
            InputStream stream = new FileInputStream("./src/test/resources/test.cnf");
            new JackrabbitCNFImporter(session).doImport(stream, false,"eol","http://jcr.eonline.com");
        } catch(IOException e) {
            fail("Problem grabbing CNF file..."+e.getMessage());

        }
    }
}
