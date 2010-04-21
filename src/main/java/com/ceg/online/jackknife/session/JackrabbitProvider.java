package com.ceg.online.jackknife.session;

import org.apache.jackrabbit.core.TransientRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;
import java.io.IOException;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.Context;
import javax.naming.InitialContext;


/**
 * @author jstanton
 */
public class JackrabbitProvider implements JCRProvider {

	/**
     * Jackrbbit based JCR Provider
     * @author colinb
	 * @param host
	 * @return
	 * @throws javax.jcr.RepositoryException
	 */
	@SuppressWarnings("unchecked")
	public Session getSession(String host) throws RepositoryException {
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		env.put(Context.PROVIDER_URL, "jnp://" + host + ":1099");
		env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
		try {
			InitialContext ctx = new InitialContext(env);
			Class cafClass = Class.forName("org.apache.jackrabbit.rmi.client.ClientAdapterFactory");
			Constructor constructor = cafClass.getConstructor(new Class[] {});
			Object cafInstance = constructor.newInstance(new Object[] {});
			Class rrClass = Class.forName("org.apache.jackrabbit.rmi.remote.RemoteRepository");
			Method cafMethod = cafClass.getMethod("getRepository", new Class[] {rrClass});
			Repository repository = (Repository)cafMethod.invoke(cafInstance, new Object[] {ctx.lookup("jnp://" + host + ":1099/jcrServer")});
			Credentials credJBoss = new SimpleCredentials("username", "password".toCharArray());
			return repository.login(credJBoss);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * This is here for things like node type registration where you need to get to the
     * *base* jackrabbit impl without the RMI layer.
     * @return
     * @throws RepositoryException
     */
    public Session getLocalSession(String configFilePath,String homeDir) throws RepositoryException, IOException {
        Repository repository = new TransientRepository(configFilePath,homeDir);
        return repository.login();
    }
}
