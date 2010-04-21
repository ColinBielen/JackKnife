package com.ceg.online.jackknife.session;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * @author jstanton
 */
public class CRXProvider implements JCRProvider{

    /**
     * CRX Based Provider.
     * @author colinb
     * @param host
     * @return
     * @throws RepositoryException
     */
   @SuppressWarnings("unchecked")
   public Session getSession(String host) throws RepositoryException {
       Repository crxRepository;
       try {
			Class clazz = Class.forName("com.day.crx.rmi.client.CRXClientRepositoryFactory"); 
			Constructor constructor = clazz.getConstructor(new Class[]{});
			constructor.setAccessible(true);
			Object instance = constructor.newInstance(new Object[]{});
			Class[] params = {
					String.class
			};	    
			Method method = clazz.getMethod("getRepository", params);
			crxRepository = (Repository)method.invoke(instance, "//" + host + ":9901/crx");
            Credentials scred = new SimpleCredentials("admin", "admin".toCharArray());
            return crxRepository.login(scred);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }
}
