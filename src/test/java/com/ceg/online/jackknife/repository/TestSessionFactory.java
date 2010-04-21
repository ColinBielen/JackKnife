package com.ceg.online.jackknife.repository;

import org.apache.jackrabbit.core.TransientRepository;
import org.junit.Test;

import static org.junit.Assert.*;
import javax.jcr.Repository;
import javax.jcr.Session;

/**
 *This class creates as simple Transitory JCR Sessions suitable for testing.

 */
public class TestSessionFactory  {

    private static String testRepositoryConfigFile = "./src/test/resources/repository-config.xml";
    private static String testRepositoryHome = "./target/test-repository-home";

    /**
     * Gets a JCR Session from the test repository...
     * @return
     * @throws Exception
     */
    public static Session getSession() throws Exception {
        Repository repository = new TransientRepository(testRepositoryConfigFile,testRepositoryHome);
        return repository.login();
    }
    /**
     * Yes, a test method for testing the test method!     
     * @throws Exception
     */
    @Test
    public void testGetSession() throws Exception {

        try {
        Session s =getSession();
            assertNotNull(s);
            s.logout();
        }catch(Exception e){
            fail("Exception thrown getting JCR Test Session"+e.getMessage());
        }
                       
    }

}
